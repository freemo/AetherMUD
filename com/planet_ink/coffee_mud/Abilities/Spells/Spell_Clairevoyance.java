package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Clairevoyance extends Spell
{
	public String ID() { return "Spell_Clairevoyance"; }
	public String name(){return "Clairevoyance";}
	public String displayText(){return "(Clairevoyance)";}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Clairevoyance();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_DIVINATION;	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if(canBeUninvoked())
		if(invoker!=null)
			invoker.tell("Your visions of '"+mob.name()+"' fade.");
		super.unInvoke();

	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&(invoker!=null)
		&&(msg.target()!=null)
		&&((((MOB)invoker).location()!=((MOB)affected).location())||(!(msg.target() instanceof Room))))
		{
			FullMsg newAffect=new FullMsg(invoker,msg.target(),CMMsg.TYP_EXAMINESOMETHING,null);
			msg.target().executeMsg(msg.target(),newAffect);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			MOB M=null;
			int tries=0;
			while(((++tries)<100)&&(M==null))
			{
				Room R=CMMap.getRandomRoom();
				if(R.numInhabitants()>0)
					M=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
				if(M.name().equals(mob.name()))
					M=null;
			}
			if(M!=null)
				commands.addElement(M.Name());
		}
		if(commands.size()<1)
		{
			mob.tell("Cast on whom?");
			return false;
		}
		String mobName=Util.combine(commands,0).trim().toUpperCase();
		MOB target=null;
		if(givenTarget instanceof MOB)
			target=(MOB)givenTarget;
		if(target!=null)
			target=mob.location().fetchInhabitant(mobName);
		if(target==null)
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(Sense.canAccess(mob,R))
				{
					MOB mob2=R.fetchInhabitant(mobName);
					if(mob2!=null){ target=mob2; break;}
				}
			}
		}
		Room newRoom=mob.location();
		if(target!=null)
			newRoom=target.location();
		else
		{
			mob.tell("You can't seem to focus on '"+mobName+"'.");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) clairevoyance, calling '"+mobName+"'.^?");
			FullMsg msg2=new FullMsg(mob,target,this,affectType(auto),null);
			if((mob.location().okMessage(mob,msg))&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location()) newRoom.send(target,msg2);
				beneficialAffect(mob,target,0);
			}

		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke clairevoyance, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}