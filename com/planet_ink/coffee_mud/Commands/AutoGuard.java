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
Copyright 2000-2008 Bo Zimmerman

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
public class AutoGuard extends StdCommand
{
	public AutoGuard(){}

	private String[] access={"AUTOGUARD","GUARD"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((!CMath.bset(mob.getBitmap(),MOB.ATT_AUTOGUARD))
		   ||((commands.size()>0)&&(((String)commands.firstElement()).toUpperCase().startsWith("G"))))
		{
			mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_AUTOGUARD));
			mob.tell("You are now on guard. You will no longer follow group leaders.");
			if(mob.isMonster())
				CMLib.commands().postSay(mob,null,"I am now on guard.",false,false);
		}
		else
		{
			mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_AUTOGUARD));
			mob.tell("You are no longer on guard.  You will now follow group leaders.");
			if(mob.isMonster())
				CMLib.commands().postSay(mob,null,"I will now follow my group leader.",false,false);
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}

