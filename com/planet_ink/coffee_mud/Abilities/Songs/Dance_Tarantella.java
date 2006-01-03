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
public class Dance_Tarantella extends Dance
{
	public String ID() { return "Dance_Tarantella"; }
	public String name(){ return "Tarantella";}
	public int abstractQuality(){ return  Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int ticks=1;
	protected String danceOf(){return name()+" Dance";}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.STAT_SAVE_POISON,affectedStats.getStat(CharStats.STAT_SAVE_POISON)+(CMLib.ableMapper().qualifyingClassLevel(invoker(),this)*2));
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if((++ticks)>=15)
		{
			Vector offenders=null;
			for(int a=0;a<mob.numEffects();a++)
			{
				Ability A=mob.fetchEffect(a);
				if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON))
				{
					if(offenders==null) offenders=new Vector();
					offenders.addElement(A);
				}
			}
			if(offenders!=null)
				for(int a=0;a<offenders.size();a++)
					((Ability)offenders.elementAt(a)).unInvoke();
		}

		return true;
	}

}
