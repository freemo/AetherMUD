package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Templar extends Cleric
{
	public String ID(){return "Templar";}
	public String name(){return "Templar";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	protected boolean disableAlignedWeapons(){return true;}
	protected boolean disableClericSpellGrant(){return true;}
	protected boolean disableAlignedSpells(){return true;}

	private int tickDown=0;

	public Templar()
	{
		maxStatAdj[CharStats.STRENGTH]=4;
		maxStatAdj[CharStats.WISDOM]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_InfuseUnholiness",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Annul",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
			CMAble.addCharAbilityMapping(ID(),3,"Specialization_EdgedWeapon",false);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_UnholyArmament",false);

			CMAble.addCharAbilityMapping(ID(),5,"Specialization_FlailedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",true);

			CMAble.addCharAbilityMapping(ID(),6,"Skill_Parry",false);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_Heresy",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_HuntGood",false);

			CMAble.addCharAbilityMapping(ID(),8,"Specialization_Polearm",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);

			CMAble.addCharAbilityMapping(ID(),9,"Skill_AttackHalf",false);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_Behemoth",false);

			CMAble.addCharAbilityMapping(ID(),10,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_DispelGood",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Specialization_Hammer",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_DesecrateLand",false);

			CMAble.addCharAbilityMapping(ID(),14,"Specialization_Axe",false);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_Bash",false);
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Hide",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			CMAble.addCharAbilityMapping(ID(),15,"Specialization_Natural",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",false);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_BloodHearth",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_BoneMoon",false);

			CMAble.addCharAbilityMapping(ID(),18,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_Tithe",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",false);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Maladiction",false);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",true);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_Absorption",false);

			CMAble.addCharAbilityMapping(ID(),21,"Thief_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Corruption",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Haunted",false);

			CMAble.addCharAbilityMapping(ID(),23,"Thief_BackStab",false);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_CreateIdol",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_SunCurse",0,"",false,false);

			CMAble.addCharAbilityMapping(ID(),25,"Skill_Attack3",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",false);

			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Avatar",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public void tick(MOB myChar, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)&&((--tickDown)<=0))
		{
			tickDown=5;
			if(myChar.fetchEffect("Prayer_AuraStrife")==null)
			{
				Ability A=CMClass.getAbility("Prayer_AuraStrife");
				if(A!=null) A.invoke(myChar,myChar,true);
			}
		}
		return;
	}

	public String statQualifications(){return "Wisdom 9+ Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Templar.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Templar.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Receives Aura of Strife which increases in power.";}
	public String otherLimitations(){return "Always fumbles good prayers.  Using non-evil prayers introduces failure chance.";}
	public String weaponLimitations(){return "";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
			&&(msg.tool()!=null)
			&&(CMAble.getQualifyingLevel(ID(),true,msg.tool().ID())>0)
			&&(myChar.isMine(msg.tool()))
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.PRAYER))
			{
				int align=myChar.getAlignment();
				Ability A=(Ability)msg.tool();

				if(A.appropriateToMyAlignment(align))
					return true;
				int hq=holyQuality(A);

				int basis=0;
				if(hq==1000)
				{
					myChar.tell("The good nature of "+A.name()+" disrupts your prayer.");
					return false;
				}
				else
				if(hq==0)
					basis=align/10;
				else
				{
					basis=(500-align)/10;
					if(basis<0) basis=basis*-1;
					basis-=10;
				}

				if(Dice.rollPercentage()>basis)
					return true;

				if(hq==0)
					myChar.tell("The evil nature of "+A.name()+" disrupts your prayer.");
				else
				if(hq==1000)
					myChar.tell("The goodness of "+A.name()+" disrupts your prayer.");
				else
				if(align>650)
					myChar.tell("The anti-good nature of "+A.name()+" disrupts your thought.");
				else
				if(align<350)
					myChar.tell("The anti-evil nature of "+A.name()+" disrupts your thought.");
				return false;
			}
		}
		return true;
	}

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
	
}
