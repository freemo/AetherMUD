package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class As extends StdCommand
{
	public As(){}

	private String[] access={"AS"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("As whom do what?");
			return false;
		}
		boolean here=false;
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		if((!CMSecurity.isAllowed(mob,mob.location(),"AS"))||(mob.isMonster()))
		{
			mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		Session mySession=mob.session();
		MOB M=CMMap.getLoadPlayer(cmd);
		if(M==null)
			M=mob.location().fetchInhabitant(cmd);
		if(M==null)
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				M=R.fetchInhabitant(cmd);
				if(M!=null) break;
			}
		if(M==null)
		{
			mob.tell("You don't know of anyone by that name.");
			return false;
		}
		Session oldSession=M.session();
		Room oldRoom=M.location();
		boolean inside=(oldRoom!=null)?oldRoom.isInhabitant(M):false;
		M.setSession(mySession);
		mySession.setMob(M);
		if(((String)commands.firstElement()).equalsIgnoreCase("here")
		   ||((String)commands.firstElement()).equalsIgnoreCase("."))
		{
			mob.location().bringMobHere(M,false);
			commands.removeElementAt(0);
		}
		M.doCommand(commands);
		if(M.playerStats()!=null) M.playerStats().setUpdated(0);
		if((oldRoom!=null)&&(inside))
			oldRoom.bringMobHere(M,false);
		else
		{
			if(M.location()!=null) 
				M.location().delInhabitant(M);
			M.setLocation(oldRoom);
		}
		M.setSession(oldSession);
		mySession.setMob(mob);
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"AT");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
