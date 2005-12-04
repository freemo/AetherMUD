package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

/* 
   Copyright 2004 Tim Kassebaum

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

public class MasterArmorsmithing extends Armorsmithing
{
	public String ID() { return "MasterArmorsmithing"; }
	public String name(){ return "Master Armorsmithing";}
	private static final String[] triggerStrings = {"MARMORSMITH","MASTERARMORSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}

    protected Vector loadRecipes(){return super.loadRecipes("masterarmorsmith.txt");}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Armorsmithing")==null)
		{
			teacher.tell(student.name()+" has not yet learned armorsmithing.");
			student.tell("You need to learn armorsmithing before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		int autoGenerate=0;

			if((auto)&&(givenTarget==this)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
		}
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"marmorsmith list\" for a list,\"marmorsmith scan\", or \"marmorsmith mend <item>\".");
			return false;
		}
		if(autoGenerate>0)
			commands.insertElementAt(new Integer(autoGenerate),0);
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}
}
