package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WeakParalysis extends StdAbility
{
	public String ID() { return "WeakParalysis"; }
	public String name(){ return "Weak Paralysis";}
	public String displayText(){ return "(Paralyzed)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new WeakParalysis();}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"WPARALYZE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public long flags(){return Ability.FLAG_BINDING;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("The paralysis eases out of your muscles.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_PARALYZE|(auto?Affect.MASK_GENERAL:0),auto?"":"^S<S-NAME> paralyze(s) <T-NAMESELF>.^?");
			if(target.location().okAffect(target,msg))
			{
				target.location().send(target,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,5,-1);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> can't move!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to paralyze <T-NAMESELF>, but fail(s)!");


		// return whether it worked
		return success;
	}
}