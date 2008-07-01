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

import java.util.*;



/* 
   Copyright 2000-2008 Bo Zimmerman

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
public interface Law extends CMCommon
{
    
    public void initialize(LegalBehavior details, Properties laws, boolean modifiableNames, boolean modifiableLaws);
    public void changeStates(LegalWarrant W, int state);
    public Vector otherCrimes();
    public Vector otherBits();
    public Vector bannedSubstances();
    public Vector bannedBits();
    public Hashtable abilityCrimes();
    public Hashtable basicCrimes();
    public Hashtable taxLaws();
    public Vector chitChat();
    public Vector chitChat2();
    public Vector chitChat3();
    public Vector jailRooms();
    public Vector releaseRooms();
    public Vector officerNames();
    public Vector judgeNames();
    public String[] messages();
    public Vector oldWarrants();
    public Vector warrants();
    public boolean arrestMobs();
    public String[] paroleMessages();
    public Integer[] paroleTimes();
    public String[] jailMessages();
    public Integer[] jailTimes();
    public String getMessage(int which);
    public String paroleMessages(int which);
    public int paroleTimes(int which);
    public String jailMessages(int which);
    public int jailTimes(int which);
    public LegalWarrant getCopkiller(Area A, LegalBehavior behav, MOB mob);
    public LegalWarrant getLawresister(Area A, LegalBehavior behav, MOB mob);
    public LegalWarrant getWarrant(MOB mob, int which);
    public LegalWarrant getOldWarrant(MOB criminal, String crime, boolean pull);
    public void resetLaw();
    public boolean hasModifiableNames();
    public boolean hasModifiableLaws();
    public String getInternalStr(String msg);
    public boolean isInternalStr(String msg);
    public void setInternalStr(String tag, String value);
    public String rawLawString();
    public boolean lawIsActivated();
    public void propertyTaxTick(Area A, boolean debugging);
    public Environmental[] getTreasuryNSafe(Area A);
    
	public static final int ACTION_WARN=0;
	public static final int ACTION_THREATEN=1;
	public static final int ACTION_PAROLE1=2;
	public static final int ACTION_PAROLE2=3;
	public static final int ACTION_PAROLE3=4;
	public static final int ACTION_PAROLE4=5;
	public static final int ACTION_JAIL1=6;
	public static final int ACTION_JAIL2=7;
	public static final int ACTION_JAIL3=8;
	public static final int ACTION_JAIL4=9;
	public static final int ACTION_EXECUTE=10;
	public static final int ACTION_HIGHEST=10;
    public static final int ACTION_MASK=255;
    
	public static final String[] ACTION_DESCS={
		"WARNING",
		"THREAT",
		"PAROLE1",
		"PAROLE2",
		"PAROLE3",
		"PAROLE4",
		"JAIL1",
		"JAIL2",
		"JAIL3",
		"JAIL4",
		"DEATH",
	};
    
    public static final int ACTIONMASK_SEPARATE=256;
    public static final int ACTIONMASK_SKIPTRIAL=512;
    public static final int ACTIONMASK_DETAIN=1024;
    public static final int ACTIONMASK_FINE=2048;
    public static final int ACTIONMASK_NORELEASE=4096;
    public static final String[] ACTIONMASK_DESCS={
        "SEPARATE",
        "SKIPTRIAL",
        "DETAIN=",
        "FINE=",
        "NORELEASE"
    };
    public static final int[] ACTIONMASK_CODES={
        ACTIONMASK_SEPARATE,
        ACTIONMASK_SKIPTRIAL,
        ACTIONMASK_DETAIN,
        ACTIONMASK_FINE,
        ACTIONMASK_NORELEASE
    };

	public static final int STATE_SEEKING=0;
	public static final int STATE_ARRESTING=1;
	public static final int STATE_SUBDUEING=2;
	public static final int STATE_MOVING=3;
	public static final int STATE_REPORTING=4;
	public static final int STATE_WAITING=5;
	public static final int STATE_PAROLING=6;
	public static final int STATE_JAILING=7;
	public static final int STATE_EXECUTING=8;
	public static final int STATE_MOVING2=9;
	public static final int STATE_RELEASE=10;
    public static final int STATE_MOVING3=11;
    public static final int STATE_DETAINING=12;

	public static final int BIT_CRIMELOCS=0;
	public static final int BIT_CRIMEFLAGS=1;
	public static final int BIT_CRIMENAME=2;
	public static final int BIT_SENTENCE=3;
	public static final int BIT_WARNMSG=4;
	public static final int BIT_NUMBITS=5;
	
	public final static int MSG_PREVOFF=0;
	public final static int MSG_WARNING=1;
	public final static int MSG_THREAT=2;
	public final static int MSG_EXECUTE=3;
	public final static int MSG_PROTECTEDMASK=4;
	public final static int MSG_TRESPASSERMASK=5;
	public final static int MSG_RESISTFIGHT=6;
	public final static int MSG_NORESIST=7;
	public final static int MSG_RESISTWARN=8;
	public final static int MSG_PAROLEDISMISS=9;
	public final static int MSG_LAWFREE=10;
	public final static int MSG_RESIST=11;
	public final static int MSG_COPKILLER=12;
	public final static int MSG_TOTAL=13;
	
	public static final String defaultLaw=
		"OFFICERS=@\n"+
		"JUDGE=@\n"+
		"JAIL=@\n"+
		"RELEASEROOM=@\n"+
		"WARNINGMSG=Your behavior is unacceptable.  Do not repeat this offense.  You may go.\n"+
		"THREATMSG=That behavior is NOT tolerated here.  Keep your nose clean, or next time I may not be so lenient.  You may go.\n"+
		"JAIL1MSG=You are hereby sentenced to minimum jail time.  Take away the prisoner!\n"+
		"JAIL2MSG=You are hereby sentenced to jail time.  Take away the prisoner!\n"+
		"JAIL3MSG=You are hereby sentenced to hard jail time.  Take away the prisoner!\n"+
		"JAIL4MSG=You are hereby sentenced to rot in jail.  Take away the prisoner!\n"+
		"PAROLE1MSG=You are hereby sentenced to a short period under the prisoner's geas. Perhaps that will make you think!\n"+
		"PAROLE2MSG=You are hereby sentenced to a period under the prisoner's geas. That will teach you, I think.\n"+
		"PAROLE3MSG=You are hereby sentenced to hard time under the prisoner's geas! That will teach you!\n"+
		"PAROLE4MSG=You are hereby sentenced to rot under the prisoner's geas!  Don't let me see you again!\n"+
		"PAROLEDISMISS=Now, get out of my sight!\n"+
		"PREVOFFMSG=You have been warned about this behavior before.\n"+
		"EXECUTEMSG=You are hereby sentenced to a brutal death.  Sentence to be carried out IMMEDIATELY!\n"+
		"LAWFREE=You are free to go.\n"+
		"CHITCHAT=\"You didn't really think you could get away with it did you?\" \"You are REALLY in for it!\" \"Convicts like you are a dime a dozen.\" \"MAKE WAY! DEAD MAN WALKING!\" \"You are gonna GET it.\" \"I love my job.\"\n"+
		"CHITCHAT2=\"You didn't really think you would get away with it did you?\" \"I hope you aren't claustrophobic!\" \"Remember not to drop your soap in there.\" \"MAKE WAY! DEAD MAN WALKING!\" \"I recommend you hold your breathe while you're in there -- I always do.  It stinks!\" \"Putting away scum like you makes it all worthwhile\"\n"+
        "CHITCHAT3=\"This is for your own good, so please don't resist.\" \"You understand that your detention is mandatory I hope.\" \"Just doing my job.\" \"Been a nice day, don't ya think?\" \"We're almost there, don't worry.\" \"Not much farther now.\"\n"+
		"RESISTWARNMSG=I said SIT DOWN! NOW!\n"+
		"NORESISTMSG=Good.  Now hold still.\n"+
		"ACTIVATED=FALSE\n"+
		"RESISTFIGHTMSG=Resisting arrest?! How DARE you!\n"+
		"COPKILLERMSG=COP-KILLER!!!!! AAARRRGGGHHHH!!!!!\n"+
		"RESISTMSG=Resisting arrest eh?  Well, have it your way.\n"+
		"PROTECTED=+ADJINT 3\n"+
		"ARRESTMOBS=true\n"+
		"TRESPASSERS=-Race +Undead\n"+
		"PAROLE1TIME=40\n"+
		"PAROLE2TIME=80\n"+
		"PAROLE3TIME=160\n"+
		"PAROLE4TIME=320\n"+
		"JAIL1TIME=20\n"+
		"JAIL2TIME=40\n"+
		"JAIL3TIME=80\n"+
		"JAIL4TIME=160\n"+
		"RESISTINGARREST=;;resisting arrest;jail1;Resisting arrest by a lawful officer is a serious crime.\n"+
		"TRESPASSING=!home !indoors;!recently;trespassing;jail3;Your kind are not allowed here.\n"+
		"NUDITY=!home !indoors;witness !recently;indecent exposure;warning;Nudity below the waist violates our high moral code.  Use the 'outfit' command if you need clothes!\n"+
		"ARMED=\n"+
		"ASSAULT=;;assaulting <T-NAME>;jail4;Assault is a hideous offense.\n"+
		"MURDER=;;murdering <T-NAME>;death;Murder is a barbarous offense.\n"+
		"PROPERTYROB=;;robbing the property of <T-NAME>;jail3;Robbery violates our high moral code.\n"+
		"TAXEVASION=;;evading taxes;jail1;Paying taxes and dieing are our solemn duties to the state.\n"+
		"TREASURY=\n"+
		"PROPERTYTAX=0\n"+
		"SALESTAX=0\n"+
		"CITTAX=10\n"+
		"THIEF_SWIPE=;;robbing <T-NAME>;jail2;Swiping violates our high moral code.\n"+
		"THIEF_STEAL=;;robbing <T-NAME>;jail3;Stealing violates our high moral code.\n"+
		"THIEF_TRAP=!home;;setting traps in city limits;jail3;Trapping puts us all in mortal danger.\n"+
		"THIEF_BRIBE=;;bribing <T-NAME>;jail2;Bribing is a violation of our moral code.\n"+
		"THIEF_CON=;;conning <T-NAME>;jail2;Conning and deception is a violation of our moral code.\n"+
		"THIEF_EMBEZZLE=;;embezzling <T-NAME>;jail4;Embezzling money is a form of vicious theft!\n"+
		"THIEF_CONTRACTHIT=;;taking out contract on <T-NAME>'s life;death;Murder by contract is a barbarous offense.\n"+
		"THIEF_DEATHTRAP=;;setting a death trap;death;Murder by trapping is a barbarous offense.\n"+
		"THIEF_FORGERY=;;forgery;jail2;Forgery is deceptive and quite illegal.\n"+
		"THIEF_RACKETEER=;;racketeering <T-NAME>;jail3;Racketeering is a form of vicious theft.\n"+
		"THIEF_ROBBERY=;;robbing <T-NAME>;jail3;Robbery violates our high moral code.\n"+
		"INEBRIATION=!home !pub !tavern !inn !bar;!recently;public intoxication;parole1;Drunkenness is a demeaning and intolerable state.\n"+
		"POISON_ALCOHOL=!home !pub !tavern !inn !bar;!recently;public intoxication;parole1;Drunkenness is a demeaning and intolerable state.\n"+
		"POISON_FIREBREATHER=!home !pub !tavern !inn !bar;!recently;public intoxication;parole1;Drunkenness is a demeaning and intolerable state.\n"+
		"POISON_LIQUOR=!home !pub !tavern !inn !bar;!recently;public intoxication;parole1;Drunkenness is a demeaning and intolerable state.\n";
}
