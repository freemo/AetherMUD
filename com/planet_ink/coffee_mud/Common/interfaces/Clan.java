package com.planet_ink.coffee_mud.core.interfaces;
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
   Copyright 2000-2005 Jeremy Vyska

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

  * Clan is the basis for clan objects.
  * A Clan is basically a collection of {@link MOB} objects,
  * including, but not limited to:
  * <ul>
  * <li> Ranks
  * <li> Jobs/Positions
  * <li> Clan Homes
  * </ul>
  * In this interface, we provide the common functions, including:
  * <li> Add/remove member
  * <li> Get/set Clan recall and donation room
  * <li> Get average alignment
  * </ul>
  * @author=Jeremy Vyska
  */
public interface Clan extends Cloneable, Tickable
{

	public static final int POS_APPLICANT=0;
	public static final int POS_MEMBER=1;
	public static final int POS_STAFF=2;
	public static final int POS_ENCHANTER=4;
	public static final int POS_TREASURER=8;
	public static final int POS_LEADER=16;
	public static final int POS_BOSS=32;
	public static final int[] POSORDER={POS_APPLICANT,
										POS_MEMBER,
										POS_STAFF,
										POS_ENCHANTER,
										POS_TREASURER,
										POS_LEADER,
										POS_BOSS};

	public static final int CLANSTATUS_ACTIVE=0;
	public static final int CLANSTATUS_PENDING=1;
	public static final int CLANSTATUS_FADING=2;
	public static final String[] CLANSTATUS_DESC={
		"ACTIVE",
		"PENDING",
		"FADING"
	};
	
	public static final int REL_NEUTRAL=0;
	public static final int REL_WAR=1;
	public static final int REL_HOSTILE=2;
	public static final int REL_FRIENDLY=3;
	public static final int REL_ALLY=4;
    
    public static final int[] REL_NEUTRALITYGAUGE={/*REL_NEUTRAL*/0,/*REL_WAR*/4, /*REL_HOSTILE*/1,/*REL_FRIENDLY*/1,/*REL_ALLY*/4};

	public static final int[][] RELATIONSHIP_VECTOR={
	{REL_NEUTRAL,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_FRIENDLY},
	{REL_WAR,		REL_WAR,		REL_WAR,		REL_WAR,		REL_WAR},
	{REL_HOSTILE,	REL_WAR,		REL_HOSTILE,	REL_HOSTILE,	REL_HOSTILE},
	{REL_FRIENDLY,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_FRIENDLY},
	{REL_FRIENDLY,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_ALLY},
	};
	
	public static final String[] REL_DESCS={
		"NEUTRAL","WAR","HOSTILE","FRIENDLY","ALLY"
	};
	public static final String[] REL_STATES={
		"NEUTRALITY TOWARDS",
		"WAR WITH",
		"HOSTILITIES WITH",
		"FRIENDSHIP WITH",
		"AN ALLIANCE WITH"
	};
	
	public static final int TROPHY_CONTROL=1;
	public static final int TROPHY_EXP=2;
	public static final int TROPHY_AREA=4;
    public static final int TROPHY_PK=8;
	public static final String TROPHY_DESCS_SHORT[]={"","CP","EXP","","AREA","","","","PK"};
	public static final String TROPHY_DESCS[]={"","Most control points","Most clan experience","","Most controlled areas","","","","Most rival player-kills"};
	
	public static final int GVT_DICTATORSHIP=0;
	public static final int GVT_OLIGARCHY=1;
	public static final int GVT_REPUBLIC=2;
	public static final int GVT_DEMOCRACY=3;
	public static final String[] GVT_DESCS={
		"CLAN",
		"GUILD",
		"UNION",
		"FELLOWSHIP"
	};
	
	public static final int[] topRanks={
		POS_BOSS,
		POS_BOSS,
		POS_BOSS,
		POS_LEADER
	};
	
	
	public static final String[][] ROL_DESCS={
		{"APPLICANT","MEMBER","STAFF","ENCHANTER","TREASURER","LEADER","BOSS"},
		{"APPLICANT","MEMBER","CHIEF","ENCHANTER","TREASURER","SECRETARY","GUILDMASTER"},
		{"APPLICANT","CITIZEN","SHERIFF","ENCHANTER","TREASURER","SECRETARY","SENATOR"},
		{"APPLICANT","CITIZEN","SOLDIER","ENCHANTER","TREASURER","MANAGER","FIRST CITIZEN"}
	};
	public static final int maxint=Integer.MAX_VALUE;
	public static final int[][] ROL_MAX={
		{maxint,maxint,maxint,1,1,maxint,1},
		{maxint,maxint,maxint,1,1,maxint,5},
		{maxint,maxint,maxint,1,1,1,5},
		{maxint,maxint,maxint,maxint,maxint,maxint,1}
	};
	
	public static final int TYPE_CLAN=1;

	public static final int FUNC_CLANACCEPT=0;
	public static final int FUNC_CLANASSIGN=1;
	public static final int FUNC_CLANEXILE=2;
	public static final int FUNC_CLANHOMESET=3;
	public static final int FUNC_CLANDONATESET=4;
	public static final int FUNC_CLANREJECT=5;
	public static final int FUNC_CLANPREMISE=6;
	public static final int FUNC_CLANPROPERTYOWNER=7;
	public static final int FUNC_CLANWITHDRAW=8;
	public static final int FUNC_CLANCANORDERUNDERLINGS=9;
	public static final int FUNC_CLANCANORDERCONQUERED=10;
	public static final int FUNC_CLANVOTEASSIGN=11;
	public static final int FUNC_CLANVOTEOTHER=12;
	public static final int FUNC_CLANDEPOSITLIST=13;
	public static final int FUNC_CLANDECLARE=14;
	public static final int FUNC_CLANTAX=15;
	public static final int FUNC_CLANENCHANT=16;
	
	public int allowedToDoThis(MOB mob, int function);
	
	public final static int VSTAT_STARTED=0;
	public final static int VSTAT_FAILED=1;
	public final static int VSTAT_PASSED=2;
	
	public final static String[] VSTAT_DESCS={
		"In Progress",
		"Failed",
		"Passed"
	};
	public void clanAnnounce(String msg);
	
	public Enumeration votes();
	public void updateVotes();
	public void addVote(Object CV);
	public void delVote(Object CV);
	public int getNumVoters(int function);
	
	public int getSize();

	public String getName();
	public String ID();
	public void setName(String newName);
	public int getType();
	public String typeName();

	public boolean updateClanPrivileges(MOB mob);
	
	/** Retrieves this Clan's basic story. 
	  * This is to make the Clan's more RP based and so we can
	  * provide up-to-date information on Clans on the web server.
	  */
	public String getPremise();
	/** Sets this Clan's basic story.  See {@link getPremise} for more info. */
	public void setPremise(String newPremise);

	/** Creates the string for the 'clandetail' command */
	public String getDetail(MOB mob);

	public String getAcceptanceSettings();
	public void setAcceptanceSettings(String newSettings);

	public String getPolitics();
	public void setPolitics(String politics);

	public int getStatus();
	public void setStatus(int newStatus);

	public String getRecall();
	public void setRecall(String newRecall);

	public String getMorgue();
	public void setMorgue(String newRecall);

	public int getTrophies();
	public void setTrophies(int trophyFlag);

	public String getDonation();
	public void setDonation(String newDonation);
  
	public long getExp();
	public void setExp(long exp);
	public void adjExp(int howMuch);
    
    public void recordClanKill();
    public int getCurrentClanKills();
	
	public long calculateMapPoints(Vector controlledAreas);
	public long calculateMapPoints();
	public Vector getControlledAreas();
	
	public int applyExpMods(int exp);
	public void setTaxes(double rate);
	public double getTaxes();
	
	public DVector getMemberList();
	public DVector getMemberList(int PosFilter);
	
	public MOB getResponsibleMember();

	public int getClanRelations(String id);
	public long getLastRelationChange(String id);
	public void setClanRelations(String id, int rel, long time);
	
	public int getGovernment();
	public void setGovernment(int type);
		
	public int getTopRank();

	public void update();
	public void destroyClan();
	public void create();
}