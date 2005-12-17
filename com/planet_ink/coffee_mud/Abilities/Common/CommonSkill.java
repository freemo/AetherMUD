package com.planet_ink.coffee_mud.Abilities.Common;
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

public class CommonSkill extends StdAbility
{
	public String ID() { return "CommonSkill"; }
	public String name(){ return "Common Skill";}
	private static final String[] triggerStrings = empty;
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "";}

	public int quality(){return Ability.INDIFFERENT;}
	protected String displayText="(Doing something productive)";
	public String displayText(){return displayText;}

	protected int trainsRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_COMMONTRAINCOST);}
	protected int practicesRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_COMMONPRACCOST);}
	protected int practicesToPractice(){return 1;}

	protected boolean allowedWhileMounted(){return true;}
	
	protected Room activityRoom=null;
	protected boolean aborted=false;
	protected boolean helping=false;
    protected boolean bundling=false;
	protected CommonSkill helpingAbility=null;
	protected int tickUp=0;
	protected String verb="working";
    protected String playSound=null;
	public int usageType(){return USAGE_MOVEMENT;}

	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}

	public int classificationCode()	{	return Ability.COMMON_SKILL; }

	private int yield=1;
	public int abilityCode(){return yield;}
	public void setAbilityCode(int newCode){yield=newCode;}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())||(mob.location()!=activityRoom)||(!CMLib.flags().aliveAwakeMobileUnbound(mob,true)))
			{aborted=true; unInvoke(); return false;}
            String sound=(playSound!=null)?CMProps.msp(playSound,10):"";
			if(tickDown==4)
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> almost done "+verb+"."+sound);
			else
			if((tickUp%4)==0)
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> continue(s) "+verb+"."+sound);
			if((helping)
			&&(helpingAbility!=null)
			&&(helpingAbility.affected instanceof MOB)
			&&(((MOB)helpingAbility.affected).isMine(helpingAbility)))
				helpingAbility.tick(helpingAbility.affected,tickID);
			if((mob.soulMate()==null)&&(mob.playerStats()!=null)&&(mob.location()!=null))
			    mob.playerStats().adjHygiene(PlayerStats.HYGIENE_COMMONDIRTY);
		}
		tickUp++;
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(((MOB)affected).location()!=null))
			{
				MOB mob=(MOB)affected;
				if(aborted)
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> stop(s) "+verb+".");
				else
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> done "+verb+".");
				helping=false;
				helpingAbility=null;
			}
		}
		super.unInvoke();
	}

	protected void commonTell(MOB mob, Environmental target, Environmental tool, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
		{
			if(str.startsWith("You")) str="I"+str.substring(3);
			if(target!=null) str=CMStrings.replaceAll(str,"<T-NAME>",target.name());
			if(tool!=null)  str=CMStrings.replaceAll(str,"<O-NAME>",tool.name());
			CMLib.commands().say(mob,null,str,false,false);
		}
		else
			mob.tell(mob,target,tool,str);
	}

	protected void commonTell(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
		{
			if(str.startsWith("You")) str="I"+str.substring(3);
			CMLib.commands().say(mob,null,str,false,false);
		}
		else
			mob.tell(str);
	}

	protected void commonEmote(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_GENERAL,str);
		else
			mob.tell(mob,null,null,str);
	}

	protected int lookingFor(int material, Room fromHere)
	{
		Vector V=new Vector();
		V.addElement(new Integer(material));
		return lookingFor(V,fromHere);
	}

	protected int lookingFor(Vector materials, Room fromHere)
	{
		Vector possibilities=new Vector();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=fromHere.getRoomInDir(d);
			Exit exit=fromHere.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(exit.isOpen()))
			{
				int material=room.myResource();
				if(materials.contains(new Integer(material&EnvResource.MATERIAL_MASK)))
				{possibilities.addElement(new Integer(d));}
			}
		}
		if(possibilities.size()==0)
			return -1;
		return ((Integer)(possibilities.elementAt(CMLib.dice().roll(1,possibilities.size(),-1)))).intValue();
	}

	public Item getRequiredFire(MOB mob,int autoGenerate)
	{
		if(autoGenerate>0) return CMClass.getItem("StdItem");
		Item fire=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2!=null)&&(I2.container()==null)&&(CMLib.flags().isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			commonTell(mob,"A fire will need to be built first.");
			return null;
		}
		return fire;
	}

	protected int destroyResources(Room room,
	        					   int howMuch,
	        					   int finalMaterial,
	        					   int otherMaterial,
	        					   Item never,
	        					   int autoGeneration)
	{
		int lostValue=0;
		if(room==null) return 0;
		if(autoGeneration>0) return 0;

		if((howMuch>0)||(otherMaterial>0))
		for(int i=room.numItems()-1;i>=0;i--)
		{
			Item I=room.fetchItem(i);
			if(I==null) break;
			if(I==never) continue;

			if((otherMaterial>0)
			&&(I instanceof EnvResource)
			&&(I.container()==null)
			&&(!CMLib.flags().isOnFire(I))
			&&(!CMLib.flags().enchanted(I))
			&&(I.material()==otherMaterial))
			{
                if(I.baseEnvStats().weight()>1)
                {
                    I.baseEnvStats().setWeight(I.baseEnvStats().weight()-1);
                    Environmental E=null;
                    for(int x=0;x<I.baseEnvStats().weight();x++)
                    {
                        E=CMLib.utensils().makeResource(otherMaterial,-1,true);
                        if(E instanceof Item)
                            room.addItemRefuse((Item)E,Item.REFUSE_PLAYER_DROP);
                    }
                    if(E instanceof Item)
                        lostValue+=((Item)E).value();
                    I.destroy();
                }
                else
                {
                    lostValue+=I.value();
                    I.destroy();
                }
                otherMaterial=-1;
			}
			else
			if((I instanceof EnvResource)
			&&(I.container()==null)
			&&(!CMLib.flags().isOnFire(I))
			&&(!CMLib.flags().enchanted(I))
			&&(I.material()==finalMaterial))
			{
				if(I.baseEnvStats().weight()>howMuch)
				{
					I.baseEnvStats().setWeight(I.baseEnvStats().weight()-howMuch);
                    Environmental E=null;
					for(int x=0;x<I.baseEnvStats().weight();x++)
					{
						E=CMLib.utensils().makeResource(finalMaterial,-1,true);
						if(E instanceof Item)
							room.addItemRefuse((Item)E,Item.REFUSE_PLAYER_DROP);
					}
                    if(E instanceof Item)
                        lostValue+=(((Item)E).value()*howMuch);
                    I.destroy();
                    howMuch=0;
				}
				else
				{
					howMuch-=I.baseEnvStats().weight();
					I.destroy();
					lostValue+=I.value();
				}
                if(howMuch<=0)
                    finalMaterial=-1;
			}
		}
		return lostValue;
	}
	
	public int[] usageCost(MOB mob)
	{
		if(mob==null)
		{
			int[] usage=new int[3];
			usage[0]=overrideMana();
			usage[1]=overrideMana();
			usage[2]=overrideMana();
			return usage;
		}
		if(usageType()==Ability.USAGE_NADA) return new int[3];

		int consumed=25;
		int diff=mob.envStats().level()-CMLib.ableMapper().qualifyingLevel(mob,this);
		if(diff>0)
		switch(diff)
		{
		case 1: consumed=20; break;
		case 2: consumed=15; break;
		case 3: consumed=10; break;
		default: consumed=5; break;
		}
		if(overrideMana()>=0) consumed=overrideMana();
		return buildCostArray(mob,consumed);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			commonEmote(mob,"<S-NAME> <S-IS-ARE> in combat!");
			return false;
		}
		if((!allowedWhileMounted())&&(mob.riding()!=null))
		{
			commonEmote(mob,"You can't do that while "+mob.riding().stateString(mob)+" "+mob.riding().name()+".");
			return false;
		}
		
		if(!CMLib.flags().canBeSeenBy(mob.location(),mob))
		{
			commonTell(mob,"<S-NAME> can't see to do that!");
			return false;
		}
		if(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob))
		{
			commonTell(mob,"You need to stand up!");
			return false;
		}
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
			{
				if(A instanceof CommonSkill)
					((CommonSkill)A).aborted=true;
				A.unInvoke();
			}
		}
		isAnAutoEffect=false;

		// if you can't move, you can't do anything!
		if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
			return false;

		int[] consumed=usageCost(mob);
		if(mob.curState().getMana()<consumed[0])
		{
			if(mob.maxState().getMana()==consumed[0])
				mob.tell("You must be at full mana to do that.");
			else
				mob.tell("You don't have enough mana to do that.");
			return false;
		}
		mob.curState().adjMana(-consumed[0],mob.maxState());
		if(mob.curState().getMovement()<consumed[1])
		{
			if(mob.maxState().getMovement()==consumed[1])
				mob.tell("You must be at full movement to do that.");
			else
				mob.tell("You don't have enough movement to do that.  You are too tired.");
			return false;
		}
		mob.curState().adjMovement(-consumed[1],mob.maxState());
		if(mob.curState().getHitPoints()<consumed[2])
		{
			if(mob.maxState().getHitPoints()==consumed[2])
				mob.tell("You must be at full health to do that.");
			else
				mob.tell("You don't have enough hit points to do that.");
			return false;
		}
		mob.curState().adjHitPoints(-consumed[2],mob.maxState());
		activityRoom=mob.location();
		
        if(!bundling)
    		helpProfficiency(mob);

		return true;
	}
}
