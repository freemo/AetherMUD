package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_GrowFood extends Chant
{
	public String ID() { return "Chant_GrowFood"; }
	public String name(){ return "Grow Food";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_GrowFood();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors to try this.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		
		int material=-1;
		Vector choices=new Vector();
		String s=Util.combine(commands,0);
		
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
		{
			int code=EnvResource.RESOURCE_DATA[i][0];
			if(((code&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION))
			{
				choices.addElement(new Integer(code));
				if((s.length()>0)&&(CoffeeUtensils.containsString(EnvResource.RESOURCE_DESCS[i],s)))
					material=code;
			}
		}
		if((material<0)&&(s.length()>0))
		{
			mob.tell("'"+s+"' is not a recognized form of food or herbs!");
			return false;
		}
		
		if((material<0)&&(choices.size()>0))
			material=((Integer)choices.elementAt(Dice.roll(1,choices.size(),-1))).intValue();
		
		if(material<0) return false;
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Food newItem=(Food)CMClass.getStdItem("GenFoodResource");
				if(material==EnvResource.RESOURCE_HERBS)
					newItem.setNourishment(1);
				else
					newItem.setNourishment(150);
				String name=EnvResource.RESOURCE_DESCS[material&EnvResource.RESOURCE_MASK].toLowerCase();
				newItem.setMaterial(material);
				newItem.setBaseValue(EnvResource.RESOURCE_DATA[material&EnvResource.RESOURCE_MASK][1]);
				newItem.baseEnvStats().setWeight(1);
				newItem.setName("a pound of "+name);
				newItem.setDisplayText("some "+name+" sits here.");
				newItem.setDescription("");
				newItem.recoverEnvStats();
				newItem.setMiscText(newItem.text());
				mob.location().addItemRefuse(newItem,Item.REFUSE_RESOURCE);
				mob.location().showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.displayName()+" pops out of the ground.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}