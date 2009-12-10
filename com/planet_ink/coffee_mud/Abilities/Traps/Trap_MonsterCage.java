package com.planet_ink.coffee_mud.Abilities.Traps;
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
public class Trap_MonsterCage extends StdTrap
{
	public String ID() { return "Trap_MonsterCage"; }
	public String name(){ return "monster cage";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 10;}
	public String requiresToSet(){return "a caged monster";}

	protected MOB monster=null;

	protected Item getCagedAnimal(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if(I instanceof CagedAnimal)
			{
				MOB M=((CagedAnimal)I).unCageMe();
				if(M!=null) return I;
			}
		}
		return null;
	}

	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		Item I=getCagedAnimal(mob);
		if(I!=null)
		{
			setMiscText(((CagedAnimal)I).cageText());
			I.destroy();
		}
		return super.setTrap(mob,E,trapBonus,qualifyingClassLevel,perm);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
		{
			// recage the motherfather
			if((tickDown<=1)
			&&(monster!=null)
			&&(monster.amDead()||(!monster.isInCombat())))
				monster.destroy();
		}
		return super.tick(ticking,tickID);
	}


	public void unInvoke()
	{
		if((monster!=null)&&(canBeUninvoked()))
			monster.destroy();
		super.unInvoke();
	}

    public Vector getTrapComponents() {
        Vector V=new Vector();
        Item I=CMClass.getItem("GenCaged");
        ((CagedAnimal)I).setCageText(text());
        I.recoverEnvStats();
        I.text();
        V.addElement(I);
        return V;
    }
    
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(getCagedAnimal(mob)==null)
		{
			if(mob!=null)
				mob.tell("You'll need to set down a caged animal of some sort first.");
			return false;
		}
		return true;
	}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null)&&(text().length()>0))
		{
			if((CMLib.dice().rollPercentage()<=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
			||(invoker().getGroupMembers(new HashSet()).contains(target)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) opening a monster cage!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> trip(s) open a caged monster!"))
			{
				super.spring(target);
				Item I=CMClass.getItem("GenCaged");
				((CagedAnimal)I).setCageText(text());
				monster=((CagedAnimal)I).unCageMe();
				if(monster!=null)
				{
					monster.baseEnvStats().setRejuv(0);
					monster.bringToLife(target.location(),true);
					monster.setVictim(target);
					if(target.getVictim()==null)
						target.setVictim(monster);
				}
			}
		}
	}
}
