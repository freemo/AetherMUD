package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;


public class StdScroll extends StdItem implements MiscMagic, Scroll
{
	public String ID(){	return "StdScroll";}
	protected boolean readableScroll=false;
	protected Vector theSpells=new Vector();

	public StdScroll()
	{
		super();

		name="a scroll";
		baseEnvStats.setWeight(1);
		displayText="a scroll is rolled up here.";
		description="A rolled up parchment marked with mystical symbols.";
		secretIdentity="";
		material=EnvResource.RESOURCE_PAPER;
		baseGoldValue=200;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdScroll();
	}

	public int numSpells()
	{
		return theSpells.size();
	}

	public Vector getSpells()
	{
		return theSpells;
	}

	public String getScrollText()
	{
		return miscText;
	}

	public int value()
	{
		if(usesRemaining()<=0) 
			return 0;
		else 
			return super.value();
	}
	public boolean useTheScroll(Ability A, MOB mob)
	{
		int manaRequired=5;
		int q=CMAble.qualifyingLevel(mob,A);
		if(q>0)
		{
			if(q<CMAble.qualifyingClassLevel(mob,A))
				manaRequired=0;
			else
				manaRequired=5;
		}
		else
		{
			manaRequired=50;
		}
		if(manaRequired>mob.curState().getMana())
		{
			mob.tell("You don't have enough mana.");
			return false;
		}
		mob.curState().adjMana(-manaRequired,mob.maxState());
		return true;
	}

	public String secretIdentity()
	{
		return StdScroll.makeSecretIdentity("scroll",super.secretIdentity()," Charges: "+usesRemaining(),getSpells());
	}

	public static String makeSecretIdentity(String thang, String id, String more, Vector V)
	{
		StringBuffer add=new StringBuffer("");
		for(int v=0;v<V.size();v++)
		{
			if(v==0)
				add.append("A "+thang+" of ");
			Ability A=(Ability)V.elementAt(v);
			if(V.size()==1)
				add.append(A.displayName());
			else
			if(v==(V.size()-1))
				add.append("and "+A.displayName());
			else
				add.append(A.displayName()+", ");
		}
		if(add.length()>0)
		{
			add.append(more+"\n");
		}
		add.append(id);
		return add.toString();
	}
	public void readIfAble(MOB mob, Scroll me, String spellName)
	{
		if(mob.isMine(me))
		{
			boolean readingMagic=(mob.fetchAffect("Spell_ReadMagic")!=null);
			if(readingMagic)
			{
				mob.tell(displayName()+" glows softly.");
				me.setReadableScroll(true);
			}
			if(me.isReadableScroll())
			{
				if(me.usesRemaining()<=0)
					mob.tell("The markings have been read off the parchment, and are no longer discernable.");
				else
				{
					Vector Spells=me.getSpells();
					if(Spells.size()==0)
						mob.tell("The scroll appears to contain no discernable information.");
					else
					{
						Ability thisOne=null;
						Vector params=new Vector();
						if(spellName.length()>0)
						{
							spellName=spellName.trim();
							thisOne=(Ability)CoffeeUtensils.fetchEnvironmental(Spells,spellName,true);
							if(thisOne==null)
								thisOne=(Ability)CoffeeUtensils.fetchEnvironmental(Spells,spellName,false);
							while((thisOne==null)&&(spellName.length()>0))
							{

								int t=spellName.lastIndexOf(" ");
								if(t<0)
									spellName="";
								else
								{
									params.insertElementAt(spellName.substring(t).trim(),0);
									spellName=spellName.substring(0,t);
									thisOne=(Ability)CoffeeUtensils.fetchEnvironmental(Spells,spellName,true);
									if(thisOne==null)
										thisOne=(Ability)CoffeeUtensils.fetchEnvironmental(Spells,spellName,false);
								}
							}
						}

						if((thisOne!=null)&&(me.useTheScroll(thisOne,mob)))
						{
							thisOne=(Ability)thisOne.copyOf();
							thisOne.invoke(mob,params,null,true);
							me.setUsesRemaining(me.usesRemaining()-1);
						}
						else
						if(spellName.length()>0)
							mob.tell("That is not written on the scroll.");
						else
						if(!mob.isMonster())
						{
							StringBuffer theNews=new StringBuffer("The scroll contains the following spells:\n\r");
							for(int u=0;u<me.getSpells().size();u++)
							{
								Ability A=(Ability)me.getSpells().elementAt(u);
								theNews.append("Level "+Util.padRight(""+CMAble.lowestQualifyingLevel(A.ID()),2)+": "+A.displayName()+"\n\r");
							}
							mob.tell(theNews.toString());
						}
					}
				}
			}
			else
				mob.tell("The markings look magical, and are unknown to you.");
		}
	}

	public void parseSpells(Scroll me, String names)
	{
		int baseValue=200;
		Vector theSpells=new Vector();
		me.setSpellList(theSpells);
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
					baseValue+=(100*CMAble.lowestQualifyingLevel(A.ID()));
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
				baseValue+=(100*CMAble.lowestQualifyingLevel(A.ID()));
				theSpells.addElement(A);
			}
		}
		me.setBaseValue(baseValue);
		me.recoverEnvStats();
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_READSOMETHING:
				if((affect.sourceMessage()==null)&&(affect.othersMessage()==null))
					readIfAble(mob,this,affect.targetMessage());
				else
					affect.addTrailerMsg(new FullMsg(affect.source(),affect.target(),affect.tool(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
				return;
			default:
				break;
			}
		}
		super.affect(myHost,affect);
	}
	public void setMiscText(String newText)
	{
		miscText=newText;
		parseSpells(this,newText);
	}
	public void setScrollText(String text)
	{ setMiscText(text); }
	public void setSpellList(Vector newOne){theSpells=newOne;}
	public boolean isReadableScroll(){return readableScroll;}
	public void setReadableScroll(boolean isTrue){readableScroll=isTrue;}
	protected static String[] CODES={"CLASS","LEVEL","ABILITY","TEXT"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+baseEnvStats().ability();
		case 2: return ""+baseEnvStats().level();
		case 3: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: baseEnvStats().setLevel(Util.s_int(val)); break;
		case 2: baseEnvStats().setAbility(Util.s_int(val)); break;
		case 3: setMiscText(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdScroll)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
