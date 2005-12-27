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
public class Spell_Shatter extends Spell
{
	public String ID() { return "Spell_Shatter"; }
	public String name(){return "Shatter";}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB mobTarget=getTarget(mob,commands,givenTarget,true,false);
		Item target=null;
		if(mobTarget!=null)
		{
			Vector goodPossibilities=new Vector();
			Vector possibilities=new Vector();
			for(int i=0;i<mobTarget.inventorySize();i++)
			{
				Item item=mobTarget.fetchInventory(i);
				if((item!=null)
				   &&(item.subjectToWearAndTear()))
				{
					if(item.amWearingAt(Item.IN_INVENTORY))
						possibilities.addElement(item);
					else
						goodPossibilities.addElement(item);
				}
				if(goodPossibilities.size()>0)
					target=(Item)goodPossibilities.elementAt(CMLib.dice().roll(1,goodPossibilities.size(),-1));
				else
				if(possibilities.size()>0)
					target=(Item)possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1));
			}
			if(target==null)
				return maliciousFizzle(mob,mobTarget,"<S-NAME> attempt(s) a shattering spell at <T-NAMESELF>, but nothing happens.");
		}

		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);

		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,affectType(auto),auto?"<T-NAME> starts vibrating!":"^S<S-NAME> utter(s) a shattering spell, causing <T-NAMESELF> to vibrate and resonate.^?");
			CMMsg msg2=CMClass.getMsg(mob,mobTarget,this,affectType(auto),null);
			if((mob.location().okMessage(mob,msg))&&((mobTarget==null)||(mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if(msg.value()<=0)
				{
					int damage=100+mob.envStats().level()-target.envStats().level();
					if(CMLib.flags().isABonusItems(target))
						damage=(int)Math.round(CMath.div(damage,2.0));
					switch(target.material()&EnvResource.MATERIAL_MASK)
					{
					case EnvResource.MATERIAL_PAPER:
					case EnvResource.MATERIAL_CLOTH:
					case EnvResource.MATERIAL_VEGETATION:
					case EnvResource.MATERIAL_PLASTIC:
					case EnvResource.MATERIAL_LEATHER:
					case EnvResource.MATERIAL_FLESH:
						damage=(int)Math.round(CMath.div(damage,3.0));
						break;
					case EnvResource.MATERIAL_WOODEN:
						damage=(int)Math.round(CMath.div(damage,1.5));
						break;
					case EnvResource.MATERIAL_GLASS:
					case EnvResource.MATERIAL_ROCK:
						damage=(int)Math.round(CMath.mul(damage,2.0));
						break;
					case EnvResource.MATERIAL_PRECIOUS:
						break;
					case EnvResource.MATERIAL_ENERGY:
						damage=0;
						break;
					}
					target.setUsesRemaining(target.usesRemaining()-damage);
					if(target.usesRemaining()>0)
						target.recoverEnvStats();
					else
					{
						target.setUsesRemaining(100);
						if(mobTarget==null)
							mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> is destroyed!");
						else
							mob.location().show(mobTarget,target,CMMsg.MSG_OK_VISUAL,"<T-NAME>, possessed by <S-NAME>, is destroyed!");
						target.unWear();
						target.destroy();
						mob.location().recoverRoomStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) a shattering spell, but nothing happens.");


		// return whether it worked
		return success;
	}
}

