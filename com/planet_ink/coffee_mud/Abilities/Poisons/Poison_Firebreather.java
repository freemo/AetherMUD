package com.planet_ink.coffee_mud.Abilities.Poisons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Poison_Firebreather extends Poison_Liquor
{
	public String ID() { return "Poison_Firebreather"; }
	public String name(){ return "Firebreather";}
	private static final String[] triggerStrings = {"LIQUORFIRE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Poison_Firebreather();}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;
		if(!(affected instanceof MOB)) return super.tick(ticking,tickID);

		MOB mob=(MOB)affected;
		Room room=mob.location();
		if((Dice.rollPercentage()<15)&&(Sense.aliveAwakeMobile(mob,true))&&(room!=null))
		{
			if(Dice.rollPercentage()<40)
			{
				if(room.numInhabitants()==1)
					room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> belch(es) fire!");
				else
				if(room.show(mob,null,this,Affect.MSG_NOISYMOVEMENT,"<S-NAME> belch(es) fire!"))
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB target=room.fetchInhabitant(i);

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_FIRE,null);
					if((mob!=target)&&(mob.mayPhysicallyAttack(target))&&(room.okAffect(mob,msg)))
					{
						room.send(mob,msg);
						invoker=mob;

						int damage = 0;
						int maxDie =  mob.envStats().level();
						if (maxDie > 10)
							maxDie = 10;
						damage += Dice.roll(maxDie,6,1);
						if(msg.wasModified())
							damage = (int)Math.round(Util.div(damage,2.0));
						ExternalPlay.postDamage(mob,target,this,damage,Affect.MASK_GENERAL|Affect.MASK_SOUND|Affect.TYP_FIRE,Weapon.TYPE_BURNING,"^FThe fire <DAMAGE> <T-NAME>!^?");
					}
				}
			}
			else
				room.show(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> belch(es) smoke!");
			disableHappiness=true;
		}
		return super.tick(ticking,tickID);
	}
}