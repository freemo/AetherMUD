package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class Spell_Earthquake extends Spell
{
	public String ID() { return "Spell_Earthquake"; }
	public String name(){return "Earthquake";}
	public String displayText(){return "(Earthquake)";}
	public int maxRange(){return 5;}
	public int minRange(){return 1;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}
	private boolean oncePerRd=false;

	public boolean tick(Tickable ticking, int tickID)
	{ oncePerRd=false; return super.tick(ticking,tickID);}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);
		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.sourceMinor()==CMMsg.TYP_STAND)
		&&(mob.location()!=null))
		{
			if(!oncePerRd)
			{
				oncePerRd=true;
				mob.location().show(mob,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> attempt(s) to stand up, and falls back down!");
			}
			return false;
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
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> regain(s) <S-HIS-HER> feet as the ground stops shaking.");
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					CommonMsgs.stand(mob,true);
				}
			}
			else
				mob.tell("The movement under your feet stops.");
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth shaking up.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{

			mob.location().show(mob,null,affectType(auto),(auto?"":"^S<S-NAME> invoke(s) a thunderous spell.^?")+CommonStrings.msp("earthquake.wav",40));
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if(Sense.isInFlight(target))
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) unaffected.");
				else
				if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						if(target.charStats().getBodyPart(Race.BODY_LEG)>0)
						{
							success=maliciousAffect(mob,target,asLevel,2,-1);
							if(success)
							{
								if(target.location()==mob.location())
									MUDFight.postDamage(mob,target,this,10,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,-1,"The ground underneath <T-NAME> shakes as <T-NAME> fall(s) to the ground!!");
							}
						}
						else
							mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) unaffected by the quake.");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to invoke a thunderous spell, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}
