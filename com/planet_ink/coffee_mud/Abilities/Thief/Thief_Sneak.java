package com.planet_ink.coffee_mud.Abilities.Thief;

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
public class Thief_Sneak extends ThiefSkill
{
	public String ID() { return "Thief_Sneak"; }
	public String name(){ return "Sneak";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SNEAK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String dir=Util.combine(commands,0);
		if(commands.size()>0) dir=(String)commands.lastElement();
		int dirCode=Directions.getGoodDirectionCode(dir);
		if(dirCode<0)
		{
			mob.tell("Sneak where?");
			return false;
		}

		if((mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
		{
			mob.tell("Sneak where?");
			return false;
		}

		HashSet H=mob.getGroupMembers(new HashSet());
		int highestLevel=0;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)&&((M!=mob)&&(!H.contains(M)))&&(highestLevel<M.envStats().level()))
				highestLevel=mob.envStats().level();
		}
		int levelDiff=mob.envStats().level()-highestLevel;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=false;
		FullMsg msg=new FullMsg(mob,null,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_DELICATE_HANDS_ACT,"You quietly sneak "+Directions.getDirectionName(dirCode)+".",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(levelDiff<0)
				levelDiff=levelDiff*8;
			else
				levelDiff=levelDiff*10;
			success=profficiencyCheck(mob,levelDiff,auto);
			if(success)
			{
				mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|EnvStats.IS_SNEAKING);
				mob.recoverEnvStats();
			}
			MUDTracker.move(mob,dirCode,false,false);
			if(success)
			{

				int disposition=mob.baseEnvStats().disposition();
				if((disposition&EnvStats.IS_SNEAKING)>0)
				{
					mob.baseEnvStats().setDisposition(disposition-EnvStats.IS_SNEAKING);
					mob.recoverEnvStats();
				}
				Ability toHide=mob.fetchAbility("Thief_Hide");
				if(toHide==null) toHide=mob.fetchAbility("Ranger_Hide");
				if(toHide!=null)
					toHide.invoke(mob,new Vector(),null,false,asLevel);
			}
			if(Sense.isSneaking(mob))
				mob.envStats().setDisposition(mob.envStats().disposition()-EnvStats.IS_SNEAKING);
		}
		return success;
	}

}
