package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_EnterAdjuster extends Property
{
	public String ID() { return "Prop_EnterAdjuster"; }
	public String name(){ return "Room entering adjuster";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	public Environmental newInstance()
	{	Prop_EnterAdjuster newOne=new Prop_EnterAdjuster(); newOne.setMiscText(text());return newOne; }

	public String accountForYourself()
	{ return "Goodies for entry.";	}

	public void eatIfAble(MOB mob)
	{
		String names=text();
		Vector theSpells=new Vector();
		int del=names.indexOf(";");
		while(del>=0)
		{
			String thisOne=names.substring(0,del);
			if((thisOne.length()>0)&&(!thisOne.equals(";")))
			{
				Ability A=(Ability)CMClass.getAbility(thisOne);
				if(A!=null)
				{
					A=(Ability)A.copyOf();
					theSpells.addElement(A);
				}
			}
			names=names.substring(del+1);
			del=names.indexOf(";");
		}
		if((names.length()>0)&&(!names.equals(";")))
		{
			Ability A=(Ability)CMClass.getAbility(names);
			if(A!=null)
			{
				A=(Ability)A.copyOf();
				theSpells.addElement(A);
			}
		}
		for(int i=0;i<theSpells.size();i++)
		{
			Ability thisOne=(Ability)((Ability)theSpells.elementAt(i)).copyOf();
			thisOne.invoke(mob,mob,true);
		}
	}

	public void EATME(MOB mob)
	{
		eatIfAble(mob);
		String readableText=text();
		mob.baseEnvStats().setAbility(mob.baseEnvStats().ability()+getVal(readableText,"abi"));
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()+getVal(readableText,"arm"));
		mob.baseEnvStats().setAttackAdjustment(mob.baseEnvStats().attackAdjustment()+getVal(readableText,"att"));
		mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()+getVal(readableText,"dam"));
		mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|getVal(readableText,"dis"));
		mob.baseEnvStats().setLevel(mob.baseEnvStats().level()+getVal(readableText,"lev"));
		mob.baseEnvStats().setRejuv(mob.baseEnvStats().rejuv()+getVal(readableText,"rej"));
		mob.baseEnvStats().setSensesMask(mob.baseEnvStats().sensesMask()|getVal(readableText,"sen"));
		mob.baseEnvStats().setSpeed(mob.baseEnvStats().speed()+getVal(readableText,"spe"));
		mob.baseEnvStats().setWeight(mob.baseEnvStats().weight()+getVal(readableText,"wei"));
		mob.baseEnvStats().setHeight(mob.baseEnvStats().height()+getVal(readableText,"hei"));

		mob.baseCharStats().setStat(CharStats.CHARISMA,mob.baseCharStats().getStat(CharStats.CHARISMA)+getVal(readableText,"cha"));
		mob.baseCharStats().setStat(CharStats.CONSTITUTION,mob.baseCharStats().getStat(CharStats.CONSTITUTION)+getVal(readableText,"con"));
		mob.baseCharStats().setStat(CharStats.DEXTERITY,mob.baseCharStats().getStat(CharStats.DEXTERITY)+getVal(readableText,"dex"));
		String val=getStr(readableText,"gen").toUpperCase();
		if((val.length()>0)&&((val.charAt(0)=='M')||(val.charAt(0)=='F')||(val.charAt(0)=='N')))
			mob.baseCharStats().setStat(CharStats.GENDER,(int)val.charAt(0));
		mob.baseCharStats().setStat(CharStats.INTELLIGENCE,mob.baseCharStats().getStat(CharStats.INTELLIGENCE)+getVal(readableText,"int"));
		val=getStr(readableText,"cla").toUpperCase();
		if((val.length()>0)&&(CMClass.getCharClass(val)!=null))
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass(val));
		val=getStr(readableText,"rac").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
			mob.baseCharStats().setMyRace(CMClass.getRace(val));
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		mob.baseCharStats().setStat(CharStats.STRENGTH,mob.baseCharStats().getStat(CharStats.STRENGTH)+getVal(readableText,"str"));
		mob.baseCharStats().setStat(CharStats.WISDOM,mob.baseCharStats().getStat(CharStats.WISDOM)+getVal(readableText,"wis"));
		if(getVal(readableText,"lev")!=0)
			mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())+getVal(readableText,"lev"));

		mob.baseState().setHitPoints(mob.curState().getHitPoints()+getVal(readableText,"hit"));
		mob.curState().setHunger(mob.curState().getHunger()+getVal(readableText,"hun"));
		mob.curState().setMana(mob.curState().getMana()+getVal(readableText,"man"));
		mob.curState().setMovement(mob.curState().getMovement()+getVal(readableText,"mov"));
		mob.curState().setThirst(mob.curState().getThirst()+getVal(readableText,"thi"));

		mob.setPractices(mob.getPractices()+getVal(readableText,"prac"));
		mob.setTrains(mob.getTrains()+getVal(readableText,"trai"));
		mob.setQuestPoint(mob.getQuestPoint()+getVal(readableText,"ques"));
		mob.setMoney(mob.getMoney()+getVal(readableText,"coin"));
		int exp=getVal(readableText,"expe");
		if(exp>0) mob.charStats().getCurrentClass().gainExperience(mob,null,mob.getLeigeID(),exp,false);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.confirmWearability();
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)&&((affected instanceof Room)||(affected instanceof Exit)))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_ENTER:
				EATME(affect.source());
				break;
			}
		}
		return super.okAffect(myHost,affect);
	}
}
