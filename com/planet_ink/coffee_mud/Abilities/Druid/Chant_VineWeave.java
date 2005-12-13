package com.planet_ink.coffee_mud.Abilities.Druid;
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

public class Chant_VineWeave extends Chant
{
	public String ID() { return "Chant_VineWeave"; }
	public String name(){ return "Vine Weave";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 50;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.location().resourceChoices()==null)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		if(((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		&&((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION)
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_COTTON)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SILK)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_HEMP)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_VINE)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_WHEAT)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SEAWEED))))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int material=EnvResource.RESOURCE_VINE;
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_VINE)))
			material=EnvResource.RESOURCE_VINE;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SILK)))
			material=EnvResource.RESOURCE_SILK;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_HEMP)))
			material=EnvResource.RESOURCE_HEMP;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_WHEAT)))
			material=EnvResource.RESOURCE_WHEAT;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SEAWEED)))
			material=EnvResource.RESOURCE_SEAWEED;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the plants.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);

				Item building=null;
				Ability A=CMClass.getAbility("Weaving");
				if(A!=null)
				{
					while((building==null)||(building.name().endsWith(" bundle")))
					{
						Vector V=new Vector();
						V.addElement(new Integer(material));
						A.invoke(mob,V,A,true,asLevel);
						if((V.size()>0)&&(V.lastElement() instanceof Item))
							building=(Item)V.lastElement();
						else
							break;
					}
				}
				if(building==null)
				{
					mob.tell("The chant failed for some reason...");
					return false;
				}

				building.recoverEnvStats();
				building.text();
				building.recoverEnvStats();

				mob.location().addItemRefuse(building,Item.REFUSE_RESOURCE);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,building.name()+" twists out of some vines and grows still.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the plants, but nothing happens.");

		// return whether it worked
		return success;
	}
}
