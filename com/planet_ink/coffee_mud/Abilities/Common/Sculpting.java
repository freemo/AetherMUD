package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Sculpting extends CommonSkill
{
	public String ID() { return "Sculpting"; }
	public String name(){ return "Sculpting";}
	private static final String[] triggerStrings = {"SCULPT","SCULPTING"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_CONTAINMASK=8;

	private Item building=null;
	private Item key=null;
	private boolean mending=false;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public Sculpting()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Sculpting();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("SCULPTING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"sculpting.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Sculpting","Recipes not found!");
			Resources.submitResource("SCULPTING RECIPES",V);
		}
		return V;
	}

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
					{
						if(mending)
							commonEmote(mob,"<S-NAME> completely mess(es) up mending "+building.displayName()+".");
						else
							commonTell(mob,"<S-NAME> completely mess(es) up sculpting "+building.displayName()+".");
					}
					else
					{
						if(mending)
							building.setUsesRemaining(100);
						else
						{
							mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
							if(key!=null)
							{
								mob.location().addItemRefuse(key,Item.REFUSE_PLAYER_DROP);
								key.setContainer(building);
							}
						}
					}
				}
				building=null;
				key=null;
				mending=false;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Sculpt what? Enter \"sculpt list\" for a list, or \"sculpt mend <item>\".");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Stone required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					if(level<=mob.envStats().level())
						buf.append(Util.padRight(item,20)+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			mending=false;
			key=null;
			messedUp=false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(building==null) return false;
			if((building.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_ROCK)
			{
				commonTell(mob,"That's not made of stone.  That can't be mended.");
				return false;
			}
			if(!building.subjectToWearAndTear())
			{
				commonTell(mob,"You can't mend "+building.displayName()+".");
				return false;
			}
			if(((Item)building).usesRemaining()>=100)
			{
				commonTell(mob,building.displayName()+" is in good condition already.");
				return false;
			}
			mending=true;
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			startStr="<S-NAME> start(s) mending "+building.displayName()+".";
			displayText="You are mending "+building.displayName();
			verb="mending "+building.displayName();
		}
		else
		{
			building=null;
			mending=false;
			key=null;
			messedUp=false;
			String recipeName=Util.combine(commands,0);
			Vector foundRecipe=null;
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=(String)V.elementAt(RCP_FINALNAME);
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					if((level<=mob.envStats().level())
					&&(replacePercent(item,"").equalsIgnoreCase(recipeName)))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to sculpt a '"+recipeName+"'.  Try \"sculpt list\" for a list.");
				return false;
			}
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			Item firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
			int foundWood=0;
			if(firstWood!=null)
				foundWood=findNumberOfResource(mob.location(),firstWood.material());
			if(foundWood==0)
			{
				commonTell(mob,"There is no stone here to make anything from!  It might need to put it down first.");
				return false;
			}
			if(foundWood<woodRequired)
			{
				commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			int woodDestroyed=woodRequired;
			for(int i=mob.location().numItems()-1;i>=0;i--)
			{
				Item I=mob.location().fetchItem(i);
				if((I instanceof EnvResource)
				&&(I.container()==null)
				&&(I.material()==firstWood.material())
				&&((--woodDestroyed)>=0))
					I.destroyThis();
			}
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			completion=Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)]).toLowerCase();
			itemName=Util.startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) sculpting "+building.displayName()+".";
			displayText="You are sculpting "+building.displayName();
			verb="sculpting "+building.displayName();
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))+(woodRequired*(firstWood.baseGoldValue())));
			building.setMaterial(firstWood.material());
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
			int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			int canContain=Util.s_int((String)foundRecipe.elementAt(RCP_CONTAINMASK));
			key=null;
			if((misctype.equalsIgnoreCase("statue"))&&(!mob.isMonster()))
			{
				try
				{
					String of=mob.session().prompt("What is this a statue of?","");
					if(of.trim().length()==0)
						return false;
					building.setName(itemName+" of "+of.trim());
					building.setDisplayText(itemName+" of "+of.trim()+" is here");
					building.setDescription(itemName+" of "+of.trim()+". ");
				}
				catch(java.io.IOException x)
				{
					return false;
				}
			}
			else
			if(building instanceof Container)
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
					((Container)building).setKeyName(new Double(Math.random()).toString());
					key=CMClass.getItem("GenKey");
					((Key)key).setKey(((Container)building).keyName());
					key.setName("a key");
					key.setDisplayText("a small key sits here");
					key.setDescription("looks like a key to "+building.displayName());
					key.recoverEnvStats();
					key.text();
				}
			}
			if(building instanceof Rideable)
			{
				if(misctype.equalsIgnoreCase("CHAIR"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SIT);
				else
				if(misctype.equalsIgnoreCase("TABLE"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_TABLE);
				else
				if(misctype.equalsIgnoreCase("LADDER"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_LADDER);
				else
				if(misctype.equalsIgnoreCase("BED"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SLEEP);
			}
			if(building instanceof Light)
			{
				((Light)building).setDuration(capacity);
				if(building instanceof Container)
					((Container)building).setCapacity(0);
			}
			building.recoverEnvStats();
			if((!building.isGettable())
			&&(!ExternalPlay.doesOwnThisProperty(mob,mob.location())))
			{
				commonTell(mob,"You are not allowed to build that here.");
				return false;
			}
			building.text();
			building.recoverEnvStats();
		}


		messedUp=!profficiencyCheck(0,auto);
		if(completion<4) completion=4;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
