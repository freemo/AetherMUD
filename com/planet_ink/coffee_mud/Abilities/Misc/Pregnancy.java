package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

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

public class Pregnancy extends StdAbility
{
	public String ID() { return "Pregnancy"; }
	public String name(){ return "Pregnancy";}
	protected long monthsRemaining=-1;
    protected long daysRemaining=-1;

	public String displayText()
	{
		int x=text().indexOf("/");
		if(x>0)
		{
			int y=text().indexOf("/",x+1);
			if(y<0) return "";
			long start=Util.s_long(text().substring(0,x));
			long divisor=MudHost.TICK_TIME*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
			long days=(System.currentTimeMillis()-start)/divisor; // down to days;
			long months=days/CMClass.globalClock().getDaysInMonth();
			if(days<1)
				return "(<1 day pregnant)";
			else
			if(months<1)
				return "("+days+" day(s) pregnant)";
			else
				return "("+months+" month(s) pregnant)";
		}
		return "";
	}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"IMPREGNATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
	public boolean isAutoInvoked(){return false;}
	public int classificationCode(){return Ability.PROPERTY;}
	private int ticksInLabor=0;


	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.target()==affected)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(Sense.canBeSeenBy(affected,msg.source()))
		&&(affected instanceof MOB)
		&&((daysRemaining>0)&&(monthsRemaining<=3)))
			msg.addTrailerMsg(new FullMsg(msg.source(),null,null,
										  CMMsg.MSG_OK_VISUAL,"\n\r"+affected.name()+" is obviously with child.\n\r",
										  CMMsg.NO_EFFECT,null,
										  CMMsg.NO_EFFECT,null));
		super.executeMsg(host,msg);
	}

	public Race mixRaces(MOB babe, Race race1, Race race2, String ID, String name)
	{
		Race GR=(Race)CMClass.getRace("GenRace").copyOf();
		GR.setRacialParms("<RACE><ID>"+ID+"</ID><NAME>"+name+"</NAME></RACE>");
		Race nonHuman=(race1.ID().equals("Human"))?race2:race1;
		Race otherRace=(nonHuman==race1)?race2:race1;
		GR.setStat("CAT",nonHuman.racialCategory());
		GR.setStat("BWEIGHT",""+((race1.lightestWeight()+race2.lightestWeight())/2));
		GR.setStat("VWEIGHT",""+((race1.weightVariance()+race2.weightVariance())/2));
		GR.setStat("MHEIGHT",""+((race1.shortestMale()+race2.shortestMale())/2));
		GR.setStat("FHEIGHT",""+((race1.shortestFemale()+race2.shortestFemale())/2));
		GR.setStat("VHEIGHT",""+((race1.heightVariance()+race2.heightVariance())/2));
		GR.setStat("PLAYER",""+CommonStrings.getIntVar(CommonStrings.SYSTEMI_MUDTHEME));
		GR.setStat("LEAVE",nonHuman.leaveStr());
		GR.setStat("ARRIVE",nonHuman.arriveStr());
		GR.setStat("HEALTHRACE","Human");
		int[] aging=(int[])race1.getAgingChart().clone();
		for(int i=0;i<aging.length;i++)
		    aging[i]+=race2.getAgingChart()[i];
		for(int i=0;i<aging.length;i++)
		    aging[i]=aging[i]/2;
		GR.setStat("AGING",Util.toStringList(aging));
		for(int i=0;i<Race.BODYPARTSTR.length;i++)
			if((race1.bodyMask()[i]>0)&&(race2.bodyMask()[i]>0))
				GR.bodyMask()[i]=((race1.bodyMask()[i]+race2.bodyMask()[i])/2);
			else
			if((race1.bodyMask()[i]<=0)&&(race2.bodyMask()[i]>=0))
				GR.bodyMask()[i]=race2.bodyMask()[i];
			else
				GR.bodyMask()[i]=race1.bodyMask()[i];

		EnvStats RS=(EnvStats)CMClass.getShared("DefaultEnvStats");
        RS.setAllValues(0);
		race1.affectEnvStats(babe,RS);
		race2.affectEnvStats(babe,RS);
		RS.setAbility(RS.ability()/2);
		RS.setArmor(RS.armor()/2);
		RS.setAttackAdjustment(RS.attackAdjustment()/2);
		RS.setDamage(RS.damage()/2);
		RS.setHeight(RS.height()/2);
		RS.setSpeed(RS.speed()/2.0);
		RS.setWeight(RS.weight()/2);
		RS.setRejuv(0);
		GR.setStat("ESTATS",CoffeeMaker.getEnvStatsStr(RS));

        CharStats S1=(CharStats)CMClass.getShared("DefaultCharStats"); S1.setAllValues(0);
        CharStats S2=(CharStats)CMClass.getShared("DefaultCharStats"); S2.setAllValues(10);
        CharStats S3=(CharStats)CMClass.getShared("DefaultCharStats"); S3.setAllValues(0);
        CharStats S4=(CharStats)CMClass.getShared("DefaultCharStats"); S4.setAllValues(10);
        CharStats SETSTAT=(CharStats)CMClass.getShared("DefaultCharStats"); SETSTAT.setAllValues(0);
        CharStats ADJSTAT=(CharStats)CMClass.getShared("DefaultCharStats"); ADJSTAT.setAllValues(0);
		race1.affectCharStats(babe,S1);
		race1.affectCharStats(babe,S2);
		race2.affectCharStats(babe,S3);
		race2.affectCharStats(babe,S4);
		for(int i=0;i<CharStats.NUM_STATS;i++)
		{
			if(i<CharStats.NUM_BASE_STATS)
			{
				if((S1.getStat(i)==S2.getStat(i))
				&&(S3.getStat(i)==S4.getStat(i)))
					SETSTAT.setStat(i,(S3.getStat(i)+S1.getStat(i))/2);
				else
				if(S1.getStat(i)==S2.getStat(i))
					SETSTAT.setStat(i,(10+S1.getStat(i))/2);
				else
				if(S3.getStat(i)==S4.getStat(i))
					SETSTAT.setStat(i,(10+S1.getStat(i))/2);
				else
					ADJSTAT.setStat(i,(S1.getStat(i)+S3.getStat(i))/2);
			}
			else
			if((i!=CharStats.GENDER)&&(i!=CharStats.AGE))
				ADJSTAT.setStat(i,(S1.getStat(i)+S3.getStat(i))/2);
		}
		GR.setStat("ASTATS",CoffeeMaker.getCharStatsStr(ADJSTAT));
		GR.setStat("CSTATS",CoffeeMaker.getCharStatsStr(SETSTAT));

        CharState CS=(CharState)CMClass.getShared("DefaultCharState"); CS.setAllValues(0);
		race1.affectCharState(babe,CS);
		race2.affectCharState(babe,CS);
		CS.setFatigue(CS.getFatigue()/2);
		CS.setHitPoints(CS.getHitPoints()/2);
		CS.setHunger(CS.getHunger()/2);
		CS.setMana(CS.getMana()/2);
		CS.setMovement(CS.getMovement()/2);
		CS.setThirst(CS.getThirst()/2);
		GR.setStat("ASTATE",CoffeeMaker.getCharStateStr(CS));

        CharState STARTCS=(CharState)CMClass.getShared("DefaultCharState"); STARTCS.setAllValues(0);
		race1.affectCharState(babe,STARTCS);
		race2.affectCharState(babe,STARTCS);
		CS.setFatigue(STARTCS.getFatigue()/2);
		CS.setHitPoints(STARTCS.getHitPoints()/2);
		CS.setHunger(STARTCS.getHunger()/2);
		CS.setMana(STARTCS.getMana()/2);
		CS.setMovement(STARTCS.getMovement()/2);
		CS.setThirst(STARTCS.getThirst()/2);
		GR.setStat("STARTASTATE",CoffeeMaker.getCharStateStr(STARTCS));

		GR.setStat("DISFLAGS",""+(Util.s_int(race1.getStat("DISFLAGS"))|Util.s_int(race2.getStat("DISFLAGS"))));

		GR.setStat("NUMRSC","");
		for(int i=0;i<nonHuman.myResources().size();i++)
			GR.setStat("GETRSCID"+i,((Item)nonHuman.myResources().elementAt(i)).ID());
		for(int i=0;i<nonHuman.myResources().size();i++)
			GR.setStat("GETRSCPARM"+i,((Item)nonHuman.myResources().elementAt(i)).text());

		GR.setStat("NUMOFT","");
		Race outfitRace=(nonHuman.outfit()!=null)?nonHuman:otherRace;
		if(outfitRace.outfit()!=null)
		{
			for(int i=0;i<outfitRace.outfit().size();i++)
				GR.setStat("GETOFTID"+i,((Item)outfitRace.outfit().elementAt(i)).ID());
			for(int i=0;i<outfitRace.outfit().size();i++)
				GR.setStat("GETOFTPARM"+i,((Item)outfitRace.outfit().elementAt(i)).text());
		}

		race1.racialAbilities(null);
		race2.racialAbilities(null);
		Vector data1=CMAble.getUpToLevelListings(race1.ID(),Integer.MAX_VALUE,true,false);
		Vector data2=CMAble.getUpToLevelListings(race2.ID(),Integer.MAX_VALUE,true,false);
		// kill half of them.
		for(int i=1;i<data1.size();i++)
			data1.removeElementAt(i);
		for(int i=1;i<data2.size();i++)
			data2.removeElementAt(i);

		if((data1.size()+data2.size())>0)
			GR.setStat("NUMRABLE",""+(data1.size()+data2.size()));
		else
			GR.setStat("NUMRABLE","");
		for(int i=0;i<data1.size();i++)
		{
			GR.setStat("GETRABLE"+i,(String)data1.elementAt(i));
			GR.setStat("GETRABLELVL"+i,""+CMAble.getQualifyingLevel(race1.ID(),false,(String)data1.elementAt(i)));
			GR.setStat("GETRABLEQUAL"+i,""+(!CMAble.getDefaultGain(race1.ID(),false,(String)data1.elementAt(i))));
			GR.setStat("GETRABLEPROF"+i,""+CMAble.getDefaultProfficiency(race1.ID(),false,(String)data1.elementAt(i)));
		}
		for(int i=0;i<data2.size();i++)
		{
			GR.setStat("GETRABLE"+(i+data1.size()),(String)data2.elementAt(i));
			GR.setStat("GETRABLELVL"+(i+data1.size()),""+CMAble.getQualifyingLevel(race2.ID(),false,(String)data2.elementAt(i)));
			GR.setStat("GETRABLEQUAL"+(i+data1.size()),""+(!CMAble.getDefaultGain(race2.ID(),false,(String)data2.elementAt(i))));
			GR.setStat("GETRABLEPROF"+(i+data1.size()),""+CMAble.getDefaultProfficiency(race2.ID(),false,(String)data2.elementAt(i)));
		}

		data1=race1.racialEffects(null);
		data2=race2.racialEffects(null);
		// kill half of them.
		for(int i=1;i<data1.size();i++)
			data1.removeElementAt(i);
		for(int i=1;i<data2.size();i++)
			data2.removeElementAt(i);

		if((data1.size()+data2.size())>0)
			GR.setStat("NUMREFF",""+(data1.size()+data2.size()));
		else
			GR.setStat("NUMREFF","");
		for(int i=0;i<data1.size();i++)
		{
			GR.setStat("GETREFF"+i,(String)data1.elementAt(i));
			GR.setStat("GETREFFLVL"+i,""+CMAble.getQualifyingLevel(race1.ID(),false,(String)data1.elementAt(i)));
			GR.setStat("GETREFFPARM"+i,""+CMAble.getDefaultProfficiency(race1.ID(),false,(String)data1.elementAt(i)));
		}
		for(int i=0;i<data2.size();i++)
		{
			GR.setStat("GETREFF"+(i+data1.size()),(String)data2.elementAt(i));
			GR.setStat("GETREFFLVL"+(i+data1.size()),""+CMAble.getQualifyingLevel(race2.ID(),false,(String)data2.elementAt(i)));
			GR.setStat("GETREFFPARM"+(i+data1.size()),""+CMAble.getDefaultProfficiency(race2.ID(),false,(String)data2.elementAt(i)));
		}

		CMClass.addRace(GR);
		CMClass.DBEngine().DBCreateRace(GR.ID(),GR.racialParms());
		return GR;
	}

	public Race getRace(MOB babe, String race1, String race2)
	{
		if(race1.indexOf(race2)>=0)
			return CMClass.getRace(race1);
		else
		if(race2.indexOf(race1)>=0)
			return CMClass.getRace(race2);

		Race R=null;
		if(race1.equalsIgnoreCase("Human")||race2.equalsIgnoreCase("Human"))
		{
			String halfRace=(race1.equalsIgnoreCase("Human")?race2:race1);
			R=CMClass.getRace(halfRace);
			if((R!=null)&&(!R.ID().toUpperCase().startsWith("HALF")))
			{
				halfRace="Half"+Util.capitalizeAndLower(R.ID().toLowerCase());
				Race testR=CMClass.getRace(halfRace);
				if(testR!=null)
					R=testR;
				else
					R=mixRaces(babe,R,CMClass.getRace("Human"),halfRace,"Half "+Util.capitalizeAndLower(R.name()));
			}
		}
		else
		if(race1.equalsIgnoreCase("Halfling")||race2.equalsIgnoreCase("Halfling"))
		{
			String halfRace=(race1.equalsIgnoreCase("Halfling")?race2:race1);
			R=CMClass.getRace(halfRace);
			if((R!=null)&&(!R.ID().endsWith("ling")))
			{
				halfRace=R.ID()+"ling";
				Race testR=CMClass.getRace(halfRace);
				if(testR!=null)
					R=testR;
				else
					R=mixRaces(babe,R,CMClass.getRace("Halfling"),halfRace,Util.capitalizeAndLower(R.name())+"ling");
			}
		}
		else
		{
			String first=null;
			if(race1.length()==race2.length())
				first=(race1.compareToIgnoreCase(race2)<0)?race1:race2;
			else
			if(race1.length()>race2.length())
				first=race1;
			else
				first=race2;
			String second=(first.equals(race1)?race2:race1);
			String halfRace=(race1.compareToIgnoreCase(race2)<0)?race1+race2:race2+race1;
			Race testR=CMClass.getRace(halfRace);
			Race FIRSTR=CMClass.getRace(first);
			Race SECONDR=CMClass.getRace(second);
			if(testR!=null)
				R=testR;
			else
				R=mixRaces(babe,
						   FIRSTR,
						   SECONDR,
						   halfRace,
						   FIRSTR.name()+"-"+SECONDR.name());
		}
		return R;
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((tickID==MudHost.TICK_MOB)
		&&(affected!=null)
		&&(affected instanceof MOB)
		&&(Sense.isInTheGame(affected,true)))
		{
			MOB mob=(MOB)affected;
			int x=text().indexOf("/");
			if(x>0)
			{
				int y=text().indexOf("/",x+1);
				if(y>x)
				{
					int z=text().indexOf("/",y+1);
					long end=Util.s_long(text().substring(x+1,y));
					long divisor=MudHost.TICK_TIME*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
					daysRemaining=(end-System.currentTimeMillis())/divisor; // down to days
					monthsRemaining=daysRemaining/CMClass.globalClock().getDaysInMonth(); // down to months
					if(daysRemaining<7) // BIRTH!
					{
						if(Sense.isSleeping(mob))
							mob.enqueCommand(Util.parse("WAKE"),0);
						if((Dice.rollPercentage()>50)&&(mob.charStats().getStat(CharStats.INTELLIGENCE)>5))
							mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> moan(s) and scream(s) in labor pain!!");
						ticksInLabor++;
						if(ticksInLabor>=45)
						{
							ticksInLabor=0;
							String race1=mob.baseCharStats().getMyRace().ID();
							char gender='F';
							String sondat="daughter";
							if(Dice.rollPercentage()>50){
								gender='M';
								sondat="son";
							}
							String desc="The "+sondat+" of "+mob.Name();
							String race2=mob.baseCharStats().getMyRace().ID();
							if(z>y)
							{
								race2=text().substring(z+1).trim();
								desc+=" and "+text().substring(y+1,z);
							}
							desc+=".";
							mob.curState().setMovement(0);
							mob.curState().setHitPoints(mob.curState().getHitPoints()/2);
							mob.location().show(mob,null,CMMsg.MSG_NOISE,"***** "+mob.name().toUpperCase()+" GIVE(S) BIRTH ******");
							if(Dice.rollPercentage()>5)
							{
								Ability A=mob.fetchEffect(ID());
								while(A!=null){
									mob.delEffect(A);
								    A.setAffectedOne(null);
									A=mob.fetchEffect(ID());
								}
								A=mob.fetchAbility(ID());
								while(A!=null){
									mob.delAbility(A);
								    A.setAffectedOne(null);
									A=mob.fetchAbility(ID());
								}
							}
							MOB babe=CMClass.getMOB("GenMOB");
							Race R=getRace(babe,race1,race2);
							if(R==null) R=mob.baseCharStats().getMyRace();
							String name="a baby "+((gender=='M')?"boy":"girl")+" "+R.name().toLowerCase();
							babe.setName(name);
							Factions.setAlignment(babe,Faction.ALIGN_GOOD);
							babe.setClanID(mob.getClanID());
							babe.setLiegeID(mob.getLiegeID());
							babe.setDescription(desc);
							babe.setDisplayText(name+" is here");
							BeanCounter.clearZeroMoney(babe,null);
							babe.baseCharStats().setMyRace(R);
							babe.baseCharStats().setStat(CharStats.CHARISMA,10);
							babe.baseCharStats().setStat(CharStats.CONSTITUTION,6);
							babe.baseCharStats().setStat(CharStats.DEXTERITY,2);
							babe.baseCharStats().setStat(CharStats.GENDER,gender);
							babe.baseCharStats().setStat(CharStats.INTELLIGENCE,2);
							babe.baseCharStats().setStat(CharStats.STRENGTH,1);
							babe.baseCharStats().setStat(CharStats.WISDOM,1);
							babe.baseCharStats().getMyRace().startRacing(babe,false);
							babe.baseEnvStats().setHeight(babe.baseEnvStats().height()/10);
							babe.baseEnvStats().setWeight(babe.baseEnvStats().weight()/10);
							babe.baseState().setHitPoints(1);
							babe.baseState().setMana(0);
							babe.baseState().setMovement(0);
							if(Dice.rollPercentage()>50)
							{
							    Ability A=mob.fetchEffect("Allergies");
							    if(A!=null)
							    {
							        A=(Ability)A.copyOf();
							        babe.addNonUninvokableEffect(A);
							    }
							    else
							    {
							        A=CMClass.getAbility("Allergies");
							        if(A!=null) A.invoke(babe,babe,true,0);
							    }
							}
							Ability STAT=CMClass.getAbility("Prop_StatTrainer");
							if(STAT!=null)
							{
								STAT.setMiscText("CHA=10 CON=6 DEX=2 INT=2 STR=1 WIS=1");
								babe.addNonUninvokableEffect(STAT);
							}
							Ability A3=CMClass.getAbility("Age");
							if(A3!=null)
							{
								A3.setMiscText(""+System.currentTimeMillis());
								babe.addNonUninvokableEffect(A3);
							}
							babe.recoverCharStats();
							babe.recoverEnvStats();
							babe.recoverMaxState();
							babe.resetToMaxState();
							Item I=CMClass.getItem("GenCaged");
							((CagedAnimal)I).cageMe(babe);
							I.baseEnvStats().setAbility(1);
							I.addNonUninvokableEffect(A3);
							I.recoverEnvStats();
							mob.location().addItem(I);
							Behavior B=CMClass.getBehavior("Emoter");
							B.setParms(Age.happyBabyEmoter);
							I.addBehavior(B);
							I.text();
							if((!mob.isMonster())&&(mob.soulMate()==null))
							{
								CoffeeTables.bump(mob,CoffeeTables.STAT_BIRTHS);
								if((Dice.rollPercentage()<20)&&(mob.fetchEffect("Disease_Depression")==null))
								{
								    Ability A=CMClass.getAbility("Diease_Depression");
								    if(A!=null) A.invoke(mob,mob,true,0);
								}
							}
                            Vector channels=ChannelSet.getFlaggedChannelNames("BIRTHS");
                            for(int i=0;i<channels.size();i++)
                                CommonMsgs.channel(mob,(String)channels.elementAt(i),mob.name()+" has just given birth to "+I.name()+"!",true);
						}
						else
							mob.tell("You are in labor!!");

					}
					else
					{
						// pregnant folk get fatigued more often.
						mob.curState().adjFatigue(monthsRemaining*100,mob.maxState());
						if((monthsRemaining<=1)&&(Dice.rollPercentage()==1))
						{
							if(Sense.isSleeping(mob))
								mob.enqueCommand(Util.parse("WAKE"),0);
							mob.tell("Oh! You had a contraction!");
						}
						else
						if((monthsRemaining<=3)&&(Dice.rollPercentage()==1)&&(Dice.rollPercentage()==1))
							mob.tell("You feel a kick in your gut.");
						else
						if((monthsRemaining>8)&&(mob.location()!=null)&&(mob.location().getArea().getTimeObj().getTimeOfDay()<2)&&(Dice.rollPercentage()==1))
						{
							if(Dice.rollPercentage()>25)
								mob.tell("You feel really sick this morning.");
							else
								mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"**BLEH** <S-NAME> just threw up.");
						}
					}
				}
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);
		long start=System.currentTimeMillis();
		Race R=mob.charStats().getMyRace();
		long tickspermudmonth=CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
		tickspermudmonth=tickspermudmonth*CMClass.globalClock().getDaysInMonth();
		int birthmonths=(int)Math.round(Util.mul((R.getAgingChart()[1]-R.getAgingChart()[0])*CMClass.globalClock().getMonthsInYear(),0.8335));
		if(birthmonths<=0) birthmonths=5;
		long ticksperbirthperiod=tickspermudmonth*birthmonths;
		long millisperbirthperiod=ticksperbirthperiod*MudHost.TICK_TIME;

		long end=start+millisperbirthperiod;
		if(success)
		{
			if(!auto)
			{
				end=start;
				start-=millisperbirthperiod;
			}
			if(mob.location().show(mob,target,this,CMMsg.TYP_GENERAL,auto?null:"<S-NAME> imgregnate(s) <T-NAMESELF>."))
			{
				setMiscText(start+"/"+end+"/"+mob.Name()+"/"+mob.charStats().getMyRace().ID());
                Vector channels=ChannelSet.getFlaggedChannelNames("CONCEPTIONS");
                for(int i=0;i<channels.size();i++)
                    CommonMsgs.channel((String)channels.elementAt(i),mob.getClanID(),target.name()+" is now in a 'family way'.",true);
                target.addNonUninvokableEffect((Ability)copyOf());
			}
		}
		else
		if(!auto)
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to impregnate <T-NAMESELF>, but fail(s)!");
		return success;
	}
}
