package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_LotsForSale extends Prop_RoomForSale
{
	public String ID() { return "Prop_LotsForSale"; }
	public String name(){ return "Putting many rooms up for sale";}
	public Environmental newInstance(){	return new Prop_LotsForSale();}

	
	private static boolean isCleanRoom(Room fromRoom, Room theRoom)
	{
		if(theRoom==null) return true;
		
		if((theRoom.ID().length()>0)
		&&((getLandTitle(theRoom)==null)||(getLandTitle(theRoom).landOwner().length()>0)))
			return false;
		
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R=theRoom.rawDoors()[d];
			if((R!=null)
			   &&(R!=fromRoom)
			   &&(R.ID().length()>0)
			   &&((getLandTitle(R)==null)||(getLandTitle(R).landOwner().length()>0)))
				return false;
		}
		return true;
	}

	public void updateLot(Room R, LandTitle T)
	{
		if(R==null) R=CMMap.getRoom(landRoomID());
		if(R==null) return;
		if(T==null) T=getLandTitle(R);
		if(T==null) return;
		super.updateLot(R,T);
		if(T.landOwner().length()==0)
		{
			boolean updateExits=false;
			for(int d=0;d<4;d++)
			{
				Room R2=R.rawDoors()[d];
				if((R2!=null)&&(isCleanRoom(R,R2)))
				{
					R.rawDoors()[d]=null;
					R.rawExits()[d]=null;
					updateExits=true;
					ExternalPlay.obliterateRoom(R2);
					R2.getArea().fillInAreaRoom(R2);
				}
			}
			if(updateExits)
				ExternalPlay.DBUpdateExits(R);
		}
		else
		{
			boolean updateExits=false;
			for(int d=0;d<4;d++)
			{
				Room R2=R.getRoomInDir(d);
				if(R2==null)
				{
					R2=CMClass.getLocale(CMClass.className(R));
					R2.setID(ExternalPlay.getOpenRoomID(R.getArea().name()));
					R2.setArea(R.getArea());
					R2.setDisplayText("An empty plot of land");
					R2.setDescription("Posted here is a notice that this lot, "+R2.ID()+", is for sale.");
					Ability newTitle=null;
					for(int a=0;a<R.numAffects();a++)
					{
						Ability A2=R.fetchAffect(a);
						if(A2!=null)
						{
							A2=(Ability)A2.copyOf();
							R2.addNonUninvokableAffect(A2);
							if(A2 instanceof LandTitle)
								newTitle=A2;
						}
					}
					if(newTitle!=null)
						((LandTitle)newTitle).setLandOwner("");
						
					R.rawDoors()[d]=R2;
					R.rawExits()[d]=CMClass.getExit("Open");
					R2.rawDoors()[Directions.getOpDirectionCode(d)]=R;
					R2.rawExits()[Directions.getOpDirectionCode(d)]=CMClass.getExit("Open");
					updateExits=true;
						
					ExternalPlay.DBCreateRoom(R2,CMClass.className(R2));
					CMMap.addRoom(R2);
					R.getArea().fillInAreaRoom(R);
					R2.getArea().fillInAreaRoom(R2);
					ExternalPlay.DBUpdateExits(R2);
				}
			}
			if(updateExits)
				ExternalPlay.DBUpdateExits(R);
		}
	}
}
