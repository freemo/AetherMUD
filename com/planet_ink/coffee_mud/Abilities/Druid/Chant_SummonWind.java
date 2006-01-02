package com.planet_ink.coffee_mud.Abilities.Druid;
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

public class Chant_SummonWind extends Chant
{
	public String ID() { return "Chant_SummonWind"; }
	public String name(){ return "Summon Wind";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_WEATHERAFFECTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int size=mob.location().getArea().numberOfProperIDedRooms();
		size=size/mob.envStats().level();
		if(size<0) size=0;
		boolean success=profficiencyCheck(mob,-size,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"^JThe sky changes color!^?":"^S<S-NAME> chant(s) into the sky for wind!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				switch(mob.location().getArea().getClimateObj().weatherType(mob.location()))
				{
				case Climate.WEATHER_BLIZZARD:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_BLIZZARD);
					break;
				case Climate.WEATHER_CLEAR:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_WINDY);
					break;
				case Climate.WEATHER_CLOUDY:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_WINDY);
					break;
				case Climate.WEATHER_DROUGHT:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DUSTSTORM);
					break;
				case Climate.WEATHER_DUSTSTORM:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DUSTSTORM);
					break;
				case Climate.WEATHER_HAIL:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_HAIL);
					break;
				case Climate.WEATHER_HEAT_WAVE:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DUSTSTORM);
					break;
				case Climate.WEATHER_RAIN:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					break;
				case Climate.WEATHER_SLEET:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_SLEET);
					break;
				case Climate.WEATHER_SNOW:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_BLIZZARD);
					break;
				case Climate.WEATHER_THUNDERSTORM:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					break;
				case Climate.WEATHER_WINDY:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_WINDY);
					break;
				case Climate.WEATHER_WINTER_COLD:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_WINDY);
					break;
				default:
					break;
				}
				mob.location().getArea().getClimateObj().forceWeatherTick(mob.location().getArea());
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the sky for wind, but the magic fizzles.");

		return success;
	}
}
