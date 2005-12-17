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
public class DoorwayGuardian extends StdBehavior
{
	public String ID(){return "DoorwayGuardian";}


    
    public String getWords()
    {
        int x=getParms().indexOf(";");
        if(x<0) return "<S-NAME> won't let <T-NAME> through there.";
        return getParms().substring(x+1);
    }
	public Exit[] getParmExits(MOB monster)
	{
		if(monster==null) return null;
		if(monster.location()==null) return null;
		if(getParms().length()==0) return null;
		Room room=monster.location();
        String parm=getParms();
        int x=parm.indexOf(";");
        if(x>0) parm=parm.substring(0,x);
		Vector V=CMParms.parse(parm);
		for(int v=0;v<V.size();v++)
		{
			int dir=Directions.getGoodDirectionCode((String)V.elementAt(v));
			if(dir>=0)
				if(room.getExitInDir(dir)!=null)
				{
					Exit[] exits={room.getExitInDir(dir),room.getReverseExit(dir)};
					return exits;
				}
		}
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Exit E=room.getExitInDir(d);
			if((E!=null)&&(E.hasADoor()))
			{
				Exit[] exits={E,room.getReverseExit(d)};
				return exits;
			}
		}
		return null;
	}


	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		if(!super.okMessage(oking,msg)) return false;
		MOB mob=msg.source();
		if(!canFreelyBehaveNormal(oking)) return true;
		MOB monster=(MOB)oking;
		if((mob.location()==monster.location())
		&&(mob!=monster)
		&&(msg.target()!=null)
		&&(!BrotherHelper.isBrother(mob,monster))
		&&(!CMLib.masking().maskCheck(getParms(),mob))
        &&(CMLib.flags().canSenseMoving(mob,monster)||(getParms().toUpperCase().indexOf("NOSNEAK")>=0)))
		{
			if(msg.target() instanceof Exit)
			{
				Exit exit=(Exit)msg.target();
				Exit texit[]=getParmExits(monster);
				if((texit!=null)
				&&(texit[0]!=exit)
				&&(texit[1]!=exit))
					return true;

				if((msg.targetMinor()!=CMMsg.TYP_CLOSE)
				&&(msg.targetMinor()!=CMMsg.TYP_LOCK))
				{
					CMMsg msgs=CMClass.getMsg(monster,mob,CMMsg.MSG_NOISYMOVEMENT,getWords());
					if(monster.location().okMessage(monster,msgs))
					{
						monster.location().send(monster,msgs);
						return false;
					}
				}
			}
			else
			if((msg.tool()!=null)
			&&(msg.target() instanceof Room)
			&&(msg.tool() instanceof Exit))
			{
				Exit exit=(Exit)msg.tool();
				Exit texit[]=getParmExits(monster);
				if((texit!=null)
				&&(texit[0]!=exit)
				&&(texit[1]!=exit))
					return true;

				CMMsg msgs=CMClass.getMsg(monster,mob,CMMsg.MSG_NOISYMOVEMENT,getWords());
				if(monster.location().okMessage(monster,msgs))
				{
					monster.location().send(monster,msgs);
					return false;
				}
			}
		}
		return true;
	}
}
