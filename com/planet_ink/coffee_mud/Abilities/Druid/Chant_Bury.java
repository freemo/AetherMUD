package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Bury extends Chant
{
	public String ID() { return "Chant_Bury"; }
	public String name(){ return "Earthfeed";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance()	{	return new Chant_Bury();}

	public static Item getBody(Room R)
	{
		if(R!=null)
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.fetchItem(i);
			if((I!=null)&&(I instanceof DeadBody))
				return I;
		}
		return null;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This chant does not work here.");
			return false;
		}
		Item target=null;
		if((commands.size()==0)&&(!auto)&&(givenTarget==null))
			target=getBody(mob.location());
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!(target instanceof DeadBody))
		{
			mob.tell("You may only feed the dead to the earth.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> bur(ys) <T-HIM-HERSELF>.":"^S<S-NAME> chant(s) to <T-NAMESELF>, returning dust to dust.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.destroy();
				if((mob.getAlignment()>=350)&&(mob.getAlignment()<=650))
				{
					double exp=10.0;
					int levelLimit=CommonStrings.getIntVar(CommonStrings.SYSTEMI_EXPRATE);
					int levelDiff=mob.envStats().level()-target.envStats().level();
					if(levelDiff>levelLimit) exp=0.0;
					MUDFight.postExperience(mob,null,null,(int)Math.round(exp),false);
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
