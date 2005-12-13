package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Dance extends StdAbility
{
	public String ID() { return "Dance"; }
	public String name(){ return "a Dance";}
	public String displayText(){ return "("+danceOf()+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"DANCE","DA"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SONG;}
	public int usageType(){return USAGE_MOVEMENT;}
	public int maxRange(){return 2;}
	protected int invokerManaCost=-1;

	protected boolean skipStandardDanceInvoke(){return false;}
	protected boolean mindAttack(){return quality()==Ability.MALICIOUS;}
	protected boolean skipStandardDanceTick(){return false;}
	protected String danceOf(){return name();}

	public int prancerLevel()
	{
		if(invoker()==null) return CMLib.ableMapper().lowestQualifyingLevel(ID());
		int x=CMLib.ableMapper().qualifyingClassLevel(invoker(),this);
		if(x<=0) x=CMLib.ableMapper().lowestQualifyingLevel(ID());
		int charisma=(invoker().charStats().getStat(CharStats.CHARISMA)-10);
		if(charisma>10)
			return x+((charisma-10)/3);
		return x;
	}

	protected int affectType(boolean auto){
		int affectType=CMMsg.MASK_MAGIC|CMMsg.MSG_CAST_SOMANTIC_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=affectType|CMMsg.MASK_MALICIOUS;
		if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
		return affectType;
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(skipStandardDanceTick())
			return true;

		if(affected==null) return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((invoker==null)
			||(invoker.location()!=mob.location())
			||(CMLib.flags().isSitting(invoker()))
			||(!CMLib.flags().aliveAwakeMobile(mob,true))
			||(!CMLib.flags().aliveAwakeMobile(invoker(),true))
			||(invoker.fetchEffect(ID())==null)
			||(!CMLib.flags().canBeSeenBy(invoker,mob)))
			{
				undance(mob,null,false);
				return false;
			}
			if(invokerManaCost<0) invokerManaCost=usageCost(invoker())[1];
			if(!mob.curState().adjMovement(-(invokerManaCost/15),mob.maxState()))
			{
				mob.tell("The dancing exhausts you.");
				undance(mob,null,false);
				return false;
			}
		}
		return true;
	}

	protected void undance(MOB mob, MOB invoker, boolean notMe)
	{
		if(mob==null) return;
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)
			&&(A instanceof Dance)
			&&((!notMe)||(!A.ID().equals(ID())))
			&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
				A.unInvoke();
		}
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((invoker()!=null)
		&&(!unInvoked)
		&&(affected==invoker())
		&&(msg.amISource(invoker()))
		&&(msg.target() instanceof Armor)
		&&(msg.targetMinor()==CMMsg.TYP_WEAR))
			unInvoke();
		super.executeMsg(host,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fumble(s) the "+name()+" due to <S-HIS-HER> armor!");
			return false;
		}

		if(skipStandardDanceInvoke())
			return true;

		if((!auto)&&(!CMLib.flags().aliveAwakeMobile(mob,false)))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		undance(mob,null,true);

		if(success)
		{
			String str=auto?"^SThe "+danceOf()+" begins!^?":"^S<S-NAME> begin(s) to dance the "+danceOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+danceOf()+" over again.^?";

			CMMsg msg=CMClass.getMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Dance newOne=(Dance)this.copyOf();
				newOne.invoker=mob;
				newOne.invokerManaCost=-1;

				HashSet h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				if(!h.contains(mob)) h.add(mob);

				Room R=mob.location();
				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();
					Room R2=follower.location();

					// malicious dances must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
					if((quality()==Ability.MALICIOUS)&&(follower!=mob))
						affectType=affectType|CMMsg.MASK_MALICIOUS;
					if(auto) affectType=affectType|CMMsg.MASK_GENERAL;

					if((R!=null)&&(R2!=null)&&(CMLib.flags().canBeSeenBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
					{
						CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
						CMMsg msg3=msg2;
						if((mindAttack())&&(follower!=mob))
							msg2=CMClass.getMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
						if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
						{
							R2.send(follower,msg2);
							if(msg2.value()<=0)
							{
								R2.send(follower,msg3);
								if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
								{
									undance(follower,null,false);
									newOne.setBorrowed(follower,true);
									if(follower!=mob)
										follower.addEffect((Ability)newOne.copyOf());
									else
										follower.addEffect(newOne);
								}
							}
						}
					}
				}
				R.recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> make(s) a false step.");

		return success;
	}
}
