package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Mine extends CommonSkill
{
	private Item found=null;
	private String foundShortName="";
	public Mine()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mine";

		displayText="You are mining...";
		verb="mining";
		miscText="";
		triggerStrings.addElement("MINE");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Mine();
	}
	public boolean tick(int tickID)
	{
		MOB mob=(MOB)affected;
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			if(tickUp==6)
			{
				if(found!=null)
				{
					mob.tell("You have found a vein of "+foundShortName+"!");
					displayText="You are mining "+foundShortName;
					verb="mining "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find anything worth mining here.\n\r");
					mob.tell(str.toString());
					unInvoke();
				}
				
			}
		}
		return super.tick(tickID);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((found!=null)&&(!aborted))
			{
				int amount=Dice.roll(1,3,0);
				String s="s";
				if(amount==1) s="";
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to mine "+amount+" pound"+s+" of "+foundShortName+".");
				for(int i=0;i<amount;i++)
				{
					Item newFound=(Item)found.copyOf();
					mob.location().addItem(newFound);
					ExternalPlay.get(mob,null,newFound,true);
				}
			}
		}
		super.unInvoke();
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="mining";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(0,auto))
		   &&(resourceType!=EnvResource.RESOURCE_CRYSTAL)
		   &&(resourceType!=EnvResource.RESOURCE_OPAL)
		   &&(resourceType!=EnvResource.RESOURCE_AMETHYST)
		   &&(resourceType!=EnvResource.RESOURCE_GARNET)
		   &&(resourceType!=EnvResource.RESOURCE_AMBER)
		   &&(resourceType!=EnvResource.RESOURCE_AQUAMARINE)
		   &&(resourceType!=EnvResource.RESOURCE_CRYSOBERYL)
		   &&(resourceType!=EnvResource.RESOURCE_TOPAZ)
		   &&(resourceType!=EnvResource.RESOURCE_DIAMOND)
		   &&(resourceType!=EnvResource.RESOURCE_GEM)
		   &&(resourceType!=EnvResource.RESOURCE_GLASS)
		   &&(resourceType!=EnvResource.RESOURCE_PEARL)
		   &&(((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_GLASS)
		   ||((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK)
		   ||((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
		   ||((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL)))
		{
			found=(Item)makeResource(resourceType);
			foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=50-mob.envStats().level();
		if(duration<15) duration=15;
		beneficialAffect(mob,mob,duration);
		return true;
	}
}
