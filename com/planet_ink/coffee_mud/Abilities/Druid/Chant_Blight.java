package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_Blight extends Chant
{
	public String ID() { return "Chant_Blight"; }
	public String name(){ return "Blight";}
	public String displayText(){return "(Blight)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_Blight();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Room)))
			return;
		Room R=(Room)affected;
		if(canBeUninvoked())
			R.showHappens(Affect.MSG_OK_VISUAL,"The blight is ended.");

		super.unInvoke();

	}
	
	public boolean isBlightable(int resource)
	{
		if(((resource&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PAPER)
		||((resource&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
		||((resource&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION)
		||(resource==EnvResource.RESOURCE_HEMP)
		||(resource==EnvResource.RESOURCE_SILK)
		||(resource==EnvResource.RESOURCE_COTTON))
			return true;
		return false;
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			if(isBlightable(R.myResource()))
				R.setResource(EnvResource.RESOURCE_SAND);
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)&&(isBlightable(I.material())))
				{
					R.showHappens(Affect.MSG_OK_VISUAL,I.name()+" withers away.");
					I.destroy();
					break;
				}
			}
		}
		return true;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.target()!=null)
		&&(affect.target() instanceof MOB)
		&&(((MOB)affect.target()).charStats().getMyRace().racialCategory().equals("Vegetation")))
		{
			int recovery=(int)Math.round(Util.div((affect.targetCode()-Affect.MASK_HURT),2.0));
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("This place is already blighted.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().showHappens(Affect.MSG_OK_VISUAL,"The soil is blighted!");
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the ground, but the magic fades.");
		// return whether it worked
		return success;
	}
}
