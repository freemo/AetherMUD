package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/*
   Copyright 2014-2016 Bo Zimmerman

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
public class Ranger_FierceCompanions extends StdAbility
{
	@Override public String ID() { return "Ranger_FierceCompanions"; }
	private final static String localizedName = CMLib.lang().L("Fierce Companions");
	@Override public String name() { return localizedName; }
	private final static String localizedDisplayText = CMLib.lang().L("(Fierce Companions)");
	@Override public String displayText() { return activated ? localizedDisplayText : ""; }
	@Override public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;}
	@Override public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	@Override public int enchantQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public boolean isAutoInvoked(){return true;}
	@Override public boolean canBeUninvoked(){return false;}
	protected volatile boolean activated=false;
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB) && (activated))
		{
			final MOB mob=(MOB)affected;
			final int level=2+(adjustedLevel(mob,0)/10);
			final double damBonus=CMath.mul(CMath.div(proficiency(),100.0),level);
			final double attBonus=CMath.mul(CMath.div(proficiency(),100.0),2*level);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(attBonus));
			affectableStats.setDamage(affectableStats.damage()+(int)Math.round(damBonus));
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		boolean found=false;
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&(((MOB)affected).isInCombat()))
		{
			final Set<MOB> companions=((MOB)affected).getGroupMembers(new HashSet<MOB>());
			for(MOB M : companions)
				if((M!=affected)&&(CMLib.flags().isAnimalIntelligence(M))&&(M.location()==((MOB)affected).location())&&(!M.amDead())&&(M.isInCombat()))
				{
					found=true;
					break;
				}
			if(CMLib.dice().rollPercentage()==1)
				super.helpProficiency((MOB)affected, 0);
		}
		if(found != activated)
		{
			activated=found;
			affected.recoverPhyStats();
		}
		return true;
	}
}
