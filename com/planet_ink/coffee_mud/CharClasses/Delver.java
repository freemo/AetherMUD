package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Delver extends StdCharClass
{
	public String ID(){return "Delver";}
	public String name(){return "Delver";}
	public String baseClass(){return "Druid";}
	public int getMinHitPointsLevel(){return 5;}
	public int getMaxHitPointsLevel(){return 22;}
	public int getBonusPracLevel(){return 2;}
	public int getBonusManaLevel(){return 13;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.CONSTITUTION;}
	public int getLevelsPerBonusDamage(){ return 6;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected String armorFailMessage(){return "<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_OREONLY;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_ROCKY;}
	private HashSet requiredWeaponMaterials=buildRequiredWeaponMaterials();
	protected HashSet requiredWeaponMaterials(){return requiredWeaponMaterials;}
	public int requiredArmorSourceMinor(){return CMMsg.TYP_CAST_SPELL;}

	public Delver()
	{
		super();
		maxStatAdj[CharStats.CONSTITUTION]=4;
		maxStatAdj[CharStats.STRENGTH]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",50,false);
			CMAble.addCharAbilityMapping(ID(),1,"Fishing",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonFungus",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonPool",50,true);

			CMAble.addCharAbilityMapping(ID(),2,"Chant_Tether",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_SummonWater",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Chant_CaveFishing",false);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_Darkvision",false);

			CMAble.addCharAbilityMapping(ID(),4,"Chant_Boulderbash",false);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_SenseMetal",false);

			CMAble.addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_DeepDarkness",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Chant_Mold",false);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_MagneticField",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Chant_EndureRust",false);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_Brittle",false);

			CMAble.addCharAbilityMapping(ID(),8,"Chant_FodderSignal",false);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_Den",false);

			CMAble.addCharAbilityMapping(ID(),9,"Chant_Rockfeet",false);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_Earthpocket",false);

			CMAble.addCharAbilityMapping(ID(),10,"Chant_CrystalGrowth",false);
			CMAble.addCharAbilityMapping(ID(),10,"Druid_GolemForm",false);

			CMAble.addCharAbilityMapping(ID(),11,"Chant_CaveIn",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_PlantPass",false);

			CMAble.addCharAbilityMapping(ID(),12,"Chant_Rockthought",false);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_SnatchLight",false);

			CMAble.addCharAbilityMapping(ID(),13,"Chant_Drifting",false);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_DistantFungalGrowth",false);

			CMAble.addCharAbilityMapping(ID(),14,"Chant_Stonewalking",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_Bury",false);

			CMAble.addCharAbilityMapping(ID(),15,"Chant_FungalBloom",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_SacredEarth",false);

			CMAble.addCharAbilityMapping(ID(),16,"Chant_BrownMold",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_SenseOres",false);

			CMAble.addCharAbilityMapping(ID(),17,"Chant_MagneticEarth",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_Earthquake",false);

			CMAble.addCharAbilityMapping(ID(),18,"Chant_Labyrinth",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_FungusFeet",false);

			
			CMAble.addCharAbilityMapping(ID(),19,"Chant_RustCurse",false);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_TremorSense",false);

			CMAble.addCharAbilityMapping(ID(),20,"Chant_StoneFriend",false);
			CMAble.addCharAbilityMapping(ID(),20,"Scrapping",false);

			CMAble.addCharAbilityMapping(ID(),21,"Chant_Worms",false);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_SenseGems",false);

			CMAble.addCharAbilityMapping(ID(),22,"Chant_Unbreakable",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_Homeopathy",false);

			CMAble.addCharAbilityMapping(ID(),23,"Chant_FindOre",false);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_MassFungalGrowth",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Chant_FindGem",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_VolcanicChasm",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Chant_SummonRockGolem",false);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_MetalMold",false);
			
			CMAble.addCharAbilityMapping(ID(),30,"Chant_ExplosiveDecompression",false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}


	protected boolean isValidBeneficiary(MOB killer,
									   MOB killed,
									   MOB mob,
									   HashSet followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!mob.charStats().getMyRace().racialCategory().endsWith("Elemental")))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}
	public String statQualifications(){return "Constitution 9+, Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Delver.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Delver.");
			return false;
		}
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Dwarf"))
		&& !(mob.charStats().getMyRace().ID().equals("Halfling"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You must be Human, Halfling, Dwarf, or Half Elf to be a Delver");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}
	public String otherBonuses(){return "";}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
	public int classDurationModifier(MOB myChar,
									 Ability skill,
									 int duration)
	{
		if(myChar==null) return duration;
		if(Util.bset(skill.flags(),Ability.FLAG_CRAFTING)
		&&(!skill.ID().equals("Sculpting"))
		&&(!skill.ID().equals("Herbalism"))
		&&(!skill.ID().equals("Masonry")))
			return duration*2;
		   
		return duration;
	}
}