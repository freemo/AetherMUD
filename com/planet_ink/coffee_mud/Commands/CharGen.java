package com.planet_ink.coffee_mud.Commands;
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
public class CharGen extends StdCommand
{
	public CharGen(){}

	private String[] access={getScr("CharGen","cmd")};
	public String[] getAccessWords(){return access;}

	protected MOB levelMOBup(int level, CharClass C)
	{
		MOB mob=CMClass.getMOB("StdMOB");
		CMLib.factions().setAlignment(mob,Faction.ALIGN_NEUTRAL);
		mob.setName(getScr("CharGen","stdmobname"));
		mob.baseCharStats().setMyRace(CMClass.getRace("Human"));
		mob.baseCharStats().setStat(CharStats.GENDER,'M');
		mob.baseCharStats().setStat(CharStats.STRENGTH,11);
		mob.baseCharStats().setStat(CharStats.WISDOM,11);
		mob.baseCharStats().setStat(CharStats.INTELLIGENCE,10);
		mob.baseCharStats().setStat(CharStats.DEXTERITY,11);
		mob.baseCharStats().setStat(CharStats.CONSTITUTION,10);
		mob.baseCharStats().setStat(CharStats.CHARISMA,10);
		mob.baseCharStats().setCurrentClass(C);
		mob.baseCharStats().setClassLevel(C,1);
		mob.baseEnvStats().setArmor(50);
		mob.baseEnvStats().setLevel(1);
		mob.baseEnvStats().setSensesMask(0);
		mob.baseState().setHitPoints(20);
		mob.baseState().setMovement(100);
		mob.baseState().setMana(100);
		mob.baseCharStats().getMyRace().startRacing(mob,false);
		CMLib.utensils().outfit(mob,mob.baseCharStats().getMyRace().outfit());
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);

		int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
		for(int lvl=1;lvl<level;lvl++)
		{
			switch(lvl % 6)
			{
			case 0:
				if(mob.baseCharStats().getStat(CharStats.STRENGTH)<(max+mob.baseCharStats().getStat(CharStats.MAX_STRENGTH_ADJ)+5))
					mob.baseCharStats().setStat(CharStats.STRENGTH,mob.baseCharStats().getStat(CharStats.STRENGTH)+1);
				break;
			case 1:
				if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<(max+mob.baseCharStats().getStat(CharStats.MAX_DEXTERITY_ADJ)+5))
					mob.baseCharStats().setStat(CharStats.DEXTERITY,mob.baseCharStats().getStat(CharStats.DEXTERITY)+1);
				break;
			case 2:
				if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<(max+mob.baseCharStats().getStat(CharStats.MAX_INTELLIGENCE_ADJ)))
					mob.baseCharStats().setStat(CharStats.INTELLIGENCE,mob.baseCharStats().getStat(CharStats.INTELLIGENCE)+1);
				break;
			case 3:
				if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<(max+mob.baseCharStats().getStat(CharStats.MAX_CONSTITUTION_ADJ)))
					mob.baseCharStats().setStat(CharStats.CONSTITUTION,mob.baseCharStats().getStat(CharStats.CONSTITUTION)+1);
				break;
			case 4:
				if(mob.baseCharStats().getStat(CharStats.CHARISMA)<(max+mob.baseCharStats().getStat(CharStats.MAX_CHARISMA_ADJ)))
					mob.baseCharStats().setStat(CharStats.CHARISMA,mob.baseCharStats().getStat(CharStats.CHARISMA)+1);
				break;
			case 5:
				if(mob.baseCharStats().getStat(CharStats.WISDOM)<(max+mob.baseCharStats().getStat(CharStats.MAX_WISDOM_ADJ)))
					mob.baseCharStats().setStat(CharStats.WISDOM,mob.baseCharStats().getStat(CharStats.WISDOM)+1);
				break;
			}
			int oldattack=mob.baseEnvStats().attackAdjustment();
			if(mob.getExpNeededLevel()==Integer.MAX_VALUE)
				mob.charStats().getCurrentClass().level(mob);
			else
				CMLib.combat().postExperience(mob,null,null,mob.getExpNeededLevel()+1,false);
			mob.recoverEnvStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
			int newAttack=mob.baseEnvStats().attackAdjustment()-oldattack;
			mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-newAttack);
			mob.recoverEnvStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
		}
		return mob;
	}

	public void averageout(MOB avgMob, int tries)
	{
		avgMob.baseCharStats().setStat(CharStats.STRENGTH,(int)Math.round(CMath.div(avgMob.baseCharStats().getStat(CharStats.STRENGTH),tries)));
		avgMob.baseCharStats().setStat(CharStats.WISDOM,(int)Math.round(CMath.div(avgMob.baseCharStats().getStat(CharStats.WISDOM),tries)));
		avgMob.baseCharStats().setStat(CharStats.INTELLIGENCE,(int)Math.round(CMath.div(avgMob.baseCharStats().getStat(CharStats.INTELLIGENCE),tries)));
		avgMob.baseCharStats().setStat(CharStats.DEXTERITY,(int)Math.round(CMath.div(avgMob.baseCharStats().getStat(CharStats.DEXTERITY),tries)));
		avgMob.baseCharStats().setStat(CharStats.CONSTITUTION,(int)Math.round(CMath.div(avgMob.baseCharStats().getStat(CharStats.CONSTITUTION),tries)));
		avgMob.baseCharStats().setStat(CharStats.CHARISMA,(int)Math.round(CMath.div(avgMob.baseCharStats().getStat(CharStats.CHARISMA),tries)));
		avgMob.baseEnvStats().setArmor((int)Math.round(CMath.div(avgMob.baseEnvStats().armor(),tries)));
		avgMob.baseState().setHitPoints((int)Math.round(CMath.div(avgMob.baseState().getHitPoints(),tries)));
		avgMob.baseState().setMovement((int)Math.round(CMath.div(avgMob.baseState().getMovement(),tries)));
		avgMob.baseState().setMana((int)Math.round(CMath.div(avgMob.baseState().getMana(),tries)));
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
		avgMob.setTrains(0);
	}

	public void addHimIn(MOB avgMob, MOB mob2)
	{
		avgMob.baseCharStats().setStat(CharStats.STRENGTH,avgMob.baseCharStats().getStat(CharStats.STRENGTH)+mob2.baseCharStats().getStat(CharStats.STRENGTH));
		avgMob.baseCharStats().setStat(CharStats.WISDOM,avgMob.baseCharStats().getStat(CharStats.WISDOM)+mob2.baseCharStats().getStat(CharStats.WISDOM));
		avgMob.baseCharStats().setStat(CharStats.INTELLIGENCE,avgMob.baseCharStats().getStat(CharStats.INTELLIGENCE)+mob2.baseCharStats().getStat(CharStats.INTELLIGENCE));
		avgMob.baseCharStats().setStat(CharStats.DEXTERITY,avgMob.baseCharStats().getStat(CharStats.DEXTERITY)+mob2.baseCharStats().getStat(CharStats.DEXTERITY));
		avgMob.baseCharStats().setStat(CharStats.CONSTITUTION,avgMob.baseCharStats().getStat(CharStats.CONSTITUTION)+mob2.baseCharStats().getStat(CharStats.CONSTITUTION));
		avgMob.baseCharStats().setStat(CharStats.CHARISMA,avgMob.baseCharStats().getStat(CharStats.CHARISMA)+mob2.baseCharStats().getStat(CharStats.CHARISMA));
		avgMob.baseEnvStats().setArmor(avgMob.baseEnvStats().armor()+mob2.baseEnvStats().armor());
		avgMob.baseState().setHitPoints(avgMob.baseState().getHitPoints()+mob2.baseState().getHitPoints());
		avgMob.baseState().setMovement(avgMob.baseState().getMovement()+mob2.baseState().getMovement());
		avgMob.baseState().setMana(avgMob.baseState().getMana()+mob2.baseState().getMana());
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
	}

	public MOB AverageClassMOB(MOB mob, int level, CharClass C, int numTries)
	{
		MOB avgMob=levelMOBup(level,C);
		int tries=0;
		for(;tries<numTries;tries++)
		{
			if((tries % 20)==0)
				mob.session().print(".");
			MOB mob2=levelMOBup(level,C);
			addHimIn(avgMob,mob2);
		}
		averageout(avgMob,tries);
		return avgMob;
	}

	public MOB AverageAllClassMOB(MOB mob, int level, int numTriesClass, int numTriesMOB)
	{
		MOB avgMob=null;
		int tries=0;
		int numClasses=0;
		for(;tries<numTriesClass;tries++)
		{
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				if(C.availabilityCode()!=0)
				{
					numClasses++;
					MOB mob2=AverageClassMOB(mob,level,C,numTriesMOB);
					if(avgMob==null)
					{
						avgMob=mob2;
						numClasses--;
					}
					else
						addHimIn(avgMob,mob2);
                    if(avgMob!=mob2) mob2.destroy();
				}
			}
		}
		averageout(avgMob,numClasses);
		return avgMob;
	}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.isMonster())
			return false;
		commands.removeElementAt(0);
		CharClass C=null;
		int level=-1;
		String ClassName="";
		if(commands.size()>0)
		{
			ClassName=(String)commands.elementAt(0);
			C=CMClass.findCharClass(ClassName);
			level=CMath.s_int(CMParms.combine(commands,1));
		}

		if((C==null)&&(ClassName.toUpperCase().indexOf("ALL")<0))
		{
			mob.tell(getScr("CharGen","enterall"));
			try
			{
				ClassName=mob.session().prompt(getScr("CharGen","entcname"+" "));
			}
			catch(Exception e){return false;}

			C=CMClass.findCharClass(ClassName);
			if((C==null)&&(ClassName.toUpperCase().indexOf("ALL")<0))
				return false;
		}

		if(level<=0)
		{
			try
			{
				level=CMath.s_int(mob.session().prompt(getScr("CharGen","entlevel")+" "));
			}
			catch(Exception e){return false;}
			if(level<=0)
				return false;
		}

		if(C!=null)
			mob.session().print(getScr("CharGen","average")+" "+C.name()+"...");
		else
			mob.session().print(getScr("CharGen","averages"));

		MOB avgMob=null;
		if(C!=null)
			avgMob=AverageClassMOB(mob, level,C, 100);
		else
			avgMob=AverageAllClassMOB(mob,level, 20, 40);

		mob.session().println("\n\r");

		if(avgMob!=null)
		{
			StringBuffer msg=CMLib.commands().getScore(avgMob);
			if(!mob.isMonster())
				mob.session().wraplessPrintln(msg.toString());
		}
        avgMob.destroy();
		return false;
	}
	public double actionsCost(){return 0.0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CHARGEN");}

	
}
