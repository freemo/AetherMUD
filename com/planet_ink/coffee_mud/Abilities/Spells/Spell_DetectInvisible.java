package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_DetectInvisible extends Spell
{

	public Spell_DetectInvisible()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Detect Invisible";
		displayText="(Detect Invisible)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.OK_SELF;

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		baseEnvStats().setLevel(6);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_DetectInvisible();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_DIVINATION;
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
			mob.tell(mob,null,"Your sight becomes less keen.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INVISIBLE);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already detecting invisibility.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"<S-NAME> open(s) <S-HIS-HER> softly glowing eyes.":"^S<S-NAME> incant(s) softly, and open(s) <S-HIS-HER> glowing eyes.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) and open(s) <S-HIS-HER> eyes softly, but the spell fizzles.");

		return success;
	}
}
