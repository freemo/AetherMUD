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
public class Rebuke extends StdCommand
{
	public Rebuke(){}

	private String[] access={"REBUKE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Rebuke whom?");
			return false;
		}
		String str=Util.combine(commands,1);
		MOB target=mob.location().fetchInhabitant(str);
		if((target==null)&&(mob.getWorshipCharID().length()>0)
		&&(CMLib.english().containsString(mob.getWorshipCharID(),str)))
			target=CMLib.map().getDeity(str);
		if((target==null)&&(mob.getLiegeID().length()>0)
		&&(CMLib.english().containsString(mob.getLiegeID(),str)))
			target=CMLib.map().getLoadPlayer(mob.getLiegeID());
		
		if(target==null)
		{
			mob.tell("You don't see anybody called '"+Util.combine(commands,1)+"' or you aren't serving '"+Util.combine(commands,1)+"'.");
			return false;
		}

		CMMsg msg=null;
		if(target!=null)
			msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_REBUKE,"<S-NAME> rebuke(s) "+target.Name()+".");
		else
			msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_REBUKE,"<S-NAME> rebuke(s) "+mob.getLiegeID()+".");
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
