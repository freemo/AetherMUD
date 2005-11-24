package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Thief_Snatch extends StdAbility
{
	public String ID() { return "Thief_Snatch"; }
	public String name(){ return "Weapon Snatch";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"SNATCH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to disarm!");
			return false;
		}
		Item weapon=mob.fetchWieldedItem();
		if(weapon==null)
		{
			mob.tell("You need a weapon to disarm someone!");
			return false;
		}
		else
		if(mob.freeWearPositions(Item.HELD)>0)
		{
			mob.tell("Your other hand needs to be free to do a weapon snatch.");
			return false;
		}

		Item hisItem=mob.getVictim().fetchWieldedItem();
		if((hisItem==null)
		||(!(hisItem instanceof Weapon))
		||((((Weapon)hisItem).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			mob.tell(mob.getVictim().charStats().HeShe()+" is not wielding a weapon!");
			return false;
		}
		else
		if(hisItem.rawLogicalAnd())
		{
			mob.tell("You can't snatch a two-handed weapon!");
			return false;
		}
		Weapon hisWeapon=(Weapon)hisItem;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=mob.getVictim().envStats().level()-mob.envStats().level();
		if(levelDiff>0)
			levelDiff=levelDiff*6;
		else
			levelDiff=0;
		boolean hit=(auto)||MUDFight.rollToHit(mob,mob.getVictim());
		boolean success=profficiencyCheck(mob,-levelDiff,auto)&&(hit);
		if((success)
		   &&(hisWeapon!=null)
		   &&((hisWeapon.rawProperLocationBitmap()==Item.WIELD)
			  ||(hisWeapon.rawProperLocationBitmap()==Item.WIELD+Item.HELD)))
		{
			FullMsg msg=new FullMsg(mob.getVictim(),hisWeapon,null,CMMsg.MSG_DROP,null);
			FullMsg msg2=new FullMsg(mob,null,this,CMMsg.MSG_THIEF_ACT,null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob.getVictim(),msg);
				mob.location().send(mob,msg2);
				mob.location().show(mob,mob.getVictim(),CMMsg.MSG_OK_VISUAL,"<S-NAME> disarm(s) <T-NAMESELF>!");
				if(mob.location().isContent(hisWeapon))
				{
					CommonMsgs.get(mob,null,hisWeapon,true);
					if(mob.isMine(hisWeapon))
					{
						msg=new FullMsg(mob,hisWeapon,null,CMMsg.MSG_HOLD,"<S-NAME> snatch(es) the <T-NAME> out of mid-air!");
						if(mob.location().okMessage(mob,msg))
							mob.location().send(mob,msg);
					}
				}
			}
		}
		else
			maliciousFizzle(mob,mob.getVictim(),"<S-NAME> attempt(s) to disarm <T-NAMESELF> and fail(s)!");
		return success;
	}
}
