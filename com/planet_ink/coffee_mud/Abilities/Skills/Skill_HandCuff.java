package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Skill_HandCuff extends StdAbility
{
	public String ID() { return "Skill_HandCuff"; }
	public String name(){ return "Handcuff";}
	public String displayText(){ return "(Handcuffed)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"HANDCUFF","CUFF"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public long flags(){return Ability.FLAG_BINDING;}
	public int usageType(){return USAGE_MOVEMENT;}

	public int amountRemaining=0;
	public boolean oldAssist=false;


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BOUND);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
			{
				if((msg.source()!=null)&&(msg.source().location()!=null))
					msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but the handcuffs prevent <S-HIM-HER>.");
				return false;
			}
			else
			if(((msg.sourceMinor()==CMMsg.TYP_FOLLOW)&&(msg.target()!=invoker()))
			||((msg.sourceMinor()==CMMsg.TYP_NOFOLLOW)&&(msg.source().amFollowing()==invoker())))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against <S-HIS-HER> cuffs.");
				amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level());
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
			else
			if(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				return true;
			else
			if(((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target()!=null)
			&&(msg.target() instanceof Room)
			&&(!((Room)msg.target()).isInhabitant(invoker))))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against <S-HIS-HER> cuffs.");
				amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level());
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
			else
			if(msg.sourceMinor()==CMMsg.TYP_ENTER)
				return true;
			else
			if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			&&((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against <S-HIS-HER> cuffs.");
				amountRemaining-=mob.charStats().getStat(CharStats.STRENGTH);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		else
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&(msg.amITarget(affected))
		&&(!mob.isInCombat())
		&&(mob.amFollowing()!=null)
		&&(msg.source().isMonster())
		&&(msg.source().getVictim()!=mob))
		{
			msg.source().tell("You may not assault this prisoner.");
			if(mob.getVictim()==msg.source())
			{
				mob.makePeace();
				mob.setVictim(null);
			}
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.setFollowing(null);
			if(!mob.amDead())
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> released from the handcuffs.");
			if(!oldAssist)
				mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOASSIST));
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!Sense.isSleeping(target))&&(!Sense.isSitting(target))&&(!auto))
		{
			mob.tell(target.name()+" doesn't look willing to cooperate.");
			return false;
		}
		if(mob.isInCombat()&&(!auto))
		{
			mob.tell("Not while you are fighting!");
			return false;
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_MALICIOUS,"<S-NAME> handcuff(s) <T-NAME>.");
			if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					amountRemaining=adjustedLevel(mob)*300;
					if(target.location()==mob.location())
					{
						success=maliciousAffect(mob,target,Integer.MAX_VALUE-1000,-1);
						if(success)
						{
							oldAssist=Util.bset(target.getBitmap(),MOB.ATT_AUTOASSIST);
							if(!oldAssist)
								target.setBitmap(Util.setb(target.getBitmap(),MOB.ATT_AUTOASSIST));
							CommonMsgs.doStandardCommand(target,"NoFollow",Util.makeVector("UNFOLLOW","QUIETLY"));
							CommonMsgs.follow(target,mob,true);
							target.setFollowing(mob);
						}
					}
				}
				if(mob.getVictim()==target) mob.setVictim(null);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to bind <T-NAME> and fail(s).");


		// return whether it worked
		return success;
	}
}