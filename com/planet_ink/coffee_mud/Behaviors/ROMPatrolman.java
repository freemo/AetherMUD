package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ROMPatrolman extends StdBehavior
{
	public String ID(){return "ROMPatrolman";}
	int tickTock=0;
	public Behavior newInstance()
	{
		return new ROMPatrolman();
	}

	public static void keepPeace(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		MOB victim=null;
		for(int i=0;i<observer.location().numInhabitants();i++)
		{
			MOB inhab=observer.location().fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
			{
				if(inhab.envStats().level()>inhab.getVictim().envStats().level())
					victim=inhab;
				else
					victim=inhab.getVictim();
			}
		}


		if(victim==null) return;
		if(BrotherHelper.isBrother(victim,observer)) return;
		observer.location().show(observer,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> blow(s) down hard on <S-HIS-HER> whistle. ***WHEEEEEEEEEEEET***");
		Vector V=observer.location().getArea().getMyMap();
		for(int r=0;r<V.size();r++)
		{
			Room room=(Room)V.elementAt(r);
			if((room!=observer.location())&&(room.numPCInhabitants()>0))
				room.showHappens(Affect.MSG_NOISE,"You hear a shrill whistling sound in the distance.");
		}
 
		Item weapon=observer.fetchWieldedItem();
		if(weapon==null) weapon=observer.myNaturalWeapon();
		boolean makePeace=false;
		boolean fight=false;
		switch(Dice.roll(1,7,-1))
		{
		case 0:
			observer.location().show(observer,null,Affect.MSG_SPEAK,"^T<S-NAME> yell(s) 'All roit! All roit! break it up!'^?");
			makePeace=true;
			break;
		case 1:
			observer.location().show(observer,null,Affect.MSG_SPEAK,"^T<S-NAME> sigh(s) 'Society's to blame, but what's a bloke to do?'^?");
			fight=true;
			break;
		case 2:
			observer.location().show(observer,null,Affect.MSG_SPEAK,"^T<S-NAME> mumble(s) 'bloody kids will be the death of us all.'^?");
			break;
		case 3:
			observer.location().show(observer,null,Affect.MSG_SPEAK,"^T<S-NAME> yell(s) 'Stop that! Stop that!' and attack(s).^?");
			fight=true;
			break;
		case 4:
			observer.location().show(observer,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> pull(s) out his billy and go(es) to work.");
			fight=true;
			break;
		case 5:
			observer.location().show(observer,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> sigh(s) in resignation and proceed(s) to break up the fight.");
			makePeace=true;
			break;
		case 6:
			observer.location().show(observer,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) 'Settle down, you hooligans!'^?");
			break;
		 }
		
		if(makePeace)
		{
			Room room=observer.location();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if((inhab!=null)
				&&(inhab.isInCombat())
				&&(inhab.getVictim().isInCombat())
				&&((observer.envStats().level()>(inhab.envStats().level()+5))
				&&(observer.getAlignment()>350)))
				{
					String msg="<S-NAME> stop(s) <T-NAME> from fighting with "+inhab.getVictim().name();
					FullMsg msgs=new FullMsg(observer,inhab,Affect.MSG_NOISYMOVEMENT,msg);
					if(observer.location().okAffect(msgs))
					{
						inhab.getVictim().makePeace();
						inhab.makePeace();
					}
				}
			}
		}
		else
		if(fight)
			ExternalPlay.postAttack(observer,victim,weapon);
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return;
		MOB mob=(MOB)ticking;
		tickTock--;
		if(tickTock<=0)
		{
			tickTock=Dice.roll(1,3,0);
			keepPeace(mob);
		}
	}
}