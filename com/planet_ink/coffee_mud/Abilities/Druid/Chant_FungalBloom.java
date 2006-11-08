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
   Copyright 2000-2006 Bo Zimmerman

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

public class Chant_FungalBloom extends Chant
{
	public String ID() { return "Chant_FungalBloom"; }
	public String name(){ return "Fungal Bloom";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		||(mob.location().domainType()==Room.DOMAIN_INDOORS_AIR)
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}

		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORNREQ_UNWORNONLY);
		if(target==null) return false;
		if(target.material()!=RawMaterial.RESOURCE_MUSHROOMS)
		{
			mob.tell(target.name()+" is not a fungus!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.setDescription("It seems to be getting puffier and puffier!");
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" seems to be puffing up!");
				Ability A=CMClass.getAbility("Bomb_Poison");
				A.setMiscText("Poison_Bloodboil");
				A.setInvoker(mob);
				A.setSavable(false);
				((Trap)A).setReset(3);
				target.addEffect(A);
				A=target.fetchEffect(A.ID());
				if(A!=null)	((Trap)A).activateBomb();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to the <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
