package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonFood extends Chant
{
	public String ID() { return "Chant_SummonFood"; }
	public String name(){ return "Summon Food";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonFood();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors to try this.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<((adjustedLevel(mob)/4)+1);i++)
				{
					Food newItem=(Food)CMClass.getStdItem("GenFoodResource");
					newItem.setName("some berries");
					newItem.setDisplayText("Some berries are growing here.");
					newItem.setDescription("Small and round, these little red berries look juicy and good.");
					newItem.setMaterial(EnvResource.RESOURCE_BERRIES);
					newItem.setNourishment(150);
					newItem.setBaseValue(1);
					newItem.setMiscText(newItem.text());
					mob.location().addItemRefuse(newItem,Item.REFUSE_RESOURCE);
				}
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Some berries quickly begin to grow here.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}
