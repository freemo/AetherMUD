package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_TimeStop extends Spell
{
	protected Vector fixed=new Vector();
	public Spell_TimeStop()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Time Stop";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Time is Stopped)";

		canAffectCode=Ability.CAN_MOBS|Ability.CAN_ROOMS;
		canTargetCode=0;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(18);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_TimeStop();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(canBeUninvoked)
		{
			if(affected instanceof Room)
			{
				Room room=(Room)affected;
				room.showHappens(Affect.MSG_OK_VISUAL, "Time starts moving again...");
				if(invoker!=null)
				{
					Ability me=invoker.fetchAffect(ID());
					if(me!=null)
						me.unInvoke();
				}
				ExternalPlay.resumeTicking(room,-1);
				for(int i=0;i<fixed.size();i++)
				{
					MOB mob2=(MOB)fixed.elementAt(i);
					ExternalPlay.resumeTicking(mob2,-1);
				}
				fixed=new Vector();
			}
			else
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;
				ExternalPlay.resumeTicking(mob,-1);
				if(mob.location()!=null)
				{
					mob.location().show(mob, null, Affect.MSG_OK_VISUAL, "Time starts moving again...");
					Ability me=mob.location().fetchAffect(ID());
					if(me!=null)
						me.unInvoke();
				}
			}
		}
		super.unInvoke();
	}
	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affected instanceof Room))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_ENTER:
			case Affect.TYP_LEAVE:
			case Affect.TYP_FLEE:
				if(affect.source()==invoker)
					affect.source().tell("You cannot travel beyond the time stopped area.");
				else
					affect.source().tell("Nothing just happened.  You didn't do that.");
				return false;
			default:
				if((affect.source()!=invoker)
				   &&(!Util.bset(affect.sourceCode(),Affect.ACT_GENERAL))
				   &&(!Util.bset(affect.targetCode(),Affect.ACT_GENERAL)))
				{
					affect.source().tell("Time is stopped. Nothing just happened.  You didn't do that.");
					return false;
				}
			}
		}
		return super.okAffect(affect);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Environmental target = mob.location();

		if(target.fetchAffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"Time has already been stopped here!");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
			return false;
		}


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType, (auto?"T":"<S-NAME> chant(s) and gesture(s) and t")+"ime suddenly STOPS!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Room room=mob.location();
				fixed=new Vector();
				ExternalPlay.suspendTicking(room,-1);
				for(int m=0;m<mob.location().numInhabitants();m++)
				{
					MOB mob2=mob.location().fetchInhabitant(m);
					if(mob2!=mob)
					{
						fixed.addElement(mob2);
						ExternalPlay.suspendTicking(mob2,-1);
					}
				}
				beneficialAffect(mob,room,3);
				//beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for awhile, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
