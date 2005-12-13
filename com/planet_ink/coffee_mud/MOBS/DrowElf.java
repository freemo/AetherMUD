package com.planet_ink.coffee_mud.MOBS;
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
   Copyright 2000-2005 Lee H. Fox

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
public class DrowElf extends StdMOB
{
	public String ID(){return "DrowElf";}
	public static final int MALE	= 0;
	public static final int FEMALE	= 1;

	public int darkDown=4;

	public DrowElf()
	{
		super();

		Random randomizer = new Random(System.currentTimeMillis());

		baseEnvStats().setLevel(4 + Math.abs(randomizer.nextInt() % 7));

		int gender = Math.abs(randomizer.nextInt() % 2);
		String sex = null;
		if (gender == MALE)
			sex = "male";
		else
			sex = "female";

		// ===== set the basics
		Username="a Drow Elf";
		setDescription("a " + sex + " Drow Fighter");
		setDisplayText("The drow is armored in black chain mail and carrying a nice arsenal of weapons");

		baseState.setHitPoints(CMLib.dice().roll(baseEnvStats().level(),20,baseEnvStats().level()));
		setMoney((int)Math.round(Util.div((50 * baseEnvStats().level()),(randomizer.nextInt() % 10 + 1))));
		baseEnvStats.setWeight(70 + Math.abs(randomizer.nextInt() % 20));

		setWimpHitPoint(5);

		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK | EnvStats.CAN_SEE_INFRARED);

		if(gender == MALE)
			baseCharStats().setStat(CharStats.GENDER,'M');
		else
			baseCharStats().setStat(CharStats.GENDER,'F');

		baseCharStats().setStat(CharStats.STRENGTH,12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.INTELLIGENCE,14 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.WISDOM,13 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.DEXTERITY,15 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.CONSTITUTION,12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.CHARISMA,13 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setMyRace(CMClass.getRace("Elf"));
		baseCharStats().getMyRace().startRacing(this,false);


		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==MudHost.TICK_MOB))
		{
			if (isInCombat())
			{
				if((--darkDown)<=0)
				{
					darkDown=4;
					castDarkness();
				}
			}

		}
		return super.tick(ticking,tickID);
	}

	protected boolean castDarkness()
	{
		if(this.location()==null)
			return true;
		if(CMLib.flags().isInDark(this.location()))
			return true;

		Ability dark=CMClass.getAbility("Spell_Darkness");
		dark.setProfficiency(100);
		dark.setBorrowed(this,true);
		if(this.fetchAbility(dark.ID())==null)
		   this.addAbility(dark);
		else
			dark=this.fetchAbility(dark.ID());

		if(dark!=null) dark.invoke(this,null,true,0);
		return true;
	}



}
