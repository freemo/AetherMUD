package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_AuraHarm extends Prayer
{
	public String ID() { return "Prayer_AuraHarm"; }
	public String name(){ return "Aura of Harm";}
	public String displayText(){ return "(Harm Aura)";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public Environmental newInstance(){	return new Prayer_AuraHarm();	}
	private int tickDown=4;


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Room)))
			return;
		Room R=(Room)affected;

		super.unInvoke();

		if(canBeUninvoked())
			R.showHappens(CMMsg.MSG_OK_VISUAL,"The harmful aura around you fades.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.tick(ticking,tickID);

		if((--tickDown)>=0) return super.tick(ticking,tickID);
		tickDown=4;
		
		Hashtable H=null;
		if((invoker()!=null)&&(invoker().location()==affected))
		{
			H=new Hashtable();
			invoker().getGroupMembers(H);
		}
		Room R=(Room)affected;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)&&((H==null)||(!H.containsKey(M))))
			{
				if(invoker()!=null)
				{
					int harming=Dice.roll(1,adjustedLevel(invoker())+3,3);
					MUDFight.postDamage(invoker(),M,this,harming,CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The unholy aura <DAMAGE> <T-NAME>!");
				}
				else
				{
					int harming=Dice.roll(1,CMAble.lowestQualifyingLevel(ID())+3,3);
					MUDFight.postDamage(M,M,this,harming,CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The unholy aura <DAMAGE> <T-NAME>!");
				}
			}
		}
		return super.tick(ticking,tickID);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("The aura of harm is already here.");
			return false;
		}
		if(target.fetchEffect("Prayer_AuraHeal")!=null)
		{
			target.fetchEffect("Prayer_AuraHeal").unInvoke();
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for all to feel pain.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"A harmful aura descends over the area!");
				maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for an aura of harm, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}