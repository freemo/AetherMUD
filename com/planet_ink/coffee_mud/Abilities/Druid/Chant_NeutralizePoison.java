package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_NeutralizePoison extends Chant
{
	public Chant_NeutralizePoison()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Neutralize Poison";

		canAffectCode=0;
		canTargetCode=Ability.CAN_MOBS;
		
		baseEnvStats().setLevel(11);
		quality=Ability.OK_OTHERS;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_NeutralizePoison();
	}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numAffects();a++)
		{
			Ability A=fromMe.fetchAffect(a);
			if(A!=null)
			{
				if((A.ID().toUpperCase().indexOf("POISON")>=0)
				||(A.name().toUpperCase().indexOf("POISON")>=0)
				||(A.displayText().toUpperCase().indexOf("POISON")>=0))
					offenders.addElement(A);
			}
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		Vector offensiveAffects=returnOffensiveAffects(target);

		if((success)&&(offensiveAffects.size()>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> feel(s) delivered from <T-HIS-HER> poisonous affliction.":"^S<S-NAME> chant(s) for <T-NAME> be delivered from <T-HIS-HER> poisonous infliction.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				int old=target.numAffects();
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.elementAt(a)).unInvoke();
				if(old>target.numAffects())
					target.tell("You feel much better!");
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> chant(s) for <T-NAME> be delivered from <T-HIS-HER> poisonous infliction, but nothing happens.");


		// return whether it worked
		return success;
	}
}
