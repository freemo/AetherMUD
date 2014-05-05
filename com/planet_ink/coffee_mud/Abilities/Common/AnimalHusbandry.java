package com.planet_ink.coffee_mud.Abilities.Common;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public class AnimalHusbandry extends CommonSkill
{
	@Override public String ID() { return "AnimalHusbandry"; }
	private final static String localizedName = CMLib.lang()._("Animal Husbandry");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"HUSBAND","ANIMALHUSBANDRY"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ANIMALAFFINITY; }

	protected MOB[] husbanding=null;
	protected boolean messedUp=false;

	public AnimalHusbandry()
	{
		super();
		displayText="You are, well, husbanding ...";
		verb="husbanding";
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((husbanding==null)||(mob.location()==null))
			{
				messedUp=true;
				unInvoke();
			}
			for(final MOB husbandM : husbanding)
			{
				if((husbandM==null)||(mob.location()==null))
				{
					messedUp=true;
					unInvoke();
				}
				if(!mob.location().isInhabitant(husbandM))
				{
					messedUp=true;
					unInvoke();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				final MOB mob=(MOB)affected;
				if((husbanding!=null)&&(!aborted))
				{
					final MOB husbandM=husbanding[0];
					final MOB wifeM=husbanding[1];
					if((husbandM==null)||(wifeM==null))
						commonTell(mob,_("You've failed to husband properly..."));
					else
					if(wifeM.fetchEffect("Pregnancy")!=null)
						commonTell(mob,_("@x1 is already pregnant.",wifeM.name()));
					else
					{
						final Social S=CMLib.socials().fetchSocial("MATE", wifeM, true);
						if(S!=null)
						{
							if(husbanding[0].charStats().getMyRace().canBreedWith(husbanding[1].charStats().getMyRace()))
							{
								Ability A=CMClass.getAbility("Chant_Fertility");
								if(A!=null) A.startTickDown(husbandM, wifeM, 2);
								A=CMClass.getAbility("Chant_Fertility");
								if(A!=null) A.startTickDown(wifeM, husbandM, 2);
							}
							S.invoke(husbandM, new XVector<String>("MATE",wifeM.name()), wifeM, false);
						}
						else
						{
							if(proficiencyCheck(mob, 0, false))
							{
								final Ability A=CMClass.getAbility("Pregnancy");
								if((A!=null)
								&&(wifeM.fetchAbility(A.ID())==null)
								&&(wifeM.fetchEffect(A.ID())==null))
								{
									A.invoke(husbandM,wifeM,true,0);
								}
							}
						}
						mob.location().show(mob,husbandM,wifeM,getActivityMessageType(),_("<S-NAME> manage(s) to coax <T-NAME> into doing <T-HIS-HER> duty towards <O-NAME>."));
						final Ability A=wifeM.fetchEffect("Pregnancy");
						if(A!=null)
						{
							A.makeNonUninvokable();
							A.setSavable(true);
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;

		verb="husbanding";
		verb="taming";
		husbanding=new MOB[2];
		if(!CMLib.law().doesHavePriviledgesHere(mob, mob.location()))
		{
			commonTell(mob,_("You need to be in your own pasture to do this."));
			return false;
		}
		if(commands.size()<2)
		{
			commonTell(mob,_("Which animals should I husband here?"));
			return false;
		}
		final String[] names=new String[]{(String)commands.get(0),CMParms.combine(commands,1)};
		int highestLevel=0;
		for(final String name : names)
		{
			final MOB M=mob.location().fetchInhabitant(name);
			if(M==null)
			{
				commonTell(mob,_("You don't see anyone called '@x1' here.",name));
				return false;
			}
			else
			{
				if(!CMLib.flags().canBeSeenBy(M,mob))
				{
					commonTell(mob,_("You don't see anyone called '@x1' here.",name));
					return false;
				}
				if((!M.isMonster())||(!CMLib.flags().isAnimalIntelligence(M)))
				{
					commonTell(mob,_("You can't use @x1.",M.name(mob)));
					return false;
				}
				if(M.fetchEffect("Pregnancy")!=null)
				{
					commonTell(mob,_("@x1 is already pregnant.",M.name(mob)));
					return false;
				}
				if(!M.charStats().getMyRace().canBreedWith(M.charStats().getMyRace()))
				{
					commonTell(mob,_("You can't use @x1.",M.name(mob)));
					return false;
				}
				if((!CMLib.flags().canMove(M))||(CMLib.flags().isBoundOrHeld(M)))
				{
					commonTell(mob,_("@x1 doesn't seem willing to cooperate.",M.name(mob)));
					return false;
				}
				if(M.charStats().getStat(CharStats.STAT_GENDER)=='M')
				{
					if(husbanding[0]!=null)
					{
						commonTell(mob,_("You can't use two males!"));
						return false;
					}
					husbanding[0]=M;
				}
				else
				if(M.charStats().getStat(CharStats.STAT_GENDER)=='F')
				{
					if(!M.isGeneric())
					{
						commonTell(mob,_("I'm sorry, @x1 just won't work out as a mother.",M.name(mob)));
						return false;
					}
					if(husbanding[1]!=null)
					{
						commonTell(mob,_("You can't use two females!"));
						return false;
					}
					husbanding[1]=M;
				}
				else
				{
					commonTell(mob,_("You can't use @x1 -- it's neuter!",M.name(mob)));
					return false;
				}
				if(M.phyStats().level()>highestLevel)
					highestLevel=M.phyStats().level();
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,-highestLevel+(2*getXLEVELLevel(mob)),auto);
		final int duration=getDuration(55,mob,highestLevel,20);
		verb="husbanding "+husbanding[0].name()+" to "+husbanding[1].name();
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),_("<S-NAME> start(s) @x1.",verb));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
