package com.planet_ink.coffee_mud.Commands;
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
public class ClanExile extends BaseClanner
{
	public ClanExile(){}

	private String[] access={"CLANEXILE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt("clanexile",0);
		String qual=Util.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		boolean found=false;
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				msg.append("You aren't even a member of a clan.");
			}
			else
			{
				C=Clans.getClan(mob.getClanID());
				if(C==null)
				{
					mob.tell("There is no longer a clan called "+mob.getClanID()+".");
					return false;
				}
				if(skipChecks||goForward(mob,C,commands,Clans.FUNC_CLANEXILE,false))
				{
					DVector apps=C.getMemberList();
					if(apps.size()<1)
					{
						mob.tell("There are no members in your "+C.typeName()+".");
						return false;
					}
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q,1)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=CMMap.getLoadPlayer(qual);
						if(M==null)
						{
							mob.tell(qual+" was not found.  Could not exile from "+C.typeName()+".");
							return false;
						}
						else
						{
							if(skipChecks||goForward(mob,C,commands,Clans.FUNC_CLANEXILE,true))
							{
								clanAnnounce(mob,"Member exiled from "+C.name()+": "+M.Name());
								CMClass.DBEngine().DBUpdateClanMembership(qual, "", 0);
								M.setClanID("");
								M.setClanRole(0);
								mob.tell(M.Name()+" has been exiled from "+C.typeName()+" '"+C.ID()+"'.");
								M.tell("You have been exiled from "+C.typeName()+" '"+C.ID()+"'.");
								C.updateClanPrivileges(M);
								return false;
							}
						}
					}
					else
					{
						msg.append(qual+" isn't a member of your "+C.typeName()+".");
					}
				}
				else
				{
					msg.append("You aren't in the right position to exile anyone from your "+C.typeName()+".");
				}
			}
		}
		else
		{
			msg.append("You haven't specified which member you are exiling.");
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
