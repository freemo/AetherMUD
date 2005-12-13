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
public class ClanExile extends BaseClanner
{
	public ClanExile(){}

	private String[] access={getScr("ClanExile","cmd")};
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
				msg.append(getScr("ClanExile","evenmember"));
			}
			else
			{
				C=CMLib.clans().getClan(mob.getClanID());
				if(C==null)
				{
					mob.tell(getScr("ClanExile","nolonger",mob.getClanID()));
					return false;
				}
				if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANEXILE,false))
				{
					DVector apps=C.getMemberList();
					if(apps.size()<1)
					{
						mob.tell(getScr("ClanExile","nomembers",C.typeName()));
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
						MOB M=CMLib.map().getLoadPlayer(qual);
						if(M==null)
						{
							mob.tell(getScr("ClanExile","clannotfound",qual,C.typeName()));
							return false;
						}
						if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANEXILE,true))
						{
							clanAnnounce(mob,getScr("ClanExile","msgex",C.typeName(),C.name(),M.Name()));
							CMLib.database().DBUpdateClanMembership(qual, "", 0);
							M.setClanID("");
							M.setClanRole(0);
							mob.tell(getScr("ClanExile","exiled",M.Name(),C.typeName(),C.clanID()));
							M.tell(getScr("ClanExile","youexiled",C.typeName(),C.clanID()));
							C.updateClanPrivileges(M);
							return false;
						}
					}
					else
					{
						msg.append(getScr("ClanExile","notmem",qual,C.typeName()));
					}
				}
				else
				{
					msg.append(getScr("ClanExile","notpos",C.typeName()));
				}
			}
		}
		else
		{
			msg.append(getScr("ClanExile","specmem"));
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
