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
public class Chant_AstralProjection extends Chant
{
	public String ID() { return "Chant_AstralProjection"; }
	public String name(){return "Astral Projection";}
	public String displayText(){return "(Astral Projection)";}
	protected int canAffectCode(){return CAN_MOBS;}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_SHAPE_SHIFTING;}
    public int abstractQuality(){return Ability.QUALITY_OK_SELF;}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((invoker!=null)&&(invoker.soulMate()==mob))
		{
			Session s=invoker.session();
			s.setMob(invoker.soulMate());
			mob.setSession(s);
			invoker.setSession(null);
			mob.tell("^HYour spirit has returned to your body...\n\r\n\r^N");
			invoker.setSoulMate(null);
			invoker.destroy();

		}
		super.unInvoke();
		if(mob!=null)
			CMLib.commands().postStand(mob,true);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return super.tick(ticking,tickID);

		if((tickID==Tickable.TICKID_MOB)
		&&(tickDown!=Integer.MAX_VALUE)
		&&(canBeUninvoked())
		&&(tickDown==1))
			CMLib.combat().postDeath(null,(MOB)affected,null);
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			unInvoke();
		return super.okMessage(myHost,msg);
	}

	public void peaceAt(MOB mob)
	{
		Room room=mob.location();
		if(room==null) return;
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB inhab=room.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.getVictim()==mob))
				inhab.setVictim(null);
		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SMELL);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_TASTE);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.soulMate()!=null)
		{
			Ability AS=target.soulMate().fetchEffect(ID());
			if(AS!=null)
			{
				AS.unInvoke();
				return false;
			}
		}
		if(CMLib.flags().isGolem(target)
		&&((target.envStats().height()<=0)||(target.envStats().weight()<=0)))
		{
			mob.tell("You are already as astral spirit.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) softly.^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			target.makePeace();
			peaceAt(target);
			MOB spirit=(MOB)target.copyOf();
			for(int a=0;a<spirit.numEffects();a++)
			{
				Ability A=spirit.fetchEffect(a);
				if(A.canBeUninvoked()) spirit.delEffect(A);
			}
			while(spirit.inventorySize()>0)
			{
				Item I=spirit.fetchInventory(0);
				if(I!=null) I.destroy();
			}
			CMLib.beanCounter().clearZeroMoney(spirit,null);
			mob.location().show(target,null,CMMsg.MSG_OK_ACTION,"^Z<S-NAME> go(es) limp!^.^?\n\r");
			beneficialAffect(spirit,target,asLevel,0);
			Ability A=CMClass.getAbility("Prop_AstralSpirit");
			spirit.addNonUninvokableEffect(A);
			Session s=target.session();
			s.setMob(spirit);
			spirit.setSession(s);
			spirit.setSoulMate(target);
			target.setSession(null);
			spirit.recoverCharStats();
			spirit.recoverEnvStats();
			spirit.recoverMaxState();
			mob.location().recoverRoomStats();
		}

		return success;
	}
}
