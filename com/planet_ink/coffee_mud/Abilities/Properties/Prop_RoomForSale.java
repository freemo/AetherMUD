package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomForSale extends Property implements LandTitle
{
	public String ID() { return "Prop_RoomForSale"; }
	public String name(){ return "Putting a room up for sale";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public Environmental newInstance(){	return new Prop_RoomForSale();}

	private final static String theStr=" This lot is for sale (look id).";
	private boolean confirmedUser=false;
	private int lastNumItems=-1;

	public String accountForYourself()
	{ return "For Sale";	}

	public int landPrice()
	{
		int price=0;
		if(text().indexOf("/")<0)
			price=Util.s_int(text());
		else
			price=Util.s_int(text().substring(text().indexOf("/")+1));
		if(price<=0) price=100000;
		return price;
	}
	public void setLandPrice(int price){
		String owner=landOwner();
		if(owner.length()>0)
			setMiscText(owner+"/"+price);
		else
			setMiscText(""+price);
	}
	public String landOwner()
	{
		if(text().indexOf("/")<0) return "";
		return text().substring(0,text().indexOf("/"));
	}

	public void setLandOwner(String owner)
	{
		int price=landPrice();
		setMiscText(owner+"/"+price);
	}

	public void updateTitle()
	{
		Room R=CMMap.getRoom(landRoomID());
		if(R==null) return;
		CMClass.DBEngine().DBUpdateRoom(R);
	}

	public String landRoomID(){
		if((affected!=null)&&(affected instanceof Room))
			return CMMap.getExtendedRoomID(((Room)affected));
		return "";
	}

	public void setLandRoomID(String landID){}

	public static LandTitle getLandTitle(Room R)
	{
		LandTitle oldTitle=null;
		for(int a=0;a<R.numEffects();a++)
			if(R.fetchEffect(a) instanceof LandTitle)
			{ oldTitle=(LandTitle)R.fetchEffect(a); break;}
		return oldTitle;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
		&&(affected!=null)
		&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			updateLot(R,this);
			Vector mobs=new Vector();
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=(MOB)R.fetchInhabitant(m);
				if((M!=null)
				&&(M.isEligibleMonster())
				&&(M.getStartRoom()==R)
				&&((M.baseEnvStats().rejuv()==0)||(M.baseEnvStats().rejuv()==Integer.MAX_VALUE)))
					mobs.addElement(M);
			}
			CMClass.DBEngine().DBUpdateTheseMOBs(R,mobs);
		}
	}

	public void colorForSale(Room R, boolean reset)
	{
		if(R.description().indexOf(theStr)<0)
		{
			if(reset)
			{
				R.setDisplayText("An empty plot");
				R.setDescription("");
			}
			R.setDescription(R.description()+theStr);
			CMClass.DBEngine().DBUpdateRoom(R);

			Item I=R.fetchItem(null,"id$");
			if((I==null)||(!I.ID().equals("GenWallpaper")))
			{
				I=CMClass.getItem("GenWallpaper");
				I.setReadable(true);
				I.setName("id");
				I.setReadableText("This room is "+CMMap.getExtendedRoomID(R));
				I.setDescription("This room is "+CMMap.getExtendedRoomID(R));
				R.addItem(I);
				CMClass.DBEngine().DBUpdateItems(R);
			}
		}
	}

	public Vector getRooms()
	{
		Vector V=new Vector();
		Room R=CMMap.getRoom(landRoomID());
		if(R!=null) V.addElement(R);
		return V;
	}

	public void updateLot(Room R, LandTitle T)
	{
		if(R==null) R=CMMap.getRoom(landRoomID());
		if(R==null) return;
		if(T==null) T=getLandTitle(R);
		if(T==null) return;
		if(T.landOwner().length()==0)
		{
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if(I.dispossessionTime()==0)
				{
					long now=System.currentTimeMillis();
					now+=(IQCalendar.MILI_HOUR*Item.REFUSE_PLAYER_DROP);
					I.setDispossessionTime(now);
				}
				if((I.envStats().rejuv()!=Integer.MAX_VALUE)
				&&(I.envStats().rejuv()!=0))
				{
					I.baseEnvStats().setRejuv(Integer.MAX_VALUE);
					I.recoverEnvStats();
				}
			}
			if(this instanceof Prop_LotsForSale)
				colorForSale(R,true);
			else
				colorForSale(R,false);
		}
		else
		{
			boolean updateItems=false;
			if(!confirmedUser)
			{
				confirmedUser=true;
				if((!CMClass.DBEngine().DBUserSearch(null,landOwner()))
				&&(Clans.getClan(landOwner())==null))
				{
					T.setLandOwner("");
					updateLot(R,T);
					return;
				}
			}

			int x=R.description().indexOf(theStr);
			if(x>=0)
			{
				R.setDescription(R.description().substring(0,x));
				CMClass.DBEngine().DBUpdateRoom(R);
			}

			// this works on the priciple that
			// 1. if an item has ONLY been removed, the lastNumItems will be != current # items
			// 2. if an item has ONLY been added, the dispossessiontime will be != null
			// 3. if an item has been added AND removed, the dispossession time will be != null on the added
			if(lastNumItems<0)
				lastNumItems=R.numItems();
			else
			if(R.numItems()!=lastNumItems)
				updateItems=true;
			
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I.dispossessionTime()!=0)
				&&(!(I instanceof DeadBody)))
					I.setDispossessionTime(0);
					
				if((I.envStats().rejuv()!=Integer.MAX_VALUE)
				&&(I.envStats().rejuv()!=0))
				{
					I.baseEnvStats().setRejuv(Integer.MAX_VALUE);
					I.recoverEnvStats();
					updateItems=true;
				}
			}
			
			if(updateItems) 
				CMClass.DBEngine().DBUpdateItems(R);
		}
	}
}
