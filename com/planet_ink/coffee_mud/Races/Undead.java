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
public class Undead extends StdRace
{
	public String ID(){	return "Undead"; }
	public String name(){ return "Undead"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Undead";}
	public boolean fertile(){return false;}
	public boolean uncharmable(){return true;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,0,0,0,0,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();

	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		super.affectCharState(affectedMOB, affectableState);
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectableState.setThirst(999999);
		affectedMOB.curState().setThirst(affectableState.getThirst());
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amITarget(myHost)
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(myHost instanceof MOB)
        &&(ID().equals("Undead")))
		    msg.source().tell(name()+" stinks of grime and decay.");
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((myHost!=null)&&(myHost instanceof MOB))
		{
			MOB mob=(MOB)myHost;
			if(msg.amITarget(mob)&&(msg.targetMinor()==CMMsg.TYP_HEALING))
			{
				int amount=msg.value();
				if((amount>0)
				&&(msg.tool()!=null)
				&&(msg.tool() instanceof Ability)
				&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HEALING|Ability.FLAG_HOLY))
				&&(!Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY)))
				{
					MUDFight.postDamage(msg.source(),mob,msg.tool(),amount,CMMsg.MASK_GENERAL|CMMsg.TYP_ACID,Weapon.TYPE_BURNING,"The healing magic from <S-NAME> <DAMAGE> <T-NAMESELF>.");
					if((mob.getVictim()==null)&&(mob!=msg.source())&&(mob.isMonster()))
						mob.setVictim(msg.source());
				}
				return false;
			}
			else
			if((msg.amITarget(mob))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY))
			&&(!Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HOLY)))
			{
				int amount=msg.value();
				if(amount>0)
				{
					MUDFight.postHealing(msg.source(),mob,msg.tool(),CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,amount,"The harming magic heals <T-NAMESELF>.");
					return false;
				}
			}
			else
			if((msg.amITarget(mob))
			&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS)
				||(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			&&((msg.targetMinor()==CMMsg.TYP_DISEASE)
				||(msg.targetMinor()==CMMsg.TYP_GAS)
				||(msg.targetMinor()==CMMsg.TYP_MIND)
				||(msg.targetMinor()==CMMsg.TYP_PARALYZE)
				||(msg.targetMinor()==CMMsg.TYP_POISON)
				||(msg.targetMinor()==CMMsg.TYP_UNDEAD)
				||(msg.sourceMinor()==CMMsg.TYP_DISEASE)
				||(msg.sourceMinor()==CMMsg.TYP_GAS)
				||(msg.sourceMinor()==CMMsg.TYP_MIND)
				||(msg.sourceMinor()==CMMsg.TYP_PARALYZE)
				||(msg.sourceMinor()==CMMsg.TYP_POISON)
				||(msg.sourceMinor()==CMMsg.TYP_UNDEAD))
			&&(!mob.amDead()))
			{
				String immunityName="certain";
				if(msg.tool()!=null)
					immunityName=msg.tool().name();
				if(mob!=msg.source())
					mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) immune to "+immunityName+" attacks from <T-NAME>.");
				else
					mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) immune to "+immunityName+".");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+100);
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+100);
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
	}
	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		DeadBody body=super.getCorpseContainer(mob,room);
		if((body!=null)&&(mob!=null))
		{
			if((mob.Name().toUpperCase().indexOf("DRACULA")>=0)
			||(mob.Name().toUpperCase().indexOf("VAMPIRE")>=0))
				body.addNonUninvokableEffect(CMClass.getAbility("Disease_Vampirism"));
			else
			if((mob.Name().toUpperCase().indexOf("GHOUL")>=0)
			||(mob.Name().toUpperCase().indexOf("GHAST")>=0))
				body.addNonUninvokableEffect(CMClass.getAbility("Disease_Cannibalism"));
	        if(ID().equals("Undead"))
	        {
	            Ability A=CMClass.getAbility("Prop_Smell");
	            body.addNonUninvokableEffect(A);
	            A.setMiscText(body.name()+" SMELLS HORRIBLE!");
	        }
		}
		return body;
	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively broken and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is very damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is somewhat damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is very weak and slightly damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has lost stability and is weak.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is unstable and slightly weak.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is unbalanced and unstable.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is in somewhat unbalanced.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition.^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}

