package com.planet_ink.coffee_mud.Abilities.Druid;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Barkskin extends Chant
{
	public Chant_Barkskin()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Barkskin";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Barkskin)";


		quality=Ability.BENEFICIAL_SELF;
		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(5);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_Barkskin();
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor() - 50);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		mob.tell("Your skin returns to normal.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You skin is already like bark.");
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
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType,"<S-NAME> chant(s) to <S-NAMESELF> and <S-HIS-HER> skin turns hard and brown!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				success=beneficialAffect(mob,mob,0);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-NAMESELF>, but nothing happens");

		// return whether it worked
		return success;
	}
}
