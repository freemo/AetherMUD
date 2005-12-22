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
   Copyright 2000-2005 Bo Zimmerman

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
public class Get extends BaseItemParser
{
	public Get(){}

	private String[] access={"GET","G"};
	public String[] getAccessWords(){return access;}

	public static boolean get(MOB mob, Item container, Item getThis, boolean quiet)
	{ return get(mob,container,getThis,quiet,"get",false);}

	public static boolean get(MOB mob,
							  Item container,
							  Item getThis,
							  boolean quiet,
							  String getWord,
							  boolean optimize)
	{
		String theWhat="<T-NAME>";
		Item target=getThis;
		Item tool=null;
		if(container!=null)
		{
			tool=getThis;
			target=container;
			theWhat="<O-NAME> from <T-NAME>";
		}
		if(!getThis.amWearingAt(Item.INVENTORY))
		{
			CMMsg msg=CMClass.getMsg(mob,getThis,null,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_REMOVE,null);
			if(!mob.location().okMessage(mob,msg))
				return false;
			mob.location().send(mob,msg);
		}
		CMMsg msg=CMClass.getMsg(mob,target,tool,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_GET,quiet?null:"<S-NAME> "+getWord+"(s) "+theWhat+".");
		if(!mob.location().okMessage(mob,msg))
			return false;
		mob.location().send(mob,msg);
		// we do this next step because, when a container is involved,
		// the item deserves to be the target of the GET.
		if(!mob.isMine(target))
		{
			msg=CMClass.getMsg(mob,getThis,null,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_GET,null);
			if(!mob.location().okMessage(mob,msg))
				return false;
			mob.location().send(mob,msg);
		}
		return true;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()>1)&&(commands.firstElement() instanceof Item))
		{
			Item item=(Item)commands.firstElement();
			Item container=null;
			boolean quiet=false;
			if(commands.elementAt(1) instanceof Item)
			{
				container=(Item)commands.elementAt(1);
				if((commands.size()>2)&&(commands.elementAt(2) instanceof Boolean))
					quiet=((Boolean)commands.elementAt(2)).booleanValue();
			}
			else
			if(commands.elementAt(1) instanceof Boolean)
				quiet=((Boolean)commands.elementAt(1)).booleanValue();
			boolean success=get(mob,container,item,quiet);
			if(item instanceof Coins)
			    ((Coins)item).putCoinsBack();
			return success;
		}

		if(commands.size()<2)
		{
			mob.tell("Get what?");
			return false;
		}
		commands.removeElementAt(0);
		boolean quiet=false;
		if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			quiet=true;
			commands.removeElementAt(commands.size()-1);
		}

		String containerName="";
		if(commands.size()>0)
			containerName=(String)commands.lastElement();
		Vector containerCommands=(Vector)commands.clone();
		Vector containers=CMLib.english().possibleContainers(mob,commands,Item.WORN_REQ_ANY,true);
		int c=0;

		int maxToGet=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMath.s_int((String)commands.firstElement())>0)
		&&(CMLib.english().numPossibleGold(null,CMParms.combine(commands,0))==0))
		{
			maxToGet=CMath.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
			if(containers.size()==0)
			{
				int fromDex=-1;
				for(int i=1;i<commands.size();i++)
				    if(((String)commands.elementAt(i)).equalsIgnoreCase("from"))
				    {	fromDex=i; break;}
				if(fromDex>0)
				{
				    String fromWhatName=CMParms.combine(commands,fromDex+1);
				    while(commands.size()>fromDex)
				        commands.removeElementAt(fromDex);
				    Environmental fromWhat=mob.location().fetchFromMOBRoomFavorsItems(mob,null,fromWhatName,Item.WORN_REQ_UNWORNONLY);
				    if(fromWhat==null)
				    {
				        mob.tell("You don't see '"+fromWhatName+"' here.");
				        return false;
				    }
				    
				    Environmental toWhat=null;
                    if((fromWhat instanceof PackagedItems)
                    &&(mob.isMine(fromWhat)))
                    {
                        mob.tell("You'll need to put that down first.");
                        return false;
                    }
				    if(fromWhat instanceof Item)
					    toWhat=CMLib.utensils().unbundle((Item)fromWhat,maxToGet);
				    if(toWhat==null)
				    {
				        mob.tell("You can't get anything from "+fromWhat.name()+".");
				        return false;
				    }
				    if(commands.size()==1)
				        commands.addElement(toWhat.name());
				}
			}
		}

		String whatToGet=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(whatToGet.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(4);}
		if(whatToGet.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);}
		boolean doneSomething=false;
		while((c<containers.size())||(containers.size()==0))
		{
			Vector V=new Vector();
			Item container=null;
			if(containers.size()>0) 
			    container=(Item)containers.elementAt(c++);
			int addendum=1;
			String addendumStr="";
			do
			{
				Environmental getThis=null;
				if((container!=null)&&(mob.isMine(container)))
				   getThis=mob.location().fetchFromMOBRoomFavorsItems(mob,container,whatToGet+addendumStr,Item.WORN_REQ_UNWORNONLY);
				else
				{
					if(!allFlag)
						getThis=CMLib.english().possibleRoomGold(mob,mob.location(),container,whatToGet);
					if(getThis==null)
						getThis=mob.location().fetchFromRoomFavorItems(container,whatToGet+addendumStr,Item.WORN_REQ_UNWORNONLY);
				}
				if(getThis==null) break;
				if((getThis instanceof Item)
				&&((CMLib.flags().canBeSeenBy(getThis,mob)||(getThis instanceof Light)))
				&&((!allFlag)||CMLib.flags().isGettable(((Item)getThis))||(getThis.displayText().length()>0))
				&&(!V.contains(getThis)))
					V.addElement(getThis);
				addendumStr="."+(++addendum);
			}
			while((allFlag)&&(addendum<=maxToGet));

			for(int i=0;i<V.size();i++)
			{
				Item getThis=(Item)V.elementAt(i);
				get(mob,container,getThis,quiet,"get",true);
				if(getThis instanceof Coins)
					((Coins)getThis).putCoinsBack();
				doneSomething=true;
			}
			mob.location().recoverRoomStats();
			mob.location().recoverRoomStats();

			if(containers.size()==0) break;
		}
		if(!doneSomething)
		{
			if(containers.size()>0)
			{
				Item container=(Item)containers.elementAt(0);
				if(((Container)container).isOpen())
                    mob.tell(mob,container,null,"You don't see that in <T-NAME>.");
				else
					mob.tell(container.name()+" is closed.");
			}
			else
			if(containerName.equalsIgnoreCase("all"))
				mob.tell("You don't see anything here.");
			else
			{
			    Vector V=CMLib.english().possibleContainers(mob,containerCommands,Item.WORN_REQ_ANY,false);
			    if(V.size()==0)
					mob.tell("You don't see '"+containerName+"' here.");
				else
			    if(V.size()==1)
					mob.tell("You don't see that in "+((Item)V.firstElement()).name()+" here.");
			    else
					mob.tell("You don't see that in any '"+containerName+"'.");
			}
		}
		return false;
	}
    public double combatActionsCost(){return 1.0;}
    public double actionsCost(){return 0.25;}
	public boolean canBeOrdered(){return true;}

	
}
