package com.planet_ink.coffee_mud.Abilities.Traps;
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
public class Bomb_Noxious extends StdBomb
{
	public String ID() { return "Bomb_Noxious"; }
	public String name(){ return "stink bomb";}
	protected int trapLevel(){return 12;}
	public String requiresToSet(){return "an egg";}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if((!(E instanceof Item))
		||(((Item)E).material()!=EnvResource.RESOURCE_EGGS))
		{
			if(mob!=null)
				mob.tell("You an egg to make this out of.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((!invoker().mayIFight(target))||(target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) the stink bomb!");
			else
			if(target.location().show(invoker(),target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,affected.name()+" explodes stink into <T-YOUPOSS> eyes!"))
			{
				super.spring(target);
				Ability A=CMClass.getAbility("Spell_StinkingCloud");
				if(A!=null) A.invoke(target,target,true,invoker().envStats().level());
			}
		}
	}

}
