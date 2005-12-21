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
public class Dismount extends StdCommand
{
	public Dismount(){}

	private String[] access={getScr("Dismount","cmd"),getScr("Dismount","cmd1"),getScr("Dismount","cmd2")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			if(mob.riding()==null)
			{
				mob.tell(getScr("Movement","dismounterr1"));
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,mob.riding(),null,CMMsg.MSG_DISMOUNT,getScr("Movement","dismounts",mob.riding().dismountString(mob)));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			Environmental E=mob.location().fetchFromRoomFavorItems(null,CMParms.combine(commands,0),Item.WORN_REQ_ANY);
			if((E==null)||(!(E instanceof Rider)))
			{
				mob.tell(getScr("Movement","dismounterr2",CMParms.combine(commands,0)));
				return false;
			}
			Rider RI=(Rider)E;
			if((RI.riding()==null)
			   ||((RI.riding() instanceof MOB)&&(!mob.location().isInhabitant((MOB)RI.riding())))
			   ||((RI.riding() instanceof Item)&&(!mob.location().isContent((Item)RI.riding())))
			   ||(!CMLib.flags().canBeSeenBy(RI.riding(),mob)))
			{
				mob.tell(getScr("Movement","dismounterr3",RI.name()));
				return false;
			}
			if((RI instanceof MOB)&&(!CMLib.flags().isBoundOrHeld(RI))&&(!((MOB)RI).willFollowOrdersOf(mob)))
			{
			    mob.tell(getScr("Movement","dismounterr4",RI.name()));
			    return false;
			}
			CMMsg msg=CMClass.getMsg(mob,RI.riding(),RI,CMMsg.MSG_DISMOUNT,getScr("Movement","dismounts2"));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		return false;
	}
	public double actionsCost(){return 1.0;}
	public boolean canBeOrdered(){return true;}

	
}
