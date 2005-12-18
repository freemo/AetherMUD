package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_DeathTrap extends ThiefSkill implements Trap
{
	public String ID() { return "Thief_DeathTrap"; }
	public String name(){ return "Death Trap";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public int quality(){ return MALICIOUS;}
	private static final String[] triggerStrings = {"DEATHTRAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	protected boolean sprung=false;

	public boolean disabled(){return false;}
	public void disable(){ unInvoke();}
	public boolean sprung(){return false;}

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Trap T=(Trap)copyOf();
		T.setInvoker(mob);
		E.addEffect(T);
		CMLib.threads().startTickDown(T,MudHost.TICK_TRAP_DESTRUCTION,new Long(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)).intValue());
		return T;
	}

	public void spring(MOB M)
	{
		if((!sprung)&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.SAVE_TRAPS)))
			CMLib.combat().postDeath(invoker(),M,null);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()==affected)
		&&(msg.source()!=invoker())
		&&(!sprung)
		&&(invoker()!=null)
		&&(invoker().mayIFight(msg.source()))
		&&(CMLib.dice().rollPercentage()>msg.source().charStats().getSave(CharStats.SAVE_TRAPS)))
			CMLib.combat().postDeath(invoker(),msg.source(),msg);
		super.executeMsg(myHost,msg);
	}

	protected Item findMostOfMaterial(Room room, int material)
	{
		int most=0;
		int mostMaterial=-1;
		Item mostItem=null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&((I.material()&EnvResource.MATERIAL_MASK)==material)
			&&(I.material()!=mostMaterial)
			&&(!CMLib.flags().isOnFire(I))
			&&(I.container()==null))
			{
				int num=findNumberOfResource(room,I.material());
				if(num>most)
				{
					mostItem=I;
					most=num;
					mostMaterial=I.material();
				}
			}
		}
		return mostItem;
	}

	protected int findNumberOfResource(Room room, int resource)
	{
		int foundWood=0;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.material()==resource)
			&&(!CMLib.flags().isOnFire(I))
			&&(I.container()==null))
				foundWood++;
		}
		return foundWood;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==MudHost.TICK_TRAP_RESET)
		{
			sprung=false;
			return false;
		}
		else
		if(tickID==MudHost.TICK_TRAP_DESTRUCTION)
		{
			unInvoke();
			return false;
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room trapThis=mob.location();

		Item resource=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
		int amount=0;
		if(resource!=null) amount=findNumberOfResource(mob.location(),resource.material());
		if(amount<100)
		{
			mob.tell("You need 100 pounds of raw metal to build this trap.");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int woodDestroyed=100;
		for(int i=mob.location().numItems()-1;i>=0;i--)
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.container()==null)
			&&(I.material()==resource.material())
			&&((--woodDestroyed)>=0))
				I.destroy();
		}

		boolean success=profficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,trapThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_GENERAL|CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_ACTION,(auto?trapThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap here."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				mob.tell("You have set the trap.");
				setTrap(mob,trapThis,mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),(CMLib.ableMapper().qualifyingClassLevel(mob,this)-CMLib.ableMapper().lowestQualifyingLevel(ID()))+1);
			}
			else
			{
				if(CMLib.dice().rollPercentage()>50)
				{
					Trap T=setTrap(mob,trapThis,mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),(CMLib.ableMapper().qualifyingClassLevel(mob,this)-CMLib.ableMapper().lowestQualifyingLevel(ID()))+1);
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) the trap on accident!");
					T.spring(mob);
				}
				else
				{
					mob.tell("You fail in your attempt to set the death trap.");
				}
			}
		}
		return success;
	}
}
