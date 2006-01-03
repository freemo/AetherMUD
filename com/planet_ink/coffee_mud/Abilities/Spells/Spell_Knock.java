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
public class Spell_Knock extends Spell
{
	public String ID() { return "Spell_Knock"; }
	public String name(){return "Knock";}
	public String displayText(){return "(Knock Spell)";}
	protected int canTargetCode(){return CAN_ITEMS|CAN_EXITS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			int theDir=-1;
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Exit E=mob.location().getExitInDir(d);
				if((E!=null)
				&&(!E.isOpen()))
				{
					theDir=d;
					break;
				}
			}
			if(theDir>=0)
				commands.addElement(Directions.getDirectionName(theDir));
		}

		String whatToOpen=CMParms.combine(commands,0);
		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().getExitInDir(dirCode);
		if(openThis==null)
			openThis=getTarget(mob,mob.location(),givenTarget,commands,Item.WORNREQ_ANY);
		if(openThis==null) return false;

		if(openThis instanceof Exit)
		{
			if(((Exit)openThis).isOpen())
			{
				mob.tell("That's already open!");
				return false;
			}
		}
		else
		if(openThis instanceof Container)
		{
			if(((Container)openThis).isOpen())
			{
				mob.tell("That's already open!");
				return false;
			}
		}
		else
		{
			mob.tell("You can't cast knock on "+openThis.name()+"!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		int levelDiff=openThis.envStats().level()-adjustedLevel(mob,asLevel);
		if(levelDiff<0) levelDiff=0;
		boolean success=profficiencyCheck(mob,-(levelDiff*25),auto);

		if(!success)
			beneficialWordsFizzle(mob,openThis,"<S-NAME> point(s) at "+openThis.name()+" and shouts incoherantly, but nothing happens.");
		else
		{

			CMMsg msg=CMClass.getMsg(mob,openThis,null,verbalCastCode(mob,openThis,auto),(auto?openThis.name()+" begin(s) to glow!":"^S<S-NAME> point(s) at <T-NAMESELF>.^?")+CMProps.msp("knock.wav",10));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int a=0;a<openThis.numEffects();a++)
				{
					Ability A=openThis.fetchEffect(a);
					if((A!=null)&&(A.ID().equalsIgnoreCase("Spell_WizardLock"))&&(A.invoker()!=null)&&(A.invoker().envStats().level()<mob.envStats().level()+3))
					{
						A.unInvoke();
						mob.location().show(mob,null,openThis,CMMsg.MSG_OK_VISUAL,"A spell around <O-NAME> seems to fade.");
						break;
					}
				}
				msg=CMClass.getMsg(mob,openThis,null,CMMsg.MSG_UNLOCK,null);
				CMLib.utensils().roomAffectFully(msg,mob.location(),dirCode);
				msg=CMClass.getMsg(mob,openThis,null,CMMsg.MSG_OPEN,"<T-NAME> opens.");
				CMLib.utensils().roomAffectFully(msg,mob.location(),dirCode);
			}
		}

		return success;
	}
}
