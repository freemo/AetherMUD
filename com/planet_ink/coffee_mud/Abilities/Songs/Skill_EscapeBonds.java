package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_EscapeBonds extends BardSkill
{
	public String ID() { return "Skill_EscapeBonds"; }
	public String name(){ return "Escape Bonds";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"ESCAPEBONDS","ESCAPE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_EscapeBonds();}
	public int usageType(){return USAGE_MANA;}


	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(!Sense.isBoundOrHeld(mob))
			unInvoke();
		else
			stats.setStat(CharStats.STRENGTH,stats.getStat(CharStats.STRENGTH)+stats.getStat(CharStats.DEXTERITY)+mob.envStats().level());
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!Sense.isBoundOrHeld(mob))
		{
			mob.tell("You don't seem to be bound by anything you can escape!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_GENERAL:0),"<S-NAME> attempt(s) to escape <S-HIS-HER> bonds.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.addEffect(this);
				mob.recoverCharStats();
				mob.location().send(mob,msg);
				mob.delEffect(this);
				mob.recoverCharStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> fumble(s) <S-HIS-HER> attempt to escape.");

		return success;
	}

}