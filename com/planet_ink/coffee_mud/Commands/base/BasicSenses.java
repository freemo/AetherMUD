package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BasicSenses
{
	private BasicSenses(){}
	public static void look(MOB mob, Vector commands, boolean quiet)
	{
		String textMsg="<S-NAME> look(s) ";
		if(mob.location()==null) return;
		if((commands!=null)&&(commands.size()>1))
		{
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("at")))
			   commands.removeElementAt(1);
			else
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("to")))
			   commands.removeElementAt(1);
			String ID=Util.combine(commands,1);

			if((ID.toUpperCase().startsWith("EXIT")&&(commands.size()==2)))
			{
				mob.location().listExits(mob);
				return;
			}
			if(ID.equalsIgnoreCase("SELF"))
				ID=mob.name();
			Environmental thisThang=null;
			int dirCode=Directions.getGoodDirectionCode(ID);
			if(dirCode>=0)
			{
				Room room=mob.location().getRoomInDir(dirCode);
				Exit exit=mob.location().getExitInDir(dirCode);
				if((room!=null)&&(exit!=null))
					thisThang=exit;
				else
				{
					mob.tell("You don't see anything that way.");
					return;
				}
			}
			if(dirCode<0)
			{
				thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID,Item.WORN_REQ_ANY);
				if((thisThang==null)
				&&(commands.size()>2)
				&&(((String)commands.elementAt(1)).equalsIgnoreCase("in")))
				{
					commands.removeElementAt(1);
					String ID2=Util.combine(commands,1);
					thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID2,Item.WORN_REQ_ANY);
					if((thisThang!=null)&&((!(thisThang instanceof Container))||(((Container)thisThang).capacity()==0)))
					{
						mob.tell("That's not a container.");
						return;
					}
				}
			}
			if(thisThang!=null)
			{
				String name="at <T-NAMESELF>.";
 				if((thisThang instanceof Room)||(thisThang instanceof Exit))
				{
					if(thisThang==mob.location())
						name="around";
					else
					if(dirCode>=0)
						name=Directions.getDirectionName(dirCode);
				}
				FullMsg msg=new FullMsg(mob,thisThang,null,Affect.MSG_EXAMINESOMETHING,textMsg+name);
				if(mob.location().okAffect(mob,msg))
					mob.location().send(mob,msg);
				if((thisThang instanceof Room)&&(Util.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS)))
					((Room)thisThang).listExits(mob);
			}
			else
				mob.tell("You don't see that here!");
		}
		else
		{
			if((commands!=null)&&(commands.size()>0))
				if(((String)commands.elementAt(0)).toUpperCase().startsWith("E"))
				{
					mob.tell("Examine what?");
					return;
				}

			FullMsg msg=new FullMsg(mob,mob.location(),null,Affect.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"around"),Affect.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"at you."),Affect.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"around"));
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
			if((Util.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS))
			&&(Sense.canBeSeenBy(mob.location(),mob)))
				mob.location().listExits(mob);
		}
	}

	public static void wimpy(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Change your wimp level to what?");
			return;
		}
		mob.setWimpHitPoint(Util.s_int(Util.combine(commands,1)));
		mob.tell("Your wimp level has been changed to "+mob.getWimpHitPoint()+" hit points.");
	}

	public static void description(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Change your description to what?");
			return;
		}
		String s=Util.combine(commands,1);
		if(s.length()>255)
			mob.tell("Your description exceeds 255 characters in length.  Please re-enter a shorter one.");
		else
		{
			mob.setDescription(s);
			mob.tell("Your description has been changed.");
		}
	}

	public static void password(MOB mob, Vector commands)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return;
		if(commands.size()<2)
		{
			mob.tell("Change your password to what?");
			return;
		}
		pstats.setPassword(Util.combine(commands,1));
		mob.tell("Your password has been changed.");
	}

	public static void emote(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("emote what?");
			return;
		}
		String emote="^E<S-NAME> "+Util.combine(commands,1)+"^?";
		FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_EMOTE,emote);
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}

	public static void mundaneTake(MOB mob, Vector commands)
	{
		if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("off"))
		{
			commands.removeElementAt(commands.size()-1);
			ItemUsage.remove(mob,commands);
		}
		else
		if((commands.size()>1)&&(((String)commands.elementAt(1)).equalsIgnoreCase("off")))
		{
			commands.removeElementAt(1);
			ItemUsage.remove(mob,commands);
		}
		else
			ItemUsage.get(mob,commands);
	}

	public static void train(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("You have "+mob.getTrains()+" training sessions. Enter HELP TRAIN for more information.");
			return;
		}
		commands.removeElementAt(0);

		String abilityName=((String)commands.elementAt(0)).toUpperCase();
		int abilityCode=mob.charStats().getCode(abilityName);
		CharClass theClass=null;
		if((!CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("NO"))
		&&(abilityCode<0))
		{
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				if(C.name().toUpperCase().startsWith(abilityName.toUpperCase()))
				{
					if(C.qualifiesForThisClass(mob,false))
					{
						abilityCode=106;
						theClass=C;
					}
					break;
				}
			}
		}

		if(abilityCode<0)
		{
			if("HIT POINTS".startsWith(abilityName.toUpperCase()))
				abilityCode=101;
			else
			if("MANA".startsWith(abilityName.toUpperCase()))
				abilityCode=102;
			else
			if("MOVE".startsWith(abilityName.toUpperCase()))
				abilityCode=103;
			else
			if("GAIN".startsWith(abilityName.toUpperCase()))
				abilityCode=104;
			else
			if("PRACTICES".startsWith(abilityName.toUpperCase()))
				abilityCode=105;
			else
			{
				mob.tell("You don't seem to have "+abilityName+".");
				return;
			}
		}
		commands.removeElementAt(0);


		if(abilityCode==104)
		{
			if(mob.getPractices()<7)
			{
				mob.tell("You don't seem to have enough practices to do that.");
				return;
			}
		}
		else
		if(mob.getTrains()==0)
		{
			mob.tell("You don't seem to have enough training sessions to do that.");
			return;
		}

		MOB teacher=null;
		if(commands.size()>0)
		{
			teacher=mob.location().fetchInhabitant((String)commands.elementAt(0));
			if(teacher!=null) commands.removeElementAt(0);
		}
		if(teacher==null)
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB possTeach=mob.location().fetchInhabitant(i);
			if((possTeach!=null)&&(possTeach!=mob))
			{
				teacher=possTeach;
				break;
			}
		}
		if((teacher==null)||((teacher!=null)&&(!Sense.canBeSeenBy(teacher,mob))))
		{
			mob.tell("That person doesn't seem to be here.");
			return;
		}
		if(teacher==mob)
		{
			mob.tell("You cannot train with yourself!");
			return;
		}
		if(Util.bset(teacher.getBitmap(),MOB.ATT_NOTEACH))
		{
			mob.tell(teacher.name()+" is refusing to teach right now.");
			return;
		}
		if(Util.bset(mob.getBitmap(),MOB.ATT_NOTEACH))
		{
			mob.tell("You are refusing training at this time.");
			return;
		}

		if((abilityCode==106)
		&&(!teacher.charStats().getCurrentClass().baseClass().equals(mob.charStats().getCurrentClass().baseClass()))
		&&(teacher.charStats().getClassLevel(theClass)<1))
	    {
			if((!CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("MULTI")))
				mob.tell("You can only learn that from another "+mob.charStats().getCurrentClass().baseClass()+".");
			else
				mob.tell("You can only learn that from another "+theClass.name()+".");
			return;
		}

		int curStat=-1;
		if(abilityCode<100)
		{
			curStat=mob.baseCharStats().getStat(abilityCode);
			if(!mob.baseCharStats().getCurrentClass().canAdvance(mob,abilityCode))
			{
				mob.tell("You cannot train that any further.");
				return;
			}

			int teachStat=mob.baseCharStats().getStat(abilityCode);
			if(curStat>teachStat)
			{
				mob.tell("You can only train with someone whose score is higher than yours.");
				return;
			}
		}

		FullMsg msg=new FullMsg(teacher,mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> train(s) with <T-NAMESELF>.");
		if(!mob.location().okAffect(mob,msg))
			return;
		mob.location().send(mob,msg);
		switch(abilityCode)
		{
		case 0:
			mob.tell("You feel stronger!");
			mob.baseCharStats().setStat(CharStats.STRENGTH,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 1:
			mob.tell("You feel smarter!");
			mob.baseCharStats().setStat(CharStats.INTELLIGENCE,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 2:
			mob.tell("You feel more dextrous!");
			mob.baseCharStats().setStat(CharStats.DEXTERITY,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 3:
			mob.tell("You feel healthier!");
			mob.baseCharStats().setStat(CharStats.CONSTITUTION,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 4:
			mob.tell("You feel more charismatic!");
			mob.baseCharStats().setStat(CharStats.CHARISMA,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 5:
			mob.tell("You feel wiser!");
			mob.baseCharStats().setStat(CharStats.WISDOM,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 101:
			mob.tell("You feel even healthier!");
			mob.baseState().setHitPoints(mob.baseState().getHitPoints()+10);
			mob.maxState().setHitPoints(mob.maxState().getHitPoints()+10);
			mob.curState().setHitPoints(mob.curState().getHitPoints()+10);
			mob.setTrains(mob.getTrains()-1);
			break;
		case 102:
			mob.tell("You feel more powerful!");
			mob.baseState().setMana(mob.baseState().getMana()+20);
			mob.maxState().setMana(mob.maxState().getMana()+20);
			mob.curState().setMana(mob.curState().getMana()+20);
			mob.setTrains(mob.getTrains()-1);
			break;
		case 103:
			mob.tell("You feel more rested!");
			mob.baseState().setMovement(mob.baseState().getMovement()+20);
			mob.maxState().setMovement(mob.maxState().getMovement()+20);
			mob.curState().setMovement(mob.curState().getMovement()+20);
			mob.setTrains(mob.getTrains()-1);
			break;
		case 104:
			mob.tell("You feel more trainable!");
			mob.setTrains(mob.getTrains()+1);
			mob.setPractices(mob.getPractices()-7);
			break;
		case 105:
			mob.tell("You feel more educatable!");
			mob.setTrains(mob.getTrains()-1);
			mob.setPractices(mob.getPractices()+5);
			break;
		case 106:
			mob.tell("You have undergone "+theClass.name()+" training!");
			mob.setTrains(mob.getTrains()-1);
			mob.baseCharStats().setCurrentClass(theClass);
			mob.recoverCharStats();
			mob.charStats().getCurrentClass().startCharacter(mob,false,true);
			break;
		}
	}

	public static void outfit(MOB mob)
	{
		if(mob==null) return;
		if(mob.charStats()==null) return;
		CharClass C=mob.charStats().getCurrentClass();
		Race R=mob.charStats().getMyRace();
		if(C!=null) C.outfit(mob);
		if(R!=null) R.outfit(mob);
		Scoring.equipment(mob);
	}

	public static void autoExits(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOEXITS));
			mob.tell("Autoexits has been turned off.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOEXITS));
			mob.tell("Autoexits has been turned on.");
		}
	}

	public static void noteach(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_NOTEACH))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_NOTEACH));
			mob.tell("You may now teach, train, or learn.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_NOTEACH));
			mob.tell("You are no longer teaching, training, or learning.");
		}
	}

	public static void afk(MOB mob)
	{
		if(mob.session()==null) return;
		if(mob.session().afkFlag())
			mob.session().setAfkFlag(false);
		else
			mob.session().setAfkFlag(true);
	}

	public static void brief(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_BRIEF))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_BRIEF));
			mob.tell("Brief room descriptions are now off.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_BRIEF));
			mob.tell("Brief room descriptions are now on.");
		}
	}

	public static void autoweather(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOWEATHER))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOWEATHER));
			mob.tell("Weather descriptions are now off.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOWEATHER));
			mob.tell("Weather descriptions are now on.");
		}
	}

	public static void autoimprovement(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOIMPROVE))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOIMPROVE));
			mob.tell("Skill improvement notifications are now off.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_AUTOIMPROVE));
			mob.tell("Skill improvement notifications are now on.");
		}
	}

	public static void ansi(MOB mob, int WhatToDo)
	{
		if(!mob.isMonster())
		{
			switch(WhatToDo)
			{
			case -1:
			{
				if(Util.bset(mob.getBitmap(),MOB.ATT_ANSI))
				{
					mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_ANSI));
					mob.tell("ANSI colour disabled.\n\r");
				}
				else
				{
					mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_ANSI));
					mob.tell("^!ANSI^N ^Hcolour^N enabled.\n\r");
				}
			}
			break;
			case 0:
			{
				if(Util.bset(mob.getBitmap(),MOB.ATT_ANSI))
				{
					mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_ANSI));
					mob.tell("ANSI colour disabled.\n\r");
				}
				else
				{
					mob.tell("ANSI is already disabled.\n\r");
				}
			}
			break;
			case 1:
			{
				if(!Util.bset(mob.getBitmap(),MOB.ATT_ANSI))
				{
					mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_ANSI));
					mob.tell("^!ANSI^N ^Hcolour^N enabled.\n\r");
				}
				else
				{
					mob.tell("^!ANSI^N is ^Halready^N enabled.\n\r");
				}
			}
			break;
			}
		}
	}

	public static void sound(MOB mob, int WhatToDo)
	{
		if(!mob.isMonster())
		{
			switch(WhatToDo)
			{
			case -1:
			{
				if(Util.bset(mob.getBitmap(),MOB.ATT_SOUND))
				{
					mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_SOUND));
					mob.tell("MSP Sound/Music disabled.\n\r");
				}
				else
				{
					mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_SOUND));
					mob.tell("MSP Sound/Music enabled.\n\r");
				}
			}
			break;
			case 0:
			{
				if(Util.bset(mob.getBitmap(),MOB.ATT_SOUND))
				{
					mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_SOUND));
					mob.tell("MSP Sound/Music disabled.\n\r");
				}
				else
				{
					mob.tell("MSP Sound/Music is already disabled.\n\r");
				}
			}
			break;
			case 1:
			{
				if(!Util.bset(mob.getBitmap(),MOB.ATT_SOUND))
				{
					mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_SOUND));
					mob.tell("MSP Sound/Music enabled.\n\r");
				}
				else
				{
					mob.tell("MSP Sound/Music is already enabled.\n\r");
				}
			}
			break;
			}
		}
	}

	public static void weather(MOB mob, Vector commands)
	{
		Room room=mob.location();
		if(room==null) return;
		if((commands.size()>1)&&((room.domainType()&Room.INDOORS)==0)&&(((String)commands.elementAt(1)).equalsIgnoreCase("WORLD")))
		{
			StringBuffer tellMe=new StringBuffer("");
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(Sense.canAccess(mob,A))
					tellMe.append(Util.padRight(A.name(),20)+": "+A.weatherDescription(room)+"\n\r");
			}
			mob.tell(tellMe.toString());
			return;
		}
		mob.tell(room.getArea().weatherDescription(room));
	}
	public static void time(MOB mob, Vector commands)
	{
		Room room=mob.location();
		if(room==null) return;
		mob.tell(room.getArea().timeDescription(mob,room));
	}

	public static void wizlist(MOB mob, Vector commands)
	{
		StringBuffer head=new StringBuffer("");
		head.append("^x[");
		head.append(Util.padRight("Race",8)+" ");
		head.append(Util.padRight("Lvl",4)+" ");
		head.append(Util.padRight("Last",18)+" ");
		head.append("] Archon Character Name^.^?\n\r");
		mob.tell("^x["+Util.centerPreserve("The Archons of "+ExternalPlay.mudName(),head.length()-10)+"]^.^?");
		Vector allUsers=ExternalPlay.getUserList();
		for(int u=0;u<allUsers.size();u++)
		{
			Vector U=(Vector)allUsers.elementAt(u);
			if(((String)U.elementAt(1)).equals("Archon"))
			{
				head.append("[");
				head.append(Util.padRight((String)U.elementAt(2),8)+" ");
				head.append(Util.padRight((String)U.elementAt(3),4)+" ");
				head.append(Util.padRight(IQCalendar.d2String(Util.s_long((String)U.elementAt(5))),18)+" ");
				head.append("] "+Util.padRight((String)U.elementAt(0),25));
				head.append("\n\r");
			}
		}
		mob.tell(head.toString());
	}
}
