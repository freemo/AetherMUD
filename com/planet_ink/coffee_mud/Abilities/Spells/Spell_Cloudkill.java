package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_Cloudkill extends Spell
	implements EvocationDevotion
{
	public Spell_Cloudkill()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Cloudkill";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Cloudkill)";


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(16);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Cloudkill();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SITTING);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("You feel less intoxicated.");
		ExternalPlay.standIfNecessary(mob);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth clouding.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			mob.location().show(mob,null,affectType,auto?"A horrendous green cloud appears!":"<S-NAME> evoke(s) a horrendous green cloud.");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_GAS,null);
				if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						mob.location().send(mob,msg2);
						invoker=mob;

						int damage = target.curState().getHitPoints();

						int midLevel=(int)Math.round(Util.div(mob.envStats().level(),2.0));
						if(target.envStats().level()>=midLevel)
						{
							while(midLevel<target.envStats().level())
							{
								damage-=(int)Math.round(Util.div(damage,2.0));
								midLevel++;
							}
						}

						if(msg2.wasModified())
							damage = (int)Math.round(Util.div(damage,2.0));

						if(damage<0) damage=0;
						if(target.location()==mob.location())
						{
							target.location().show(target,null,Affect.MSG_OK_ACTION,"The gas "+ExternalPlay.standardHitWord(-1,damage)+" <S-NAME>. <S-NAME> collapse(s)!");
							this.maliciousAffect(mob,target,2,-1);
							ExternalPlay.postDamage(mob,target,this,damage);
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to evoke a green cloud, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}