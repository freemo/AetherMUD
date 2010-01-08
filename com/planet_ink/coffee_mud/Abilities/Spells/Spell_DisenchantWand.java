package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Spell_DisenchantWand extends Spell
{
	public String ID() { return "Spell_DisenchantWand"; }
	public String name(){return "Disenchant Wand";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;	}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if((success)
		&&(target instanceof Wand)
		&&(((Wand)target).usesRemaining()>0)
		&&(((Wand)target).getSpell()!=null))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				((Wand)target).setSpell(null);
				((Wand)target).setUsesRemaining(0);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> fades and becomes dull!");
				if((target.baseEnvStats().disposition()&EnvStats.IS_BONUS)==EnvStats.IS_BONUS)
					target.baseEnvStats().setDisposition(target.baseEnvStats().disposition()-EnvStats.IS_BONUS);
				target.recoverEnvStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> and whisper(s), but nothing happens.");


		// return whether it worked
		return success;
	}
}
