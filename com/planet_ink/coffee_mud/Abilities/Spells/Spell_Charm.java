package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_Charm extends Spell
	implements CharmDevotion
{
	public Spell_Charm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Charm";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Charmed)";

		quality=Ability.MALICIOUS;


		baseEnvStats().setLevel(6);

		addQualifyingClass("Mage",6);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);
		addQualifyingClass("Thief",24);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Charm();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.source()==mob.amFollowing()))
				unInvoke();
		if((affect.amISource(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.target()==mob.amFollowing()))
		{
			mob.tell("You like "+mob.amFollowing().charStats().himher()+" too much.");
			return false;
		}

		return super.okAffect(affect);
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		if(affect.amISource((MOB)affected))
			((MOB)affected).setFollowing(invoker);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affecting()==null)||(!(affecting() instanceof MOB)))
			return;

		if(affected == affecting())
			((MOB)affecting()).setFollowing(invoker);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your free-will returns.");
		mob.tell("You are no longer following anyone.");
		if(mob.amFollowing()!=null)
			mob.amFollowing().tell(mob.name()+" is no longer following you.");
		mob.setFollowing(null);
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if((!target.isMonster())||(levelDiff>=3))
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}

		if(!Sense.canSpeak(mob))
		{
			mob.tell("You can't speak!");
			return false;
		}

		// if they can't hear the sleep spell, it
		// won't happen
		if((!auto)&&(!Sense.canBeHeardBy(mob,target)))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(-50-((target.charStats().getIntelligence()*3)+(levelDiff*5)),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			String str=auto?"":"<S-NAME> smile(s) and wink(s) at <T-NAMESELF>";
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_CAST_VERBAL_SPELL,str+".");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,Affect.MSK_CAST_VERBAL|Affect.TYP_MIND);
					if(success);
					{
						mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> follow(s) <S-NAME>!");
						target.setFollowing(mob);
					}
				}
			}
		}
		if(!success)
			return maliciousFizzle(mob,target,"<S-NAME> smile(s) and wink(s) at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
