package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class EndlessSky extends StdGrid
{
	public String ID(){return "EndlessSky";}
	protected boolean crossLinked=false;

	public EndlessSky()
	{
		super();
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_AIR;
		domainCondition=Room.CONDITION_NORMAL;
		setDisplayText("Up in the sky");
		setDescription("");
		xsize=CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKYSIZE);
		ysize=CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKYSIZE);
		if((xsize==0)||(ysize==0))
		{
			xsize=3;
			ysize=3;
		}
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		return InTheAir.isOkAirAffect(this,msg);
	}
	public String getChildLocaleID(){return "InTheAir";}

	public Room findMyCenter(int d)
	{
		if(d!=Directions.DOWN)
			return findCenterRoom(d);
		else
			return subMap[subMap.length-1][subMap[0].length-1];
	}
	
	protected void buildFinalLinks()
	{
		Exit ox=CMClass.getExit("Open");
		for(int d=0;d<Directions.NUM_DIRECTIONS-1;d++)
		{
			Room dirRoom=rawDoors()[d];
			Exit dirExit=rawExits()[d];
			if((dirExit==null)||(dirExit.hasADoor()))
				dirExit=ox;
			if(dirRoom!=null)
			{
				Exit altExit=dirRoom.rawExits()[Directions.getOpDirectionCode(d)];
				if(altExit==null) altExit=ox;
				switch(d)
				{
					case Directions.NORTH:
						for(int x=0;x<subMap.length;x++)
							linkRoom(subMap[x][0],dirRoom,d,dirExit,altExit);
						break;
					case Directions.SOUTH:
						for(int x=0;x<subMap.length;x++)
							linkRoom(subMap[x][subMap[0].length-1],dirRoom,d,dirExit,altExit);
						break;
					case Directions.EAST:
						for(int y=0;y<subMap[0].length;y++)
							linkRoom(subMap[subMap.length-1][y],dirRoom,d,dirExit,altExit);
						break;
					case Directions.WEST:
						for(int y=0;y<subMap[0].length;y++)
							linkRoom(subMap[0][y],dirRoom,d,dirExit,altExit);
						break;
					case Directions.UP:
						linkRoom(subMap[0][0],dirRoom,d,dirExit,altExit);
						break;
					case Directions.DOWN:
						linkRoom(subMap[subMap.length-1][subMap[0].length-1],dirRoom,d,dirExit,altExit);
						break;
				}
			}
		}
	}

	public void buildGrid()
	{
		clearGrid(null);
		try
		{
			Exit ox=CMClass.getExit("Open");
			subMap=new Room[xsize][ysize];
			for(int x=0;x<subMap.length;x++)
				for(int y=0;y<subMap[x].length;y++)
				{
					Room newRoom=getGridRoom(x,y);
					if(newRoom!=null)
					{
						subMap[x][y]=newRoom;
						if((y>0)&&(subMap[x][y-1]!=null))
						{
							linkRoom(newRoom,subMap[x][y-1],Directions.NORTH,ox,ox);
							linkRoom(newRoom,subMap[x][y-1],Directions.UP,ox,ox);
						}

						if((x>0)&&(subMap[x-1][y]!=null))
							linkRoom(newRoom,subMap[x-1][y],Directions.WEST,ox,ox);
						CMMap.addRoom(newRoom);
					}
				}
			buildFinalLinks();
			if((subMap[0][0]!=null)
			&&(subMap[0][0].rawDoors()[Directions.UP]==null)
			&&(xsize>1))
				linkRoom(subMap[0][0],subMap[1][0],Directions.UP,ox,ox);
			for(int y=0;y<subMap[0].length;y++)
				linkRoom(subMap[0][y],subMap[subMap.length-1][y],Directions.WEST,ox,ox);
			for(int x=0;x<subMap.length;x++)
				linkRoom(subMap[x][0],subMap[x][subMap[x].length-1],Directions.NORTH,ox,ox);
			for(int x=1;x<subMap.length;x++)
				linkRoom(subMap[x][0],subMap[x-1][subMap[x-1].length-1],Directions.UP,ox,ox);
		}
		catch(Exception e)
		{
			clearGrid(null);
		}
	}
}
