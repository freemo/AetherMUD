package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClimbableSurface extends StdRoom
{
	public ClimbableSurface()
	{
		super();
		name="the surface";
		baseEnvStats.setWeight(4);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_ROCKS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new ClimbableSurface();
	}
	
	public void mountLadder(MOB mob, Rideable ladder)
	{
		String mountStr=ladder.mountString(Affect.TYP_MOUNT);
		FullMsg msg=new FullMsg(mob,ladder,null,Affect.MSG_MOUNT,"<S-NAME> "+mountStr+" <T-NAMESELF>.");
		Room room=(Room)((Item)ladder).owner();
		if(mob.location()==room) room=null;
		if((mob.location().okAffect(msg))
		&&((room==null)||(room.okAffect(msg))))
		{
			mob.location().send(mob,msg);
			if(room!=null)
				room.sendOthers(mob,msg);
		}
	}
	
	public Rideable findALadder(MOB mob, Room room)
	{
		if(room==null) return null;
		if(mob.riding()!=null) return null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I!=null)
			   &&(I instanceof Rideable)
			   &&(Sense.canBeSeenBy(I,mob))
			   &&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_LADDER))
				return (Rideable)I;
		}
		return null;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(Sense.isSleeping(this)) return true;

		if(affect.amITarget(this)
		&&(Util.bset(affect.targetCode(),Affect.MASK_MOVE))
		&&(!Sense.isFalling(affect.source()))
		&&(!Sense.isClimbing(affect.source()))
		&&(!Sense.isInFlight(affect.source())))
		{
			Rideable ladder=findALadder(affect.source(),this);
			if(ladder!=null)
				mountLadder(affect.source(),ladder);
			if(!Sense.isClimbing(affect.source()))
			{
				affect.source().tell("You need to climb that way, if you know how.");
				return false;
			}
		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if(Sense.isSleeping(this)) return;

		if((affect.target() instanceof Item)
		   &&((!(affect.target() instanceof Rideable))
			  ||(((Rideable)affect.target()).rideBasis()!=Rideable.RIDEABLE_LADDER))
		   &&(!Sense.isFlying(affect.target()))
			  &&(affect.targetMinor()==Affect.TYP_DROP))
		{
			Ability falling=CMClass.getAbility("Falling");
			falling.setAffectedOne(this);
			falling.invoke(null,null,affect.target(),true);
		}
		else
		if(affect.amITarget(this)
			&&(Util.bset(affect.targetCode(),Affect.MASK_MOVE))
			&&(!Sense.isFalling(affect.source())))
		{
			MOB mob=affect.source();
			if(isInhabitant(mob))
			{
				if((!Sense.isInFlight(mob))
				&&(!Sense.isClimbing(mob))
				&&(getRoomInDir(Directions.DOWN)!=null)
				&&(getExitInDir(Directions.DOWN)!=null)
				&&(getExitInDir(Directions.DOWN).isOpen()))
				{
					Rideable ladder=findALadder(mob,this);
					if(ladder!=null)
						mountLadder(mob,ladder);
					if(!Sense.isClimbing(mob))
					{
						ladder=findALadder(mob,getRoomInDir(Directions.DOWN));
						if(ladder!=null)
						{
							ExternalPlay.look(mob,null,false);
							mountLadder(mob,ladder);
						}
						if(Sense.isClimbing(mob))
							ExternalPlay.move(mob,Directions.DOWN,false,true);
						else
						{
							Ability falling=CMClass.getAbility("Falling");
							falling.setAffectedOne(null);
							falling.invoke(null,null,mob,true);
						}
					}
				}
			}
		}
	}
}
