package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class LockSmith extends CommonSkill
{
	public String ID() { return "LockSmith"; }
	public String name(){ return "Locksmithing";}
	private static final String[] triggerStrings = {"LOCKSMITH","LOCKSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item building=null;
	private Environmental workingOn=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public LockSmith()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",15,ID(),false);}
	}
	public Environmental newInstance(){	return new LockSmith();}

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
						commonTell(mob,"You've ruined "+building.name()+"!");
					else
						mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if(tickDown==6)
			{
				if((workingOn!=null)&&(mob.location()!=null))
				{
					if(workingOn instanceof Exit)
					{
						if(!((Exit)workingOn).hasALock())
						{
							int dir=-1;
							for(int d=0;d<4;d++)
								if(mob.location().getExitInDir(d)==workingOn)
								{dir=d; break;}
							if((messedUp)||(dir<0))
							{
								commonTell(mob,"You've ruined the lock.");
								building=null;
								unInvoke();
							}
							Exit exit2=mob.location().getPairedExit(dir);
							Room room2=mob.location().getRoomInDir(dir);
							((Exit)workingOn).baseEnvStats().setLevel(mob.envStats().level());
							((Exit)workingOn).recoverEnvStats();
							((Exit)workingOn).setDoorsNLocks(true,false,true,true,true,true);
							((Exit)workingOn).setKeyName(((Key)building).getKey());
							ExternalPlay.DBUpdateExits(mob.location());
							if((exit2!=null)
							   &&(exit2.hasADoor())
							   &&(exit2.isGeneric())
							   &&(room2!=null))
							{
								((Exit)exit2).setDoorsNLocks(true,false,true,true,true,true);
								((Exit)exit2).setKeyName(((Key)building).getKey());
								ExternalPlay.DBUpdateExits(room2);
							}
						}
					}
					else
					if(workingOn instanceof Container)
					{
						if(!((Container)workingOn).hasALock())
						{
							if(messedUp)
							{
								commonTell(mob,"You've ruined the lock.");
								building=null;
								unInvoke();
							}
							((Container)workingOn).setLidsNLocks(true,false,true,true);
							((Container)workingOn).setKeyName(((Key)building).getKey());
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Locksmith what or where? Enter the name of a container or door direction.");
			return false;
		}
		String startStr=null;
		int completion=8;
		building=null;
		boolean keyFlag=false;
		workingOn=null;
		messedUp=false;
		int woodRequired=1;
		String recipeName=Util.combine(commands,0);
		int dir=Directions.getGoodDirectionCode(recipeName);
		if(dir<0)
			workingOn=mob.location().fetchFromMOBRoomFavorsItems(mob,null,recipeName,Item.WORN_REQ_UNWORNONLY);
		else
			workingOn=mob.location().getExitInDir(dir);

		if((workingOn==null)||(!Sense.canBeSeenBy(workingOn,mob)))
		{
			commonTell(mob,"You don't see a '"+recipeName+"' here.");
			return false;
		}
		String keyName=""+Math.random();
		if(workingOn instanceof Exit)
		{
			if(!((Exit)workingOn).hasADoor())
			{
				commonTell(mob,"There is no door in that direction.");
				return false;
			}
			if(!workingOn.isGeneric())
			{
				commonTell(mob,"That door isn't built right -- it can't be modified.");
				return false;
			}
			if(((Exit)workingOn).hasALock())
			{
				keyName=((Exit)workingOn).keyName();
				keyFlag=true;
			}
			else
				woodRequired=5;

			String titleInName="";
			for(int a=0;a<mob.location().numAffects();a++)
			{
				Ability A=mob.location().fetchAffect(a);
				if((A!=null)&&(A instanceof LandTitle))
				{ titleInName=((LandTitle)A).landOwner(); break;}
			}
			if((!(titleInName.equals(mob.Name())||((mob.amFollowing()!=null)&&(titleInName.equals(mob.amFollowing().Name())))))
			   &&(dir>=0))
			{
				Room R2=mob.location().getRoomInDir(dir);
				if(R2!=null)
				for(int a=0;a<R2.numAffects();a++)
				{
					Ability A=R2.fetchAffect(a);
					if((A!=null)&&(A instanceof LandTitle))
					{ titleInName=((LandTitle)A).landOwner(); break;}
				}
			}
			if(!(titleInName.equals(mob.Name())
			   ||((mob.amFollowing()!=null)&&(titleInName.equals(mob.amFollowing().Name())))))
			{
				commonTell(mob,"You'll need the permission of the owner to do that.");
				return false;
			}
		}
		else
		if(workingOn instanceof Container)
		{
			if(!((Container)workingOn).hasALid())
			{
				commonTell(mob,"That doesn't have a lid.");
				return false;
			}
			if(!workingOn.isGeneric())
			{
				commonTell(mob,"That just isn't built right -- it can't be modified.");
				return false;
			}
			if(((Container)workingOn).hasALock())
			{
				keyName=((Container)workingOn).keyName();
				keyFlag=true;
			}
			else
				woodRequired=3;
		}
		else
		{
			commonTell(mob,"You can't put a lock on that.");
			return false;
		}

		Item firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
		if(firstWood==null)
			firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_MITHRIL);
		int foundWood=0;
		if(firstWood!=null)
			foundWood=findNumberOfResource(mob.location(),firstWood.material());
		if(foundWood==0)
		{
			commonTell(mob,"There is no metal here to make anything from!  It might need to put it down first.");
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
				I.destroy();
		}
		building=CMClass.getItem("GenKey");
		if(building==null)
		{
			commonTell(mob,"There's no such thing as a GenKey!!!");
			return false;
		}
		completion=15-((mob.envStats().level()-workingOn.envStats().level()));
		if(keyFlag) completion=completion/2;
		String itemName=(EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)]+" key").toLowerCase();
		itemName=Util.startWithAorAn(itemName);
		building.setName(itemName);
		startStr="<S-NAME> start(s) working on "+(keyFlag?"a key for ":"")+workingOn.name()+".";
		displayText="You are working on "+(keyFlag?"a key for ":"")+workingOn.name();
		verb="working on "+(keyFlag?"a key for ":"")+workingOn.name();
		building.setDisplayText(itemName+" is here");
		building.setDescription(itemName+". ");
		((Key)building).setKey(keyName);
		building.baseEnvStats().setWeight(woodRequired);
		building.setBaseValue(1);
		building.setMaterial(firstWood.material());
		building.baseEnvStats().setLevel(workingOn.envStats().level());
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();

		int profficiencyAddition=0;
		if(workingOn.envStats().level()>mob.envStats().level())
			profficiencyAddition=workingOn.envStats().level()-mob.envStats().level();
		messedUp=!profficiencyCheck(profficiencyAddition*5,auto);
		if(completion<8) completion=8;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
