package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
public class FightFlee extends ActiveTicker
{
	public String ID(){return "FightFlee";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public FightFlee()
	{
        super();
		minTicks=1;maxTicks=1;chance=33;
		tickReset();
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			if(mob.isInCombat()
			   &&(mob.getVictim()!=null)
			   &&(mob.getVictim().getVictim()==mob))
				CommonMsgs.flee(mob,"");
		}
		return true;
	}
}
