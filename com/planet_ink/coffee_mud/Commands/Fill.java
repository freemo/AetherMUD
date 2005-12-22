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
public class Fill extends BaseItemParser
{
	public Fill(){}

	private String[] access={"FILL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Fill what, from what?");
			return false;
		}
		commands.removeElementAt(0);
		if((commands.size()<2)&&(!(mob.location() instanceof Drink)))
		{
			mob.tell("From what should I fill the "+(String)commands.elementAt(0)+"?");
			return false;
		}
		Environmental fillFromThis=null;
		if((commands.size()==1)&&(mob.location() instanceof Drink))
			fillFromThis=mob.location();
		else
		{
			String thingToFillFrom=(String)commands.lastElement();
			fillFromThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToFillFrom,Item.WORN_REQ_ANY);
			if((fillFromThis==null)||((fillFromThis!=null)&&(!CMLib.flags().canBeSeenBy(fillFromThis,mob))))
			{
				mob.tell("I don't see "+thingToFillFrom+" here.");
				return false;
			}
			commands.removeElementAt(commands.size()-1);
		}

		int maxToFill=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMath.s_int((String)commands.firstElement())>0)
		&&(CMLib.english().numPossibleGold(mob,CMParms.combine(commands,0))==0))
		{
			maxToFill=CMath.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		String thingToFill=CMParms.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(thingToFill.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToFill="ALL "+thingToFill.substring(4);}
		if(thingToFill.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToFill="ALL "+thingToFill.substring(0,thingToFill.length()-4);}
		do
		{
			Item fillThis=mob.fetchInventory(null,thingToFill+addendumStr);
			if(fillThis==null) break;
			if((CMLib.flags().canBeSeenBy(fillThis,mob))
			&&(!V.contains(fillThis)))
				V.addElement(fillThis);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(maxToFill<addendum));
		if(V.size()==0)
			mob.tell("You don't seem to have '"+thingToFill+"'.");
		else
		for(int i=0;i<V.size();i++)
		{
			Environmental fillThis=(Environmental)V.elementAt(i);
			CMMsg fillMsg=CMClass.getMsg(mob,fillThis,fillFromThis,CMMsg.MSG_FILL,"<S-NAME> fill(s) <T-NAME> from <O-NAME>.");
			if((!mob.isMine(fillThis))&&(fillThis instanceof Item))
			{
				if(CMLib.commands().postGet(mob,null,(Item)fillThis,false))
					if(mob.location().okMessage(mob,fillMsg))
						mob.location().send(mob,fillMsg);
			}
			else
			if(mob.location().okMessage(mob,fillMsg))
				mob.location().send(mob,fillMsg);
		}
		return false;
	}
    public double combatActionsCost(){return 1.0;}
    public double actionsCost(){return 0.25;}
	public boolean canBeOrdered(){return true;}

	
}
