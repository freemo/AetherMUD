package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class Centaur extends StdMOB
{
	public String ID(){return "Centaur";}
	public Centaur()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a centaur";
		setDescription("A creature whose upper body is that of a man, and lower body that of a horse.");
		setDisplayText("A centaur gallops around...");
		Factions.setAlignment(this,Faction.ALIGN_GOOD);
		setMoney(200);
		baseEnvStats.setWeight(600 + Math.abs(randomizer.nextInt() % 101));


		baseCharStats().setStat(CharStats.INTELLIGENCE,5 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.STRENGTH,12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.DEXTERITY,9 + Math.abs(randomizer.nextInt() % 6));

		baseEnvStats().setDamage(7);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(4);
		baseEnvStats().setArmor(0);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		addBehavior(CMClass.getBehavior("Mobile"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
