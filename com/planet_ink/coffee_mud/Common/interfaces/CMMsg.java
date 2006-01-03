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
import java.util.Vector;

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
public interface CMMsg extends CMObject
{
	public int targetMajor();
	public int targetMinor();
	public int targetCode();
    public void setTargetCode(int code);
	public String targetMessage();
    public void setTargetMessage(String str);
    public boolean isTarget(Environmental E);
    public boolean isTarget(int codeOrMask);
    public boolean isTarget(String codeOrMaskDesc);

	public int sourceMajor();
	public int sourceMinor();
	public int sourceCode();
    public boolean isSource(Environmental E);
    public boolean isSource(int codeOrMask);
    public boolean isSource(String codeOrMaskDesc);
    public void setSourceCode(int code);
	public String sourceMessage();
    public void setSourceMessage(String str);

	public int othersMajor();
	public int othersMinor();
	public int othersCode();
    public boolean isOthers(Environmental E);
    public boolean isOthers(int codeOrMask);
    public boolean isOthers(String codeOrMaskDesc);
    public void setOthersCode(int code);
	public String othersMessage();
    public void setOthersMessage(String str);

	public Environmental target();
	public Environmental tool();
	public MOB source();

	public boolean amITarget(Environmental thisOne);
	public boolean amISource(MOB thisOne);

	public int value();
	public void setValue(int amount);

    public void modify(MOB source, Environmental target, int newAllCode, String allMessage);
    public void modify(MOB source, int newAllCode, String allMessage);
    public void modify(MOB source, int newAllCode, String allMessage, int newValue);
    public void modify(MOB source, Environmental target, Environmental tool, int newAllCode, String allMessage);
    public void modify(MOB source,
                       Environmental target,
                       Environmental tool,
                       int newSourceCode,
                       String sourceMessage,
                       String targetMessage,
                       String othersMessage);
	public void modify(MOB source,
						Environmental target,
						Environmental tool,
						int newSourceCode,
						String sourceMessage,
						int newTargetCode,
						String targetMessage,
						int newOthersCode,
						String othersMessage);
    public void modify(MOB source,
               Environmental target,
               Environmental tool,
               int newSourceCode,
               int newTargetCode,
               int newOthersCode,
               String Message);

	public Vector trailerMsgs();
	public void addTrailerMsg(CMMsg msg);

	// 0-1999 are message types
	// 2000-2047 are channels
	// flags are 2048, 4096, 8192, 16384,
	// flags are 2048, 4096, 8192, 16384, 32768, 65536,
	//131072, 262144, 524288, 1048576, and 2097152


	// helpful seperator masks
	public static final int MINOR_MASK=2047;
	public static final int MAJOR_MASK=4192256;

	// masks for all messages
	public static final int MASK_HANDS=2048;       // small hand movements
	public static final int MASK_MOVE=4096;        // large body movements (travel)
	public static final int MASK_EYES=8192;        // looking and seeing
	public static final int MASK_MOUTH=16384;      // speaking and eating
	public static final int MASK_SOUND=32768;      // general body noises
	public static final int MASK_ALWAYS=65536;    // anything!
	public static final int MASK_MAGIC=131072;     // the magic mask!
	public static final int MASK_DELICATE=262144;  // for thief skills!
	public static final int MASK_MALICIOUS=524288; // for attacking
	public static final int MASK_CHANNEL=1048576;  // for channel messages
	public static final int MASK_OPTIMIZE=2097152; // to optomize a repeated msg

	// minor messages
	public static final int TYP_AREAAFFECT=1;
	public static final int TYP_PUSH=2;
	public static final int TYP_PULL=3;
	public static final int TYP_RECALL=4;
	public static final int TYP_OPEN=5;
	public static final int TYP_CLOSE=6;
	public static final int TYP_PUT=7;
	public static final int TYP_GET=8;
	public static final int TYP_UNLOCK=9;
	public static final int TYP_LOCK=10;
	public static final int TYP_WIELD=11;
	public static final int TYP_GIVE=12;
	public static final int TYP_BUY=13;
	public static final int TYP_SELL=14;
	public static final int TYP_DROP=15;
	public static final int TYP_WEAR=16;
	public static final int TYP_FILL=17;
	public static final int TYP_DELICATE_HANDS_ACT=18;
	public static final int TYP_VALUE=19;
	public static final int TYP_HOLD=20;
	public static final int TYP_NOISYMOVEMENT=21;
	public static final int TYP_QUIETMOVEMENT=22;
	public static final int TYP_WEAPONATTACK=23;
	public static final int TYP_LOOK=24;
	public static final int TYP_READ=25;
	public static final int TYP_NOISE=26;
	public static final int TYP_SPEAK=27;
	public static final int TYP_CAST_SPELL=28;
	public static final int TYP_LIST=29;
	public static final int TYP_EAT=30;
	public static final int TYP_ENTER=31;
	public static final int TYP_FOLLOW=32;
	public static final int TYP_LEAVE=33;
	public static final int TYP_SLEEP=34;
	public static final int TYP_SIT=35;
	public static final int TYP_STAND=36;
	public static final int TYP_FLEE=37;
	public static final int TYP_NOFOLLOW=38;
	public static final int TYP_WRITE=39;
	public static final int TYP_FIRE=40;
	public static final int TYP_COLD=41;
	public static final int TYP_WATER=42;
	public static final int TYP_GAS=43;
	public static final int TYP_MIND=44;
	public static final int TYP_GENERAL=45;
	public static final int TYP_JUSTICE=46;
	public static final int TYP_ACID=47;
	public static final int TYP_ELECTRIC=48;
	public static final int TYP_POISON=49;
	public static final int TYP_UNDEAD=50;
	public static final int TYP_MOUNT=51;
	public static final int TYP_DISMOUNT=52;
	public static final int TYP_OK_ACTION=53;
	public static final int TYP_OK_VISUAL=54;
	public static final int TYP_DRINK=55;
	public static final int TYP_HANDS=56;
	public static final int TYP_PARALYZE=57;
	public static final int TYP_WAND_USE=58;
	public static final int TYP_SERVE=59;
	public static final int TYP_REBUKE=60;
	public static final int TYP_ADVANCE=61;
	public static final int TYP_DISEASE=62;
	public static final int TYP_DEATH=63;
	public static final int TYP_DEPOSIT=64;
	public static final int TYP_WITHDRAW=65;
	public static final int TYP_EMOTE=66;
	public static final int TYP_QUIT=67;
	public static final int TYP_SHUTDOWN=68;
	public static final int TYP_VIEW=69;
	public static final int TYP_RETIRE=70;
	public static final int TYP_RETREAT=71;
	public static final int TYP_PANIC=72;
	public static final int TYP_THROW=73;
	public static final int TYP_EXTINGUISH=74;
	public static final int TYP_TELL=75;
	public static final int TYP_SITMOVE=76;
	public static final int TYP_KNOCK=77;
	public static final int TYP_PRACTICE=78;
	public static final int TYP_TEACH=79;
	public static final int TYP_REMOVE=80;
	public static final int TYP_EXPCHANGE=81;
	public static final int TYP_DAMAGE=82;
	public static final int TYP_HEALING=83;
	public static final int TYP_ROOMRESET=84;
	public static final int TYP_RELOAD=85;
	public static final int TYP_SNIFF=86;
	public static final int TYP_ACTIVATE=87;
	public static final int TYP_DEACTIVATE=88;
    public static final int TYP_FACTIONCHANGE=89;
    public static final int TYP_LOGIN=90;
    public static final int TYP_LEVEL=91;
    public static final int TYP_EXAMINE=92;
    public static final int TYP_ORDER=93;
    public static final int TYP_EXPIRE=94;

	public static final int TYP_CHANNEL=2000; //(2000-2047 are channels)
    public static final Object[][] MISC_DESCS={
        {"CHANNEL",new Integer(2000),new Integer(2047)},
    };
    public static final String[] TYPE_DESCS={"NOTHING",
        "AREAAFFECT", "PUSH", "PULL", "RECALL", "OPEN", "CLOSE", "PUT", "GET", 
        "UNLOCK", "LOCK", "WIELD", "GIVE", "BUY", "SELL", "DROP", "WEAR", "FILL", 
        "DELICATE_HANDS_ACT", "VALUE", "HOLD", "NOISYMOVEMENT", "QUIETMOVEMENT", 
        "WEAPONATTACK", "LOOK", "READ", "NOISE", "SPEAK", "CAST_SPELL","LIST", 
        "EAT", "ENTER", "FOLLOW", "LEAVE", "SLEEP", "SIT", "STAND", "FLEE", 
        "NOFOLLOW", "WRITE", "FIRE", "COLD", "WATER", "GAS", "MIND", "GENERAL", 
        "JUSTICE", "ACID", "ELECTRIC", "POISON", "UNDEAD", "MOUNT", "DISMOUNT", 
        "OK_ACTION", "OK_VISUAL", "DRINK", "HANDS", "PARALYZE", "WAND_USE", "SERVE", 
        "REBUKE", "ADVANCE", "DISEASE", "DEATH", "DEPOSIT", "WITHDRAW", "EMOTE", 
        "QUIT", "SHUTDOWN", "VIEW", "RETIRE", "RETREAT","PANIC", "THROW", "EXTINGUISH", 
        "TELL", "SITMOVE", "KNOCK", "PRACTICE", "TEACH", "REMOVE", "EXPCHANGE", 
        "DAMAGE", "HEALING", "ROOMRESET", "RELOAD", "SNIFF", "ACTIVATE", "DEACTIVATE", 
        "FACTIONCHANGE", "LOGIN", "LEVEL", "EXAMINE"
    };
    public static final String[] MASK_DESCS={
        "HANDS","MOVE","EYES","MOUTH","SOUND","GENERAL","MAGIC","DELICATE","MALICIOUS","CHANNEL","OPTIMIZE"
    };

	// helpful message groupings
	public static final int MSK_CAST_VERBAL=MASK_SOUND|MASK_MOUTH|MASK_MAGIC;
	public static final int MSK_CAST_MALICIOUS_VERBAL=MASK_SOUND|MASK_MOUTH|MASK_MAGIC|MASK_MALICIOUS;
	public static final int MSK_CAST_SOMANTIC=MASK_HANDS|MASK_MAGIC;
	public static final int MSK_CAST_MALICIOUS_SOMANTIC=MASK_HANDS|MASK_MAGIC|MASK_MALICIOUS;
	public static final int MSK_HAGGLE=MASK_HANDS|MASK_SOUND|MASK_MOUTH;
	public static final int MSK_CAST=MSK_CAST_VERBAL|MSK_CAST_SOMANTIC;
	public static final int MSK_CAST_MALICIOUS=MSK_CAST_MALICIOUS_VERBAL|MSK_CAST_MALICIOUS_SOMANTIC;
	public static final int MSK_MALICIOUS_MOVE=MASK_MALICIOUS|MASK_MOVE|MASK_SOUND;

	// all major messages
	public static final int NO_EFFECT=0;
	public static final int MSG_AREAAFFECT=MASK_ALWAYS|TYP_AREAAFFECT;
	public static final int MSG_PUSH=MASK_HANDS|TYP_PUSH;
	public static final int MSG_PULL=MASK_HANDS|TYP_PULL;
	public static final int MSG_RECALL=MASK_SOUND|TYP_RECALL; // speak precludes animals
	public static final int MSG_OPEN=MASK_HANDS|TYP_OPEN;
	public static final int MSG_CLOSE=MASK_HANDS|TYP_CLOSE;
	public static final int MSG_PUT=MASK_HANDS|TYP_PUT;
	public static final int MSG_GET=MASK_HANDS|TYP_GET;
	public static final int MSG_UNLOCK=MASK_HANDS|TYP_UNLOCK;
	public static final int MSG_LOCK=MASK_HANDS|TYP_LOCK;
	public static final int MSG_WIELD=MASK_HANDS|TYP_WIELD;
	public static final int MSG_GIVE=MASK_HANDS|TYP_GIVE;
	public static final int MSG_BUY=MSK_HAGGLE|TYP_BUY;
	public static final int MSG_SELL=MSK_HAGGLE|TYP_SELL;
	public static final int MSG_DROP=MASK_HANDS|TYP_DROP;
	public static final int MSG_WEAR=MASK_HANDS|TYP_WEAR;
	public static final int MSG_FILL=MASK_HANDS|MASK_MOVE|MASK_SOUND|TYP_FILL;
	public static final int MSG_DELICATE_SMALL_HANDS_ACT=MASK_HANDS|MASK_DELICATE|TYP_DELICATE_HANDS_ACT;
	public static final int MSG_DELICATE_HANDS_ACT=MASK_HANDS|MASK_MOVE|MASK_DELICATE|TYP_DELICATE_HANDS_ACT;
	public static final int MSG_THIEF_ACT=MASK_HANDS|MASK_MOVE|MASK_DELICATE|TYP_JUSTICE;
	public static final int MSG_VALUE=MSK_HAGGLE|TYP_VALUE;
	public static final int MSG_HOLD=MASK_HANDS|TYP_HOLD;
	public static final int MSG_NOISYMOVEMENT=MASK_HANDS|MASK_SOUND|MASK_MOVE|TYP_NOISYMOVEMENT;
	public static final int MSG_QUIETMOVEMENT=MASK_HANDS|MASK_MOVE|TYP_QUIETMOVEMENT;
	public static final int MSG_RELOAD=MASK_HANDS|TYP_QUIETMOVEMENT;
	public static final int MSG_WEAPONATTACK=MASK_HANDS|MASK_MOVE|MASK_SOUND|MASK_MALICIOUS|TYP_WEAPONATTACK;
	public static final int MSG_LOOK=MASK_EYES|TYP_LOOK;
	public static final int MSG_READ=MASK_EYES|TYP_READ;
	public static final int MSG_NOISE=MASK_SOUND|TYP_NOISE;
	public static final int MSG_SPEAK=MASK_SOUND|MASK_MOUTH|TYP_SPEAK;
	public static final int MSG_CAST_VERBAL_SPELL=MSK_CAST_VERBAL|TYP_CAST_SPELL;
	public static final int MSG_LIST=MASK_SOUND|MASK_MOUTH|TYP_LIST;
	public static final int MSG_EAT=MASK_HANDS|MASK_MOUTH|TYP_EAT;
	public static final int MSG_ENTER=MASK_MOVE|MASK_SOUND|TYP_ENTER;
	public static final int MSG_CAST_ATTACK_VERBAL_SPELL=MSK_CAST_MALICIOUS_VERBAL|TYP_CAST_SPELL;
	public static final int MSG_LEAVE=MASK_MOVE|MASK_SOUND|TYP_LEAVE;
	public static final int MSG_SLEEP=MASK_MOVE|TYP_SLEEP;
	public static final int MSG_SIT=MASK_MOVE|TYP_SIT;
	public static final int MSG_STAND=MASK_MOVE|TYP_STAND;
	public static final int MSG_FLEE=MASK_MOVE|MASK_SOUND|TYP_FLEE;
	public static final int MSG_CAST_SOMANTIC_SPELL=MSK_CAST_SOMANTIC|TYP_CAST_SPELL;
	public static final int MSG_CAST_ATTACK_SOMANTIC_SPELL=MSK_CAST_MALICIOUS_SOMANTIC|TYP_CAST_SPELL;
	public static final int MSG_CAST=MSK_CAST|TYP_CAST_SPELL;
	public static final int MSG_CAST_MALICIOUS=MSK_CAST_MALICIOUS|TYP_CAST_SPELL;
	public static final int MSG_OK_ACTION=MASK_SOUND|MASK_ALWAYS|TYP_OK_ACTION;
	public static final int MSG_OK_VISUAL=MASK_ALWAYS|TYP_OK_VISUAL;
	public static final int MSG_DRINK=MASK_HANDS|MASK_MOUTH|TYP_DRINK;
	public static final int MSG_HANDS=MASK_HANDS|TYP_HANDS;
	public static final int MSG_EMOTE=MASK_SOUND|MASK_HANDS|TYP_EMOTE;
	public static final int MSG_FOLLOW=MASK_ALWAYS|TYP_FOLLOW;
	public static final int MSG_NOFOLLOW=MASK_ALWAYS|TYP_NOFOLLOW;
	public static final int MSG_WRITE=MASK_HANDS|TYP_WRITE;
	public static final int MSG_MOUNT=MASK_MOVE|MASK_SOUND|TYP_MOUNT;
	public static final int MSG_DISMOUNT=MASK_MOVE|MASK_SOUND|TYP_DISMOUNT;
	public static final int MSG_SERVE=MASK_MOUTH|MASK_SOUND|TYP_SERVE;
	public static final int MSG_REBUKE=MASK_MOUTH|MASK_SOUND|TYP_REBUKE;
	public static final int MSG_ADVANCE=MASK_MOVE|MASK_SOUND|MASK_MALICIOUS|TYP_ADVANCE;
	public static final int MSG_DEATH=MASK_SOUND|MASK_ALWAYS|TYP_DEATH;
	public static final int MSG_WITHDRAW=MASK_HANDS|TYP_WITHDRAW;
	public static final int MSG_DEPOSIT=MASK_HANDS|TYP_DEPOSIT;
	public static final int MSG_QUIT=MASK_ALWAYS|TYP_QUIT;
	public static final int MSG_SHUTDOWN=MASK_ALWAYS|TYP_SHUTDOWN;
	public static final int MSG_VIEW=MASK_SOUND|MASK_MOUTH|TYP_VIEW;
	public static final int MSG_RETIRE=MASK_ALWAYS|TYP_RETIRE;
	public static final int MSG_RETREAT=MASK_MOVE|MASK_SOUND|TYP_RETREAT;
	public static final int MSG_PANIC=MASK_MOVE|MASK_SOUND|TYP_PANIC;
	public static final int MSG_THROW=MASK_HANDS|MASK_SOUND|TYP_THROW;
	public static final int MSG_EXTINGUISH=MASK_HANDS|TYP_EXTINGUISH;
	public static final int MSG_TELL=MASK_ALWAYS|TYP_TELL;
	public static final int MSG_SITMOVE=MASK_MOVE|TYP_SITMOVE;
	public static final int MSG_KNOCK=MASK_HANDS|MASK_SOUND|TYP_KNOCK;
	public static final int MSG_PRACTICE=MASK_HANDS|MASK_SOUND|MASK_MOVE|TYP_PRACTICE;
	public static final int MSG_TEACH=MASK_HANDS|MASK_SOUND|MASK_MOUTH|MASK_MOVE|TYP_TEACH;
	public static final int MSG_REMOVE=MASK_HANDS|TYP_REMOVE;
	public static final int MSG_DAMAGE=MASK_ALWAYS|TYP_DAMAGE;
	public static final int MSG_HEALING=MASK_ALWAYS|TYP_HEALING;
	public static final int MSG_ROOMRESET=MASK_ALWAYS|TYP_ROOMRESET;
	public static final int MSG_SNIFF=MASK_HANDS|TYP_SNIFF;
	public static final int MSG_ACTIVATE=MASK_HANDS|TYP_ACTIVATE;
	public static final int MSG_DEACTIVATE=MASK_HANDS|TYP_DEACTIVATE;
    public static final int MSG_LOGIN=MASK_ALWAYS|TYP_LOGIN;
    public static final int MSG_LEVEL=MASK_ALWAYS|TYP_LEVEL;
    public static final int MSG_EXAMINE=MASK_EYES|TYP_EXAMINE;
	public static final int MSG_ORDER=MASK_SOUND|MASK_MOUTH|TYP_ORDER;
	public static final int MSG_EXPIRE=MASK_ALWAYS|TYP_EXPIRE;
}