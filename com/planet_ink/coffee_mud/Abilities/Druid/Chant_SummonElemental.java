package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonElemental extends Chant
{
	public Chant_SummonElemental()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Summon Elemental";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Summon Elemental)";

		quality=Ability.BENEFICIAL_SELF;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_SummonElemental();
	}

	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected!=null)&&(affected instanceof MOB)&&(invoker!=null))
			{
				if(((((MOB)affected).amFollowing()==null)
				||(((MOB)affected).location()!=invoker.location()))
				&&(invoker.riding()!=affected))
					((MOB)affected).destroy();
			}
		}
		return super.tick(tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> chant(s) and summon(s) help from another Plain.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				target.addNonUninvokableAffect(this);
				ExternalPlay.follow(target,mob,true);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=(MOB)CMClass.getMOB("GenMOB");
		newMOB=(MOB)newMOB.newInstance();
		newMOB.baseEnvStats().setLevel(adjustedLevel(caster)/2);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Unique"));
		switch(Dice.roll(1,4,0))
		{
		case 1:
			newMOB.setName("a fire elemental");
			newMOB.setDisplayText("a fire elemental is flaming nearby.");
			newMOB.setDescription("A large beast, wreathed in flame, with sparkling eyes and a hot temper.");
			newMOB.baseEnvStats().setDisposition(newMOB.baseEnvStats().disposition()|EnvStats.IS_LIGHT);
			newMOB.addAbility(CMClass.getAbility("Firebreath"));
			break;
		case 2:
			newMOB.setName("an ice elemental");
			newMOB.setDisplayText("an ice elemental is chilling out here.");
			newMOB.setDescription("A large beast, made of ice, with crytaline eyes and a cold disposition.");
			newMOB.addAbility(CMClass.getAbility("Frostbreath"));
			break;
		case 3:
			newMOB.setName("an earth elemental");
			newMOB.setDisplayText("an earth elemental looks right at home.");
			newMOB.setDescription("A large beast, made of rock and dirt, with a hard stare.");
			newMOB.addAbility(CMClass.getAbility("Gasbreath"));
			break;
		case 4:
			newMOB.setName("an air elemental");
			newMOB.setDisplayText("an air elemental blows right by.");
			newMOB.setDescription("A large beast, made of swirling clouds and air.");
			newMOB.addAbility(CMClass.getAbility("Lighteningbreath"));
			break;
		}
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.addAbility(CMClass.getAbility("CombatAbilities"));
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location());
		newMOB.setStartRoom(null);
		return(newMOB);


	}
}
