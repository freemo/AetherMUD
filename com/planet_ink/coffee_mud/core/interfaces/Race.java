package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;
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
public interface Race extends Tickable, StatsAffecting, MsgListener, CMObject
{
	public final static int AGE_INFANT=0;
	public final static int AGE_TODDLER=1;
	public final static int AGE_CHILD=2;
	public final static int AGE_YOUNGADULT=3;
	public final static int AGE_MATURE=4;
	public final static int AGE_MIDDLEAGED=5;
	public final static int AGE_OLD=6;
	public final static int AGE_VENERABLE=7;
	public final static int AGE_ANCIENT=8;
	public final static String[] AGE_DESCS={"Infant","Toddler","Child","Young adult","Adult", "Mature", "Old", "Venerable", "Ancient"};

	public String ID();
	public String name();
	public String racialCategory();
	public CMObject copyOf();
	
	public int availabilityCode();
	public void startRacing(MOB mob, boolean verifyOnly);
	
	public void setHeightWeight(EnvStats stats, char gender);
	public int shortestMale();
	public int shortestFemale();
	public int heightVariance();
	public int lightestWeight();
	public int weightVariance();
	public int getMaxWeight();
	public int[] getAgingChart();
	public long forbiddenWornBits();
	public int[] bodyMask();
	public boolean fertile();
	public Vector outfit();
	public String healthText(MOB mob);
	public Weapon myNaturalWeapon();
	public Vector myResources();
	
	public DeadBody getCorpseContainer(MOB mob, Room room);
	public void reRoll(MOB mob, CharStats C);
	
	public boolean isGeneric();
	public String racialParms();
	public void setRacialParms(String parms);
	
	public String arriveStr();
	public String leaveStr();
	
	public void level(MOB mob);
	public boolean classless();
	public boolean leveless();
	public boolean expless();
	
	public String[] getStatCodes();
	public String getStat(String code);
	public void setStat(String code, String val);
	public boolean sameAs(Race E);
	
	public Vector racialAbilities(MOB mob);
	public Vector racialEffects(MOB mob);
	public void agingAffects(MOB mob, CharStats baseStats, CharStats charStats);
	
	public final static int BODY_ANTENEA=0;
	public final static int BODY_EYE=1;
	public final static int BODY_EAR=2;
	public final static int BODY_HEAD=3;
	public final static int BODY_NECK=4;
	public final static int BODY_ARM=5;
	public final static int BODY_HAND=6;
	public final static int BODY_TORSO=7;
	public final static int BODY_LEG=8;
	public final static int BODY_FOOT=9;
	public final static int BODY_NOSE=10;
	public final static int BODY_GILL=11;
	public final static int BODY_MOUTH=12;
	public final static int BODY_WAIST=13;
	public final static int BODY_TAIL=14;
	public final static int BODY_WING=15;
	public final static int BODY_PARTS=16;
	public final static int BODY_OTHERMASKCODE=1048576;
	public final static String[] BODYPARTSTR={
		"ANTENEA","EYE","EAR","HEAD","NECK","ARM","HAND","TORSO","LEG","FOOT",
		"NOSE","GILL","MOUTH","WAIST","TAIL","WING"};
	
	public final static int GENFLAG_NOCLASS=1;
	public final static int GENFLAG_NOLEVELS=2;
	public final static int GENFLAG_NOEXP=4;
	
	public final static long[] BODY_WEARVECTOR={
		Item.ON_HEAD, // ANTENEA, having any of these removes that pos
		Item.ON_EYES, // EYES, having any of these adds this position
		Item.ON_EARS, // EARS, gains a wear position here for every 2
		Item.ON_HEAD, // HEAD, gains a wear position here for every 1
		Item.ON_NECK, // NECK, gains a wear position here for every 1
		Item.ON_ARMS, // ARMS, gains a wear position here for every 2
		Item.ON_HANDS, // HANDS, gains a wear position here for every 1 
		Item.ON_TORSO, // TORSO, gains a wear position here for every 1
		Item.ON_LEGS, // LEGS, gains a wear position here for every 2
		Item.ON_FEET, // FEET, gains a wear position here for every 2
		Item.ON_HEAD, // NOSE, No applicable wear position for this body part
		Item.ON_HEAD, // GILLS, No applicable wear position for this body part
		Item.ON_MOUTH, // MOUTH, gains a wear position here for every 1
		Item.ON_WAIST, // WAIST, gains a wear position here for every 1
		Item.ON_BACK, // TAIL, having any of these removes that pos
		Item.ON_BACK, // WINGS, having any of these removes that pos
	};
	
	public final static long[][] BODY_WEARGRID={
		{Item.ON_HEAD,-1}, // ANTENEA, having any of these removes that pos
		{Item.ON_EYES,2}, // EYES, having any of these adds this position
		{Item.ON_EARS,2}, // EARS, gains a wear position here for every 2
		{Item.ON_HEAD,1}, // HEAD, gains a wear position here for every 1
		{Item.ON_NECK,1}, // NECK, gains a wear position here for every 1
		{Item.ON_ARMS,2}, // ARMS, gains a wear position here for every 2
		{Item.WIELD|Item.HELD|Item.ON_HANDS
	     |Item.ON_LEFT_FINGER|Item.ON_LEFT_WRIST
		 |Item.ON_RIGHT_FINGER|Item.ON_RIGHT_WRIST,1}, // HANDS, gains a wear position here for every 1 
			// lots of exceptions apply to the above
		{Item.ON_TORSO|Item.ON_BACK,1}, // TORSO, gains a wear position here for every 1
		{Item.ON_LEGS,2}, // LEGS, gains a wear position here for every 2
		{Item.ON_FEET,2}, // FEET, gains a wear position here for every 2
		{-1,-1}, // NOSE, No applicable wear position for this body part
		{-1,-1}, // GILLS, No applicable wear position for this body part
		{Item.ON_MOUTH,1}, // MOUTH, gains a wear position here for every 1
		{Item.ON_WAIST,1}, // WAIST, gains a wear position here for every 1
		{-1,-1}, // TAIL, having any of these removes that pos
		{Item.ON_BACK,-1}, // WINGS, having any of these removes that pos
	};
}
