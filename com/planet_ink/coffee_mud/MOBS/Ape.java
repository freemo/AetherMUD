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
public class Ape extends StdMOB
{
	public String ID(){return "Ape";}
	public Ape()
	{
		super();
		Username="an ape";
		setDescription("The ape is big, the ape is black, the ape means business..");
		setDisplayText("An ape sits here watching you.");
		CMLib.factions().setAlignment(this,Faction.ALIGN_NEUTRAL);
		setMoney(0);
		setWimpHitPoint(2);

		baseEnvStats().setDamage(1);

		baseCharStats().setMyRace(CMClass.getRace("Ape"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(6);
		baseEnvStats().setArmor(50);

		baseState.setHitPoints(CMLib.dice().roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
