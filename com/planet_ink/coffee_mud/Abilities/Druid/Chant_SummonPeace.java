package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonPeace extends Chant
{
	public String ID() { return "Chant_SummonPeace"; }
	public String name(){ return "Summon Peace";}
	public int quality(){return Ability.BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		boolean someoneIsFighting=false;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB inhab=mob.location().fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
				someoneIsFighting=true;
		}

		if((success)&&(someoneIsFighting))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"A feeling of peace descends.":"^S<S-NAME> chant(s) for peace.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB inhab=mob.location().fetchInhabitant(i);
					if((inhab!=null)&&(inhab.isInCombat()))
					{
						inhab.tell("You feel at peace.");
						inhab.makePeace();
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for peace, but nothing happens.");


		// return whether it worked
		return success;
	}
}
