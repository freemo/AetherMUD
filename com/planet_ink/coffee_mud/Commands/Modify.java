package com.planet_ink.coffee_mud.Commands;
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
import java.io.IOException;

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
public class Modify extends BaseGenerics
{
	public Modify(){}

	private String[] access={"MODIFY","MOD"};
	public String[] getAccessWords(){return access;}

	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ITEM [ITEM NAME](@ room/[MOB NAME]) [LEVEL, ABILITY, REJUV, USES, MISC] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=((String)commands.elementAt(2));
		MOB srchMob=mob;
		Room srchRoom=mob.location();
		int x=itemID.indexOf("@");
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if(rest.equalsIgnoreCase("room"))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					mob.tell("MOB '"+rest+"' not found.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return;
				}
				srchMob=M;
				srchRoom=null;
			}
		}
		String command="";
		if(commands.size()>3)
			command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=CMParms.combine(commands,4);

		Item modItem=null;
		if((srchMob!=null)&&(srchRoom!=null))
			modItem=(Item)srchRoom.fetchFromMOBRoomFavorsItems(srchMob,null,itemID,Item.WORNREQ_ANY);
		else
		if(srchMob!=null)
			modItem=srchMob.fetchInventory(itemID);
		else
		if(srchRoom!=null)
			modItem=srchRoom.fetchAnyItem(itemID);
		if(modItem==null)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		Item copyItem=(Item)modItem.copyOf();
		if(command.equals("LEVEL"))
		{
			int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modItem.baseEnvStats().setLevel(newLevel);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			int newAbility=CMath.s_int(restStr);
			modItem.baseEnvStats().setAbility(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("HEIGHT"))
		{
			int newAbility=CMath.s_int(restStr);
			modItem.baseEnvStats().setHeight(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modItem.baseEnvStats().setRejuv(newRejuv);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
			else
			{
				modItem.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modItem.recoverEnvStats();
				mob.tell(modItem.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("USES"))
		{
			int newUses=CMath.s_int(restStr);
			if(newUses>=0)
			{
				modItem.setUsesRemaining(newUses);
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modItem.isGeneric())
				genMiscSet(mob,modItem);
			else
				modItem.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if((command.length()==0)&&(modItem.isGeneric()))
		{
			genMiscSet(mob,modItem);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		{
			mob.tell("...but failed to specify an aspect.  Try LEVEL, ABILITY, HEIGHT, REJUV, USES, or MISC.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
		}
		if(!copyItem.sameAs(modItem))
			Log.sysOut("Items",mob.Name()+" modified item "+modItem.ID()+".");
	}

    protected void flunkCmd1(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ROOM [NAME, AREA, DESCRIPTION, AFFECTS, BEHAVIORS, CLASS, XGRID, YGRID] [TEXT]\n\r");
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}

    protected void flunkCmd2(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY AREA [NAME, DESCRIPTION, CLIMATE, FILE, AFFECTS, BEHAVIORS, ADDSUB, DELSUB, XGRID, YGRID] [TEXT]\n\r");
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}
	public void rooms(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()==2)
		{
			int showFlag=-1;
			if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
				showFlag=-999;
			boolean ok=false;
			Room oldRoom=(Room)mob.location().copyOf();
			while(!ok)
			{
				int showNumber=0;
				genRoomType(mob,mob.location(),++showNumber,showFlag);
				genDisplayText(mob,mob.location(),++showNumber,showFlag);
				genDescription(mob,mob.location(),++showNumber,showFlag);
				if(mob.location() instanceof GridZones)
				{
					genGridLocaleX(mob,(GridZones)mob.location(),++showNumber,showFlag);
					genGridLocaleY(mob,(GridZones)mob.location(),++showNumber,showFlag);
					//((GridLocale)mob.location()).buildGrid();
				}
				genBehaviors(mob,mob.location(),++showNumber,showFlag);
				genAffects(mob,mob.location(),++showNumber,showFlag);
				if(showFlag<-900){ ok=true; break;}
				if(showFlag>0){ showFlag=-1; continue;}
				showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
			if((!oldRoom.sameAs(mob.location()))&&(!mob.location().amDestroyed()))
			{
				CMLib.database().DBUpdateRoom(mob.location());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
				Log.sysOut("Rooms",mob.Name()+" modified room "+mob.location().roomID()+".");
			}
			return;
		}
		if(commands.size()<3) { flunkCmd1(mob); return;}

		String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr="";
		if(commands.size()>=3)
			restStr=CMParms.combine(commands,3);

		if(command.equalsIgnoreCase("AREA"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			Area A=CMLib.map().getArea(restStr);
			boolean reid=false;
			if(A==null)
			{
				if(!mob.isMonster())
				{
					if(mob.session().confirm("\n\rThis command will create a BRAND NEW AREA\n\r with Area code '"+restStr+"'.  Are you SURE (y/N)?","N"))
					{
						String areaType="";
						int tries=0;
						while((areaType.length()==0)&&((++tries)<10))
						{
							areaType=mob.session().prompt("Enter an area type to create (default=StdArea): ","StdArea");
							if(CMClass.getAreaType(areaType)==null)
							{
								mob.session().println("Invalid area type! Valid ones are:");
								mob.session().println(CMLib.lister().reallyList(CMClass.areaTypes(),-1,null).toString());
								areaType="";
							}
						}
						if(areaType.length()==0) areaType="StdArea";
						A=CMLib.database().DBCreateArea(restStr,areaType);
						mob.location().setArea(A);
                        CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A);
						reid=true;
					}
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"This entire area twitches.\n\r");
				}
				else
				{
					mob.tell("Sorry Charlie!");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
			}
			else
			{
				mob.location().setArea(A);
				if(A.getRandomProperRoom()!=null)
					reid=true;
				else
					CMLib.database().DBUpdateRoom(mob.location());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"This area twitches.\n\r");
			}
			
			if(reid)
			{
				Room R=mob.location();
	    		synchronized(("SYNC"+R.roomID()).intern())
	    		{
	    			R=CMLib.map().getRoom(R);
					String oldID=R.roomID();
					Room reference=CMLib.map().findConnectingRoom(R);
					String checkID=null;
					if(reference!=null)
						checkID=A.getNewRoomID(reference,CMLib.map().getRoomDir(reference,R));
					else
						checkID=A.getNewRoomID(R,-1);
					mob.location().setRoomID(checkID);
					CMLib.database().DBReCreate(R,oldID);
	    		}
				
				try
				{
					for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
					{
						Room R2=(Room)r.nextElement();
			    		synchronized(("SYNC"+R2.roomID()).intern())
			    		{
			    			R2=CMLib.map().getRoom(R2);
							for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
							{
								Room thatRoom=R2.rawDoors()[dir];
								if((thatRoom!=null)&&(thatRoom.roomID().equals(R)))
								{
									CMLib.database().DBUpdateExits(R2);
									break;
								}
							}
			    		}
					}
			    }catch(NoSuchElementException e){}
			}
		}
		else
		if(command.equalsIgnoreCase("NAME"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDisplayText(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("CLASS"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			Room newRoom=CMClass.getLocale(restStr);
			if(newRoom==null)
			{
				mob.tell("'"+restStr+"' is not a valid room locale.");
				return;
			}
			changeRoomType(mob.location(),newRoom);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if((command.equalsIgnoreCase("XGRID"))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setXGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if((command.equalsIgnoreCase("YGRID"))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setYGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("DESCRIPTION"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDescription(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("AFFECTS"))
		{
			genAffects(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("BEHAVIORS"))
		{
			genBehaviors(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		{
			flunkCmd1(mob);
			return;
		}
		mob.location().recoverRoomStats();
		Log.sysOut("Rooms",mob.Name()+" modified room "+mob.location().roomID()+".");
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location()==null) return;
		if(mob.location().getArea()==null) return;
		Area myArea=mob.location().getArea();

		String oldName=myArea.Name();
		Vector allMyDamnRooms=new Vector();
		for(Enumeration e=myArea.getCompleteMap();e.hasMoreElements();)
			allMyDamnRooms.addElement(e.nextElement());

		Resources.removeResource("HELP_"+myArea.Name().toUpperCase());
		if(commands.size()==2)
		{
			int showFlag=-1;
			if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
				showFlag=-999;
			boolean ok=false;
			while(!ok)
			{
				int showNumber=0;
				genName(mob,myArea,++showNumber,showFlag);
				genDescription(mob,myArea,++showNumber,showFlag);
				genAuthor(mob,myArea,++showNumber,showFlag);
				genTechLevel(mob,myArea,++showNumber,showFlag);
				genClimateType(mob,myArea,++showNumber,showFlag);
				genTimeClock(mob,myArea,++showNumber,showFlag);
				genCurrency(mob,myArea,++showNumber,showFlag);
				genArchivePath(mob,myArea,++showNumber,showFlag);
                genParentAreas(mob,myArea,++showNumber,showFlag);
                genChildAreas(mob,myArea,++showNumber,showFlag);
				genSubOps(mob,myArea,++showNumber,showFlag);
				if(myArea instanceof GridZones)
				{
					genGridLocaleX(mob,(GridZones)myArea,++showNumber,showFlag);
					genGridLocaleY(mob,(GridZones)myArea,++showNumber,showFlag);
				}
				genBehaviors(mob,myArea,++showNumber,showFlag);
				genAffects(mob,myArea,++showNumber,showFlag);
				genImage(mob,myArea,++showNumber,showFlag);
				if(showFlag<-900){ ok=true; break;}
				if(showFlag>0){ showFlag=-1; continue;}
				showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
		}
		else
		{
			if(commands.size()<3) { flunkCmd1(mob); return;}

			String command=((String)commands.elementAt(2)).toUpperCase();
			String restStr="";
			if(commands.size()>=3)
				restStr=CMParms.combine(commands,3);

			if(command.equalsIgnoreCase("NAME"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setName(restStr);
			}
			else
			if(command.equalsIgnoreCase("DESC"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setDescription(restStr);
			}
			else
			if(command.equalsIgnoreCase("FILE"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setArchivePath(restStr);
			}
			else
			if((command.equalsIgnoreCase("XGRID"))&&(myArea instanceof GridZones))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				((GridZones)myArea).setXGridSize(CMath.s_int(restStr));
			}
			else
			if((command.equalsIgnoreCase("YGRID"))&&(myArea instanceof GridZones))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				((GridZones)myArea).setYGridSize(CMath.s_int(restStr));
			}
			if(command.equalsIgnoreCase("CLIMATE"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				int newClimate=0;
				for(int i=0;i<restStr.length();i++)
					switch(Character.toUpperCase(restStr.charAt(i)))
					{
					case 'R':
						newClimate=newClimate|Area.CLIMASK_WET;
						break;
					case 'H':
						newClimate=newClimate|Area.CLIMASK_HOT;
						break;
					case 'C':
						newClimate=newClimate|Area.CLIMASK_COLD;
						break;
					case 'W':
						newClimate=newClimate|Area.CLIMATE_WINDY;
						break;
					case 'D':
						newClimate=newClimate|Area.CLIMATE_WINDY;
						break;
					case 'N':
						// do nothing
						break;
					default:
						mob.tell("Invalid CLIMATE code: '"+restStr.charAt(i)+"'.  Valid codes include: R)AINY, H)OT, C)OLD, D)RY, W)INDY, N)ORMAL.\n\r");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
						return;
					}
				myArea.setClimateType(newClimate);
			}
			else
			if(command.equalsIgnoreCase("ADDSUB"))
			{
				if((commands.size()<4)||(!CMLib.database().DBUserSearch(null,restStr)))
				{
					mob.tell("Unknown or invalid username given.\n\r");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
				myArea.addSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase("DELSUB"))
			{
				if((commands.size()<4)||(!myArea.amISubOp(restStr)))
				{
					mob.tell("Unknown or invalid staff name given.  Valid names are: "+myArea.getSubOpList()+".\n\r");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
				myArea.delSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase("AFFECTS"))
			{
				genAffects(mob,myArea,1,1);
				myArea.recoverEnvStats();
			}
			else
			if(command.equalsIgnoreCase("BEHAVIORS"))
			{
				genBehaviors(mob,myArea,1,1);
				myArea.recoverEnvStats();
			}
			else
			{
				flunkCmd2(mob);
				return;
			}
		}

		if((!myArea.Name().equals(oldName))&&(!mob.isMonster()))
		{
			if(mob.session().confirm("Is changing the name of this area really necessary (y/N)?","N"))
			{
				for(Enumeration r=myArea.getCompleteMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
		    		synchronized(("SYNC"+R.roomID()).intern())
		    		{
		    			R=CMLib.map().getRoom(R);
						if((R.roomID().startsWith(oldName+"#"))
						&&(CMLib.map().getRoom(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1))==null))
						{
			    			R=CMLib.map().getRoom(R);
							String oldID=R.roomID();
							R.setRoomID(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1));
							CMLib.database().DBReCreate(R,oldID);
						}
						else
							CMLib.database().DBUpdateRoom(R);
		    		}
				}
				try
				{
					for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
			    		synchronized(("SYNC"+R.roomID()).intern())
			    		{
			    			R=CMLib.map().getRoom(R);
							boolean doIt=false;
							for(int d=0;d<R.rawDoors().length;d++)
							{
								Room R2=R.rawDoors()[d];
								if((R2!=null)&&(R2.getArea()==myArea))
								{ doIt=true; break;}
							}
							if(doIt)
								CMLib.database().DBUpdateExits(R);
			    		}
					}
			    }catch(NoSuchElementException e){}
			}
			else
				myArea.setName(oldName);
		}
		else
			myArea.setName(oldName);
		myArea.recoverEnvStats();
		mob.location().recoverRoomStats();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		if(myArea.name().equals(oldName))
			CMLib.database().DBUpdateArea(myArea.Name(),myArea);
		else
		{
			CMLib.database().DBUpdateArea(oldName,myArea);
			CMLib.map().renameRooms(myArea,oldName,allMyDamnRooms);
		}
		Log.sysOut("Rooms",mob.Name()+" modified area "+myArea.Name()+".");
	}

	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.DIRECTIONS_DESC+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		
		Exit thisExit=mob.location().rawExits()[direction];
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit '"+((String)commands.elementAt(2))+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		if(thisExit.isGeneric())
		{
			modifyGenExit(mob,thisExit);
			return;
		}
		
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		//String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr=CMParms.combine(commands,3);

		if(thisExit.isGeneric())
			modifyGenExit(mob,thisExit);
		else
		if(restStr.length()>0)
			thisExit.setMiscText(restStr);
		else
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room room=(Room)r.nextElement();
				for(int e2=0;e2<Directions.NUM_DIRECTIONS;e2++)
				{
					Exit exit=room.rawExits()[e2];
					if((exit!=null)&&(exit==thisExit))
					{
						CMLib.database().DBUpdateExits(room);
						room.getArea().fillInAreaRoom(room);
						break;
					}
				}
			}
	    }catch(NoSuchElementException e){}
		
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,thisExit.name()+" shake(s) under the transforming power.");
		Log.sysOut("Exits",mob.location().roomID()+" exits changed by "+mob.Name()+".");
	}

	public boolean races(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String raceID=CMParms.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+"' is an invalid race id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		modifyGenRace(mob,R);
		CMLib.database().DBDeleteRace(R.ID());
		CMLib.database().DBCreateRace(R.ID(),R.racialParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,R.name()+"'s everywhere shake under the transforming power!");
		return true;
	}

	public boolean classes(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY CLASS [CLASS ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String classID=CMParms.combine(commands,2);
		CharClass C=CMClass.getCharClass(classID);
		if(C==null)
		{
			mob.tell("'"+classID+"' is an invalid class id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(C.isGeneric()))
		{
			mob.tell("'"+C.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		modifyGenClass(mob,C);
		CMLib.database().DBDeleteClass(C.ID());
		CMLib.database().DBCreateClass(C.ID(),C.classParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,C.name()+"'s everywhere shake under the transforming power!");
		return true;
	}

	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.session().rawPrintln("but fail to specify the proper fields.\n\rThe format is MODIFY SOCIAL [NAME] ([PARAM])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
        String name=((String)commands.elementAt(2)).toUpperCase();
        String stuff="";
        if(commands.size()>3)
            stuff=CMParms.combine(commands,3).toUpperCase().trim();
        if(stuff.startsWith("<")||stuff.startsWith(">")||(stuff.startsWith("T-")))
            stuff="TNAME";
        if(stuff.equals("TNAME")) 
            stuff="<T-NAME>";
        String oldStuff=stuff;
        if(stuff.equals("NONE")) 
            stuff="";
        if(CMLib.socials().FetchSocial((name+" "+stuff).trim(),false)==null)
        {
            mob.tell("The social '"+stuff+"' does not exist.");
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
            return;
        }
		CMLib.socials().modifySocialInterface(mob,(name+" "+oldStuff).trim());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The happiness of all mankind has just fluxuated!");
	}

	public void players(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY USER [PLAYER NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB M=CMLib.map().getPlayer(mobID);
		if(M==null)
			for(Enumeration p=CMLib.map().players();p.hasMoreElements();)
			{
				MOB mob2=(MOB)p.nextElement();
				if(mob2.Name().equalsIgnoreCase(mobID))
				{ M=mob2; break;}
			}
		MOB TM=CMClass.getMOB("StdMOB");
		if((M==null)&&(CMLib.database().DBUserSearch(TM,mobID)))
		{
			M=CMClass.getMOB("StdMOB");
			M.setName(TM.Name());
			CMLib.database().DBReadPlayer(M);
			CMLib.database().DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setUpdated(M.playerStats().lastDateTime());
			M.recoverEnvStats();
			M.recoverCharStats();
		}
        TM.destroy();
		if(M==null)
		{
			mob.tell("There is no such player as '"+mobID+"'!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		MOB copyMOB=(MOB)M.copyOf();
		modifyPlayer(mob,M);
		if(!copyMOB.sameAs(M))
			Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+".");
	}
	public void mobs(MOB mob, Vector commands)
		throws IOException
	{

		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY MOB [MOB NAME] [LEVEL, ABILITY, REJUV, MISC] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=((String)commands.elementAt(2));
		String command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=CMParms.combine(commands,4);


		MOB modMOB=mob.location().fetchInhabitant(mobID);
		if(modMOB==null)
		{
			mob.tell("I don't see '"+mobID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		if(!modMOB.isMonster())
		{
			mob.tell(modMOB.Name()+" is a player! Try MODIFY USER!");
			return;
		}
		MOB copyMOB=(MOB)modMOB.copyOf();

		if(command.equals("LEVEL"))
		{
			int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modMOB.baseEnvStats().setLevel(newLevel);
				modMOB.recoverCharStats();
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			int newAbility=CMath.s_int(restStr);
			modMOB.baseEnvStats().setAbility(newAbility);
			modMOB.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modMOB.baseEnvStats().setRejuv(newRejuv);
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
			else
			{
				modMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modMOB.recoverEnvStats();
				mob.tell(modMOB.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modMOB.isGeneric())
				genMiscSet(mob,modMOB);
			else
				modMOB.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
		}
		else
		{
			mob.tell("...but failed to specify an aspect.  Try LEVEL, ABILITY, REJUV, or MISC.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
		}
		if(!modMOB.sameAs(copyMOB))
			Log.sysOut("Mobs",mob.Name()+" modified mob "+modMOB.Name()+".");
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			items(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			rooms(mob,commands);
		}
		else
		if(commandType.equals("RACE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDRACES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			races(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLASSES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			classes(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDAREAS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			areas(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			exits(mob,commands);
		}
		else
		if(commandType.equals("CLAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLANS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Modify the status of which clan?  Use clanlist.");
			else
			{
				String name=CMParms.combine(commands,2);
				Clan C=CMLib.clans().findClan(name);
				if(C==null)
					mob.tell("Clan '"+name+"' is unknown.  Try clanlist.");
				else
				{
					switch(C.getStatus())
					{
					case Clan.CLANSTATUS_ACTIVE:
						C.setStatus(Clan.CLANSTATUS_PENDING);
						mob.tell("Clan '"+C.name()+"' has been changed from active to pending!");
						C.update();
						break;
					case Clan.CLANSTATUS_PENDING:
						C.setStatus(Clan.CLANSTATUS_ACTIVE);
						mob.tell("Clan '"+C.name()+"' has been changed from pending to active!");
						C.update();
						break;
					case Clan.CLANSTATUS_FADING:
						C.setStatus(Clan.CLANSTATUS_ACTIVE);
						mob.tell("Clan '"+C.name()+"' has been changed from fading to active!");
						C.update();
						break;
					default:
						mob.tell("Clan '"+C.name()+"' has not been changed!");
						break;
					}
				}
			}
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDSOCIALS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			socials(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			mobs(mob,commands);
		}
		else
        if(commandType.startsWith("JSCRIPT"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"JSCRIPTS")) 
                return errorOut(mob);
            if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)!=1)
            {
                mob.tell("This command is only used when your Scriptable Javascripts require approval as specified in your coffeemud.ini file.");
                return true;
            }
            Long L=null;
            Object O=null;
            Hashtable j=CMSecurity.getApprovedJScriptTable();
            boolean somethingFound=false;
            for(Enumeration e=j.keys();e.hasMoreElements();)
            {
                L=(Long)e.nextElement();
                O=j.get(L);
                if(O instanceof StringBuffer)
                {
                    somethingFound=true;
                    mob.tell("Unapproved script:\n\r"+((StringBuffer)O).toString()+"\n\r");
                    if((!mob.isMonster())
                    &&(mob.session().confirm("Approve this script (Y/n)?","Y")))
                        CMSecurity.approveJScript(mob.Name(),L.longValue());
                    else
                        j.remove(L);
                }
            }
            if(!somethingFound)
                mob.tell("No Javascripts require approval at this time.");
        }
        else
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			players(mob,commands);
		}
		else
        if(commandType.equals("POLL"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"POLLS")) return errorOut(mob);
            String name=CMParms.combine(commands,2);
            Poll P=null;
            if(CMath.isInteger(name))
                P=CMLib.polls().getPoll(CMath.s_int(name)-1);
            else
            if(name.length()>0)
                P=CMLib.polls().getPoll(name);
            if(P==null)
            {
                mob.tell("POLL '"+name+"' not found. Try LIST POLLS.");
                mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
                return false;
            }
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
            P.modifyVote(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^SThe world's uncertainty has changed.^?");
            Log.sysOut("CreateEdit",mob.Name()+" modified Poll "+P.getName()+".");
        }
        else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Start/Stop which quest?  Use list quests.");
			else
			{
				String name=CMParms.combine(commands,2);
                Quest Q=null;
                if(CMath.isInteger(name))
                {
                    Q=CMLib.quests().fetchQuest(CMath.s_int(name)-1);
                    if(Q!=null) name=Q.name();
                }
                if(Q==null) Q=CMLib.quests().fetchQuest(name);
				if(Q==null)
					mob.tell("Quest '"+name+"' is unknown.  Try list quests.");
				else
				if(!mob.isMonster())
				{
					if((Q.running())&&(mob.session().confirm("Stop quest '"+Q.name()+"' (y/N)?","N")))
					{
						Q.stopQuest();
						mob.tell("Quest '"+Q.name()+"' stopped.");
					}
					else
					if((!Q.running())&&(mob.session().confirm("Start quest '"+Q.name()+"' (Y/n)?","Y")))
					{
						Q.startQuest();
                        if(!Q.running())
    						mob.tell("Quest '"+Q.name()+"' NOT started -- check your mug.log for errors.");
                        else
                            mob.tell("Quest '"+Q.name()+"' started.");
					}
				}
			}
		}
        else
        if(commandType.equals("FACTION"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDFACTIONS")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
            if(commands.size()<3)
                mob.tell("Modify which faction?  Use list factions.");
            else
            {
                String name=CMParms.combine(commands,2);
                Faction F=CMLib.factions().getFaction(name);
                if(F==null) F=CMLib.factions().getFactionByName(name);
                if(F==null)
                    mob.tell("Faction '"+name+"' is unknown.  Try list factions.");
                else
                if(!mob.isMonster())
                {
                    modifyFaction(mob,F);
                    Log.sysOut("CreateEdit",mob.Name()+" modified Faction "+F.name()+" ("+F.factionID()+").");
                }
            }
        }
		else
		{
			String allWord=CMParms.combine(commands,1);
			int x=allWord.indexOf("@");
			MOB srchMob=mob;
			Room srchRoom=mob.location();
			if(x>0)
			{
				String rest=allWord.substring(x+1).trim();
				allWord=allWord.substring(0,x).trim();
				if(rest.equalsIgnoreCase("room"))
					srchMob=null;
				else
				if(rest.length()>0)
				{
					MOB M=srchRoom.fetchInhabitant(rest);
					if(M==null)
					{
						mob.tell("MOB '"+rest+"' not found.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return false;
					}
					srchMob=M;
					srchRoom=null;
				}
			}
			Environmental thang=null;
			if((srchMob!=null)&&(srchRoom!=null))
				thang=srchRoom.fetchFromMOBRoomFavorsItems(srchMob,null,allWord,Item.WORNREQ_ANY);
			else
			if(srchMob!=null)
				thang=srchMob.fetchInventory(allWord);
			else
			if(srchRoom!=null)
				thang=srchRoom.fetchFromRoomFavorItems(null,allWord,Item.WORNREQ_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) 
                    return errorOut(mob);
				Item copyItem=(Item)thang.copyOf();
				if(!thang.isGeneric())
				{
					int showFlag=-1;
					if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
						showFlag=-999;
					boolean ok=false;
					while(!ok)
					{
						int showNumber=0;
						genLevel(mob,thang,++showNumber,showFlag);
						genAbility(mob,thang,++showNumber,showFlag);
						genRejuv(mob,thang,++showNumber,showFlag);
						genUses(mob,(Item)thang,++showNumber,showFlag);
						genMiscText(mob,thang,++showNumber,showFlag);
						if(showFlag<-900){ ok=true; break;}
						if(showFlag>0){ showFlag=-1; continue;}
						showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
						if(showFlag<=0)
						{
							showFlag=-1;
							ok=true;
						}
					}
				}
				else
					genMiscSet(mob,thang);
				thang.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
				if(!copyItem.sameAs(thang))
	                Log.sysOut("CreateEdit",mob.Name()+" modified item "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(mob.location())+".");
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) 
                    return errorOut(mob);
				MOB copyMOB=(MOB)thang.copyOf();
				if((!thang.isGeneric())&&(((MOB)thang).isMonster()))
				{
					int showFlag=-1;
					if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
						showFlag=-999;
					boolean ok=false;
					while(!ok)
					{
						int showNumber=0;
						genLevel(mob,thang,++showNumber,showFlag);
						genAbility(mob,thang,++showNumber,showFlag);
						genRejuv(mob,thang,++showNumber,showFlag);
						genMiscText(mob,thang,++showNumber,showFlag);
						if(showFlag<-900){ ok=true; break;}
						if(showFlag>0){ showFlag=-1; continue;}
						showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
						if(showFlag<=0)
						{
							showFlag=-1;
							ok=true;
						}
					}
					if(!copyMOB.sameAs(thang))
	                    Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(((MOB)thang).location())+".");
				}
				else
				if(!((MOB)thang).isMonster())
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
					players(mob,CMParms.parse("MODIFY USER \""+thang.Name()+"\""));
				}
				else
                {
					genMiscSet(mob,thang);
					if(!copyMOB.sameAs(thang))
	                    Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(((MOB)thang).location())+".");
                }
				thang.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
			}
			else
			if((Directions.getGoodDirectionCode(allWord)>=0)||(thang instanceof Exit))
			{
				if(Directions.getGoodDirectionCode(allWord)>=0)
					thang=mob.location().rawExits()[Directions.getGoodDirectionCode(allWord)];

				if(thang!=null)
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
					Exit copyExit=(Exit)thang.copyOf();
					genMiscText(mob,thang,1,1);
					thang.recoverEnvStats();
					try
					{
						for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
						{
							Room room=(Room)r.nextElement();
				    		synchronized(("SYNC"+room.roomID()).intern())
				    		{
				    			room=CMLib.map().getRoom(room);
								for(int e2=0;e2<Directions.NUM_DIRECTIONS;e2++)
								{
									Exit exit=room.rawExits()[e2];
									if((exit!=null)&&(exit==thang))
									{
										CMLib.database().DBUpdateExits(room);
										break;
									}
								}
				    		}
						}
				    }catch(NoSuchElementException e){}
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
					if(!copyExit.sameAs(thang))
						Log.sysOut("CreateEdit",mob.Name()+" modified exit "+thang.ID()+".");
				}
				else
				{
					commands.insertElementAt("EXIT",1);
					execute(mob,commands);
				}
			}
			else
			if(CMLib.socials().FetchSocial(allWord,true)!=null)
			{
				commands.insertElementAt("SOCIAL",1);
				execute(mob,commands);
			}
			else
				mob.tell("\n\rYou cannot modify a '"+commandType+"'. However, you might try an ITEM, EXIT, QUEST, MOB, USER, JSCRIPT, FACTION, SOCIAL, CLAN, POLL, or ROOM.");
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}

	
}
