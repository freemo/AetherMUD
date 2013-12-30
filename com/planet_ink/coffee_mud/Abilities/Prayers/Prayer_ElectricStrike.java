package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Prayer_ElectricStrike extends Prayer
{
	public String ID() { return "Prayer_ElectricStrike"; }
	public String name(){ return "Electric Strike";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			Prayer_ElectricStrike newOne=(Prayer_ElectricStrike)this.copyOf();
			CMMsg msg=CMClass.getMsg(mob,target,newOne,verbalCastCode(mob,target,auto),(auto?"<T-NAME> is filled with a holy charge!":"^S<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+"!^?")+CMLib.protocol().msp("lightning.wav",40));
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_ELECTRIC|(auto?CMMsg.MASK_ALWAYS:0),null);
			final Room R=target.location();
			if((R.okMessage(mob,msg))&&((R.okMessage(mob,msg2))))
			{
				R.send(mob,msg);
				R.send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					int harming=CMLib.dice().roll(1,adjustedLevel(mob,asLevel),5);
					CMLib.combat().postDamage(mob,target,this,harming,CMMsg.MASK_ALWAYS|CMMsg.TYP_ELECTRIC,Weapon.TYPE_STRIKING,"^SThe ELECTRIC STRIKE <DAMAGES> <T-NAME>!^?");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
