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
public class Trap_Infected extends StdTrap
{
	public String ID() { return "Trap_Infected"; }
	public String name(){ return "infected";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 13;}
	public String requiresToSet(){return "some diseased food";}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE))
				offenders.addElement(A);
		}
		return offenders;
	}

	protected Item getPoison(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)
			&&(I instanceof Food))
			{
				Vector V=returnOffensiveAffects(I);
				if(V.size()>0)
					return I;
			}
		}
		return null;
	}
    public Vector getTrapComponents() {
        Vector V=new Vector();
        Item I=CMLib.materials().makeItemResource(RawMaterial.RESOURCE_MEAT);
        Ability A=CMClass.getAbility(text());
        if(A==null) A=CMClass.getAbility("Disease_Cold");
        I.addNonUninvokableEffect(A);
        V.addElement(I);
        return V;
    }

	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		Item I=getPoison(mob);
		if(I!=null){
			Vector V=returnOffensiveAffects(I);
			if(V.size()>0)
				setMiscText(((Ability)V.firstElement()).ID());
			I.destroy();
		}
		return super.setTrap(mob,E,trapBonus,qualifyingClassLevel,perm);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=getPoison(mob);
		if(I==null)
		{
			if(mob!=null)
				mob.tell("You'll need to set down some diseased food first.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet()).contains(target))
			||(target==invoker())
			||(CMLib.dice().rollPercentage()<=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off an infectous trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> notice(s) that "+affected.name()+" is infected!"))
			{
				super.spring(target);
				Ability A=CMClass.getAbility(text());
				if(A==null) A=CMClass.getAbility("Disease_Cold");
				if(A!=null) A.invoke(invoker(),target,true,0);
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
			}
		}
	}
}
