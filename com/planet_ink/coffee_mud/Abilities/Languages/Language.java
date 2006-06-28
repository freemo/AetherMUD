package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Language extends StdAbility
{
	public String ID() { return "Language"; }
	public String name(){ return "Languages";}
	private static final String[] triggerStrings = {"SPEAK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected int trainsRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_LANGTRAINCOST);}
	protected int practicesRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_LANGPRACCOST);}
	public int classificationCode(){return Ability.ACODE_LANGUAGE;}

	private static Hashtable emptyHash=new Hashtable();
	private static Vector emptyVector=new Vector();
	protected boolean spoken=false;
	private final static String consonants="bcdfghjklmnpqrstvwxz";
	private final static String vowels="aeiouy";
	public boolean beingSpoken(){return spoken;}
	public void setBeingSpoken(boolean beingSpoken){spoken=beingSpoken;}
	public Hashtable translationHash(){ return emptyHash; }
	public Vector translationVector(){ return emptyVector; }
	public String displayText()
	{
		if(beingSpoken()) return "(Speaking "+name()+")";
		return "";
	}

	protected String fixCase(String like,String make)
	{
		StringBuffer s=new StringBuffer(make);
		char lastLike=' ';
		for(int x=0;x<make.length();x++)
		{
			if(x<like.length()) lastLike=like.charAt(x);
			s.setCharAt(x,fixCase(lastLike,make.charAt(x)));
		}
		return s.toString();
	}
	protected char fixCase(char like,char make)
	{
		if(Character.isUpperCase(like))
			return Character.toUpperCase(make);
		return Character.toLowerCase(make);
	}
	protected String translate(String word)
	{
		if(translationHash().containsKey(word.toUpperCase()))
			return fixCase(word,(String)translationHash().get(word.toUpperCase()));
		MOB M=CMLib.map().getPlayer(word);
		if(M!=null) return word;
		if(translationVector().size()>0)
		{
			String[] choices=null;
			try{ choices=(String[])translationVector().elementAt(word.length()-1);}catch(Exception e){}
			if(choices==null) choices=(String[])translationVector().lastElement();
			return choices[CMLib.dice().roll(1,choices.length,-1)];
		}
		return word;
	}

	protected String getMsgFromAffect(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end);
		return null;
	}
	protected String subStitute(String affmsg, String msg)
	{
		if(affmsg==null) return null;
		int start=affmsg.indexOf("'");
		int end=affmsg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
	}

	protected int numChars(String words)
	{
		int num=0;
		for(int i=0;i<words.length();i++)
		{
			if(Character.isLetter(words.charAt(i)))
				num++;
		}
		return num;
	}

	protected String messChars(String words, int numToMess)
	{
		numToMess=numToMess/2;
		if(numToMess==0) return words;
		StringBuffer w=new StringBuffer(words);
		while(numToMess>0)
		{
			int x=CMLib.dice().roll(1,words.length(),-1);
			char c=words.charAt(x);
			if(Character.isLetter(c))
			{
				if(vowels.indexOf(c)>=0)
					w.setCharAt(x,fixCase(c,vowels.charAt(CMLib.dice().roll(1,vowels.length(),-1))));
				else
					w.setCharAt(x,fixCase(c,consonants.charAt(CMLib.dice().roll(1,consonants.length(),-1))));
				numToMess--;
			}
		}
		return w.toString();
	}

    protected String scrambleAll(String str, int numToMess)
    {
        StringBuffer newStr=new StringBuffer("");
        int start=0;
        int end=0;
        int state=-1;
        while(start<=str.length())
        {
            char c='\0';
            if(end>=str.length())
                c=' ';
            else
                c=str.charAt(end);
            switch(state)
            {
            case -1:
                if(Character.isLetter(c))
                { state=0; end++;}
                else
                { newStr.append(c); end++;start=end;}
                break;
            case 0:
                if(Character.isLetter(c))
                { end++;}
                else
                if(Character.isDigit(c))
                { newStr.append(str.substring(start,end+1)); end++; start=end; state=1; }
                else
                { newStr.append(translate(str.substring(start,end))+c); end++; start=end; state=-1; }
                break;
            case 1:
                if(Character.isLetterOrDigit(c))
                { newStr.append(c); end++; start=end;}
                else
                { newStr.append(c); end++; start=end; state=-1; }
                break;
            }
        }
        return newStr.toString();
    }
    
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected instanceof MOB)&&(beingSpoken()))
		{
			if((msg.amISource((MOB)affected))
			&&(msg.sourceMessage()!=null)
			&&(msg.tool()==null)
			&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))))
			{
				String str=getMsgFromAffect(msg.othersMessage());
				if(str==null) str=getMsgFromAffect(msg.targetMessage());
				if(str!=null)
				{
	                String smsg=getMsgFromAffect(msg.sourceMessage());
	                int numToMess=(int)Math.round(CMath.mul(numChars(str),CMath.div(100-proficiency(),100)));
	                if(numToMess>0) smsg=messChars(smsg,numToMess);
	                str=scrambleAll(str,numToMess);
					msg.modify(msg.source(),
								  msg.target(),
								  this,
								  msg.sourceCode(),
								  subStitute(msg.sourceMessage(),smsg),
								  msg.targetCode(),
								  subStitute(msg.targetMessage(),str),
								  msg.othersCode(),
								  subStitute(msg.othersMessage(),str));
	                if(CMLib.flags().aliveAwakeMobile((MOB)affected,true))
	    				helpProficiency((MOB)affected);
				}
			}
	        else
	        if((msg.sourceMinor()==CMMsg.TYP_WRITE)
	        &&(msg.source()==affected)
	        &&(msg.target() instanceof Item)
	        &&(CMLib.flags().isReadable((Item)msg.target()))
	        &&(msg.targetMessage()!=null)
	        &&(msg.targetMessage().length()>0))
	        {
	            Ability L=null;
	            for(int i=msg.target().numEffects()-1;i>=0;i--)
	            {
	                L=msg.target().fetchEffect(i);
	                if((L instanceof Language)&&(!L.ID().equals(ID())))
	                {
	                    msg.source().tell(msg.target().name()+" is already written in "+L.name()+" and can not have "+name()+" writing added.");
	                    return false;
	                }
	            }
	        }
	        else
			if((msg.target()==affected)&&(msg.source()!=affected))
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_ORDER:
				case CMMsg.TYP_BUY:
				case CMMsg.TYP_SELL:
				case CMMsg.TYP_LIST:
				case CMMsg.TYP_VIEW:
				case CMMsg.TYP_WITHDRAW:
				case CMMsg.TYP_DEPOSIT:
				if((!CMSecurity.isAllowed(msg.source(),msg.source().location(),"ORDER"))
				&&(!CMSecurity.isAllowed(msg.source(),msg.source().location(),"CMDMOBS")||(!((MOB)msg.target()).isMonster()))
				&&(!CMSecurity.isAllowed(msg.source(),msg.source().location(),"CMDROOMS")||(!((MOB)msg.target()).isMonster())))
				{
					Language L=(Language)msg.source().fetchEffect(ID());
					if((L==null)
					||(!L.beingSpoken())
					||((CMLib.dice().rollPercentage()*2)>(L.proficiency()+proficiency())))
					{
						msg.setTargetCode(CMMsg.TYP_SPEAK);
						msg.setSourceCode(CMMsg.TYP_SPEAK);
						msg.setOthersCode(CMMsg.TYP_SPEAK);
						String reply=null;
						if((L==null)||(!L.beingSpoken()))
							reply="<S-NAME> <S-IS-ARE> speaking "+name()+" and do(es) not appear to understand <T-YOUPOSS> words.";
						else
							reply="<S-NAME> <S-IS-ARE> having trouble understanding <T-YOUPOSS> pronunciation.";
						msg.addTrailerMsg(CMClass.getMsg((MOB)msg.target(),msg.source(),null,CMMsg.MSG_OK_VISUAL,reply));
					}
					break;
				}
				default:
					break;
				}
		}
		return super.okMessage(myHost,msg);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!auto)
		{
			for(int a=0;a<mob.numAllEffects();a++)
			{
				Ability A=mob.fetchEffect(a);
				if((A!=null)&&(A instanceof Language))
				{
					if(mob.isMonster())
						A.setProficiency(100);
					if(A.ID().equals(ID()))
						((Language)A).setBeingSpoken(true);
					else
						((Language)A).setBeingSpoken(false);
				}
			}
			isAnAutoEffect=false;
			mob.tell("You are now speaking "+name()+".");
		}
		else
			setBeingSpoken(true);
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)))
		&&(msg.tool() !=null)
		&&(msg.sourceMessage()!=null)
		&&(msg.tool() instanceof Language)
		&&(msg.tool().ID().equals(ID())))
		{
			String str=this.getMsgFromAffect(msg.sourceMessage());
			if(str!=null)
			{
				int numToMess=(int)Math.round(CMath.mul(numChars(str),CMath.div(100-proficiency(),100)));
				if(numToMess>0)
					str=messChars(str,numToMess);
				if(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),this.subStitute(msg.othersMessage(),str)+" (translated from "+ID()+")"));
				else
				if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
				{
					String otherMes=msg.targetMessage();
					if(msg.target()!=null)
						otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.targetCode(),this.subStitute(otherMes,str)+" (translated from "+ID()+")",CMMsg.NO_EFFECT,null));
				}
				else
				if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf("'")>0))
				{
					String otherMes=msg.othersMessage();
					if(msg.target()!=null)
						otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,null,msg.othersCode(),subStitute(otherMes,str)+" (translated from "+ID()+")",CMMsg.NO_EFFECT,null));
				}
			}
		}
        else
        if((affected instanceof MOB)
        &&(msg.source()==affected)
        &&(beingSpoken())
        &&(msg.target() instanceof Item)
        &&(msg.sourceMinor()==CMMsg.TYP_WRITE)
        &&(CMLib.flags().isReadable((Item)msg.target()))
        &&(msg.targetMessage()!=null)
        &&(msg.targetMessage().length()>0))
        {
            Ability L=null;
            for(int i=msg.target().numEffects()-1;i>=0;i--)
            {
                L=msg.target().fetchEffect(i);
                if(L instanceof Language)
                {
                    msg.target().delEffect(L);
                    break;
                }
            }
            msg.target().addNonUninvokableEffect((Ability)this.copyOf());
        }
        else
        if((affected instanceof Item)
        &&(!canBeUninvoked())
        &&(msg.target()==affected)
        &&(msg.targetMinor()==CMMsg.TYP_READ)
        &&((msg.targetMessage()==null)||(!msg.targetMessage().equals("CANCEL")))
        &&(!(affected instanceof LandTitle))
        &&(CMLib.flags().canBeSeenBy(this,msg.source()))
        &&((CMLib.flags().isReadable((Item)affected))
        &&(((Item)affected).readableText()!=null)
        &&(((Item)affected).readableText().length()>0)))
        {
            // first make sure the Item does not handle it,
            // since THIS item is in another language.
            msg.modify(msg.source(),
                       msg.target(),
                       msg.tool(),
                       msg.sourceCode(),
                       msg.sourceMessage(),
                       msg.targetCode(),
                       "CANCEL",
                       msg.othersCode(),
                       msg.othersMessage());
            Language L=(Language)msg.source().fetchEffect(ID());
            String str=((Item)affected).readableText();
            if(str.startsWith("FILE=")
            ||str.startsWith("FILE="))
            {
                StringBuffer buf=Resources.getFileResource(str.substring(5),true);
                if((buf!=null)&&(buf.length()>0))
                    str=buf.toString();
                else
                    str="";
            }
            int numToMess=numChars(str);
            if(numToMess==0)
                msg.source().tell("There is nothing written on "+affected.name()+".");
            else
            {
                if(L!=null)
                    numToMess=(int)Math.round(CMath.mul(numChars(str),CMath.div(100-L.proficiency(),100)));
                String original=messChars(str,numToMess);
                str=scrambleAll(str,numToMess);
                msg.source().tell("It says '"+str+"'");
                if((L!=null)&&(!original.equals(str)))
                    msg.source().tell("It says '"+original+"' (translated from "+L.name()+").");
            }
        }
	}
}
