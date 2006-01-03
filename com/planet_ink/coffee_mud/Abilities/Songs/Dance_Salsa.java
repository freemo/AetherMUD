package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Dance_Salsa extends Dance
{
	public String ID() { return "Dance_Salsa"; }
	public String name(){ return "Salsa";}
	public int abstractQuality(){ return  Ability.QUALITY_BENEFICIAL_OTHERS;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		Vector choices=new Vector();
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)
			&&(M!=mob)
			&&(CMLib.flags().canBeSeenBy(M,mob))
			&&(M.charStats().getStat(CharStats.STAT_GENDER)!=mob.charStats().getStat(CharStats.STAT_GENDER))
			&&(M.charStats().getStat(CharStats.STAT_GENDER)!='N')
			&&(M.charStats().getSave(CharStats.STAT_CHARISMA)>14))
				choices.addElement(M);
		}
		if(choices.size()>0)
		{
			MOB M=(MOB)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
			if(CMLib.dice().rollPercentage()==1)
			{
				Item I=mob.fetchFirstWornItem(Item.WORN_WAIST);
				if(I!=null)	CMLib.commands().postRemove(mob,I,false);
				I=mob.fetchFirstWornItem(Item.WORN_LEGS);
				if(I!=null)	CMLib.commands().postRemove(mob,I,false);
				mob.doCommand(CMParms.parse("MATE "+M.name()));
			}
			else
			if(CMLib.dice().rollPercentage()>10)
				switch(CMLib.dice().roll(1,5,0))
				{
				case 1:
					mob.tell("You feel strange urgings towards "+M.name()+".");
					break;
				case 2:
					mob.tell("You have strong happy feelings towards "+M.name()+".");
					break;
				case 3:
					mob.tell("You feel very appreciative of "+M.name()+".");
					break;
				case 4:
					mob.tell("You feel very close to "+M.name()+".");
					break;
				case 5:
					mob.tell("You feel lovingly towards "+M.name()+".");
					break;
				}
		}

		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+6);
	}

}
