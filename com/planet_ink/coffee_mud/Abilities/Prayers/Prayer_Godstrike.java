package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Godstrike extends Prayer
{
	public String ID() { return "Prayer_Godstrike"; }
	public String name(){ return "Godstrike";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY;}
	public Environmental newInstance(){	return new Prayer_Godstrike();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		boolean undead=target.charStats().getMyRace().racialCategory().equals("Undead");

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if((success)&&(target.getAlignment()<350))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			Prayer_Godstrike newOne=(Prayer_Godstrike)this.copyOf();
			FullMsg msg=new FullMsg(mob,target,newOne,affectType(auto)|CMMsg.MASK_MALICIOUS,(auto?"<T-NAME> is filled with holy fury!":"^S<S-NAME> "+prayWord(mob)+" for power against the evil inside <T-NAMESELF>!^?")+CommonStrings.msp("spelldam1.wav",40));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					int harming=Dice.roll(3,adjustedLevel(mob),adjustedLevel(mob));
					if(undead) harming=harming*2;
					if(target.getAlignment()<350)
						MUDFight.postDamage(mob,target,this,harming,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^SThe holy STRIKE of the gods <DAMAGE> <T-NAME>!^?");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
