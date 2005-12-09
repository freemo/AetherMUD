package com.planet_ink.coffee_mud.interfaces;
import com.planet_ink.coffee_mud.common.INI;
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
public interface TimeClock extends Tickable
{
	public String timeDescription(MOB mob, Room room);
    public String getShortTimeDescription();
    public String getShortestTimeDescription();
	public int getYear();
	public void setYear(int y);
	public int getMonth();
	public void setMonth(int m);
	public int getMoonPhase();
	public int getDayOfMonth();
	public void setDayOfMonth(int d);
	public int getTimeOfDay();
	public boolean setTimeOfDay(int t);
	public int getTODCode();
	public void tickTock(int howManyHours);
	public void save();
	public int getSeasonCode();
	public void setLoadName(String name);
	public int getHoursInDay();
	public void setHoursInDay(int h);
	public int getDaysInMonth();
	public void setDaysInMonth(int d);
	public int getMonthsInYear();
	public String[] getMonthNames();
	public void setMonthsInYear(String[] months);
	public int[] getDawnToDusk();
	public void setDawnToDusk(int dawn, int day, int dusk, int night);
	public String[] getWeekNames();
	public int getDaysInWeek();
	public void setDaysInWeek(String[] days);
	public String[] getYearNames();
	public void setYearNames(String[] years);
    public TimeClock deriveClock(long millis);
    public long deriveMillisAfter(TimeClock C);
    public int determineSeason(String str);
    public void initializeINIClock(INI page);
	
	public final static String[] MOON_PHASES={
		"There is a new moon in the sky.",
		"The moon is in the waxing crescent phase.",
		"The moon is in its first quarter.",
		"The moon is in the waxing gibbous phase (almost full).",
		"There is a full moon in the sky.",
		"The moon is in the waning gibbous phase (no longer full).",
		"The moon is in its last quarter.",
		"The moon is in the waning crescent phase.",
		"There is a BLUE MOON! Oh my GOD! Run away!!!!!"
	};
	public final static int PHASE_NEW=0;
	public final static int PHASE_WAXCRESCENT=1;
	public final static int PHASE_WAXQUARTER=2;
	public final static int PHASE_WAXGIBBOUS=3;
	public final static int PHASE_FULL=4;
	public final static int PHASE_WANEGIBBOUS=5;
	public final static int PHASE_WANDEQUARTER=6;
	public final static int PHASE_WANECRESCENT=7;
	public final static int PHASE_BLUE=8;
	public final static String[] PHASE_DESC={"NEW","WAXCRESCENT","WAXQUARTER","WAXGIBBOUS","FULL","WANEGIBBOUS","WANEQUARTER","WANECRESCENT","BLUE"};
	
	public final static String[] TOD_DESC={
		"It is dawn ","It is daytime ","It is dusk ","It is nighttime "
	};
										   
	public final static int TIME_DAWN=0;
	public final static int TIME_DAY=1;
	public final static int TIME_DUSK=2;
	public final static int TIME_NIGHT=3;
	
	public final static int SEASON_SPRING=0;
	public final static int SEASON_SUMMER=1;
	public final static int SEASON_FALL=2;
	public final static int SEASON_WINTER=3;
	public final static String[] SEASON_DESCS={"SPRING","SUMMER","FALL","WINTER"};
	
}
