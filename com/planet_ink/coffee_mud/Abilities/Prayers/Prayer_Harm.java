package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Harm extends Prayer
{
	public String ID() { return "Prayer_Harm"; }
	public String name(){ return "Harm";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public Environmental newInstance(){	return new Prayer_Harm();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		boolean undead=target.charStats().getMyRace().racialCategory().equals("Undead");

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,(undead?0:CMMsg.MASK_MALICIOUS)|affectType(auto),(auto?"<T-NAME> cringe(s) in pain.":"^S<S-NAME> "+prayWord(mob)+" to deliver tremendous pain at <T-NAMESELF>!^?")+CommonStrings.msp("spelldam2.wav",40));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					int harming=Dice.roll(4,adjustedLevel(mob)+24,8);
					MUDFight.postDamage(mob,target,this,harming,CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The unholy spell <DAMAGE> <T-NAME>!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
