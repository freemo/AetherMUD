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
public class Report extends BaseAbleLister
{
	public Report(){}

	private String[] access={"REPORT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			StringBuffer buf=new StringBuffer(
							    "say \"I have "+mob.curState().getHitPoints()
							   +"/"+mob.maxState().getHitPoints()+" hit points, "
							   +mob.curState().getMana()+"/"+mob.maxState().getMana()
							   +" mana, "+mob.curState().getMovement()
							   +"/"+mob.maxState().getMovement()+" move");
			if((!CMSecurity.isDisabled("EXPERIENCE"))
			&&!mob.charStats().getCurrentClass().expless()
			&&!mob.charStats().getMyRace().expless()
			&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
			   buf.append(", and need "+mob.getExpNeededLevel()+" to level");
			buf.append(".\"");
			Command C=CMClass.getCommand("Say");
			if(C!=null) C.execute(mob,CMParms.parse(buf.toString()));
		}
		else
		{
			int level=parseOutLevel(commands);
			String s=CMParms.combine(commands,1).toUpperCase();
			if("SPELLS".startsWith(s))
				CMLib.commands().postSay(mob,null,("^NMy spells:^? "+getAbilities(mob,Ability.SPELL,-1,false,level)),false,false);
			else
			if("SKILLS".startsWith(s))
			{
				Vector V=new Vector();
				V.addElement(new Integer(Ability.THIEF_SKILL));
				V.addElement(new Integer(Ability.SKILL));
				V.addElement(new Integer(Ability.COMMON_SKILL));
				CMLib.commands().postSay(mob,null,("^NMy skills:^? "+getAbilities(mob,V,-1,false,level)),false,false);
			}
			else
			if("AFFECTS".startsWith(s))
			{
				
				StringBuffer aff=new StringBuffer("\n\r^!I am affected by:^? ");
				Command C=CMClass.getCommand("Affect");
				if(C!=null) C.execute(mob,CMParms.makeVector(aff));
				CMLib.commands().postSay(mob,null,aff.toString(),false,false);
			}
			else
			if("PRAYERS".startsWith(s))
				CMLib.commands().postSay(mob,null,("^NMy prayers:^? "+getAbilities(mob,Ability.PRAYER,-1,false,level)),false,false);
			else
			if(("POWERS".startsWith(s))||("SUPER POWERS".startsWith(s)))
				CMLib.commands().postSay(mob,null,("^NMy super powers:^? "+getAbilities(mob,Ability.SUPERPOWER,-1,false,level)),false,false);
			else
			if("EVIL DEEDS".startsWith(s))
				CMLib.commands().postSay(mob,null,("^NMy evil deeds:^? "+getAbilities(mob,Ability.EVILDEED,-1,false,level)),false,false);
			else
			if("CHANTS".startsWith(s))
				CMLib.commands().postSay(mob,null,("^NMy chants:^? "+getAbilities(mob,Ability.CHANT,-1,false,level)),false,false);
			else
			if("SONGS".startsWith(s))
				CMLib.commands().postSay(mob,null,("^NMy songs:^? "+getAbilities(mob,Ability.SONG,-1,false,level)),false,false);
			else
				mob.tell("'"+s+"' is unknown.  Try SPELLS, SKILLS, PRAYERS, CHANTS, or SONGS.");
		}
		return false;
	}
    public double combatActionsCost(){return 1.0;}
    public double actionsCost(){return 0.25;}
	public boolean canBeOrdered(){return true;}

	
}
