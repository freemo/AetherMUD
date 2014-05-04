package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Fighter_BodyToss extends MonkSkill
{
	@Override public String ID() { return "Fighter_BodyToss"; }
	@Override public String name(){ return "Body Toss";}
	private static final String[] triggerStrings = {"BODYTOSS"};
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return Ability.CAN_MOBS;}
	@Override public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_GRAPPLING;}
	@Override public int usageType(){return USAGE_MOVEMENT;}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(anyWeapons(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.rangeToTarget()>0)
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isSitting(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.charStats().getBodyPart(Race.BODY_ARM)<=1)
				return Ability.QUALITY_INDIFFERENT;
			if(target.basePhyStats().weight()>(mob.basePhyStats().weight()*2))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=mob.getVictim();
		if(target==null)
		{
			mob.tell(_("You can only do this in combat!"));
			return false;
		}
		if(anyWeapons(mob))
		{
			mob.tell(_("You must be unarmed to use this skill."));
			return false;
		}
		if(mob.rangeToTarget()>0)
		{
			mob.tell(_("You must get closer to @x1 first!",target.charStats().himher()));
			return false;
		}
		if(CMLib.flags().isSitting(mob))
		{
			mob.tell(_("You need to stand up!"));
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_ARM)<=1)
		{
			mob.tell(_("You need arms to do this."));
			return false;
		}
		if(target.basePhyStats().weight()>(mob.basePhyStats().weight()*2))
		{
			mob.tell(_("@x1 is too big for you to toss!",target.name(mob)));
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),_("^F^<FIGHT^><S-NAME> pick(s) up <T-NAMESELF> and toss(es) <T-HIM-HER> into the air!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int dist=2+getXLEVELLevel(mob);
				if(mob.location().maxRange()<2) dist=mob.location().maxRange();
				mob.setAtRange(dist);
				target.setAtRange(dist);
				CMLib.combat().postDamage(mob,target,this,CMLib.dice().roll(1,12,0),CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,"The hard landing <DAMAGE> <T-NAME>!");
				if(mob.getVictim()==null) mob.setVictim(null); // correct range
				if(target.getVictim()==null) target.setVictim(null); // correct range
			}
		}
		else
			return beneficialVisualFizzle(mob,target,_("<S-NAME> attempt(s) to pick up <T-NAMESELF>, but fail(s)."));

		// return whether it worked
		return success;
	}
}
