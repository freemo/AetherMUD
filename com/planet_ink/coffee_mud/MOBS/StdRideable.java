package com.planet_ink.coffee_mud.MOBS;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class StdRideable extends StdMOB implements Rideable
{
	public String ID(){return "StdRideable";}
	protected int rideBasis=Rideable.RIDEABLE_LAND;
	protected int riderCapacity=2;
	protected Vector riders=new Vector();
	public StdRideable()
	{
		super();
		Username="a horse";
		setDescription("A brown riding horse looks sturdy and reliable.");
		setDisplayText("a horse stands here.");
		baseCharStats().setMyRace(CMClass.getRace("Horse"));
		baseEnvStats().setWeight(700);
		recoverEnvStats();
	}

	protected void cloneFix(MOB E)
	{
		super.cloneFix(E);
		riders=new Vector();
	}
	public DeadBody killMeDead(boolean createBody)
	{
		while(riders.size()>0)
		{
			Rider mob=fetchRider(0);
			if(mob!=null)
			{
				mob.setRiding(null);
				delRider(mob);
			}
		}
		return super.killMeDead(createBody);
	}
	public void destroy()
	{
		while(riders.size()>0)
		{
			Rider mob=fetchRider(0);
			if(mob!=null)
			{
				mob.setRiding(null);
				delRider(mob);
			}
		}
		super.destroy();
	}

	public boolean isMobileRideBasis()
	{
		switch(rideBasis()){
			case RIDEABLE_SIT:
			case RIDEABLE_TABLE:
			case RIDEABLE_ENTERIN:
			case RIDEABLE_SLEEP:
			case RIDEABLE_LADDER:
				return false;
		}
		return true;
	}
	
	public boolean savable()
	{
		Rider R=null;
		for(int r=0;r<numRiders();r++)
		{
			R=fetchRider(r);
			if(!R.savable())
				return false;
		}
		return super.savable();
	}
	
	// common item/mob stuff
	public int rideBasis(){return rideBasis;}
	public void setRideBasis(int basis){rideBasis=basis;}
	public int riderCapacity(){ return riderCapacity;}
	public void setRiderCapacity(int newCapacity){riderCapacity=newCapacity;}
	public int numRiders(){return riders.size();}
	public boolean mobileRideBasis(){return true;}
	public Rider fetchRider(int which)
	{
		try	{ return (Rider)riders.elementAt(which);	}
		catch(java.lang.ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public String putString(Rider R)
	{
		return "on";
	}
	public void addRider(Rider mob)
	{
		if((mob!=null)&&(!riders.contains(mob)))
			riders.addElement(mob);
	}
	public void delRider(Rider mob)
	{
		if(mob!=null)
			while(riders.removeElement(mob));
	}
	public void recoverEnvStats()
	{
		super.recoverEnvStats();
		if(rideBasis==Rideable.RIDEABLE_AIR)
			envStats().setDisposition(envStats().disposition()|EnvStats.IS_FLYING);
		else
		if(rideBasis==Rideable.RIDEABLE_WATER)
			envStats().setDisposition(envStats().disposition()|EnvStats.IS_SWIMMING);
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(amRiding(affected))
			for(int a=0;a<numEffects();a++)
			{
				Ability A=fetchEffect(a);
				if((A!=null)&&(A.bubbleAffect()))
				   A.affectCharStats(affected,affectableStats);
			}
	}
	public void affectCharState(MOB affected, CharState affectableStats)
	{
		super.affectCharState(affected,affectableStats);
		if(amRiding(affected))
			for(int a=0;a<numEffects();a++)
			{
				Ability A=fetchEffect(a);
				if((A!=null)&&(A.bubbleAffect()))
				   A.affectCharState(affected,affectableStats);
			}
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(!CMLib.flags().hasSeenContents(this))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
			if(amRiding(mob))
			{
				if((mob.isInCombat())&&(mob.rangeToTarget()==0))
				{
					affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-mob.baseEnvStats().attackAdjustment());
					affectableStats.setDamage(affectableStats.damage()-mob.baseEnvStats().damage());
				}
				for(int a=0;a<numEffects();a++)
				{
					Ability A=fetchEffect(a);
					if((A!=null)&&(A.bubbleAffect()))
					   A.affectEnvStats(affected,affectableStats);
				}
			}
		}
	}
	public boolean amRiding(Rider mob)
	{
		return riders.contains(mob);
	}
	public String stateString(Rider R)
	{
		return "riding on";
	}
	public String mountString(int commandType, Rider R)
	{
		return "mount(s)";
	}
	public String dismountString(Rider R)
	{
		return "dismount(s)";
	}
	public String stateStringSubject(Rider R)
	{
		if((R instanceof Rideable)&&((Rideable)R).rideBasis()==Rideable.RIDEABLE_WAGON)
			return "pulling along";
		return "being ridden by";
	}

	public HashSet getRideBuddies(HashSet list)
	{
		if(list==null) return list;
		if(!list.contains(this)) list.add(this);
		for(int r=0;r<numRiders();r++)
		{
			Rider R=fetchRider(r);
			if((R instanceof MOB)
			&&(!list.contains(R)))
				list.add(R);
		}
		return list;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_DISMOUNT:
			if(msg.amITarget(this))
			{
				if((msg.tool()!=null)
				   &&(msg.tool() instanceof Rider))
				{
					if(!amRiding((Rider)msg.tool()))
					{
						msg.source().tell(msg.tool()+" is not "+stateString((Rider)msg.tool())+" "+name()+"!");
						if(((Rider)msg.tool()).riding()==this)
							((Rider)msg.tool()).setRiding(null);
						return false;
					}
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().tell("You are not "+stateString(msg.source())+" "+name()+"!");
					if(msg.source().riding()==this)
						msg.source().setRiding(null);
					return false;
				}
				// protects from standard mob rejection
				return true;
			}
			break;
		case CMMsg.TYP_SIT:
			if(amRiding(msg.source()))
			{
				msg.source().tell("You are "+stateString(msg.source())+" "+name()+"!");
				msg.source().setRiding(this);
				return false;
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell("You cannot simply sit on "+name()+", try 'mount'.");
				return false;
			}
			else
			if(msg.source() instanceof Rideable)
			{
				msg.source().tell("You are not allowed on "+name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_SLEEP:
			if(amRiding(msg.source()))
			{
				msg.source().tell("You are "+stateString(msg.source())+" "+name()+"!");
				msg.source().setRiding(this);
				return false;
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell("You cannot lie down on "+name()+".");
				return false;
			}
			else
			if((msg.source() instanceof Rideable)&&(msg.source()!=this))
			{
				msg.source().tell("You are not allowed on "+name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_MOUNT:
			if(amRiding(msg.source()))
			{
				msg.source().tell(null,msg.source(),null,"<T-NAME> <T-IS-ARE> "+stateString(msg.source())+" "+name()+"!");
				msg.source().setRiding(this);
				return false;
			}
		    if(msg.amITarget(this))
		    {
		        Rider whoWantsToRide=(msg.tool() instanceof Rider)?(Rider)msg.tool():msg.source();
				if(amRiding(whoWantsToRide))
				{
					msg.source().tell(whoWantsToRide.name()+" is "+stateString(whoWantsToRide)+" "+name()+"!");
					whoWantsToRide.setRiding(this);
					return false;
				}
				if((msg.tool() instanceof MOB)&&(!CMLib.flags().isBoundOrHeld(msg.tool())))
			    {
					msg.source().tell(msg.tool().name()+" won't let you do that.");
					return false;
				}
				if(riding()==whoWantsToRide)
				{
					msg.source().tell(msg.tool().name()+" can not be mounted to "+name()+"!");
					return false;
				}
				if((msg.tool() instanceof Rideable)&&(msg.tool() instanceof MOB))
				{
					msg.source().tell(msg.tool().name()+" is not allowed on "+name()+".");
					return false;
				}
				if((msg.tool() instanceof Rideable)
				&&(msg.tool() instanceof Item)
				&&(((Rideable)msg.tool()).rideBasis()!=Rideable.RIDEABLE_WAGON))
				{
					msg.source().tell(msg.tool().name()+" can not be mounted on "+name()+".");
					return false;
				}
				if((baseEnvStats().weight()*5<whoWantsToRide.baseEnvStats().weight()))
				{
					msg.source().tell(name()+" is too small for "+whoWantsToRide.name()+".");
					return false;
				}
				if((numRiders()>=riderCapacity())
				&&(!amRiding(whoWantsToRide)))
				{
					// for items
					msg.source().tell("No more can fit on "+name()+".");
					// for mobs
					// msg.source().tell("No more can fit on "+name()+".");
					return false;
				}
				// protects from standard item rejection
				return true;
		    }
			break;
		case CMMsg.TYP_ENTER:
			if(amRiding(msg.source())
			   &&(msg.target()!=null)
			   &&(msg.target() instanceof Room))
			{
				Room sourceRoom=msg.source().location();
				Room targetRoom=(Room)msg.target();
				if((sourceRoom!=null)&&(!msg.amITarget(sourceRoom)))
				{
					boolean ok=((targetRoom.domainType()&Room.INDOORS)==0)
								||(targetRoom.maxRange()>4);
					switch(rideBasis)
					{
					case Rideable.RIDEABLE_LAND:
						if((targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
							ok=false;
						break;
					case Rideable.RIDEABLE_AIR:
						break;
					case Rideable.RIDEABLE_WATER:
						if((sourceRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
						&&(targetRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
						&&(sourceRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
						&&(targetRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))
							ok=false;
						if((targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
							ok=false;
						break;
					}
					if(!ok)
					{
						msg.source().tell("You cannot ride "+name()+" that way.");
						return false;
					}
					if(CMLib.flags().isSitting(msg.source()))
					{
						msg.source().tell("You cannot crawl while "+stateString(msg.source())+" "+name()+".");
						return false;
					}
				}
			}
			break;
		case CMMsg.TYP_GIVE:
			if(msg.target() instanceof MOB)
			{
				MOB tmob=(MOB)msg.target();
				if((amRiding(tmob))&&(!amRiding(msg.source())))
				{
					msg.source().tell(msg.source(),tmob,null,"<T-NAME> must dismount first.");
					return false;
				}
			}
			break;
		case CMMsg.TYP_BUY:
		case CMMsg.TYP_BID:
		case CMMsg.TYP_SELL:
			if(amRiding(msg.source()))
			{
				msg.source().tell("You cannot do that while "+stateString(msg.source())+" "+name()+".");
				return false;
			}
			break;
		}
		if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
		&&(amRiding(msg.source()))
		&&((msg.sourceMessage()!=null)||(msg.othersMessage()!=null))
		&&(((!CMLib.utensils().reachableItem(msg.source(),msg.target())))
			|| ((!CMLib.utensils().reachableItem(msg.source(),msg.tool())))
			|| ((msg.sourceMinor()==CMMsg.TYP_GIVE)&&(msg.target()!=null)&&(msg.target() instanceof MOB)&&(msg.target()!=this)&&(!amRiding((MOB)msg.target())))))
		{
			msg.source().tell("You cannot do that while "+stateString(msg.source())+" "+name()+".");
			return false;
		}
		if(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		{
			if((msg.amITarget(this))
			   &&((msg.source().riding()==this)
				  ||(this.amRiding(msg.source()))))
			{
				msg.source().tell("You can't attack "+name()+" right now.");
				if(getVictim()==msg.source()) setVictim(null);
				if(msg.source().getVictim()==this) msg.source().setVictim(null);
				return false;
			}
			else
			if((msg.amISource(this))
			   &&(msg.target()!=null)
			   &&(msg.target() instanceof MOB)
			   &&((amRiding((MOB)msg.target()))
				  ||(((MOB)msg.target()).riding()==this)))

			{
				MOB targ=(MOB)msg.target();
				tell("You can't attack "+targ.name()+" right now.");
				if(getVictim()==targ) setVictim(null);
				if(targ.getVictim()==this) targ.setVictim(null);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		switch(msg.targetMinor())
		{
        case CMMsg.TYP_LOOK:
        case CMMsg.TYP_EXAMINE:
            if((msg.target()==this)
            &&(numRiders()>0)
            &&(CMLib.flags().canBeSeenBy(this,msg.source())))
                msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,displayText(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
            break;
		case CMMsg.TYP_DISMOUNT:
			if((msg.tool()!=null)
			   &&(msg.tool() instanceof Rider))
			{
				((Rider)msg.tool()).setRiding(null);
				if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
			}
			else
			if(amRiding(msg.source()))
			{
				msg.source().setRiding(null);
				if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_MOUNT:
			if(msg.amITarget(this))
			{
				if((msg.tool()!=null)
				   &&(msg.tool() instanceof Rider))
				{
					((Rider)msg.tool()).setRiding(this);
					if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().setRiding(this);
					if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
				}
			}
			break;
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_QUIT:
		case CMMsg.TYP_PANIC:
		case CMMsg.TYP_DEATH:
			if(amRiding(msg.source()))
			{
			   msg.source().setRiding(null);
				if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
			}
			break;
		}
	}
}
