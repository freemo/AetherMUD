package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CreateIdol extends Prayer
{
	public String ID() { return "Prayer_CreateIdol"; }
	public String name(){ return "Create Idol";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_CURSE;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Prayer_CreateIdol();	}
	public boolean bubbleAffect(){return true;}

	public void affectEnvStats(Environmental aff, EnvStats affectableStats)
	{
		super.affectEnvStats(aff,affectableStats);
		if((affected instanceof Item)&&(((Item)affected).container()==null))
		{
			affectableStats.setArmor(affectableStats.armor()+20);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-10);
		}
	}

	public void affectCharStats(MOB aff, CharStats affectableStats)
	{
		super.affectCharStats(aff,affectableStats);
		if((affected instanceof Item)&&(((Item)affected).container()==null))
		{
			if(affectableStats.getStat(CharStats.STRENGTH)>3) affectableStats.setStat(CharStats.STRENGTH,3);
			if(affectableStats.getStat(CharStats.DEXTERITY)>2) affectableStats.setStat(CharStats.DEXTERITY,2);
			if(affectableStats.getStat(CharStats.CONSTITUTION)>1) affectableStats.setStat(CharStats.CONSTITUTION,1);
		}
	}

	public void affectCharState(MOB aff, CharState affectableState)
	{
		super.affectCharState(aff,affectableState);
		if((affected instanceof Item)&&(((Item)affected).container()==null))
		{
			aff.curState().setFatigue(CharState.FATIGUED_MILLIS+(long)10);
			affectableState.setMovement(20);
		}
	}
	
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.target() instanceof MOB)
		&&(affected instanceof Item)
		&&(msg.tool()==affected)
		&&(!((MOB)msg.target()).willFollowOrdersOf(msg.source())))
		{
			msg.source().tell(msg.target().name()+" won`t accept "+msg.tool().name()+".");
			return false;
		}
		return super.okMessage(host,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.getWorshipCharID().length()==0)||(CMMap.getDeity(mob.getWorshipCharID())==null))
		{
			mob.tell("You must worship a god to use this prayer.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int material=-1;
		Room R=mob.location();
		if(R!=null)
		{
			if(((R.myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK)
				||((R.myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL)
				||((R.myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL))
				material=R.myResource();
			else
			{
				Vector V=R.resourceChoices();
				if(V.size()>0)
				for(int v=0;v<V.size()*10;v++)
				{
					int rsc=((Integer)V.elementAt(Dice.roll(1,V.size(),-1))).intValue();
					if(((rsc&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK)
						||((rsc&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL)
						||((rsc&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL))
					{
						material=rsc;
						break;
					}
				}
			}
		}

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if((success)&&(material>0))
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for an idol.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item newItem=(Item)CMClass.getStdItem("GenItem");
				newItem.setBaseValue(1);
				String name=Util.startWithAorAn(EnvResource.RESOURCE_DESCS[material&EnvResource.RESOURCE_MASK].toLowerCase()+" idol of "+mob.getWorshipCharID());
				newItem.setName(name);
				newItem.setDisplayText(name+" sits here.");
				newItem.baseEnvStats().setDisposition(EnvStats.IS_EVIL);
				newItem.baseEnvStats().setWeight(10);
				newItem.setMaterial(material);
				newItem.recoverEnvStats();
				Sense.setRemovable(newItem,false);
				Sense.setDroppable(newItem,false);
				newItem.addNonUninvokableEffect((Ability)copyOf());
				mob.location().addItemRefuse(newItem,Item.REFUSE_RESOURCE);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" grows out of the ground.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for an idol, but there is no answer.");

		// return whether it worked
		return success;
	}
}
