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
public class QuestChat extends MudChat 
{

	public String ID(){return "QuestChat";}
	private Hashtable alreadySaid=new Hashtable();
	private Vector addedChatData=new Vector();
	private Quest myQuest=null;
	
    public void registerDefaultQuest(Quest Q){ myQuest=Q;}
    
    public void setParms(String newParms)
    {
    	if(newParms.startsWith("+"))
    	{
    		Vector V=CMParms.parseSemicolons(newParms.substring(1),false);
    		StringBuffer rsc=new StringBuffer("");
    		for(int v=0;v<V.size();v++)
    			rsc.append(((String)V.elementAt(v))+"\n\r");
    		V=super.parseChatData(rsc,new Vector());
    		for(int v=0;v<V.size();v++)
    		{
    			Vector V2=(Vector)V.elementAt(v);
    			for(int v2=1;v2<V2.size();v2++)
    				addedChatData.addElement(V2.elementAt(v2));
    		}
    		myChatGroup=null;
    	}
    	else
    		super.setParms(newParms);
    }
    
	protected boolean match(MOB speaker, String expression, String message, String[] rest)
	{
		if(expression.indexOf("::")>=0)
		{
			 int x=expression.length()-1;
			 char c=' ';
			 boolean coded=false;
			 while(x>=0)
			 {
				 c=expression.charAt(x);
				 if((c==':')&&(x>0)&&(expression.charAt(x-1)==':'))
				 {
					 if(coded)
					 {
						 String codeStr=expression.substring(x+2).toUpperCase().trim();
						 expression=expression.substring(0,x-1).trim();
						 Vector V=(Vector)alreadySaid.get(speaker.Name().toUpperCase());
						 if(V==null)
						 {
							 V=new Vector();
							 alreadySaid.put(speaker.Name().toUpperCase(),V);
						 }
						 else
					     if(V.contains(codeStr))
					    	 return false;
						 if(super.match(speaker,expression,message,rest))
						 {
							 V.addElement(codeStr);
							 if(myQuest!=null)
							 {
								 String stat=myQuest.getStat("CHAT:"+speaker.Name().toUpperCase());
								 if(stat.length()>0) stat+=" ";
								 myQuest.setStat("CHAT:"+speaker.Name().toUpperCase(),stat+codeStr);
							 }
							 return true;
						 }
						 return false;
					 }
					 break;
				 }
				 coded=true;
				 x--;
			 }
		}
		return super.match(speaker,expression,message,rest);
	}
    
	protected Vector getMyChatGroup(MOB forMe, Vector chatGroups)
	{
		if((myChatGroup!=null)&&(myOldName.equals(forMe.Name())))
			return myChatGroup;
		Vector chatGrp=super.getMyChatGroup(forMe,chatGroups);
		if((addedChatData==null)||(addedChatData.size()==0)) return chatGrp;
		chatGrp=(Vector)chatGrp.clone();
		for(int v=0;v<addedChatData.size();v++)
			if(chatGrp.size()==(v+1))
				chatGrp.addElement(addedChatData.elementAt(v));
			else
				chatGrp.insertElementAt(addedChatData.elementAt(v),v+1);
		chatGrp.trimToSize();
		return chatGrp;
	}
	
	
}
