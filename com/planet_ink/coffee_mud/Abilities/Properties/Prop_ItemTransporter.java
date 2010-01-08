package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class Prop_ItemTransporter extends Property
{
	public String ID() { return "Prop_ItemTransporter"; }
	public String name(){ return "Item Transporter";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS;}
	protected Room roomDestination=null;
	protected MOB mobDestination=null;
	protected Item nextDestination=null;
    protected static Hashtable possiblePossibilities=new Hashtable();
    protected static Hashtable lastLooks=new Hashtable();

	public String accountForYourself()
	{ return "Item Transporter";	}

	public Item ultimateParent(Item item)
	{
		if(item==null) return null;
		if(item.container()==null) return item;
		if(item.container().container()==item)
			item.container().setContainer(null);
		if(item.container()==item)
			item.setContainer(null);
		return ultimateParent(item.container());
	}

	private synchronized boolean setDestination()
	{
		Vector possibilities=(Vector)possiblePossibilities.get(text());
		Integer lastLook=(Integer)lastLooks.get(text());
		if((possibilities==null)||(lastLook==null)||(lastLook.intValue()<0))
		{
			possibilities=new Vector();
			possiblePossibilities.put(text(),possibilities);
			lastLook=Integer.valueOf(10);
			lastLooks.put(text(),lastLook);
		}
		else
			lastLooks.put(text(),Integer.valueOf(lastLook.intValue()-1));
		if(possibilities.size()==0)
		{
			roomDestination=null;
			mobDestination=null;
			nextDestination=null;
			try
			{
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room room=(Room)r.nextElement();
					Ability A=room.fetchEffect("Prop_ItemTransReceiver");
					if((A!=null)&&(A.text().equalsIgnoreCase(text())))
						possibilities.addElement(room);
					for(int i=0;i<room.numItems();i++)
					{
						Item item=room.fetchItem(i);
						if((item!=null)&&(item!=affected))
						{
							A=item.fetchEffect("Prop_ItemTransReceiver");
							if((A!=null)&&(A.text().equalsIgnoreCase(text())))
								possibilities.addElement(item);
						}
					}
					for(int m=0;m<room.numInhabitants();m++)
					{
						MOB mob=room.fetchInhabitant(m);
						if((mob!=null)&&(mob!=affected))
						{
							A=mob.fetchEffect("Prop_ItemTransReceiver");
							if((A!=null)&&(A.text().equalsIgnoreCase(text())))
								possibilities.addElement(mob);
							for(int i=0;i<mob.inventorySize();i++)
							{
								Item item=mob.fetchInventory(i);
								if((item!=null)&&(item!=affected))
								{
									A=item.fetchEffect("Prop_ItemTransReceiver");
									if((A!=null)&&(A.text().equalsIgnoreCase(text())))
										possibilities.addElement(item);
								}
							}
						}
					}
				}
		    }catch(NoSuchElementException e){}
		}
		if(possibilities.size()>0)
		{
			Environmental E=(Environmental)possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1));
			nextDestination=null;
			if(E instanceof Room)
				roomDestination=(Room)E;
			else
			if(E instanceof MOB)
				mobDestination=(MOB)E;
			else
			if(E instanceof Item)
			{
				nextDestination=(Item)E;
				if((nextDestination!=null)&&(nextDestination.owner()!=null))
				{
					if(nextDestination.owner() instanceof Room)
						roomDestination=(Room)nextDestination.owner();
					else
					if(nextDestination.owner() instanceof MOB)
						mobDestination=(MOB)nextDestination.owner();
				}
				else
					nextDestination=null;
			}
		}
		if((mobDestination==null)&&(roomDestination==null))
			return false;
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected==null) return true;

		if(((msg.amITarget(affected))
			&&((msg.targetMinor()==CMMsg.TYP_PUT)
			   ||(msg.targetMinor()==CMMsg.TYP_GIVE))
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Item))
		||((affected instanceof MOB)
			&&(msg.amISource((MOB)affected))
			&&(msg.targetMinor()==CMMsg.TYP_GET)
			&&(msg.target() !=null)
			&&(msg.target() instanceof Item))
		||((affected instanceof Room)
			&&(msg.targetMinor()==CMMsg.TYP_DROP)
			&&(msg.target()!=null)
			&&(msg.target() instanceof Item))
		||((affected instanceof Room)
			&&(msg.sourceMinor()==CMMsg.TYP_THROW)
		    &&(affected==CMLib.map().roomLocation(msg.target()))
			&&(msg.tool() instanceof Item)))
		{
			if(!setDestination())
			{
				msg.source().tell("The transporter has no possible ItemTransReceiver with the code '"+text()+"'.");
				return false;
			}
		}
		return true;
	}

	public synchronized void tryToMoveStuff()
	{
		if((mobDestination!=null)||(roomDestination!=null))
		{
			Room room=roomDestination;
			MOB mob=mobDestination;
			Room roomMover=null;
			MOB mobMover=null;
			Item container=null;
			if(affected==null) return;
			if(affected instanceof Room)
				roomMover=(Room)affected;
			else
			if(affected instanceof MOB)
				mobMover=(MOB)affected;
			else
			if(affected instanceof Item)
			{
				container=(Item)affected;
				if((container.owner()!=null)&&(container.owner() instanceof Room))
					roomMover=(Room)container.owner();
				else
				if((container.owner()!=null)&&(container.owner() instanceof MOB))
					mobMover=(MOB)container.owner();
			}
			Vector itemsToMove=new Vector();
			if(roomMover!=null)
			{
				for(int i=0;i<roomMover.numItems();i++)
				{
					Item item=roomMover.fetchItem(i);
					if((item!=null)
					   &&(item!=container)
					   &&(item.amWearingAt(Wearable.IN_INVENTORY))
					   &&((item.container()==container)||(ultimateParent(item)==container)))
					   itemsToMove.addElement(item);
				}
				for(int i=0;i<itemsToMove.size();i++)
					roomMover.delItem((Item)itemsToMove.elementAt(i));
			}
			else
			if(mobMover!=null)
			{
				int oldNum=itemsToMove.size();
				for(int i=0;i<mobMover.inventorySize();i++)
				{
					Item item=mobMover.fetchInventory(i);
					if((item!=null)
					   &&(item!=container)
					   &&(item.amWearingAt(Wearable.IN_INVENTORY))
					   &&((item.container()==container)||(ultimateParent(item)==container)))
					   itemsToMove.addElement(item);
				}
				for(int i=oldNum;i<itemsToMove.size();i++)
					mobMover.delInventory((Item)itemsToMove.elementAt(i));
			}
			if(itemsToMove.size()>0)
			{
				mobDestination=null;
				roomDestination=null;
				if(room!=null)
					for(int i=0;i<itemsToMove.size();i++)
					{
						Item item=(Item)itemsToMove.elementAt(i);
						if((item.container()==null)||(item.container()==container))
							item.setContainer(nextDestination);
						room.addItemRefuse(item,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
					}
				if(mob!=null)
					for(int i=0;i<itemsToMove.size();i++)
					{
						Item item=(Item)itemsToMove.elementAt(i);
						if((item.container()==null)||(item.container()==container))
							item.setContainer(nextDestination);
						if(mob instanceof ShopKeeper)
							((ShopKeeper)mob).getShop().addStoreInventory(item);
						else
							mob.addInventory(item);
					}
				if(room!=null) room.recoverRoomStats();
				if(mob!=null){
					mob.recoverCharStats();
					mob.recoverEnvStats();
					mob.recoverMaxState();
				}
			}
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
			tryToMoveStuff();
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		// amazingly important that this happens first!
		super.executeMsg(myHost,msg);
		if((msg.targetMinor()==CMMsg.TYP_GET)
		||(msg.targetMinor()==CMMsg.TYP_GIVE)
		||(msg.targetMinor()==CMMsg.TYP_PUT)
		||(msg.sourceMinor()==CMMsg.TYP_THROW)
		||(msg.targetMinor()==CMMsg.TYP_DROP))
			tryToMoveStuff();
	}
}
