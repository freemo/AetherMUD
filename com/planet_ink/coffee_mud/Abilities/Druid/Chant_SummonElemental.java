package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2006 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class Chant_SummonElemental extends Chant
{
	public String ID() { return "Chant_SummonElemental"; }
	public String name(){ return "Summon Elemental";}
	public String displayText(){return "(Summon Elemental)";}
	public int abstractQuality(){return Ability.BENEFICIAL_SELF;}
	public int enchantQuality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)&&(affected instanceof MOB)&&(invoker!=null))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(mob.location()!=invoker.location())))
				{
					mob.delEffect(this);
					if(mob.amDead()) mob.setLocation(null);
					mob.destroy();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) and summon(s) help from another Plain.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				target.addNonUninvokableEffect((Ability)this.copyOf());
				if(target.isInCombat()) target.makePeace();
				CMLib.commands().postFollow(target,mob,true);
				if(target.amFollowing()!=mob)
					mob.tell(target.name()+" seems unwilling to follow you.");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=CMClass.getMOB("GenMOB");
		newMOB.baseEnvStats().setLevel(adjustedLevel(caster,0));
		switch(CMLib.dice().roll(1,4,0))
		{
		case 1:
			newMOB.setName("a fire elemental");
			newMOB.setDisplayText("a fire elemental is flaming nearby.");
			newMOB.setDescription("A large beast, wreathed in flame, with sparkling eyes and a hot temper.");
			newMOB.baseEnvStats().setDisposition(newMOB.baseEnvStats().disposition()|EnvStats.IS_LIGHTSOURCE);
			CMLib.factions().setAlignment(newMOB,Faction.ALIGN_EVIL);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("FireElemental"));
			newMOB.addAbility(CMClass.getAbility("Firebreath"));
			break;
		case 2:
			newMOB.setName("an ice elemental");
			newMOB.setDisplayText("an ice elemental is chilling out here.");
			newMOB.setDescription("A large beast, made of ice, with crytaline eyes and a cold disposition.");
			CMLib.factions().setAlignment(newMOB,Faction.ALIGN_GOOD);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("WaterElemental"));
			newMOB.addAbility(CMClass.getAbility("Frostbreath"));
			break;
		case 3:
			newMOB.setName("an earth elemental");
			newMOB.setDisplayText("an earth elemental looks right at home.");
			newMOB.setDescription("A large beast, made of rock and dirt, with a hard stare.");
			CMLib.factions().setAlignment(newMOB,Faction.ALIGN_NEUTRAL);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("EarthElemental"));
			newMOB.addAbility(CMClass.getAbility("Gasbreath"));
			break;
		case 4:
			newMOB.setName("an air elemental");
			newMOB.setDisplayText("an air elemental blows right by.");
			newMOB.setDescription("A large beast, made of swirling clouds and air.");
			CMLib.factions().setAlignment(newMOB,Faction.ALIGN_GOOD);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("AirElemental"));
			newMOB.addAbility(CMClass.getAbility("Lighteningbreath"));
			break;
		}
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setSpeed(newMOB.baseCharStats().getCurrentClass().getLevelSpeed(newMOB));
		newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.addBehavior(CMClass.getBehavior("CombatAbilities"));
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
		newMOB.setStartRoom(null);
		newMOB.addNonUninvokableEffect(this);
		return(newMOB);
	}
}
