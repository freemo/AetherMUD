package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class VeryAggressive extends Aggressive
{
	public String ID(){return "VeryAggressive";}
	public Behavior newInstance()
	{
		return new VeryAggressive();
	}
	
	public boolean grantsAggressivenessTo(MOB M)
	{
		return !ExternalPlay.zapperCheck(getParms(),M);
	}

	public static void tickVeryAggressively(Environmental ticking, 
											int tickID,
											Behavior B)
	{
		if(tickID!=Host.MOB_TICK) return;
		if(!canFreelyBehaveNormal(ticking)) return;
		MOB mob=(MOB)ticking;
		
		// ridden things dont wander!
		if(ticking instanceof Rideable)
			if(((Rideable)ticking).numRiders()>0)
				return;
		
		if(((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location()))
		||(!Sense.canTaste(mob)))
		   return;

		// let's not do this 100%
		if(Dice.rollPercentage()>15) return;
		
		Room thisRoom=mob.location();
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.isASysOp(thisRoom)))
				return;
		}

		int dirCode=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=thisRoom.getRoomInDir(d);
			Exit exit=thisRoom.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(room.getArea().name().equals(thisRoom.getArea().name())))
			{
				if(exit.isOpen())
				{
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB inhab=room.fetchInhabitant(i);
						if((inhab!=null)
						&&(!inhab.isMonster())
						&&(inhab.envStats().level()<(mob.envStats().level()+11))
						&&(inhab.envStats().level()>(mob.envStats().level()-11)))
						{
							dirCode=d;
							break;
						}
					}
				}
			}
			if(dirCode>=0) break;
		}
		if(dirCode>=0)
		{
			ExternalPlay.move(mob,dirCode,false,false);
			pickAFight(mob,B);
		}
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		tickVeryAggressively(ticking,tickID,this);
	}
}
