package com.planet_ink.coffee_mud.core;
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
   Copyright 2000-2005 Bo Zimmerman

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
public class CMMap
{
	protected static Vector areasList = new Vector();
	protected static Vector roomsList = new Vector();
	protected static Vector playersList = new Vector();
	protected static Vector deitiesList = new Vector();
    protected static Vector postOfficeList=new Vector();
	protected static Hashtable startRooms=new Hashtable();
	protected static Hashtable deathRooms=new Hashtable();
	protected static Hashtable bodyRooms=new Hashtable();
	protected static final int QUADRANT_WIDTH=10;
	protected static Vector space=new Vector();
    protected static Hashtable globalHandlers=new Hashtable();

	public static void theWorldChanged()
	{
		for (Enumeration a=areas(); a.hasMoreElements();)
			((Area)a.nextElement()).clearMaps();
	}
	// areas
	public static int numAreas() { return areasList.size(); }
	public static void addArea(Area newOne)
	{
		areasList.addElement(newOne);
	}
	public static void delArea(Area oneToDel)
	{
		areasList.remove(oneToDel);
	}
	public static void trimRoomsList()
	{
	    synchronized(roomsList)
	    {
	        roomsList.trimToSize();
	    }
	}
	public static Area getArea(String calledThis)
	{
		for(Enumeration a=areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(A.Name().equalsIgnoreCase(calledThis))
				return A;
		}
		return null;
	}
    public static Area findArea(String calledThis)
    {
        Area A=getArea(calledThis);
        if(A!=null) return A;
        for(Enumeration a=areas();a.hasMoreElements();)
        {
            A=(Area)a.nextElement();
            if(A.Name().toUpperCase().startsWith(calledThis))
                return A;
        }
        return null;
    }
	public static Enumeration areas(){
		return areasList.elements();
	}
	public static Area getFirstArea()
	{
		if (areas().hasMoreElements())
			return (Area) areas().nextElement();
		return null;
	}
	public static Area getRandomArea()
	{
		Area A=null;
		while((numAreas()>0)&&(A==null))
		{
			try{
				A=(Area)areasList.elementAt(Dice.roll(1,numAreas(),-1));
			}catch(Exception e){}
		}
		return A;
	}

    public static void addGlobalHandler(Environmental E, int category)
    {
        Vector V=(Vector)globalHandlers.get(new Integer(category));
        if(V==null)
        {
            V=new Vector();
            globalHandlers.put(new Integer(category),V);
        }
        if(!V.contains(E))
            V.add(E);
    }
    
    public static void delGlobalHandler(Environmental E, int category)
    {
        Vector V=(Vector)globalHandlers.get(new Integer(category));
        if(V==null) return;
        V.removeElement(E);
    }
    
    
    
    
    public static MOB god(Room R){
        MOB everywhereMOB=CMClass.getMOB("StdMOB");
        everywhereMOB.setName("somebody");
        everywhereMOB.setLocation(R);
        return everywhereMOB;
    }

	public static boolean isObjectInSpace(SpaceObject O){return space.contains(O);}
	public static void delObjectInSpace(SpaceObject O){	space.removeElement(O);}
	public static void addObjectToSpace(SpaceObject O){	space.addElement(O);}
	
	public static long getDistanceFrom(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt(Util.mul((O1.coordinates()[0]-O2.coordinates()[0]),(O1.coordinates()[0]-O2.coordinates()[0]))
									+Util.mul((O1.coordinates()[1]-O2.coordinates()[1]),(O1.coordinates()[1]-O2.coordinates()[1]))
									+Util.mul((O1.coordinates()[2]-O2.coordinates()[2]),(O1.coordinates()[2]-O2.coordinates()[2]))));
	}
	public static double[] getDirection(SpaceObject FROM, SpaceObject TO)
	{
		double[] dir=new double[2];
		double x=new Long(TO.coordinates()[0]-FROM.coordinates()[0]).doubleValue();
		double y=new Long(TO.coordinates()[1]-FROM.coordinates()[1]).doubleValue();
		double z=new Long(TO.coordinates()[2]-FROM.coordinates()[2]).doubleValue();
		dir[0]=Math.toDegrees(Math.acos(x/Math.sqrt((x*x)+(y*y))));
		dir[1]=Math.toDegrees(Math.acos(z/Math.sqrt((z*z)+(y*y))));
		return dir;
	}
	
	public static void moveSpaceObject(SpaceObject O)
	{
		double x1=Math.cos(Math.toRadians(O.direction()[0]))*Math.sin(Math.toRadians(O.direction()[1]));
		double y1=Math.sin(Math.toRadians(O.direction()[0]))*Math.sin(Math.toRadians(O.direction()[1]));
		double z1=Math.cos(O.direction()[1]);
		O.coordinates()[0]=O.coordinates()[0]+Math.round(Util.mul(O.velocity(),x1));
		O.coordinates()[1]=O.coordinates()[1]+Math.round(Util.mul(O.velocity(),y1));
		O.coordinates()[2]=O.coordinates()[2]+Math.round(Util.mul(O.velocity(),z1));
	}
	
	public static long getRelativeVelocity(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt(new Long(((O1.velocity()*O1.coordinates()[0])-(O2.velocity()*O2.coordinates()[0])*(O1.velocity()*O1.coordinates()[0])-(O2.velocity()*O2.coordinates()[0]))
									+((O1.velocity()*O1.coordinates()[1])-(O2.velocity()*O2.coordinates()[1])*(O1.velocity()*O1.coordinates()[1])-(O2.velocity()*O2.coordinates()[1]))
									+((O1.velocity()*O1.coordinates()[2])-(O2.velocity()*O2.coordinates()[2])*(O1.velocity()*O1.coordinates()[2])-(O2.velocity()*O2.coordinates()[2]))).doubleValue()));
	}
	
	public static String createNewExit(Room from, Room room, int direction)
	{
		Room opRoom=from.rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[Directions.getOpDirectionCode(direction)];

		if((reverseRoom!=null)&&(reverseRoom==from))
			return "Opposite room already exists and heads this way.  One-way link created.";

		if(opRoom!=null)
			from.rawDoors()[direction]=null;

		from.rawDoors()[direction]=room;
		Exit thisExit=from.rawExits()[direction];
		if(thisExit==null)
		{
			thisExit=CMClass.getExit("StdOpenDoorway");
			from.rawExits()[direction]=thisExit;
		}
		CMLib.database().DBUpdateExits(from);

		if(room.rawDoors()[Directions.getOpDirectionCode(direction)]==null)
		{
			room.rawDoors()[Directions.getOpDirectionCode(direction)]=from;
			room.rawExits()[Directions.getOpDirectionCode(direction)]=thisExit;
			CMLib.database().DBUpdateExits(room);
		}
		return "";
	}

	public static String getOpenRoomID(String AreaID)
	{
		int highest=Integer.MIN_VALUE;
		int lowest=Integer.MAX_VALUE;
		Hashtable allNums=new Hashtable();
		try
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R.getArea().Name().equals(AreaID))
				&&(R.roomID().startsWith(AreaID+"#")))
				{
					int newnum=Util.s_int(R.roomID().substring(AreaID.length()+1));
					if(newnum>=highest)	highest=newnum;
					if(newnum<=lowest) lowest=newnum;
					allNums.put(new Integer(newnum),R);
				}
			}
	    }catch(NoSuchElementException e){}
		if((highest<0)&&(getRoom(AreaID+"#0"))==null)
			return AreaID+"#0";
		if(lowest>highest) lowest=highest+1;
		for(int i=lowest;i<=highest+1000;i++)
		{
			if((!allNums.containsKey(new Integer(i)))
			&&(getRoom(AreaID+"#"+i)==null))
				return AreaID+"#"+i;
		}
		return AreaID+"#"+Math.random();
	}


	public static int numRooms() { return roomsList.size(); }
	public static void addRoom(Room newOne)
	{
	    synchronized(roomsList)
	    {
			roomsList.addElement(newOne);
	    }
		theWorldChanged();
	}
	public static void delRoom(Room oneToDel)
	{
	    synchronized(roomsList)
	    {
			if(oneToDel instanceof GridLocale)
				((GridLocale)oneToDel).clearGrid(null);
			roomsList.removeElement(oneToDel);
	    }
		theWorldChanged();
	}
	public static void justDelRoom(Room oneToDel)
	{
	    synchronized(roomsList)
	    {
		    if(oneToDel!=null)
				roomsList.removeElement(oneToDel);
	    }
	}

    
    public static boolean sendGlobalMessage(MOB host, int category, CMMsg msg)
    {
        Vector V=(Vector)globalHandlers.get(new Integer(category));
        if(V==null) return true;
        try{
            Environmental E=null;
            for(int v=V.size()-1;v>=0;v--)
            {
                E=(Environmental)V.elementAt(v);
                if(!CMLib.flags().isInTheGame(E,true))
                {
                    if(!CMLib.flags().isInTheGame(E,false))
                        CMMap.delGlobalHandler(E,category);
                }
                else
                if(!E.okMessage(host,msg))
                    return false;
            }
            for(int v=V.size()-1;v>=0;v--)
            {
                E=(Environmental)V.elementAt(v);
                E.executeMsg(host,msg);
            }
        }
        catch(ArrayIndexOutOfBoundsException x){}
        return true;
    }

	public static String getExtendedRoomID(Room R)
	{
		if(R==null) return "";
		if(R.roomID().length()>0) return R.roomID();
		Area A=R.getArea();
		if(A==null) return "";
		GridLocale GR=R.getGridParent();
		if(GR!=null) return GR.getChildCode(R);
		return R.roomID();
	}

	public static Room getRoom(String calledThis)
	{
		try
		{
			if(calledThis==null) return null;
			if(calledThis.endsWith(")"))
			{
				int child=calledThis.lastIndexOf("#(");
				if(child>1)
				{
					Room R=getRoom(calledThis.substring(0,child));
					if((R!=null)&&(R instanceof GridLocale))
                    {
						R=((GridLocale)R).getChild(calledThis);
                        if(R!=null) return R;
                    }
				}
			}
            Room R=null;
            for(Enumeration e=roomsList.elements();e.hasMoreElements();)
			{
				R = (Room)e.nextElement();
				if (R.roomID().equalsIgnoreCase(calledThis))
					return R;
			}
		}
		catch(java.util.NoSuchElementException x){}
		return null;
	}
	public static Enumeration rooms() {
		return roomsList.elements();
	}
	public static void replaceRoom(Room newOne, Room oldOne)
	{
	    synchronized(roomsList)
	    {
			if(oldOne instanceof GridLocale)
			  ((GridLocale)oldOne).clearGrid(null);
			roomsList.removeElement(oldOne);
			roomsList.addElement(newOne);
	    }
		theWorldChanged();
	}
	public static Room getFirstRoom()
	{
		if (rooms().hasMoreElements())
			return (Room) rooms().nextElement();
		return null;
	}
	public static Room getRandomRoom()
	{
		Room R=null;
		while((numRooms()>0)&&(R==null))
		{
			try{
				R=(Room)roomsList.elementAt(Dice.roll(1,numRooms(),-1));
			}catch(Exception e){}
		}
		return R;
	}

	public static int numDeities() { return deitiesList.size(); }
	public static void addDeity(Deity newOne)
	{
		if (!deitiesList.contains(newOne))
			deitiesList.add(newOne);
	}
	public static void delDeity(Deity oneToDel)
	{
		deitiesList.remove(oneToDel);
	}
	public static Deity getDeity(String calledThis)
	{
		Deity D = null;
		for (Enumeration i=deities(); i.hasMoreElements();)
		{
			D = (Deity)i.nextElement();
			if (D.Name().equalsIgnoreCase(calledThis))
				return D;
		}
		return null;
	}
	public static Enumeration deities() { return deitiesList.elements(); }

    public static int numPostOffices() { return postOfficeList.size(); }
    public static void addPostOffice(PostOffice newOne)
    {
        if (!postOfficeList.contains(newOne))
            postOfficeList.add(newOne);
    }
    public static void delPostOffice(PostOffice oneToDel)
    {
        postOfficeList.remove(oneToDel);
    }
    public static PostOffice getPostOffice(String chain, String areaNameOrBranch)
    {
        PostOffice P = null;
        for (Enumeration i=postOffices(); i.hasMoreElements();)
        {
            P = (PostOffice)i.nextElement();
            if((P.postalChain().equalsIgnoreCase(chain))
            &&(P.postalBranch().equalsIgnoreCase(areaNameOrBranch)))
                return P;
        }
        Area A=CMMap.findArea(areaNameOrBranch);
        if(A==null) return null;
        for (Enumeration i=postOffices(); i.hasMoreElements();)
        {
            P = (PostOffice)i.nextElement();
            if((P.postalChain().equalsIgnoreCase(chain))
            &&(P instanceof MOB)
            &&(((MOB)P).getStartRoom()!=null)
            &&(((MOB)P).getStartRoom().getArea()==A))
                return P;
        }
        return null;
    }
    public static Enumeration postOffices() { return postOfficeList.elements(); }
    
	public static int numPlayers() { return playersList.size(); }
	public static void addPlayer(MOB newOne) { playersList.add(newOne); }
	public static void delPlayer(MOB oneToDel) { playersList.remove(oneToDel); }
	public static MOB getPlayer(String calledThis)
	{
		MOB M = null;

		for (Enumeration p=players(); p.hasMoreElements();)
		{
			M = (MOB)p.nextElement();
			if (M.Name().equalsIgnoreCase(calledThis))
				return M;
		}
		return null;
	}

	public static MOB getLoadPlayer(String last)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return null;

		MOB M=getPlayer(last);
		if(M!=null) return M;

		for(Enumeration p=players();p.hasMoreElements();)
		{
			MOB mob2=(MOB)p.nextElement();
			if(mob2.Name().equalsIgnoreCase(last))
			{ return mob2;}
		}

		MOB TM=CMClass.getMOB("StdMOB");
		if(CMLib.database().DBUserSearch(TM,last))
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
		return M;
	}

	public static Enumeration players() { return playersList.elements(); }

    
	public static Room getStartRoom(MOB mob)
	{
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String realrace=mob.baseCharStats().getMyRace().ID().toUpperCase();
		realrace.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity.replace(' ','_');
		String align=CMLib.flags().getAlignmentName(mob);
		String roomID=(String)startRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)startRooms.get(realrace);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=(String)startRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
		{
		    Vector V=mob.fetchFactionRanges();
		    for(int v=0;v<V.size();v++)
			    if(startRooms.containsKey(((String)V.elementAt(v)).toUpperCase()))
			    { roomID=(String)startRooms.get(((String)V.elementAt(v)).toUpperCase()); break;}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=(String)startRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)startRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=getRoom("START");
		if((room==null)&&(numRooms()>0))
			room=getFirstRoom();
		return room;
	}

	public static Room getDeathRoom(MOB mob)
	{
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity.replace(' ','_');
		String align=CMLib.flags().getAlignmentName(mob);
		String roomID=(String)deathRooms.get(race);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=(String)deathRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
		{
		    Vector V=mob.fetchFactionRanges();
		    for(int v=0;v<V.size();v++)
			    if(deathRooms.containsKey(((String)V.elementAt(v)).toUpperCase()))
			    { roomID=(String)deathRooms.get(((String)V.elementAt(v)).toUpperCase()); break;}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=(String)deathRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)deathRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.getStartRoom();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=mob.getStartRoom();
		if((room==null)&&(numRooms()>0))
			room=getFirstRoom();
		return room;
	}

	public static Room getBodyRoom(MOB mob)
	{
	    if((mob.getClanID().length()>0)
	    &&(mob.getClanRole()!=Clan.POS_APPLICANT)
	    &&((!mob.isMonster())||(mob.getStartRoom()==null)))
	    {
	        Clan C=Clans.getClan(mob.getClanID());
		    if((C!=null)&&(C.getMorgue().length()>0))
		    {
		        Room room=CMMap.getRoom(C.getMorgue());
		        if((room!=null)&&(CoffeeUtensils.doesHavePriviledgesHere(mob,room)))
		            return room;
		    }
	    }
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race.replace(' ','_');
		String realrace=mob.baseCharStats().getMyRace().ID().toUpperCase();
		realrace.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity.replace(' ','_');
		String align=CMLib.flags().getAlignmentName(mob);
		String roomID=(String)bodyRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)bodyRooms.get(realrace);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=(String)bodyRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
		{
		    Vector V=mob.fetchFactionRanges();
		    for(int v=0;v<V.size();v++)
			    if(bodyRooms.containsKey(((String)V.elementAt(v)).toUpperCase()))
			    { roomID=(String)bodyRooms.get(((String)V.elementAt(v)).toUpperCase()); break;}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=(String)bodyRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=(String)bodyRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.location();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=getRoom(roomID);
		if(room==null)
			room=mob.location();
		if((room==null)&&(numRooms()>0))
			room=getFirstRoom();
		return room;
	}

	private static void pageRooms(CMProps page, Hashtable table, String start)
	{
		for(Enumeration i=page.keys();i.hasMoreElements();)
		{
			String k=(String)i.nextElement();
			if(k.startsWith(start+"_"))
				table.put(k.substring(start.length()+1),page.getProperty(k));
		}
		String thisOne=page.getProperty(start);
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("ALL",thisOne);
	}

	public static void initStartRooms(CMProps page)
	{
		startRooms=new Hashtable();
		pageRooms(page,startRooms,"START");
	}

	public static void initDeathRooms(CMProps page)
	{
		deathRooms=new Hashtable();
		pageRooms(page,deathRooms,"DEATH");
	}

	public static void initBodyRooms(CMProps page)
	{
		bodyRooms=new Hashtable();
		pageRooms(page,bodyRooms,"MORGUE");
	}

	public static void renameRooms(Area A, String oldName, Vector allMyDamnRooms)
	{
		Vector onesToRenumber=new Vector();
		for(int r=0;r<allMyDamnRooms.size();r++)
		{
			Room R=(Room)allMyDamnRooms.elementAt(r);
			R.setArea(A);
			if(oldName!=null)
			{
				if(R.roomID().startsWith(oldName+"#"))
				{
					Room R2=CMMap.getRoom(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
					if((R2==null)||(!R2.roomID().startsWith(A.Name()+"#")))
					{
						String oldID=R.roomID();
						R.setRoomID(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
						CMLib.database().DBReCreate(R,oldID);
					}
					else
						onesToRenumber.addElement(R);
				}
				else
					CMLib.database().DBUpdateRoom(R);
			}
		}
		A.clearMaps();
		if(oldName!=null)
		{
			for(int r=0;r<onesToRenumber.size();r++)
			{
				Room R=(Room)onesToRenumber.elementAt(r);
				String oldID=R.roomID();
				R.setRoomID(CMMap.getOpenRoomID(A.Name()));
				CMLib.database().DBReCreate(R,oldID);
			}
		}
	}

	public static void unLoad()
	{
		areasList.clear();
		roomsList.clear();
		deitiesList.clear();
		playersList.clear();
		space=new Vector();
		bodyRooms=new Hashtable();
		startRooms=new Hashtable();
		deathRooms=new Hashtable();
        globalHandlers.clear();
	}

    
    public boolean explored(Room R, Vector areas)
    {
        if((R==null) 
        ||(Util.bset(R.envStats().sensesMask(),EnvStats.SENSE_ROOMUNEXPLORABLE))
        ||(R.getArea()==null))
            return false;
        return false;
    }
    
	public static class CrossExit
	{
		public int x;
		public int y;
		public int dir;
		public String destRoomID="";
		public boolean out=false;
		public static CrossExit make(int xx, int xy, int xdir, String xdestRoomID, boolean xout)
		{   CrossExit EX=new CrossExit();
			EX.x=xx;EX.y=xy;EX.dir=xdir;EX.destRoomID=xdestRoomID;EX.out=xout;
			return EX;
		}
	}
}
