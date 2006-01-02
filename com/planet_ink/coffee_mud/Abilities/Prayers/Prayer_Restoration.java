package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_Restoration extends Prayer
{
	public String ID() { return "Prayer_Restoration"; }
	public String name(){ return "Restoration";}
	public int abstractQuality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALING;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
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
			CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"<T-NAME> become(s) surrounded by a bright light.":"^S<S-NAME> "+prayWord(mob)+" over <T-NAMESELF> for restorative healing.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int healing=target.maxState().getHitPoints()-target.curState().getHitPoints();
				if(healing>0)
				{
					CMLib.combat().postHealing(mob,target,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,healing,null);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> look(s) much healthier!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				Ability A=target.fetchEffect("Amputation");
				if(A!=null)
				{
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> missing parts are restored!");
					A=target.fetchAbility(A.ID());
					if(A!=null) target.delAbility(A);
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				
				A=target.fetchEffect("Fighter_AtemiStrike");
				if((A!=null)&&(A.canBeUninvoked()))
				{
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> atemi damage is healed!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				
				A=target.fetchEffect("Undead_EnergyDrain");
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> lost levels are restored!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				A=target.fetchEffect("Undead_WeakEnergyDrain");
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> lost levels are restored!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				A=target.fetchEffect("Undead_ColdTouch");
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> no longer cold and weak!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				Vector offensiveAffects=Prayer_RestoreSmell.returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can smell again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_RestoreVoice.returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can speak again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_RemovePoison.returnOffensiveAffects(target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> is cured of <S-HIS-HER> poisonous afflication!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_Freedom.returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can move again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_CureDisease.returnOffensiveAffects(target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> is cured of <S-HIS-HER> disease!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_CureBlindness.returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can see again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_CureDeafness.returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can hear again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" over <T-NAMESELF>, but "+hisHerDiety(mob)+" does not heed.");


		// return whether it worked
		return success;
	}
}
