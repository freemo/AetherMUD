package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_ControlUndead extends StdSkill
{
	public String ID() { return "Skill_ControlUndead"; }
	public String name(){ return "Control Undead";}
	public String displayText(){ return "(Controlled)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_DEATHLORE;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"CONTROL"};
	public String[] triggerStrings(){return triggerStrings;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&((msg.amISource((MOB)affected)))
		&&(msg.target()==invoker()))
		{
			if((!invoker().isInCombat())&&(msg.source().getVictim()!=invoker()))
			{
				msg.source().tell("You're too submissive towards "+invoker().name());
				if(invoker().getVictim()==msg.source())
				{
					invoker().makePeace();
					invoker().setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((target.baseCharStats().getMyRace()==null)
		||(!target.baseCharStats().getMyRace().racialCategory().equals("Undead")))
		{
			mob.tell(auto?"Only the undead can be controlled.":"You can only control the undead.");
			return false;
		}

		if(CMLib.flags().isGood(mob))
		{
			mob.tell("Only the wicked may control the undead.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,((mob.envStats().level()+(2*getXLEVELLevel(mob)))-target.envStats().level())*30,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_CAST_ATTACK_SOMANTIC_SPELL|(auto?CMMsg.MASK_ALWAYS:0),auto?"<T-NAME> seem(s) controlled.":"^S<S-NAME> control(s) <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if((mob.envStats().level()-target.envStats().level())>6)
					{
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> is now controlled.");
						target.makePeace();
						CMLib.commands().postFollow(target,mob,false);
						CMLib.combat().makePeaceInGroup(mob);
						invoker=mob;
						if(target.amFollowing()!=mob)
							mob.tell(target.name()+" seems unwilling to follow you.");
					}
					else
					{
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) submissive!");
						target.makePeace();
						beneficialAffect(mob,target,asLevel,5);
					}

				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to control <T-NAMESELF>, but fail(s).");


		// return whether it worked
		return success;
	}
}
