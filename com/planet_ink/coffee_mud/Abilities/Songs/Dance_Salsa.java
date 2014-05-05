package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Dance_Salsa extends Dance
{
	@Override public String ID() { return "Dance_Salsa"; }
	public final static String localizedName = CMLib.lang()._("Salsa");
	@Override public String name() { return localizedName; }
	@Override public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		final MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		final Vector choices=new Vector();
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			final MOB M=mob.location().fetchInhabitant(i);
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
			final MOB M=(MOB)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
			if(CMLib.dice().rollPercentage()==1)
			{
				Item I=mob.fetchFirstWornItem(Wearable.WORN_WAIST);
				if(I!=null)	CMLib.commands().postRemove(mob,I,false);
				I=mob.fetchFirstWornItem(Wearable.WORN_LEGS);
				if(I!=null)	CMLib.commands().postRemove(mob,I,false);
				mob.doCommand(CMParms.parse("MATE "+M.Name()),Command.METAFLAG_FORCED);
			}
			else
			if(CMLib.dice().rollPercentage()>10)
				switch(CMLib.dice().roll(1,5,0))
				{
				case 1:
					mob.tell(_("You feel strange urgings towards @x1.",M.name(mob)));
					break;
				case 2:
					mob.tell(_("You have strong happy feelings towards @x1.",M.name(mob)));
					break;
				case 3:
					mob.tell(_("You feel very appreciative of @x1.",M.name(mob)));
					break;
				case 4:
					mob.tell(_("You feel very close to @x1.",M.name(mob)));
					break;
				case 5:
					mob.tell(_("You feel lovingly towards @x1.",M.name(mob)));
					break;
				}
		}

		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+4+getXLEVELLevel(invoker()));
	}

}
