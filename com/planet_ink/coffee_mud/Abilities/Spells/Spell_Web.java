package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2000-2010 Bo Zimmerman

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
public class Spell_Web extends Spell
{
	public String ID() { return "Spell_Web"; }
	public String name(){return "Web";}
	public String displayText(){return "(Webbed)";}
	public int maxRange(){return adjustedMaxInvokerRange(5);}
	public int minRange(){return 1;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
	public long flags(){return Ability.FLAG_BINDING;}

	public int amountRemaining=0;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BOUND);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if((!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
			&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
			{
				if(mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against the web."))
				{
					amountRemaining-=(mob.charStats().getStat(CharStats.STAT_STRENGTH)+mob.envStats().level());
					if(amountRemaining<0)
						unInvoke();
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the web.");
			CMLib.commands().postStand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth webbing.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),(auto?"":"^S<S-NAME> speak(s) and wave(s) <S-HIS-HER> arms.^?")+CMProps.msp("web.wav",40)))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
				if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						amountRemaining=160;
						if(target.location()==mob.location())
						{
							success=maliciousAffect(mob,target,asLevel,(adjustedLevel(mob,asLevel)*10),-1);
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> become(s) stuck in a mass of web!");
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> speak(s) and wave(s) <S-HIS-HER> arms, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}
