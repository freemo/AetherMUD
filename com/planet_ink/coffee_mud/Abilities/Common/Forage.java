package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Forage extends CommonSkill
{
	private Item found=null;
	private String foundShortName="";
	public Forage()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Forage";

		displayText="You are foraging...";
		verb="foraging";
		miscText="";
		triggerStrings.addElement("FORAGE");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Forage();
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
					mob.tell("You have found some "+foundShortName+"!");
					displayText="You are foraging for "+foundShortName;
					verb="foraging for "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find anything worth foraging around here.\n\r");
					int d=lookingFor(EnvResource.MATERIAL_VEGETATION,mob.location());
					if(d<0)
						str.append("You might try elsewhere.");
					else
						str.append("You might try "+Directions.getInDirectionName(d)+".");
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
			if(found!=null)
			{
				int amount=Dice.roll(1,5,0);
				String s="s";
				if(amount==1) s="";
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to gather "+amount+" pound"+s+" of "+foundShortName+".");
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
		verb="foraging";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int resourceType=mob.location().myResource();
		if((profficiencyCheck(0,auto))
		   &&((resourceType&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION))
		{
			found=(Item)makeResource(resourceType);
			foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		}
		int duration=35-mob.envStats().level();
		if(duration<10) duration=10;
		beneficialAffect(mob,mob,duration);
		return true;
	}
}
