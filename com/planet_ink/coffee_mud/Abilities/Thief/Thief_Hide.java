package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Hide extends ThiefSkill
{
	public Thief_Hide()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hide";
		displayText="(Ability to hide)";
		miscText="";

		triggerStrings.addElement("HIDE");

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(3);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Hide();
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if(affect.amISource(mob))
		{
			if(((!Util.bset(affect.sourceMajor(),Affect.ACT_SOUND)
				 ||(affect.sourceMinor()==Affect.TYP_SPEAK)))
			 &&(!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
			 &&(affect.sourceMajor()>0))
				unInvoke();
		}
		return;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_HIDDEN);
		if(Sense.isSneaking(affected))
			affectableStats.setDisposition(affectableStats.disposition()-Sense.IS_SNEAKING);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already hiding.");
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}

		String str="You creep into a shadow and remain completely still.";

		boolean success=profficiencyCheck(0,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to hide and fail(s).");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,auto?Affect.MSG_OK_ACTION:(Affect.MSG_DELICATE_HANDS_ACT|Affect.ACT_MOVE),str,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Ability newOne=(Ability)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
			}
			else
				success=false;
		}
		return success;
	}
}
