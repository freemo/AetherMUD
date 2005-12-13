package com.planet_ink.coffee_mud.Abilities.interfaces;
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
public interface Ability  extends Environmental
{
	// general classifications
	public static final int SKILL=0;
	public static final int SPELL=1;
	public static final int PRAYER=2;
	public static final int SONG=3;
	public static final int TRAP=4;
	public static final int PROPERTY=5;
	public static final int THIEF_SKILL=6;
	public static final int LANGUAGE=7;
	public static final int CHANT=8;
	public static final int COMMON_SKILL=9;
	public static final int DISEASE=10;
	public static final int POISON=11;
	public static final int SUPERPOWER=12;
	public static final int EVILDEED=13;
	public static final int ALL_CODES=31; // this is a mask, not a total
	public static final String[] TYPE_DESCS={
		"SKILL","SPELL","PRAYER","SONG","TRAP","PROPERTY",
		"THIEF SKILL","LANGUAGE","CHANT","COMMON SKILL",
		"DISEASE","POISON","SUPERPOWER","EVILDEED"
	};

	// domains
	public static final int DOMAIN_DIVINATION=1<<5;
	public static final int DOMAIN_ABJURATION=2<<5;
	public static final int DOMAIN_ILLUSION=3<<5;
	public static final int DOMAIN_EVOCATION=4<<5;
	public static final int DOMAIN_ALTERATION=5<<5;
	public static final int DOMAIN_TRANSMUTATION=6<<5;
	public static final int DOMAIN_ENCHANTMENT=7<<5;
	public static final int DOMAIN_CONJURATION=8<<5;
	public static final String[] DOMAIN_DESCS={
		"NOTHING","DIVINATION","ABJURATION","ILLUSION",
		"INVOCATION/EVOCATION","ALTERATION","TRANSMUTATION",
		"ENCHANTMENT/CHARM","CONJURATION"
	};

	// affect/target flags
	public static final int CAN_MOBS=1;
	public static final int CAN_ITEMS=2;
	public static final int CAN_AREAS=4;
	public static final int CAN_ROOMS=8;
	public static final int CAN_EXITS=16;

	public static final int ALL_DOMAINS=(255<<5);
	// the classification incorporates the above
	public int classificationCode();

	// qualities
	public static final int MALICIOUS=0;
	public static final int INDIFFERENT=1;
	public static final int OK_SELF=2;
	public static final int OK_OTHERS=3;
	public static final int BENEFICIAL_SELF=4;
	public static final int BENEFICIAL_OTHERS=5;
	// the quality is used for more intelligent
	// usage by mobs.  it returns one of the above,
	// and dictates how an ability is to be used
	// by mobs
	public int quality();

	// misc_flags
	public static final int FLAG_BINDING=1;
	public static final int FLAG_MOVING=2;
	public static final int FLAG_TRANSPORTING=4;
	public static final int FLAG_WEATHERAFFECTING=8;
	public static final int FLAG_SUMMONING=16;
	public static final int FLAG_CHARMING=32;
	public static final int FLAG_TRACKING=64;
	public static final int FLAG_HEATING=128;
	public static final int FLAG_BURNING=256;
	public static final int FLAG_HOLY=512;
	public static final int FLAG_UNHOLY=1024;
	public static final int FLAG_PARALYZING=2048;
	public static final int FLAG_MOONSUMMONING=4096;
	public static final int FLAG_HEALING=16384;
	public static final int FLAG_CURSE=32768;
	public static final int FLAG_BLESSING=65536;
	public static final int FLAG_CRAFTING=131072;
	public static final int FLAG_MOONCHANGING=262144;
	public static final int FLAG_GATHERING=524288;
	public static final int FLAG_NOORDERING=1048576;

	public static final String[] FLAG_DESCS={
	    "BINDING",
	    "MOVING",
	    "TRANSPORTING",
	    "WEATHERAFFECTING",
	    "SUMMONING",
	    "CHARMING",
	    "TRACKING",
	    "HEATING",
	    "BURNING",
	    "HOLY",
	    "UNHOLY",
	    "PARALYZING",
	    "MOONSUMMONING",
	    "HEALING",
	    "CURSE",
	    "BLESSING",
	    "CRAFTING",
	    "MOONCHANGING",
	    "GATHERING",
	    "NOORDERING"
	};

	public long flags();
	// these are flags which deliver slightly more
	// specific information about this ability.


	// who is responsible for initiating this ability?
	public MOB invoker();
	public void setInvoker(MOB mob);

	// who (or what) is being affected by the abilitys use?
	public Environmental affecting();
	public void setAffectedOne(Environmental being);

	// whether or not this ability is also a command
	// most skills are, properties never are
	public boolean putInCommandlist();

	// the initial command word to activate this ability
	// or its brethren (cast, trip, etc..)
	public String[] triggerStrings();
	public int castingTime();
	public int combatCastingTime();

	// when a mob uses an ability manually, this is the method
	// to make it happen.
    public boolean preInvoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel);
	public boolean invoke(MOB mob, Environmental target, boolean auto, int asLevel);
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel);

	// for affects which may be uninvoked, this will do the trick!
	public void unInvoke();
	public boolean bubbleAffect();
	public boolean canBeUninvoked();
	public void makeNonUninvokable();

	// for abilities which are on a timer, this method will
	// make those abilities last a very long time (until reboot perhaps)
	public void makeLongLasting();

	// an autoinvocating effect is an ability which affects the
	// mob just by having the ability.  Dodge is an example of this.
	// the autoinvacation method is called to initiate this.
	// this method should be called WHENEVER an ability is
	// added to a MOB for the first time.
	public boolean autoInvocation(MOB mob);
	public boolean isAutoInvoked();
	public boolean isNowAnAutoEffect();
	public int[] usageCost(MOB mob);
	public final static int USAGE_NADA=0;
	public final static int USAGE_MANA=1;
	public final static int USAGE_MOVEMENT=2;
	public final static int USAGE_HITPOINTS=4;
	public final static int USAGE_MANAINDEX=0;
	public final static int USAGE_MOVEMENTINDEX=1;
	public final static int USAGE_HITPOINTSINDEX=2;
	public int usageType();

	// a borrowed ability is one derived from some other source
	// than the mobs knowledge, such as a magic item, or
	// a class behavior.  borrowed abilities are not saved,
	// and neither are borrowed properties or affects.
	public boolean isBorrowed(Environmental toMe);
	public void setBorrowed(Environmental toMe, boolean truefalse);

	// An optional numeric code for this ability
	public int abilityCode();
	public void setAbilityCode(int newCode);

	// any external files which may be required to make this ability work
	// files returned by this method should not be base distrib files!
	public Vector externalFiles();

	// methods to assist in teaching and learning the
	// abilities
	public boolean canBeTaughtBy(MOB teacher, MOB student);
	public boolean canBePracticedBy(MOB teacher, MOB student);
	public boolean canBeLearnedBy(MOB teacher, MOB student);
	public void teach(MOB teacher, MOB student);
	public void practice(MOB teacher, MOB student);
	public String requirements();

	public boolean canTarget(Environmental E);
	public boolean canAffect(Environmental E);

	// for use by the identify spell, this should return a
	// nice description of any properties incorporated
	// by this effect
	public String accountForYourself();

	// For clerics, usually, whether this ability is
	// appropriate for the mob using it
    public boolean appropriateToMyFactions(MOB mob);
	public int adjustedLevel(MOB mob, int asLevel);

	// intelligently add this ability as an effect upon a target,
	// and start a new clock (if necessary), setting the timer
	// on the effect as needed.
	public void startTickDown(MOB invoker, Environmental affected, int tickTime);

	// as an ability, how profficient the mob is at it.
	public int profficiency();
	public void setProfficiency(int newProfficiency);
	public boolean profficiencyCheck(MOB mob, int adjustment, boolean auto);
	public void helpProfficiency(MOB mob);
}
