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
public class Ignore extends StdCommand
{
	public Ignore(){}

	private String[] access={"IGNORE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		HashSet h=pstats.getIgnored();
		if((commands.size()<2)||(((String)commands.elementAt(1)).equalsIgnoreCase("list")))
		{
			if(h.size()==0)
				mob.tell("You have no names on your ignore list.  Use IGNORE ADD to add more.");
			else
			{
				StringBuffer str=new StringBuffer("You are ignoring: ");
				for(Iterator e=h.iterator();e.hasNext();)
					str.append(((String)e.next())+" ");
				mob.tell(str.toString());
			}
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("ADD"))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Add whom?");
				return false;
			}
			MOB M=CMClass.getMOB("StdMOB");
			if(!CMLib.database().DBUserSearch(M,name))
			{
				mob.tell("No player by that name was found.");
                M.destroy();
				return false;
			}
			if(h.contains(M.Name()))
			{
				mob.tell("That name is already on your list.");
                M.destroy();
				return false;
			}
			h.add(M.Name());
			mob.tell("The Player '"+M.Name()+"' has been added to your ignore list.");
            M.destroy();
		}
		else
		if(((String)commands.elementAt(1)).equalsIgnoreCase("REMOVE"))
		{
			String name=CMParms.combine(commands,2);
			if(name.length()==0)
			{
				mob.tell("Remove whom?");
				return false;
			}
			if(!h.contains(name))
			{
				mob.tell("That name '"+name+"' does not appear on your list.  Watch your casing!");
				return false;
			}
			h.remove(name);
			mob.tell("The Player '"+name+"' has been removed from your ignore list.");
		}
		else
		{
			mob.tell("Parameter '"+((String)commands.elementAt(1))+"' is not recognized.  Try LIST, ADD, or REMOVE.");
			return false;
		}
		return false;
	}
	public int actionsCost(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
