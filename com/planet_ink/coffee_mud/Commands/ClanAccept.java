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
public class ClanAccept extends BaseClanner
{
	public ClanAccept(){}

	private String[] access={getScr("ClanAccept","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());

		commands.setElementAt("clanaccept",0);
		String qual=CMParms.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		boolean found=false;
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				msg.append(getScr("ClanAccept","even"));
			}
			else
			{
				C=CMLib.clans().getClan(mob.getClanID());
				if(C==null)
				{
					mob.tell(getScr("ClanAccept","nolonger",mob.getClanID()));
					return false;
				}
				if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANACCEPT,false))
				{
					DVector apps=C.getMemberList(Clan.POS_APPLICANT);
					if(apps.size()<1)
					{
						mob.tell(getScr("ClanAccept","noapplic",C.typeName()));
						return false;
					}
					qual=CMStrings.capitalizeAndLower(qual);
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
							mob.tell(getScr("ClanAccept","notfound",qual,C.typeName()));
							return false;
						}
						if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANACCEPT,true))
						{
							clanAnnounce(mob,getScr("ClanAccept","newmember",C.typeName(),C.name(),M.Name()));
							M.setClanID(mob.getClanID());
							M.setClanRole(Clan.POS_MEMBER);
							CMLib.database().DBUpdateClanMembership(qual, mob.getClanID(), Clan.POS_MEMBER);
							mob.tell(getScr("ClanAccept","hasaccepted",M.Name(),C.typeName(),C.clanID()));
							M.tell(getScr("ClanAccept","hasaccepyou",mob.Name(),C.typeName(),C.clanID()));
							C.updateClanPrivileges(M);
							return false;
						}
					}
					else
					{
						msg.append(getScr("ClanAccept","appli",qual,C.typeName()));
					}
				}
				else
				{
					msg.append(getScr("ClanAccept","notright",C.typeName()));
				}
			}
		}
		else
		{
			msg.append(getScr("ClanAccept","notspec"));
		}
		mob.tell(msg.toString());
		return false;
	}
	public double actionsCost(){return 0.0;}
	public boolean canBeOrdered(){return false;}

	
}
