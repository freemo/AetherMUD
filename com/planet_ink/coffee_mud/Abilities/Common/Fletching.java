package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Fletching extends CommonSkill
{
	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_AMMOTYPE=6;
	private static final int RCP_AMOCAPACITY=7;
	private static final int RCP_ARMORDMG=8;
	private static final int RCP_MAXRANGE=9;
	private static final int RCP_EXTRAREQ=10;
	
	private Item building=null;
	private boolean messedUp=false;
	private boolean mending=false;
	public Fletching()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Fletching";

		miscText="";
		triggerStrings.addElement("FLETCH");
		triggerStrings.addElement("FLETCHING");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}
	
	public Environmental newInstance()
	{
		return new Fletching();
	}
	

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("FLECTHING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"fletching.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Fletching","Recipes not found!");
			Resources.submitResource("FLECTHING RECIPES",V);
		}
		return V;
	}
	
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Specialization_Ranged")==null)
		{
			teacher.tell(student.name()+" has not yet specialized in ranged weapons.");
			student.tell("You need to specialize in ranged weapons to learn "+name()+".");
			return false;
		}
		return true;
	}
	
	public void unInvoke()
	{
		if(canBeUninvoked)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(mending)
							mob.tell("You completely mess up mending "+building.name()+".");
						else
							mob.tell("You completely mess up making "+building.name()+".");
					}
					else
					{
						if(mending)
							building.setUsesRemaining(100);
						else
							mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
					}
				}
				building=null;
				mending=false;
			}
		}
		super.unInvoke();
	}
	
	public boolean tick(int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if(building==null)
				unInvoke();
		}
		return super.tick(tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			mob.tell("Make what? Enter \"fletch list\" for a list, or \"fletch mend <item>\".");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Wood required\n\r");
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
			mob.tell(buf.toString());
			return true;
		}
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			mending=false;
			messedUp=false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(building==null) return false;
			if((!(building instanceof Weapon))
			||(((Weapon)building).weaponClassification()!=Weapon.CLASS_RANGED)
			   &&(((Weapon)building).weaponClassification()!=Weapon.CLASS_THROWN))
			{
				mob.tell("You don't know how to mend that sort of thing.");
				return false;
			}
			if(!building.subjectToWearAndTear())
			{
				mob.tell("You can't mend "+building.name()+".");
				return false;
			}
			mending=true;
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			startStr="<S-NAME> start(s) mending "+building.name()+".";
			displayText="You are mending "+building.name();
			verb="mending "+building.name();
		}
		else
		{
			building=null;
			mending=false;
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
				mob.tell("You don't know how to make a '"+recipeName+"'.  Try \"fletch list\" for a list.");
				return false;
			}
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			String otherRequired=(String)foundRecipe.elementAt(RCP_EXTRAREQ);
			Item firstWood=null;
			Item firstOther=null;
			int foundWood=0;
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().fetchItem(i);
				if((I instanceof EnvResource)
				&&(!Sense.isOnFire(I))
				&&(I.container()==null))
				{
					if((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
					{
						if(firstWood==null)firstWood=I;
						if(firstWood.material()==I.material())
							foundWood++;
					}
					else
					if((otherRequired.length()>0)
					&&(firstOther==null)
					&&(EnvResource.MATERIAL_DESCS[(I.material()&EnvResource.MATERIAL_MASK)>>8].equalsIgnoreCase(otherRequired)))
						firstOther=I;
				}
			}
			if((foundWood==0)&&(woodRequired>0))
			{
				mob.tell("There is no wood here to make anything from!  You might need to put it down first.");
				return false;
			}
			if((otherRequired.length()>0)&&(firstOther==null))
			{
				mob.tell("You need a pound of "+otherRequired.toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
				return false;
			}
			if((firstOther!=null)&&((firstOther.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL))
			{
				Item fire=null;
				for(int i=0;i<mob.location().numItems();i++)
				{
					Item I2=mob.location().fetchItem(i);
					if((I2!=null)&&(I2.container()==null)&&(Sense.isOnFire(I2)))
					{
						fire=I2;
						break;
					}
				}
				if((fire==null)||(!mob.location().isContent(fire)))
				{
					mob.tell("You'll need to build a fire first.");
					return false;
				}
			}
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			int woodDestroyed=woodRequired;
			for(int i=mob.location().numItems()-1;i>=0;i--)
			{
				Item I=mob.location().fetchItem(i);
				if((I instanceof EnvResource)
				&&((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
				&&(I.container()==null)
				&&(!Sense.isOnFire(I))
				&&(I.material()==firstWood.material())
				&&((--woodDestroyed)>=0))
					I.destroyThis();
				else
				if((firstOther!=null)&&(I==firstOther))
					I.destroyThis();
			}
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				mob.tell("There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			completion=Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)]).toLowerCase();
			if(new String("aeiou").indexOf(Character.toLowerCase(itemName.charAt(0)))>=0)
				itemName="an "+itemName;
			else
				itemName="a "+itemName;
			building.setName(itemName);
			startStr="You start making "+building.name()+".";
			displayText="You are making "+building.name();
			verb="making "+building.name();
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE)));
			building.setMaterial(firstWood.material());
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			String ammotype=(String)foundRecipe.elementAt(RCP_AMMOTYPE);
			int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_AMOCAPACITY));
			int maxrange=Util.s_int((String)foundRecipe.elementAt(RCP_MAXRANGE));
			int armordmg=Util.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
			if(building instanceof Weapon)
			{
				if(ammotype.length()>0)
				{
					((Weapon)building).setAmmoCapacity(capacity);
					((Weapon)building).setAmmoRemaining(0);
					((Weapon)building).setAmmunitionType(ammotype);
				}
				building.baseEnvStats().setDamage(armordmg);
				((Weapon)building).setRanges(((Weapon)building).minRange(),maxrange);
			}
			else
			if(ammotype.length()>0)
			{
				building.setSecretIdentity(ammotype);
				building.setUsesRemaining(capacity);
			}
			building.recoverEnvStats();
			building.text();
			building.recoverEnvStats();
		}
		
		
		messedUp=!profficiencyCheck(0,auto);
		if(completion<4) completion=4;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}