package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class GenSuperPill extends GenPill
{
	public String ID(){	return "GenSuperPill";}
	public GenSuperPill()
	{
		super();

		name="a pill";
		baseEnvStats.setWeight(1);
		displayText="An strange pill lies here.";
		description="Large and round, with strange markings.";
		secretIdentity="";
		baseGoldValue=200;
		recoverEnvStats();
		material=EnvResource.RESOURCE_CORN;
	}

	public Environmental newInstance()
	{
		return new GenSuperPill();
	}
	public boolean isGeneric(){return true;}

	public static int getVal(String text, String key)
	{
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
					x++;
				if(x<text.length())
				{
					char pm=text.charAt(x);
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						if(pm=='+')
							return Util.s_int(text.substring(0,x));
						else
							return -Util.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0;
	}

	public static String getStr(String text, String key)
	{
		String oldText=text;
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
					x++;
				if(x<text.length())
				{
					while((x<text.length())&&(!Character.isLetter(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						oldText=oldText.substring(x);
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isLetter(text.charAt(x))))
							x++;
						return oldText.substring(0,x).trim();
					}

				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return "";
	}

	public String secretIdentity()
	{
		String id=StdScroll.makeSecretIdentity("super pill",super.secretIdentity(),getSpells(this));
		return id;
	}

	public void EATME(MOB mob)
	{
		if(getSpells(this).size()>0)
			eatIfAble(mob,this);
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
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass(val).ID());
		val=getStr(readableText,"rac").toUpperCase();
		if((val.length()>0)&&(CMClass.getRace(val)!=null))
		{
			mob.baseCharStats().setMyRace(CMClass.getRace(val));
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		mob.baseCharStats().setStat(CharStats.STRENGTH,mob.baseCharStats().getStat(CharStats.STRENGTH)+getVal(readableText,"str"));
		mob.baseCharStats().setStat(CharStats.WISDOM,mob.baseCharStats().getStat(CharStats.WISDOM)+getVal(readableText,"wis"));

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
		if(exp>0) mob.charStats().getCurrentClass().gainExperience(mob,null,mob.getLeigeID(),exp);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.charStats().getMyRace().confirmGear(mob);
	}

	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_EAT:
				if((affect.sourceMessage()==null)&&(affect.othersMessage()==null))
				{
					EATME(mob);
					super.affect(affect);
				}
				else
					affect.addTrailerMsg(new FullMsg(affect.source(),affect.target(),affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
				break;
			default:
				super.affect(affect);
				break;
			}
		}
		else
			super.affect(affect);
	}
}
