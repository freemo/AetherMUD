package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2012 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Wainwrighting extends CraftingSkill implements ItemCraftor
{
	public String ID() { return "Wainwrighting"; }
	public String name(){ return "Wainwrighting";}
	private static final String[] triggerStrings = {"WAINWRIGHTING"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "WOODEN";}
    public String parametersFormat(){ return 
        "ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
        +"ITEM_CLASS_ID\tLID_LOCK\tCONTAINER_CAPACITY\tRIDE_CAPACITY\tCONTAINER_TYPE\t"
        +"CODED_SPELL_LIST";}

	protected static final int RCP_FINALNAME=0;
	protected static final int RCP_LEVEL=1;
	protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_NUMRIDERS=8;
	protected static final int RCP_CONTAINMASK=9;
	protected static final int RCP_SPELL=10;

	protected Item key=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

    public String parametersFile(){ return "wainwright.txt";}
    protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonEmote(mob,"<S-NAME> mess(es) up building "+building.name()+".");
					else
					{
						dropAWinner(mob,building);
						if(key!=null)
						{
							dropAWinner(mob,key);
							if(building instanceof Container)
								key.setContainer((Container)building);
						}
					}
				}
				building=null;
				key=null;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		int autoGenerate=0;
		if((auto)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
			givenTarget=null;
		}
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Wainwright what? Enter \"wainwright list\" for a list.");
			return false;
		}
        if((!auto)
        &&(commands.size()>0)
        &&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
        {
            bundling=true;
            if(super.invoke(mob,commands,givenTarget,auto,asLevel))
                return super.bundle(mob,commands);
            return false;
        }
		List<List<String>> recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int duration=4;
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer(CMStrings.padRight("Item",25)+" Level Capacity Wood required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.get(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.get(RCP_LEVEL));
					String wood=getComponentDescription(mob,V,RCP_WOOD);
					int capacity=CMath.s_int((String)V.get(RCP_CAPACITY));
					if((level<=xlevel(mob))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,25)+" "+CMStrings.padRight(""+level,5)+" "+CMStrings.padRight(""+capacity,8)+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		building=null;
		key=null;
		messedUp=false;
		int amount=-1;
		if((commands.size()>1)&&(CMath.isNumber((String)commands.lastElement())))
		{
			amount=CMath.s_int((String)commands.lastElement());
			commands.removeElementAt(commands.size()-1);
		}
		String recipeName=CMParms.combine(commands,0);
		List<String> foundRecipe=null;
		List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			List<String> V=matches.get(r);
			if(V.size()>0)
			{
				int level=CMath.s_int((String)V.get(RCP_LEVEL));
                if((autoGenerate>0)||(level<=xlevel(mob)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to build a '"+recipeName+"'.  Try \"list\" as your parameter for a list.");
			return false;
		}
		
		final String woodRequiredStr = (String)foundRecipe.get(RCP_WOOD);
		final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName), autoGenerate);
		if(componentsFoundList==null) return false;
		int woodRequired=CMath.s_int(woodRequiredStr);
        woodRequired=adjustWoodRequired(woodRequired,mob);
        
		if(amount>woodRequired) woodRequired=amount;
		int[] pm={RawMaterial.MATERIAL_WOODEN};
		String misctype=(String)foundRecipe.get(RCP_MISCTYPE);
		int[][] data=fetchFoundResourceData(mob,
											woodRequired,"wood",pm,
											0,null,null,
											false,
											autoGenerate,
											null);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];
        bundling=misctype.equalsIgnoreCase("BUNDLE");
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int lostValue=autoGenerate>0?0:
            CMLib.materials().destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null)
            +CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
		building=CMClass.getItem((String)foundRecipe.get(RCP_CLASSTYPE));
		if(building==null)
		{
			commonTell(mob,"There's no such thing as a "+foundRecipe.get(RCP_CLASSTYPE)+"!!!");
			return false;
		}
		duration=getDuration(CMath.s_int((String)foundRecipe.get(RCP_TICKS)),mob,CMath.s_int((String)foundRecipe.get(RCP_LEVEL)),4);
		String itemName=replacePercent((String)foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
		if(bundling)
			itemName="a "+woodRequired+"# "+itemName;
		else
			itemName=CMLib.english().startWithAorAn(itemName);
		building.setName(itemName);
		startStr="<S-NAME> start(s) building "+building.name()+".";
		displayText="You are building "+building.name();
		verb="building "+building.name();
        playSound="hammer.wav";
		building.setDisplayText(itemName+" lies here");
		building.setDescription(itemName+". ");
		building.basePhyStats().setWeight(woodRequired);
		building.setBaseValue(CMath.s_int((String)foundRecipe.get(RCP_VALUE)));
		building.setMaterial(data[0][FOUND_CODE]);
		building.basePhyStats().setLevel(CMath.s_int((String)foundRecipe.get(RCP_LEVEL)));
		building.setSecretIdentity("This is the work of "+mob.Name()+".");
		int capacity=CMath.s_int((String)foundRecipe.get(RCP_CAPACITY));
		long canContain=getContainerType((String)foundRecipe.get(RCP_CONTAINMASK));
		int riders=CMath.s_int((String)foundRecipe.get(RCP_NUMRIDERS));
		String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.get(RCP_SPELL)).trim():"";
		addSpells(building,spell);
		key=null;
		if(building instanceof Rideable)
		{
			((Rideable)building).setRideBasis(Rideable.RIDEABLE_WAGON);
			((Rideable)building).setRiderCapacity(riders);
		}

		if((building instanceof Container)
		&&(!(building instanceof Armor)))
		{
			if(capacity>0)
			{
				((Container)building).setCapacity(capacity+woodRequired);
				((Container)building).setContainTypes(canContain);
			}
			if(misctype.equalsIgnoreCase("LID"))
				((Container)building).setLidsNLocks(true,false,false,false);
			else
			if(misctype.equalsIgnoreCase("LOCK"))
			{
				((Container)building).setLidsNLocks(true,false,true,false);
				((Container)building).setKeyName(Double.toString(Math.random()));
				key=CMClass.getItem("GenKey");
				((DoorKey)key).setKey(((Container)building).keyName());
				key.setName("a key");
				key.setDisplayText("a small key sits here");
				key.setDescription("looks like a key to "+building.name());
				key.recoverPhyStats();
				key.text();
			}
		}
		if(bundling) building.setBaseValue(lostValue);
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();


		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb="bundling "+RawMaterial.CODES.NAME(building.material()).toLowerCase();
			startStr="<S-NAME> start(s) "+verb+".";
			displayText="You are "+verb;
		}

		if(autoGenerate>0)
		{
			commands.addElement(building);
			return true;
		}

		CMMsg msg=CMClass.getMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
