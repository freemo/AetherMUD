package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class LizardMan extends StdRace
{
	public String ID(){	return "LizardMan"; }
	public String name(){ return "Lizard Man"; }
	protected int shortestMale(){return 72;}
	protected int shortestFemale(){return 66;}
	protected int heightVariance(){return 3;}
	protected int lightestWeight(){return 200;}
	protected int weightVariance(){return 50;}
	protected long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Lizard Man";}
	
	protected static Vector resources=new Vector();

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.INTELLIGENCE,6);
	}
	public String arriveStr()
	{
		return "ambles in";
	}
	public String leaveStr()
	{
		return "runs";
	}
	public void startRacing(MOB mob, boolean verifyOnly)
	{
		super.startRacing(mob,verifyOnly);
		Ability A=CMClass.getAbility("Draconic");
		if(A!=null)
		{
 			mob.addAbility(A);
			A.autoInvocation(mob);
			if(mob.isMonster())
				A.invoke(mob,mob,true);
		}
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("sharp claws");
			naturalWeapon.setWeaponType(Weapon.TYPE_SLASHING);
		}
		return naturalWeapon;
	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is facing a cold death!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody wounds and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut and bruised heavily.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some minor cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few bruises and scratched scales.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a few small bruises.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" tongue",EnvResource.RESOURCE_SCALES));
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" hide",EnvResource.RESOURCE_SCALES));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
