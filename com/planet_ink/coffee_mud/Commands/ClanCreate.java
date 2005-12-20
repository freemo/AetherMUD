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
public class ClanCreate extends BaseClanner
{
	public ClanCreate(){}

	private String[] access={getScr("ClanCreate","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			if(!mob.isMonster())
			{
				int cost=CMProps.getIntVar(CMProps.SYSTEMI_CLANCOST);
				if(cost>0)
				{
					if(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)<new Integer(cost).doubleValue())
					{
						mob.tell(getScr("ClanCreate","cantafford",CMLib.beanCounter().nameCurrencyShort(mob,cost)));
						return false;
					}
				}
				try
				{
					String check=mob.session().prompt(getScr("ClanCreate","aresure"),getScr("ClanCreate","noword"));
					if(check.equalsIgnoreCase("Y"))
					{
						String doubleCheck=mob.session().prompt(getScr("ClanCreate","enternamec"),"");
						if(doubleCheck.length()<1)
							return false;
                        if(doubleCheck.length()>30) // Robert checking length
                        {
                            mob.tell(getScr("ClanCreate","shorter"));
                            return false;
                        }
						Clan C=CMLib.clans().findClan(doubleCheck);
						if((CMLib.database().DBUserSearch(null,doubleCheck))
						||(doubleCheck.equalsIgnoreCase("All")))
							msg.append(getScr("ClanCreate","notava"));
						else
						if(C!=null)
							msg.append(getScr("ClanCreate","alexist",C.clanID()));
						else
						{
							if(mob.session().confirm(getScr("ClanCreate","iscorrect",doubleCheck), getScr("ClanCreate","noword")))
							{
								int govtType=-1;
								while(govtType==-1)
								{
									String govt=mob.session().prompt(
									getScr("ClanCreate","msg1")
									+getScr("ClanCreate","msg2")
									+getScr("ClanCreate","msg3")
									+getScr("ClanCreate","msg4")
									+getScr("ClanCreate","msg5")
									+": ","");
									if(govt.length()==0){ mob.tell(getScr("ClanCreate","aborted")); return false;}
									for(int i=0;i<Clan.GVT_DESCS.length;i++)
										if(govt.equalsIgnoreCase(Clan.GVT_DESCS[i]))
											govtType=i;
								}

								if(cost>0)
									CMLib.beanCounter().subtractMoney(mob,cost);

								Clan newClan=CMLib.clans().getClanType(Clan.TYPE_CLAN);
								newClan.setName(doubleCheck);
								newClan.setGovernment(govtType);
								newClan.setStatus(Clan.CLANSTATUS_PENDING);
								newClan.create();
								CMLib.database().DBUpdateClanMembership(mob.Name(),newClan.getName(),newClan.getTopRank());
								newClan.updateClanPrivileges(mob);
								clanAnnounce(mob, getScr("ClanCreate","cison",newClan.typeName(),newClan.clanID()));
							}
						}
					}
				}
				catch(java.io.IOException e)
				{
				}
			}
		}
		else
		{
			msg.append(getScr("ClanCreate","almember",mob.getClanID()));
		}
		mob.tell(msg.toString());
		return false;
	}
	public int actionsCost(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
