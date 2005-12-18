package com.planet_ink.coffee_mud.Abilities.Paladin;
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
   Copyright 2000-2005 Bo Zimmerman

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

public class Paladin_CraftHolyAvenger extends com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill
{
	public String ID() { return "Paladin_CraftHolyAvenger"; }
	public String name(){ return "Craft Holy Avenger";}
	private static final String[] triggerStrings = {"CRAFTHOLY","CRAFTHOLYAVENGER","CRAFTAVENGER"};
	public String[] triggerStrings(){return triggerStrings;}

    protected Item building=null;
    protected Item fire=null;
    protected boolean messedUp=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(fire==null)
			||(!CMLib.flags().isOnFire(fire))
			||(!mob.location().isContent(fire))
			||(mob.isMine(fire)))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonEmote(mob,"<S-NAME> mess(es) up crafting the Holy Avenger.");
					else
						mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
				}
				building=null;
			}
		}
		super.unInvoke();
	}


	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if((student.fetchAbility("Specialization_Sword")==null))
		{
			teacher.tell(student.name()+" has not yet specialized in swords.");
			student.tell("You need to specialize in swords to learn "+name()+".");
			return false;
		}
		if(student.fetchAbility("Weaponsmithing")==null)
		{
			teacher.tell(student.name()+" has not yet learned weaponsmithing.");
			student.tell("You need to learn weaponsmithing before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		int completion=16;
		fire=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2!=null)&&(I2.container()==null)&&(CMLib.flags().isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			commonTell(mob,"A fire will need to be built first.");
			return false;
		}
		building=null;
		messedUp=false;
		int woodRequired=50;
		int[] pm={EnvResource.MATERIAL_METAL,EnvResource.MATERIAL_MITHRIL};
		int[][] data=fetchFoundResourceData(mob,
											woodRequired,"metal",pm,
											0,null,null,
											false,
											auto?EnvResource.RESOURCE_MITHRIL:0);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null,auto?1:0);
		building=CMClass.getWeapon("GenWeapon");
		completion=50-CMLib.ableMapper().qualifyingClassLevel(mob,this);
		String itemName="the Holy Avenger";
		building.setName(itemName);
		String startStr="<S-NAME> start(s) crafting "+building.name()+".";
		displayText="You are crafting "+building.name();
		verb="crafting "+building.name();
		int hardness=EnvResource.RESOURCE_DATA[data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK][3]-5;
		building.setDisplayText(itemName+" is here");
		building.setDescription(itemName+". ");
		building.baseEnvStats().setWeight(woodRequired);
		building.setBaseValue(0);
		building.setMaterial(data[0][FOUND_CODE]);
		building.baseEnvStats().setLevel(mob.envStats().level());
		building.baseEnvStats().setAbility(5);

		int highestAttack=(CMLib.ableMapper().qualifyingClassLevel(mob,this)/2);
		int highestDamage=CMLib.ableMapper().qualifyingClassLevel(mob,this);
		Weapon w=(Weapon)building;
		w.setWeaponClassification(Weapon.CLASS_SWORD);
		w.setWeaponType(Weapon.TYPE_SLASHING);
		w.setRanges(w.minRange(),1);
		building.setRawLogicalAnd(true);
		building.baseEnvStats().setAttackAdjustment(highestAttack+(hardness*5));
		building.baseEnvStats().setDamage(highestDamage+(hardness*2));
		Ability A=CMClass.getAbility("Prop_HaveZapper");
		A.setMiscText("-CLASS +Paladin -ALIGNMENT +Good");
		building.addNonUninvokableEffect(A);

		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();

		messedUp=!profficiencyCheck(mob,0,auto);
		if(completion<6) completion=6;
		CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,completion);
		}
		return true;
	}
}
