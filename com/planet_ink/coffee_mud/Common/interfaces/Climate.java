package com.planet_ink.coffee_mud.Common.interfaces;
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
public interface Climate extends Tickable, CMObject
{
	public final static int WEATHER_CLEAR=0;
	public final static int WEATHER_CLOUDY=1;
	public final static int WEATHER_WINDY=2;
	public final static int WEATHER_RAIN=3;
	public final static int WEATHER_THUNDERSTORM=4;
	public final static int WEATHER_SNOW=5;
	public final static int WEATHER_HAIL=6;
	public final static int WEATHER_HEAT_WAVE=7;
	public final static int WEATHER_SLEET=8;
	public final static int WEATHER_BLIZZARD=9;
	public final static int WEATHER_DUSTSTORM=10;
	public final static int WEATHER_DROUGHT=11;
	public final static int WEATHER_WINTER_COLD=12;
	public final static int NUM_WEATHER=13;
	
    public static final int WEATHER_TICK_DOWN=300; // 300 = 20 minutes * 60 seconds / 4
    
	public final static String[] WEATHER_DESCS=
	{ "CLEAR","CLOUDY","WINDY","RAIN","THUNDERSTORM","SNOW","HAIL","HEAT","SLEET","BLIZZARD","DUST","DROUGHT","COLD"};
	
	public int weatherType(Room room);
	public int nextWeatherType(Room room);
	public String weatherDescription(Room room);
	public String nextWeatherDescription(Room room);
	public void setNextWeatherType(int weatherCode);
	public void setCurrentWeatherType(int weatherCode);
	public boolean canSeeTheMoon(Room room);
	public boolean canSeeTheSun(Room room);

	public String getWeatherDescription(Area A);
	public String getNextWeatherDescription(Area A);
	
	public void forceWeatherTick(Area A);
	public int adjustWaterConsumption(int base, MOB mob, Room room);
	public int adjustMovement(int base, MOB mob, Room room);
}
