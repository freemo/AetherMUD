package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;

public interface Area extends Environmental
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
	
	public final static String[] WEATHER_DESCS=
	{ "CLEAR","CLOUDY","WINDY","RAIN","THUNDERSTORM","SNOW","HAIL","HEAT","SLEET","BLIZZARD","DUST","DROUGHT","COLD"};
	
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
	public final static String[] TOD_DESC={
		"It is dawn ","It is daytime ","It is dusk ","It is nighttime "
	};
	
	public final static int CLIMASK_NORMAL=0;
	public final static int CLIMASK_WET=1;
	public final static int CLIMASK_COLD=2;
	public final static int CLIMATE_WINDY=4;
	public final static int CLIMASK_HOT=8;
	public final static int CLIMASK_DRY=16;
	public final static String[] CLIMATE_DESCS={"NORMAL","WET","COLD","WINDY","HOT","DRY"};
	public final static int NUM_CLIMATES=6;
	public final static int ALL_CLIMATE_MASK=31;
	
	public final static int A_FULL_DAY=16; // groups of 10 minutes, so 2 hours, 40 minutes=24 hours.
	public final static int DAYS_IN_MONTH=30; // number of days in a month
	public final static int MONTHS_IN_YEAR=12; // number of months in a full year
									   
	public final static int TIME_DAWN=0;
	public final static int TIME_DAY=1;
	public final static int TIME_DUSK=2;
	public final static int TIME_NIGHT=3;
	
	public final static int SEASON_SPRING=0;
	public final static int SEASON_SUMMER=1;
	public final static int SEASON_FALL=2;
	public final static int SEASON_WINTER=3;
	public final static String[] SEASON_DESCS={"SPRING","SUMMER","FALL","WINTER"};
	
	public int weatherType(Room room);
	public int nextWeatherType(Room room);
	public String weatherDescription(Room room);
	public String nextWeatherDescription(Room room);
	public int climateType();
	public int getSeasonCode();
	public void setNextWeatherType(int weatherCode);
	public void setCurrentWeatherType(int weatherCode);

	public int adjustWaterConsumption(int base, MOB mob, Room room);
	public int adjustMovement(int base, MOB mob, Room room);
	
	public void setClimateType(int newClimateType);
	public String getWeatherDescription();
	public String getNextWeatherDescription();
	
	public String timeDescription(MOB mob, Room room);
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
	
	public void tickControl(boolean start);
	public StringBuffer getAreaStats();
	public Vector getMyMap();
	public void clearMap();
	public void toggleMobility(boolean onoff);
	
	public void addSubOp(String username);
	public void delSubOp(String username);
	public boolean amISubOp(String username);
	public String getSubOpList();
	public void setSubOpList(String list);
	public Vector getSubOpVectorList();
}
