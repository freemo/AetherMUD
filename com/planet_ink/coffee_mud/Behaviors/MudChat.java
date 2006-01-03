package com.planet_ink.coffee_mud.Behaviors;
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
public class MudChat extends StdBehavior
{
	public String ID(){return "MudChat";}


	//----------------------------------------------
	// format: first group is general mob (no other
	// fit found).  All groups are chat groups.
	// each chat group includes a string describing
	// qualifying mobs followed by one or more chat
	// collections.
	protected Vector myChatGroup=null;
	protected String myOldName="";
	// chat collection: first string is the pattern
	// match string
	// following strings are the proposed responses.
	//----------------------------------------------

	protected MOB lastReactedTo=null;
	protected Vector responseQue=new Vector();
	protected int tickDown=3;
	protected final static int TALK_WAIT_DELAY=8;
	protected int talkDown=0;
	// responseQue is a qued set of commands to
	// run through the standard command processor,
	// on tick or more.
	protected final static int RESPONSE_DELAY=2;

    public void setParms(String newParms)
    {
        super.setParms(newParms);
        responseQue=new Vector();
    }

	protected static synchronized Vector getChatGroups(String parms)
	{
		Vector rsc=null;
		String filename="chat.dat";
		int x=parms.indexOf("=");
		if(x>0)	filename=parms.substring(0,x);
		rsc=(Vector)Resources.getResource("MUDCHAT GROUPS-"+filename);
		if(rsc==null)
		{
			rsc=loadChatData(filename,new Vector());
			Resources.submitResource("MUDCHAT GROUPS-"+filename,rsc);
		}
		return rsc;
	}

	public Vector externalFiles()
	{
		int x=parms.indexOf("=");
		if(x>0)
		{
		    Vector xmlfiles=new Vector();
			String filename=parms.substring(0,x).trim();
			if(filename.length()>0)
			    xmlfiles.addElement(filename.trim());
			return xmlfiles;
		}
		return null;
	}

	protected static Vector loadChatData(String resourceName, Vector chatGroups)
	{
		StringBuffer rsc=new CMFile("resources/"+resourceName,null,true).text();
		Vector currentChatGroup=new Vector();
		Vector otherChatGroup;
		currentChatGroup.addElement("");
		chatGroups.addElement(currentChatGroup);
		String str=nextLine(rsc);
		Vector currentChatPattern=null;
		while(str!=null)
		{
			if(str.length()>0)
			switch(str.charAt(0))
			{
			case '"':
				Log.sysOut("MudChat",str.substring(1));
				break;
			case '*':
				if((str.length()==1)||("([{".indexOf(str.charAt(1))<0))
					break;
			case '(':
			case '[':
			case '{':
				currentChatPattern=new Vector();
				currentChatPattern.addElement(str);
				if(currentChatGroup!=null)
					currentChatGroup.addElement(currentChatPattern);
				break;
			case '>':
				currentChatGroup=new Vector();
				currentChatGroup.addElement(str.substring(1).trim());
				chatGroups.addElement(currentChatGroup);
				currentChatPattern=null;
				break;
			case '@':
				otherChatGroup=matchChatGroup(str.substring(1).trim(),chatGroups);
				if(otherChatGroup==null)
					otherChatGroup=(Vector)chatGroups.elementAt(0);
				for(int v1=1;v1<otherChatGroup.size();v1++)
					currentChatGroup.addElement(otherChatGroup.elementAt(v1));
				break;
			case '%':
				{
	  				StringBuffer rsc2=new StringBuffer(Resources.getFileResource(str.substring(1).trim(),true).toString());
	  				if(rsc2.length()<1) { Log.sysOut("MudChat","Error reading resource "+resourceName); }
	  				rsc.insert(0,rsc2.toString());
				}
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				if(currentChatPattern!=null)
					currentChatPattern.addElement(str);
				break;
			}
			str=nextLine(rsc);
		}
		return chatGroups;
	}

	public static String nextLine(StringBuffer tsc)
	{
		String ret=null;
		if((tsc!=null)&&(tsc.length()>0))
		{
			int y=tsc.toString().indexOf("\n\r");
			if(y<0)
			{
				tsc.setLength(0);
				ret="";
			}
			else
			{
				ret=tsc.substring(0,y).trim();
				tsc.delete(0,y+2);
			}
		}
		return ret;

	}


	protected static Vector matchChatGroup(String myName, Vector chatGroups)
	{
		for(int i=1;i<chatGroups.size();i++)
		{
			Vector V=(Vector)chatGroups.elementAt(i);
			Vector Names=new Vector();
			if(V.size()>0)
				if(((String)V.elementAt(0)).length()>0)
				{
					String names=((String)V.elementAt(0));
					while(names.length()>0)
					{
						int y=names.indexOf(" ");
						if(y>=0)
						{
							Names.addElement(names.substring(0,y).trim().toUpperCase());
							names=names.substring(y+1);
						}
						else
						{
							Names.addElement(names.trim().toUpperCase());
							names="";
						}
					}
					for(int j=0;j<Names.size();j++)
					{
						if(((String)Names.elementAt(j)).equalsIgnoreCase(myName))
							return V;
					}
				}
		}
		return null;
	}

	protected Vector getMyChatGroup(MOB forMe, Vector chatGroups)
	{
		if((myChatGroup!=null)&&(myOldName.equals(forMe.Name())))
			return myChatGroup;
		myOldName=forMe.Name();
		Vector V=matchChatGroup(myOldName.toUpperCase(),chatGroups);
		if(V!=null) return V;
		V=matchChatGroup(forMe.description(),chatGroups);
		if(V!=null) return V;
		V=matchChatGroup(forMe.displayText(),chatGroups);
		if(V!=null) return V;
		V=matchChatGroup(CMClass.className(forMe),chatGroups);
		if(V!=null) return V;
		if(getParms().length()>0)
		{
			int x=getParms().indexOf("=");
			if(x<0)
				V=matchChatGroup(getParms(),chatGroups);
			else
			if(getParms().substring(x+1).trim().length()>0)
				V=matchChatGroup(getParms().substring(x+1),chatGroups);
		}
		if(V!=null) return V;
		return (Vector)chatGroups.elementAt(0);
	}

	protected void queResponse(Vector responses, MOB source, MOB target, String rest)
	{
		int total=0;
		for(int x=1;x<responses.size();x++)
			total+=CMath.s_int(((String)responses.elementAt(x)).substring(0,1));

		String selection=null;
		int select=CMLib.dice().roll(1,total,0);
		for(int x=1;x<responses.size();x++)
		{
			select-=CMath.s_int(((String)responses.elementAt(x)).substring(0,1));
			if(select<=0)
			{
				selection=(String)responses.elementAt(x);
				break;
			}
		}

		if(selection!=null)
		{
			Vector selections=CMParms.parseSquiggleDelimited(selection.substring(1).trim(),true);
			for(int v=0;v<selections.size();v++)
			{
				String finalCommand=(String)selections.elementAt(v);
				if(finalCommand.trim().length()==0)
					return;
				else
				if(finalCommand.startsWith(":"))
				{
					finalCommand="emote "+finalCommand.substring(1).trim();
					if(source!=null)
						finalCommand=CMStrings.replaceAll(finalCommand," her "," "+source.charStats().hisher()+" ");
				}
				else
				if(finalCommand.startsWith("!"))
					finalCommand=finalCommand.substring(1).trim();
				else
				if(finalCommand.startsWith("\""))
					finalCommand="say \""+finalCommand.substring(1).trim()+"\"";
				else
				if(target!=null)
					finalCommand="sayto \""+target.name()+"\" "+finalCommand.trim();

				if(finalCommand.indexOf("$r")>=0)
					finalCommand=CMStrings.replaceAll(finalCommand,"$r",rest);
				if((target!=null)&&(finalCommand.indexOf("$t")>=0))
					finalCommand=CMStrings.replaceAll(finalCommand,"$t",target.name());
				if((source!=null)&&(finalCommand.indexOf("$n")>=0))
					finalCommand=CMStrings.replaceAll(finalCommand,"$n",source.name());
				if(finalCommand.indexOf("$$")>=0)
					finalCommand=CMStrings.replaceAll(finalCommand,"$$","$");

				Vector V=CMParms.parse(finalCommand);
				V.insertElementAt(new Integer(RESPONSE_DELAY),0);
				for(int f=0;f<responseQue.size();f++)
				{
					Vector V1=(Vector)responseQue.elementAt(f);
					if(CMParms.combine(V1,1).equalsIgnoreCase(finalCommand))
					{
						V=null;
						break;
					}
				}
				if(V!=null)
					responseQue.addElement(V);
			}
		}
	}


	protected boolean match(String expression, String message, String[] rest)
	{
		int l=expression.length();
		if(l==0) return true;
		if((expression.charAt(0)=='(')
		&&(expression.charAt(l-1)==')'))
			expression=expression.substring(1,expression.length()-1);

		int end=0;
		for(;((end<expression.length())&&(("(&|~").indexOf(expression.charAt(end))<0));end++);
		String check=null;
		if(end<expression.length())
		{
			check=expression.substring(0,end);
			expression=expression.substring(end);
		}
		else
		{
			check=expression;
			expression="";
		}
		boolean response=true;
		if(check.startsWith("="))
		{
			response=check.substring(1).trim().equalsIgnoreCase(message.trim());
			if(response)
				rest[0]="";
		}
		else
		if(check.startsWith("^"))
		{
			response=message.trim().startsWith(check.substring(1));
			if(response)
				rest[0]=message.substring(check.substring(1).trim().length());
		}
		else
		if(check.length()>0)
		{
			int x=message.toUpperCase().indexOf(check.toUpperCase());
			response=(x>=0);
			if(response)
				rest[0]=message.substring(x+check.length());
		}
		else
		{
			response=true;
			rest[0]=message;
		}

		if(expression.length()>0)
		{
			if(expression.startsWith("("))
			{
				int expEnd=0;
				int parenCount=1;
				while(((++expEnd)<expression.length())&&(parenCount>0))
					if(expression.charAt(expEnd)=='(')
						parenCount++;
					else
					if(expression.charAt(expEnd)==')')
					{
						parenCount--;
						if(parenCount<=0) break;
					}
				if(expEnd<expression.length()&&(parenCount<=0))
				{
					return response&match(expression.substring(1,expEnd),message,rest);
				}
				return response;
			}
			else
			if(expression.startsWith("&"))
				return response&&match(expression.substring(1),message,rest);
			else
			if(expression.startsWith("|"))
				return response||match(expression.substring(1),message,rest);
			else
			if(expression.startsWith("~"))
				return response&&(!match(expression.substring(1),message,rest));

		}
		return response;
	}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);

		if((!canActAtAll(affecting))
		||(CMSecurity.isDisabled("MUDCHAT")))
			return;
		MOB mob=msg.source();
		MOB monster=(MOB)affecting;
		if((!msg.amISource(monster))
		&&(!mob.isMonster())
		&&(CMLib.flags().canBeHeardBy(mob,monster))
		&&(CMLib.flags().canBeSeenBy(mob,monster))
		&&(CMLib.flags().canBeSeenBy(monster,mob)))
		{
			Vector myResponses=null;
			myChatGroup=getMyChatGroup(monster,getChatGroups(getParms()));
			String rest[]=new String[1];
			boolean combat=((monster.isInCombat()))||(mob.isInCombat());

			if((msg.targetMinor()==CMMsg.TYP_SPEAK)
			&&(msg.amITarget(monster)
			   ||((msg.target()==null)
			      &&(mob.location()==monster.location())
				  &&(talkDown<=0)
				  &&(mob.location().numPCInhabitants()<3)))
			&&(CMLib.flags().canBeHeardBy(mob,monster))
			&&(myChatGroup!=null)
			&&(lastReactedTo!=msg.source())
			&&(msg.sourceMessage()!=null)
			&&(msg.targetMessage()!=null))
			{
				int x=msg.sourceMessage().indexOf("'");
				int y=msg.sourceMessage().lastIndexOf("'");
				if((x>=0)&&(y>x))
				{
					String str=" "+msg.sourceMessage().substring(x+1,y)+" ";
					int l=0;
					for(int i=1;i<myChatGroup.size();i++)
					{
						Vector possResponses=(Vector)myChatGroup.elementAt(i);
						String expression=((String)possResponses.elementAt(0)).trim();
						if(expression.startsWith("*"))
						{
							if(!combat) continue;
							expression=expression.substring(1);
						}
						else
						if(combat) continue;

						l=expression.length();
						if((l>0)
						&&(expression.charAt(0)=='(')
						&&(expression.charAt(l-1)==')'))
						{
							if(match(expression.substring(1,expression.length()-1),str,rest))
							{
								if(myResponses==null) myResponses=new Vector();
								myResponses.addAll(possResponses);
                                break;
							}
						}
					}
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(CMLib.flags().canBeHeardBy(mob,monster))
			&&(CMLib.flags().canBeSeenBy(mob,monster))
			&&(mob.isMonster())
			&&(msg.source()!=monster))
			   talkDown=TALK_WAIT_DELAY;
			else
			if((CMLib.flags().canBeHeardBy(mob,monster))
			&&(CMLib.flags().canBeSeenBy(mob,monster))
			&&(CMLib.flags().canBeSeenBy(monster,mob))
			&&(talkDown<=0)
			&&(lastReactedTo!=msg.source())
			&&(myChatGroup!=null))
			{
				String str=null;
				char c1='[';
				char c2=']';
				if((msg.amITarget(monster)&&(msg.targetMessage()!=null)))
					str=" "+msg.targetMessage()+" ";
				else
				if(msg.othersMessage()!=null)
				{
					c1='{';
					c2='}';
					str=" "+msg.othersMessage()+" ";
				}
				if(str!=null)
				{
					int l=0;
					for(int i=1;i<myChatGroup.size();i++)
					{
						Vector possResponses=(Vector)myChatGroup.elementAt(i);
						String expression=((String)possResponses.elementAt(0)).trim();
						if(expression.startsWith("*"))
						{
							if(!combat) continue;
							expression=expression.substring(1);
						}
						else
						if(combat) continue;
						l=expression.length();
						if((l>0)
						&&(expression.charAt(0)==c1)
						&&(expression.charAt(l-1)==c2))
						{
							if(match(expression.substring(1,expression.length()-1),str,rest))
							{
								if(myResponses==null) myResponses=new Vector();
								myResponses.addAll(possResponses);
                                break;
							}
						}
					}
				}
			}


			if(myResponses!=null)
			{
				lastReactedTo=msg.source();
				queResponse(myResponses,monster,mob,rest[0]);
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((tickID==Tickable.TICKID_MOB)
		&&(ticking instanceof MOB)
		&&(!CMSecurity.isDisabled("MUDCHAT")))
		{
			if(!canActAtAll(ticking))
			{
				responseQue.removeAllElements();
				return true;
			}

			if(talkDown>0) talkDown--;

			if(tickDown>=0)
			{
				--tickDown;
				if(tickDown<0)
				{
					myChatGroup=getMyChatGroup((MOB)ticking,getChatGroups(getParms()));
				}
			}
			for(int t=responseQue.size()-1;t>=0;t--)
			{
				Vector que=(Vector)responseQue.elementAt(t);
				Integer I=(Integer)que.elementAt(0);
				I=new Integer(I.intValue()-1);
				que.setElementAt(I,0);
				if(I.intValue()<=0)
				{
					que.removeElementAt(0);
					responseQue.removeElementAt(t);
					((MOB)ticking).doCommand(que);
					lastReactedTo=null;
					// you've done one, so get out before doing another!
					break;
				}
			}
		}
		return true;
	}
}
