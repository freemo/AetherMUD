package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class Copy extends StdCommand
{
	public Copy(){}

	private String[] access={getScr("Copy","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,getScr("Copy","wavearms"));
		commands.removeElementAt(0); // copy
		if(commands.size()<1)
		{
			mob.tell(getScr("Copy","failfi"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Copy","flub"));
			return false;
		}
		int number=1;
		if(commands.size()>1)
		{
			number=CMath.s_int((String)commands.firstElement());
			if(number<1)
				number=1;
			else
				commands.removeElementAt(0);
		}
		String name=CMParms.combine(commands,0);
		Environmental dest=mob.location();
		int x=name.indexOf("@");
		if(x>0)
		{
			String rest=name.substring(x+1).trim();
			name=name.substring(0,x).trim();
			if((!rest.equalsIgnoreCase("room"))
			&&(rest.length()>0))
			{
				MOB M=mob.location().fetchInhabitant(rest);
				if(M==null)
				{
					mob.tell(getScr("Copy","mobnf",rest));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Copy","flub"));
					return false;
				}
				dest=M;
			}
		}
		Environmental E=null;
        ShopKeeper SK=null;
		int dirCode=Directions.getGoodDirectionCode(name);
		if(dirCode>=0)
			E=mob.location();
		else
			E=mob.location().fetchFromRoomFavorItems(null,name,Item.WORNREQ_UNWORNONLY);

        if(E==null) E=mob.location().fetchFromRoomFavorMOBs(null,name,Item.WORNREQ_UNWORNONLY);
		if(E==null)	E=mob.fetchInventory(name);
		if(E==null)
		{
		    try
		    {
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					E=R.fetchInhabitant(name);
					if(E==null) E=R.fetchAnyItem(name);
					if(E!=null) break;
				}
		    }catch(NoSuchElementException e){}
		}
		if(E==null)
		{
		    try
		    {
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB mob2=R.fetchInhabitant(m);
						if(mob2!=null)
						{
							E=mob2.fetchInventory(name);
                            SK=CMLib.coffeeShops().getShopKeeper(mob2);
							if((E==null)&&(SK!=null))
								E=SK.getShop().getStock(name,null,SK.whatIsSold(),mob2.getStartRoom());
						}
						if(E!=null) break;
					}
					if(E!=null) break;
				}
		    }catch(NoSuchElementException e){}
		}
		if(E==null)
		{
			mob.tell(getScr("Copy","living",name));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Copy","flub"));
			return false;
		}
		Room room=mob.location();
		for(int i=0;i<number;i++)
		{
			if(E instanceof MOB)
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"COPYMOBS"))
				{
					mob.tell(getScr("Copy","copy",E.name()));
					return false;
				}
				MOB newMOB=(MOB)E.copyOf();
				newMOB.setSession(null);
				newMOB.setStartRoom(room);
				newMOB.setLocation(room);
				newMOB.recoverCharStats();
				newMOB.recoverEnvStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				newMOB.bringToLife(room,true);
				if(i==0)
				{
					if(number>1)
						room.show(newMOB,null,CMMsg.MSG_OK_ACTION,getScr("Copy","su",number+"",newMOB.name()));
					else
						room.show(newMOB,null,CMMsg.MSG_OK_ACTION,getScr("Copy","se",newMOB.name()));
					Log.sysOut("SysopUtils",getScr("Copy","sy",mob.Name(),number+"",newMOB.Name()));
				}
			}
			else
			if((E instanceof Item)&&(!(E instanceof ArchonOnly)))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"COPYITEMS"))
				{
					mob.tell(getScr("Copy","copy",E.name()));
					return false;
				}
				Item newItem=(Item)E.copyOf();
				newItem.setContainer(null);
				newItem.wearAt(0);
				String end=getScr("Copy","stringend");
				if(dest instanceof Room)
					((Room)dest).addItem(newItem);
				else
				if(dest instanceof MOB)
				{
					((MOB)dest).addInventory(newItem);
					end=getScr("Copy","ia",dest.name());
				}
				if(i==0)
				{
					if(number>1)
						room.showHappens(CMMsg.MSG_OK_ACTION,getScr("Copy","sm",number+"",newItem.name(),end));
					else
						room.showHappens(CMMsg.MSG_OK_ACTION,getScr("Copy","sw",newItem.name(),end));
					Log.sysOut("SysopUtils",mob.Name(),getScr("Copy","ya",number+"",newItem.ID()));
				}
			}
			else
			if((E instanceof Room)&&(dirCode>=0))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"COPYROOMS"))
				{
					mob.tell(getScr("Copy","copy",E.name()));
					return false;
				}
				if(room.getRoomInDir(dirCode)!=null)
				{
					mob.tell(getScr("Copy","roomal",Directions.getInDirectionName(dirCode)));
					return false;
				}
				Room newRoom=(Room)room.copyOf();
				newRoom.clearSky();
				if(newRoom instanceof GridLocale)
					((GridLocale)newRoom).clearGrid(null);
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					newRoom.rawDoors()[d]=null;
					newRoom.rawExits()[d]=null;
				}
				room.rawDoors()[dirCode]=newRoom;
				newRoom.rawDoors()[Directions.getOpDirectionCode(dirCode)]=room;
				if(room.rawExits()[dirCode]==null)
					room.rawExits()[dirCode]=CMClass.getExit("Open");
				newRoom.rawExits()[Directions.getOpDirectionCode(dirCode)]=(Exit)(room.rawExits()[dirCode].copyOf());
				newRoom.setRoomID(CMLib.map().getOpenRoomID(room.getArea().Name()));
				newRoom.setArea(room.getArea());
				CMLib.database().DBCreateRoom(newRoom,CMClass.className(newRoom));
				CMLib.database().DBUpdateExits(newRoom);
				CMLib.database().DBUpdateExits(room);
				if(newRoom.numInhabitants()>0)
					CMLib.database().DBUpdateMOBs(newRoom);
				if(newRoom.numItems()>0)
					CMLib.database().DBUpdateItems(newRoom);
				newRoom.getArea().fillInAreaRoom(newRoom);
				if(i==0)
				{
					if(number>1)
						room.showHappens(CMMsg.MSG_OK_ACTION,getScr("Copy","yy",number+"",room.roomTitle(),Directions.getInDirectionName(dirCode)));
					else
						room.showHappens(CMMsg.MSG_OK_ACTION,getScr("Copy","ma",room.roomTitle(),Directions.getInDirectionName(dirCode)));
					Log.sysOut("SysopUtils",getScr("Copy","mk",mob.Name(),number+"",room.roomID()));
				}
				else
					room.showHappens(CMMsg.MSG_OK_ACTION,getScr("Copy","ma",room.roomTitle(),Directions.getInDirectionName(dirCode)));
				room=newRoom;
			}
			else
			{
				mob.tell(getScr("Copy","cantj",E.name()));
				room.showOthers(mob,null,CMMsg.MSG_OK_ACTION,getScr("Copy","flub"));
				break;
			}
		}
		if((E instanceof Item)&&(!(E instanceof ArchonOnly))&&(room!=null))
		    room.recoverRoomStats();
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"COPY");}

	
}
