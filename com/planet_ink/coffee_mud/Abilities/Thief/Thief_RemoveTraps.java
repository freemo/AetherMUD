package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_RemoveTraps extends ThiefSkill
{
	public String ID() { return "Thief_RemoveTraps"; }
	public String name(){ return "Remove Traps";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"DETRAP","UNTRAP","REMOVETRAPS"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_RemoveTraps();}
	public Environmental lastChecked=null;

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whatTounlock=Util.combine(commands,0);
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
			unlockThis=mob.location().getExitInDir(dirCode);
		if((unlockThis==null)&&(whatTounlock.equalsIgnoreCase("room")||whatTounlock.equalsIgnoreCase("here")))
			unlockThis=mob.location();
		if(unlockThis==null)
			unlockThis=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(unlockThis==null) return false;
		int oldProfficiency=profficiency();

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(+((mob.envStats().level()
											 -unlockThis.envStats().level())*3),auto);
		Trap theTrap=CMClass.fetchMyTrap(unlockThis);
		Trap opTrap=null;
		if(unlockThis instanceof Exit)
		{
			dirCode=ExternalPlay.getMyDirCode((Exit)unlockThis,mob.location(),dirCode);
			if(dirCode>=0)
			{
				Exit exit=mob.location().getReverseExit(dirCode);
				if(exit!=null)
					opTrap=CMClass.fetchMyTrap(exit);
			}
		}
		FullMsg msg=new FullMsg(mob,unlockThis,this,auto?Affect.MSG_OK_ACTION:Affect.MSG_DELICATE_HANDS_ACT,Affect.MSG_DELICATE_HANDS_ACT,Affect.MSG_OK_ACTION,auto?unlockThis.name()+" begins to glow.":"<S-NAME> attempt(s) to safely deactivate a trap on "+unlockThis.name()+".");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			if((unlockThis==lastChecked)&&((theTrap==null)||(theTrap.disabled())))
				setProfficiency(oldProfficiency);
			if(success)
			{
				if(theTrap!=null)
					theTrap.disable();
				if(opTrap!=null)
					opTrap.disable();
			}
			if(!auto)
				mob.tell("You have completed your attempt.");
			lastChecked=unlockThis;
		}

		return success;
	}
}