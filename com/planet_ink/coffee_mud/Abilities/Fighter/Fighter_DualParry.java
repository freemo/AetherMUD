package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Fighter_DualParry extends StdAbility
{
	public String ID() { return "Fighter_DualParry"; }
	public String name(){ return "Dual Parry";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	boolean lastTime=false;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(mob.rangeToTarget()==0))
		{
			if((msg.tool()!=null)&&(msg.tool() instanceof Item))
			{
				Item attackerWeapon=(Item)msg.tool();
				Item myOtherWeapon=mob.fetchFirstWornItem(Item.HELD);
				if((myOtherWeapon!=null)
				&&(attackerWeapon!=null)
				&&(myOtherWeapon instanceof Weapon)
				&&(attackerWeapon instanceof Weapon)
				&&(!myOtherWeapon.rawLogicalAnd())
				&&(Sense.canBeSeenBy(msg.source(),mob))
				&&(((Weapon)myOtherWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)myOtherWeapon).weaponClassification()!=Weapon.CLASS_RANGED)
				&&(((Weapon)myOtherWeapon).weaponClassification()!=Weapon.CLASS_THROWN)
				&&(((Weapon)myOtherWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_RANGED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_THROWN))
				{
					FullMsg msg2=new FullMsg(mob,msg.source(),this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> parr(ys) "+attackerWeapon.name()+" attack with "+myOtherWeapon.name()+"!");
					if((profficiencyCheck(null,mob.charStats().getStat(CharStats.DEXTERITY)-90,false))
					&&(!lastTime)
					&&(mob.location().okMessage(mob,msg2)))
					{
						lastTime=true;
						mob.location().send(mob,msg2);
						helpProfficiency(mob);
						return false;
					}
					else
						lastTime=false;
				}
			}
		}
		return true;
	}
}