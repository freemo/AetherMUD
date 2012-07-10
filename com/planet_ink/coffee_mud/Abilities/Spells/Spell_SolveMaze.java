package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/* 
   Copyright 2000-2012 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Spell_SolveMaze extends Spell
{
	public String ID() { return "Spell_SolveMaze"; }
	public String name(){return "Solve Maze";}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canTargetCode(){return 0;}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room targetR=mob.location();
		if((targetR==null) || (targetR.getGridParent()==null))
		{
			mob.tell("This spell only works when you are in a maze");
			return false;
		}
		
		GridLocale grid = targetR.getGridParent();
		
		int direction=-1;
		Room outRoom=null;
		if((commands.size()>0)
		&&(commands.firstElement() instanceof String)
		&&(((String)commands.firstElement()).toLowerCase().startsWith("ma")))
			commands.remove(0);
		if(commands.size()==0)
		{
			List<Integer> list=new Vector<Integer>();
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				Room R=grid.getRoomInDir(d);
				if((R!=null)&&(R.roomID().length()>0))
					list.add(Integer.valueOf(d));
			}
			if(list.size()==0)
			{
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					Room R=grid.getRoomInDir(d);
					if(R!=null)
						list.add(Integer.valueOf(d));
				}
			}
			if(list.size()>0)
			{
				direction=list.get(CMLib.dice().roll(1, list.size(), -1)).intValue();
				outRoom=grid.getRoomInDir(direction);
			}
		}
		else
		{
			direction=Directions.getDirectionCode(commands.firstElement().toString());
			if(direction>=0)
				outRoom=grid.getRoomInDir(direction);
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		boolean success=proficiencyCheck(mob,0,auto);
		if(success && (targetR != null) && (outRoom !=null) )
		{
			CMMsg msg=CMClass.getMsg(mob,targetR,this,somanticCastCode(mob,targetR,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around, pointing in different directions.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(outRoom instanceof GridLocale)
					outRoom=((GridLocale)outRoom).prepareGridLocale(targetR,outRoom, direction);
				final int radius = (grid.xGridSize()*grid.yGridSize())+2;
				mob.tell("The directions are taking shape in your mind: \n\r" +
					CMLib.tracking().getTrailToDescription(targetR, new Vector<Room>(), CMLib.map().getExtendedRoomID(outRoom), false, false, radius, null,1));
			}
		}
		else
			beneficialVisualFizzle(mob,targetR,"<S-NAME> wave(s) <S-HIS-HER> hands around, looking more frustrated every second.");


		// return whether it worked
		return success;
	}
}
