package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

import java.util.*;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Util;
public class IndoorUnderWater extends StdRoom implements Drink
{
	public String ID(){return "IndoorUnderWater";}
	public IndoorUnderWater()
	{
		super();
		baseEnvStats.setWeight(3);
		name="the water";
		baseEnvStats().setSensesMask(baseEnvStats().sensesMask()|EnvStats.CAN_NOT_BREATHE);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_SWIMMING);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_UNDERWATER;
		domainCondition=Room.CONDITION_WET;
		baseThirst=0;
	}

	public Environmental newInstance()
	{
		return new IndoorUnderWater();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(Sense.isSleeping(this)) 
			return super.okAffect(myHost,affect);
			  
		if((affect.targetMinor()==affect.TYP_FIRE)
		||(affect.targetMinor()==affect.TYP_GAS)
		||(affect.sourceMinor()==affect.TYP_FIRE)
		||(affect.sourceMinor()==affect.TYP_GAS))
		{
			affect.source().tell("That won't work underwater.");
			return false;
		}
		else
		if(affect.amITarget(this)&&(affect.targetMinor()==Affect.TYP_DRINK))
		{
			if(liquidType()==EnvResource.RESOURCE_SALTWATER)
			{
				affect.source().tell("You don't want to be drinking saltwater.");
				return false;
			}
			return true;
		}
		return super.okAffect(myHost,affect);
	}
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if(affect.amITarget(this)&&(affect.targetMinor()==Affect.TYP_DRINK))
		{
			MOB mob=affect.source();
			boolean thirsty=mob.curState().getThirst()<=0;
			boolean full=!mob.curState().adjThirst(thirstQuenched(),mob.maxState());
			if(thirsty)
				mob.tell("You are no longer thirsty.");
			else
			if(full)
				mob.tell("You have drunk all you can.");
		}
	}
	public int thirstQuenched(){return 500;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return EnvResource.RESOURCE_FRESHWATER;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean containsDrink(){return true;}
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
