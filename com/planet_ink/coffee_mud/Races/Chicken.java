package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

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
public class Chicken extends StdRace
{
	public String ID(){	return "Chicken"; }
	public String name(){ return "Chicken"; }
	public int shortestMale(){return 13;}
	public int shortestFemale(){return 13;}
	public int heightVariance(){return 6;}
	public int lightestWeight(){return 20;}
	public int weightVariance(){return 5;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_EYES;}
	public String racialCategory(){return "Avian";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,1 ,0 ,0 ,1 ,2 ,2 ,0 ,0 ,1 ,1 ,0 ,2 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,1,2,4,7,15,20,21,22};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)

	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setPermaStat(CharStats.STRENGTH,3);
		affectableStats.setPermaStat(CharStats.DEXTERITY,4);
		affectableStats.setPermaStat(CharStats.INTELLIGENCE,1);
	}
	public String arriveStr()
	{
		return "walks in";
	}
	public String leaveStr()
	{
		return "walks";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a menacing beak");
			naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
		}
		return naturalWeapon;
	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is hovering on deaths door!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood and matted feathers.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody matted feathers.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody matted feathers.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a lot of missing feathers.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p has a few missing feathers.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has a missing feather.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few feathers out of place.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a some ruffled features.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health.^N";
	}
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        if((tickID==MudHost.TICK_MOB)&&(ticking instanceof MOB))
        {
            if((Dice.rollPercentage()>99)&&(((MOB)ticking).inventorySize()<9))
            {
                Item I=CMClass.getItem("GenFoodResource");
                I.setName("an egg");
                I.setDisplayText("an egg has been left here.");
                I.setMaterial(EnvResource.RESOURCE_EGGS);
                I.setDescription("It looks like a chicken egg!");
                I.baseEnvStats().setWeight(1);
                ((MOB)ticking).addInventory((Item)I.copyOf());
            }
            if((((MOB)ticking).inventorySize()>5)
            &&(((MOB)ticking).location()!=null)
            &&(((MOB)ticking).location().fetchItem(null,"an egg")==null))
            {
                Item I=((MOB)ticking).fetchInventory("an egg");
                if(I!=null)
                {
                    ((MOB)ticking).location().show(((MOB)ticking),null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> lay(s) an egg.");
                    I.removeFromOwnerContainer();
                    I.executeMsg((MOB)ticking,new FullMsg((MOB)ticking,I,null,CMMsg.TYP_ROOMRESET,null));
                    ((MOB)ticking).location().addItemRefuse(I,Item.REFUSE_RESOURCE);
                    ((MOB)ticking).location().recoverRoomStats();
                }
            }
        }
        return true;
    }
    
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" lips",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" feathers",EnvResource.RESOURCE_FEATHERS));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" meat",EnvResource.RESOURCE_POULTRY));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
