package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2000-2006 Bo Zimmerman

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

public class Smelting extends CraftingSkill
{
	public String ID() { return "Smelting"; }
	public String name(){ return "Smelting";}
	private static final String[] triggerStrings = {"SMELT","SMELTING"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "METAL|MITHRIL";}

	protected static final int RCP_FINALNAME=0;
	protected static final int RCP_LEVEL=1;
	protected static final int RCP_TICKS=2;
	//private static final int RCP_WOOD_ALWAYSONEONE=3;
	//private static final int RCP_VALUE_DONTMATTER=4;
	//private static final int RCP_CLASSTYPE=5;
	protected static final int RCP_METALONE=6;
	protected static final int RCP_METALTWO=7;

	protected int amountMaking=0;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(amountMaking<1)
			||(getRequiredFire(mob,0)==null))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

    protected Vector loadRecipes(){return super.loadRecipes("smelting.txt");}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					amountMaking=amountMaking*(abilityCode());
					if(messedUp)
						commonEmote(mob,"<S-NAME> ruin(s) "+building.name()+"!");
					else
					for(int i=0;i<amountMaking;i++)
					{
						Item copy=(Item)building.copyOf();
						copy.setMiscText(building.text());
						copy.recoverEnvStats();
						mob.location().addItemRefuse(copy,Item.REFUSE_PLAYER_DROP);
					}
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,0);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"smelt list\" for a list.");
			return false;
		}
		Vector recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(CMStrings.padRight("Item",20)+" Lvl "+CMStrings.padRight("Metal #1",16)+" Metal #2\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
					String metal1=((String)V.elementAt(RCP_METALONE)).toLowerCase();
					String metal2=((String)V.elementAt(RCP_METALTWO)).toLowerCase();
					if(level<=mob.envStats().level())
						buf.append(CMStrings.padRight(item,20)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(metal1,16)+" "+metal2+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		Item fire=getRequiredFire(mob,0);
		if(fire==null) return false;
		building=null;
		messedUp=false;
		String recipeName=CMParms.combine(commands,0);
		int maxAmount=0;
		if((commands.size()>1)&&(CMath.isNumber((String)commands.lastElement())))
		{
			maxAmount=CMath.s_int((String)commands.lastElement());
			commands.removeElementAt(commands.size()-1);
			recipeName=CMParms.combine(commands,0);
		}
		Vector foundRecipe=null;
		Vector matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			Vector V=(Vector)matches.elementAt(r);
			if(V.size()>0)
			{
				int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
                if(level<=mob.envStats().level())
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to make '"+recipeName+"'.  Try \"smelt list\" for a list.");
			return false;
		}
		String doneResourceDesc=(String)foundRecipe.elementAt(RCP_FINALNAME);
		String resourceDesc1=(String)foundRecipe.elementAt(RCP_METALONE);
		String resourceDesc2=(String)foundRecipe.elementAt(RCP_METALTWO);
		int resourceCode1=-1;
		int resourceCode2=-1;
		int doneResourceCode=-1;
		for(int i=0;i<RawMaterial.RESOURCE_DESCS.length;i++)
		{
			String desc=RawMaterial.RESOURCE_DESCS[i];
			if(desc.equalsIgnoreCase(resourceDesc1))
				resourceCode1=i;
			if(desc.equalsIgnoreCase(resourceDesc2))
				resourceCode2=i;
			if(desc.equalsIgnoreCase(doneResourceDesc))
				doneResourceCode=i;
		}
		if((resourceCode1<0)||(resourceCode2<0)||(doneResourceCode<0))
		{
			commonTell(mob,"CoffeeMud error in this alloy.  Please let your local Archon know.");
			return false;
		}
		int amountResource1=findNumberOfResource(mob.location(),RawMaterial.RESOURCE_DATA[resourceCode1][0]);
		int amountResource2=findNumberOfResource(mob.location(),RawMaterial.RESOURCE_DATA[resourceCode2][0]);
		if(amountResource1==0)
		{
			commonTell(mob,"There is no "+resourceDesc1+" here to make "+doneResourceDesc+" from.  It might need to put it down first.");
			return false;
		}
		if(amountResource2==0)
		{
			commonTell(mob,"There is no "+resourceDesc2+" here to make "+doneResourceDesc+" from.  It might need to put it down first.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		amountMaking=amountResource1;
		if(amountResource2<amountResource1) amountMaking=amountResource2;
		if((maxAmount>0)&&(amountMaking>maxAmount)) amountMaking=maxAmount;
		destroyResources(mob.location(),amountMaking,RawMaterial.RESOURCE_DATA[resourceCode1][0],0,null,0);
		destroyResources(mob.location(),amountMaking,RawMaterial.RESOURCE_DATA[resourceCode2][0],0,null,0);
		completion=CMath.s_int((String)foundRecipe.elementAt(RCP_TICKS))-((mob.envStats().level()-CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
		amountMaking+=amountMaking;
		building=(Item)CMLib.materials().makeResource(RawMaterial.RESOURCE_DATA[doneResourceCode][0],-1,false);
		startStr="<S-NAME> start(s) smelting "+doneResourceDesc.toLowerCase()+".";
		displayText="You are smelting "+doneResourceDesc.toLowerCase();
        playSound="sizzling.wav";
		verb="smelting "+doneResourceDesc.toLowerCase();

		messedUp=!proficiencyCheck(mob,0,auto);
		if(completion<4) completion=4;

		CMMsg msg=CMClass.getMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,completion);
		}
		return true;
	}
}
