package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Dice;
import com.planet_ink.coffee_mud.utils.Directions;
import java.util.*;
public class StdMaze extends StdGrid
{
	public String ID(){return "StdMaze";}
	public StdMaze()
	{
		super();
	}
	public Environmental newInstance()
	{
		return new StdMaze();
	}

	protected void buildFinalLinks()
	{
		Exit ox=CMClass.getExit("Open");
		for(int d=0;d<Directions.NUM_DIRECTIONS-1;d++)
		{
			Room dirRoom=rawDoors()[d];
			if(dirRoom!=null)
			{
				alts[d]=findCenterRoom(d);
				if(alts[d]!=null)
					linkRoom(alts[d],dirRoom,d,ox,ox);
			}
		}
	}

	protected boolean goodDir(int x, int y, int dirCode)
	{
		if(dirCode==Directions.UP) return false;
		if(dirCode==Directions.DOWN) return false;
		if((x==0)&&(dirCode==Directions.WEST)) return false;
		if((y==0)&&(dirCode==Directions.NORTH)) return false;
		if((x>=(subMap.length-1))&&(dirCode==Directions.EAST)) return false;
		if((y>=(subMap[0].length-1))&&(dirCode==Directions.SOUTH)) return false;
		return true;
	}

	protected Room roomDir(int x, int y, int dirCode)
	{
		if(!goodDir(x,y,dirCode)) return null;
		return subMap[getX(x,dirCode)][getY(y,dirCode)];
	}

	protected int getY(int y, int dirCode)
	{
		switch(dirCode)
		{
		case Directions.NORTH:
			return y-1;
		case Directions.SOUTH:
			return y+1;
		}
		return y;
	}
	protected int getX(int x, int dirCode)
	{
		switch(dirCode)
		{
		case Directions.EAST:
			return x+1;
		case Directions.WEST:
			return x-1;
		}
		return x;
	}

	protected void mazify(Hashtable visited, int x, int y)
	{
		
		if(visited.get(subMap[x][y])!=null) return;
		Room room=subMap[x][y];
		visited.put(room,room);
		Exit ox=CMClass.getExit("Open");

		boolean okRoom=true;
		while(okRoom)
		{
			okRoom=false;
			for(int d=0;d<Directions.NUM_DIRECTIONS-1;d++)
			{
				Room possRoom=roomDir(x,y,d);
				if(possRoom!=null)
					if(visited.get(possRoom)==null)
					{
						okRoom=true;
						break;
					}
			}
			if(okRoom)
			{
				Room goRoom=null;
				int dirCode=-1;
				while(goRoom==null)
				{
					int d=Dice.roll(1,Directions.NUM_DIRECTIONS,0)-1;
					Room possRoom=roomDir(x,y,d);
					if(possRoom!=null)
						if(visited.get(possRoom)==null)
						{
							goRoom=possRoom;
							dirCode=d;
						}
				}
				linkRoom(room,goRoom,dirCode,ox,ox);
				mazify(visited,getX(x,dirCode),getY(y,dirCode));
			}
		}
	}

	protected void buildMaze()
	{
		Hashtable visited=new Hashtable();
		int x=xsize/2;
		int y=ysize/2;
		mazify(visited,x,y);
	}

	public void buildGrid()
	{
		clearGrid();
		try
		{
			subMap=new Room[xsize][ysize];
			for(int x=0;x<subMap.length;x++)
				for(int y=0;y<subMap[x].length;y++)
				{
					Room newRoom=getGridRoom(x,y);
					if(newRoom!=null)
					{
						subMap[x][y]=newRoom;
						CMMap.addRoom(newRoom);
					}
				}
			buildMaze();
			buildFinalLinks();
		}
		catch(Exception e)
		{
			clearGrid();
		}
	}
}
