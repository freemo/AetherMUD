package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_Resistance extends StdSkill
{
	public String ID() { return "Skill_Resistance"; }
	public String name(){ return "Resistance";}
	private String displayText="";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int resistanceCode=0;

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		resistanceCode=0;
		if(newText.equalsIgnoreCase("acid"))
			resistanceCode=CharStats.SAVE_ACID;
		else
		if(newText.equalsIgnoreCase("cold"))
			resistanceCode=CharStats.SAVE_COLD;
		else
		if(newText.equalsIgnoreCase("electricity"))
			resistanceCode=CharStats.SAVE_ELECTRIC;
		else
		if(newText.equalsIgnoreCase("fire"))
			resistanceCode=CharStats.SAVE_FIRE;
		else
		if(newText.equalsIgnoreCase("gas"))
			resistanceCode=CharStats.SAVE_GAS;
		else
		if(newText.equalsIgnoreCase("mind"))
			resistanceCode=CharStats.SAVE_MIND;
		else
		if(newText.equalsIgnoreCase("paralysis"))
			resistanceCode=CharStats.SAVE_PARALYSIS;
		else
		if(newText.equalsIgnoreCase("magic"))
			resistanceCode=CharStats.SAVE_MAGIC;
		else
		if(newText.equalsIgnoreCase("traps"))
			resistanceCode=CharStats.SAVE_TRAPS;
		else
		if(newText.equalsIgnoreCase("justice"))
			resistanceCode=CharStats.SAVE_JUSTICE;
		else
		if(newText.equalsIgnoreCase("poison"))
			resistanceCode=CharStats.SAVE_POISON;
		else
		if(newText.equalsIgnoreCase("water"))
			resistanceCode=CharStats.SAVE_WATER;
		else
		if(newText.equalsIgnoreCase("undead"))
			resistanceCode=CharStats.SAVE_UNDEAD;
		else
		if(newText.equalsIgnoreCase("disease"))
			resistanceCode=CharStats.SAVE_DISEASE;
		if(resistanceCode>0)
			displayText="(Resistance to "+newText.trim().toLowerCase()+")";
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		int amount=(int)Math.round(Util.mul(Util.div(profficiency(),100.0),affected.envStats().level()));
		if(resistanceCode>0)
			affectableStats.setStat(resistanceCode,affectableStats.getStat(resistanceCode)+amount);
		else
		{
			affectableStats.setStat(CharStats.SAVE_ACID,affectableStats.getStat(CharStats.SAVE_ACID)+amount);
			affectableStats.setStat(CharStats.SAVE_COLD,affectableStats.getStat(CharStats.SAVE_COLD)+amount);
			affectableStats.setStat(CharStats.SAVE_ELECTRIC,affectableStats.getStat(CharStats.SAVE_ELECTRIC)+amount);
			affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_FIRE)+amount);
			affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+amount);
			affectableStats.setStat(CharStats.SAVE_TRAPS,affectableStats.getStat(CharStats.SAVE_TRAPS)+amount);
			affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+amount);
			affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+amount);
			affectableStats.setStat(CharStats.SAVE_MAGIC,affectableStats.getStat(CharStats.SAVE_MAGIC)+amount);
			affectableStats.setStat(CharStats.SAVE_JUSTICE,affectableStats.getStat(CharStats.SAVE_JUSTICE)+amount);
			affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+amount);
			affectableStats.setStat(CharStats.SAVE_WATER,affectableStats.getStat(CharStats.SAVE_WATER)+amount);
			affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+amount);
			affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+amount);
		}
	}
}
