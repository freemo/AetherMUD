package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Bard extends StdCharClass
{
	private static boolean abilitiesLoaded=false;
	
	public Bard()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=18;
		maxStat[CharStats.CHARISMA]=25;
		bonusPracLevel=1;
		manaMultiplier=8;
		attackAttribute=CharStats.DEXTERITY;
		levelsPerBonusDamage=7;
		bonusAttackLevel=1;
		name=myID;
		
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Detection",true);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Nothing",true);
			CMAble.addCharAbilityMapping(ID(),2,"Song_Seeing",true);
			CMAble.addCharAbilityMapping(ID(),2,"Thief_Lore",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),3,"Song_Valor",true);
			CMAble.addCharAbilityMapping(ID(),4,"Song_Charm",true);
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Appraise",false);
			CMAble.addCharAbilityMapping(ID(),5,"Song_Armor",true);
			CMAble.addCharAbilityMapping(ID(),5,"Song_Babble",true);
			CMAble.addCharAbilityMapping(ID(),6,"Song_Clumsiness",true);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Song_Rage",true);
			CMAble.addCharAbilityMapping(ID(),8,"Song_Mute",true);
			CMAble.addCharAbilityMapping(ID(),8,"Thief_Distract",false);
			CMAble.addCharAbilityMapping(ID(),9,"Thief_Peek",false);
			CMAble.addCharAbilityMapping(ID(),9,"Song_Serenity",true);
			CMAble.addCharAbilityMapping(ID(),10,"Song_Revelation",true);
			CMAble.addCharAbilityMapping(ID(),10,"Song_Friendship",true);
			CMAble.addCharAbilityMapping(ID(),11,"Song_Inebriation",true);
			CMAble.addCharAbilityMapping(ID(),11,"Song_Comprehension",true);
			CMAble.addCharAbilityMapping(ID(),12,"Song_Health",true);
			CMAble.addCharAbilityMapping(ID(),12,"Song_Mercy",true);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_Trip",false);
			CMAble.addCharAbilityMapping(ID(),13,"Song_Silence",true);
			CMAble.addCharAbilityMapping(ID(),14,"Song_Dexterity",true);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),15,"Thief_DetectTraps",false);
			CMAble.addCharAbilityMapping(ID(),15,"Song_Protection",true);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_ReadMagic",false);
			CMAble.addCharAbilityMapping(ID(),16,"Song_Mana",true);
			CMAble.addCharAbilityMapping(ID(),17,"Song_Quickness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),18,"Song_Lethargy",true);
			CMAble.addCharAbilityMapping(ID(),18,"Song_Flight",true);
			CMAble.addCharAbilityMapping(ID(),19,"Song_Knowledge",true);
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Swipe",false);
			CMAble.addCharAbilityMapping(ID(),20,"Song_Blasting",true);
			CMAble.addCharAbilityMapping(ID(),21,"Song_Strength",true);
			CMAble.addCharAbilityMapping(ID(),21,"Song_Thanks",true);
			CMAble.addCharAbilityMapping(ID(),22,"Song_Lullibye",true);
			CMAble.addCharAbilityMapping(ID(),22,"Song_Distraction",true);
			CMAble.addCharAbilityMapping(ID(),23,"Song_Flying",true);
			CMAble.addCharAbilityMapping(ID(),23,"Thief_Steal",false);
			CMAble.addCharAbilityMapping(ID(),24,"Song_Death",true);
			CMAble.addCharAbilityMapping(ID(),24,"Song_Disgust",true);
			CMAble.addCharAbilityMapping(ID(),25,"Song_Rebirth",true);

		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Charisma 9+, Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStat(CharStats.CHARISMA) <= 8)
			return false;
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY) <= 8)
			return false;
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&&(!(mob.charStats().getMyRace().ID().equals("HalfElf"))))
			return(false);

		return true;
	}
	public String weaponLimitations(){return new Thief().weaponLimitations();}
	public String armorLimitations(){return new Thief().armorLimitations();}
	public String otherLimitations(){return new Thief().otherLimitations();}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(!Thief.thiefOk(myChar,affect))
			return false;
		return super.okAffect(myChar, affect);
	}

}
