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
   Copyright 2000-2006 Bo Zimmerman

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
public class Play extends StdAbility
{
	public String ID() { return "Play"; }
	public String name(){ return "a song played";}
	public String displayText(){ return "("+songOf()+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"PLAY","PL","PLA"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SONG;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int maxRange(){return 2;}

	protected int requiredInstrumentType(){return -1;}
	protected boolean skipStandardSongInvoke(){return false;}
	protected boolean mindAttack(){return quality()==Ability.MALICIOUS;}
	protected boolean skipStandardSongTick(){return false;}
	protected boolean persistantSong(){return true;}
	protected String songOf(){return name();}

	protected MusicalInstrument instrument=null;

	protected int affectType(boolean auto){
		int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_SOMANTIC_SPELL;
		if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
		return affectType;
	}

	public String instrumentName(){
		if(instrument!=null) return instrument.name();
		return "something";
	}

	public int invokerLevel()
	{
		if(invoker()!=null)
		{
			if(instrument!=null)
				return invoker().envStats().level()+instrument.envStats().ability();
			return invoker().envStats().level();
		}
		else
		if(affected!=null)
			return affected.envStats().level();
		else
			return 1;
	}

	protected void inpersistantAffect(MOB mob)
	{
	}

	public static boolean usingInstrument(MusicalInstrument I, MOB mob)
	{
		if((I==null)||(mob==null)) return false;
		if(I instanceof Rideable)
			return (((Rideable)I).amRiding(mob)
					&&(mob.fetchFirstWornItem(Item.WORN_WIELD)==null)
					&&(mob.fetchFirstWornItem(Item.WORN_HELD)==null));
		return mob.isMine(I)&&(!I.amWearingAt(Item.IN_INVENTORY));
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(skipStandardSongTick())
			return true;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if((invoker==null)
		||(invoker.fetchEffect(ID())==null)
		||((instrument!=null)&&(!usingInstrument(instrument,invoker)))
		||(invoker.location()!=mob.location())
		||(!CMLib.flags().aliveAwakeMobileUnbound(invoker,true))
		||(!CMLib.flags().canBeHeardBy(invoker,mob)))
		{
			unplay(mob,null,false);
			return false;
		}
		return true;
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected==invoker)
		&&(msg.amISource(invoker))
		&&(!unInvoked)
		&&(instrument!=null))
		{
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(instrument.amWearingAt(Item.WORN_MOUTH)))
				unInvoke();
			else
			if(((msg.sourceMinor()==CMMsg.TYP_REMOVE)
			   ||(msg.sourceMinor()==CMMsg.TYP_WEAR)
			   ||(msg.sourceMinor()==CMMsg.TYP_WIELD))
			&&(instrument.amWearingAt(Item.WORN_HELD)))
				unInvoke();
		}
	}

	protected void unplay(MOB mob, MOB invoker, boolean notMe)
	{
		if(mob==null) return;
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)
			&&(A instanceof Play)
			&&((!notMe)||(!A.ID().equals(ID())))
			&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
				A.unInvoke();
		}
	}

	public static MusicalInstrument getInstrument(MOB mob, int requiredInstrumentType, boolean noisy)
	{
		MusicalInstrument instrument=null;
		if((mob.riding()!=null)&&(mob.riding() instanceof MusicalInstrument))
		{
			if(!usingInstrument((MusicalInstrument)mob.riding(),mob))
			{
				if(noisy)
					mob.tell("You need to free your hands to play "+mob.riding().name()+".");
				return null;
			}
			instrument=(MusicalInstrument)mob.riding();
		}
		if(instrument==null)
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			&&(I instanceof MusicalInstrument)
			&&(I.container()==null)
			&&(usingInstrument((MusicalInstrument)I,mob)))
			{ instrument=(MusicalInstrument)I; break;}
		}
		if(instrument==null)
		{
			if(noisy)
				mob.tell("You need an instrument!");
			return null;
		}
		if((requiredInstrumentType>=0)&&(instrument.instrumentType()!=requiredInstrumentType))
		{
			if(noisy)
				mob.tell("This song can only be played on "+MusicalInstrument.TYPE_DESC[requiredInstrumentType].toLowerCase()+".");
			return null;
		}
		return instrument;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!auto)
		{
			instrument=getInstrument(mob,requiredInstrumentType(),true);
			if(instrument==null) return false;
			if((mob.riding()!=null)&&(mob.riding() instanceof MusicalInstrument))
			{
				if(!usingInstrument((MusicalInstrument)mob.riding(),mob))
				{
					mob.tell("You need to free your hands to play "+mob.riding().name()+".");
					return false;
				}
				instrument=(MusicalInstrument)mob.riding();
			}
			if(instrument==null)
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)
				&&(I instanceof MusicalInstrument)
				&&(I.container()==null)
				&&(usingInstrument((MusicalInstrument)I,mob)))
				{ instrument=(MusicalInstrument)I; break;}
			}
			if(instrument==null)
			{
				mob.tell("You need an instrument!");
				return false;
			}
			if((requiredInstrumentType()>=0)&&(instrument.instrumentType()!=requiredInstrumentType()))
			{
				mob.tell("This song can only be played on "+MusicalInstrument.TYPE_DESC[requiredInstrumentType()].toLowerCase()+".");
				return false;
			}
		}

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fumble(s) playing "+name()+" due to <S-HIS-HER> armor!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(skipStandardSongInvoke())
			return true;

		if((!auto)&&(!CMLib.flags().aliveAwakeMobileUnbound(mob,false)))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		unplay(mob,mob,true);
		if(success)
		{
			String str=auto?"^S"+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to play "+songOf()+" on "+instrumentName()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) playing "+songOf()+" on "+instrumentName()+" again.^?";

			CMMsg msg=CMClass.getMsg(mob,null,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Play newOne=(Play)this.copyOf();

				HashSet h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				if(!h.contains(mob)) h.add(mob);

				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();

					// malicious songs must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
					if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
					if((quality()==Ability.MALICIOUS)&&(follower!=mob))
						affectType=affectType|CMMsg.MASK_MALICIOUS;

					if((CMLib.flags().canBeHeardBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
					{
						CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
						CMMsg msg3=msg2;
						if((mindAttack())&&(follower!=mob))
							msg2=CMClass.getMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
						if((mob.location().okMessage(mob,msg2))&&(mob.location().okMessage(mob,msg3)))
						{
							follower.location().send(follower,msg2);
							if(msg2.value()<=0)
							{
								follower.location().send(follower,msg3);
								if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
								{
									if(persistantSong())
									{
										newOne.setSavable(false);
										if(follower!=mob)
											follower.addEffect((Ability)newOne.copyOf());
										else
											follower.addEffect(newOne);
									}
									else
										inpersistantAffect(follower);
								}
							}
						}
					}
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
