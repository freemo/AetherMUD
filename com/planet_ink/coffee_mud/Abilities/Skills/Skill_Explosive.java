package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Explosive extends StdAbility
{
	public String ID() { return "Skill_Explosive"; }
	public String name(){ return "Explosive Touch";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"EXPLOTOUCH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Explosive();}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;


		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			str=auto?"<T-NAME> is **BLASTED**!":"^F<S-NAME> ** BLAST(S) ** <T-NAMESELF>!^?";
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int damage=Dice.roll(1,90+mob.envStats().level(),30);
				if(msg.value()>0)
					damage=damage/2;
				MUDFight.postDamage(mob,target,this,damage,CMMsg.TYP_OK_VISUAL,Weapon.TYPE_BURSTING,"The blast <DAMAGE> <T-NAME>!!!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to ** BLAST ** <T-NAMESELF>, but end(s) up looking silly.");

		return success;
	}

}
