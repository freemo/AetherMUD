package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Auction extends Property
{
	public String ID() { return "Prop_Auction"; }
	public String name(){ return "Auction Ticker";}
	protected int canAffectCode(){return 0;}
	public Environmental newInstance(){	return new Prop_Auction();}
	public String accountForYourself(){ return "";	}
	public Environmental auctioning=null;
	public MOB highBidder=null;
	public int highBid=Integer.MIN_VALUE;
	public int bid=Integer.MIN_VALUE;
	public int state=-1;
	public long tickDown=0;
	public long auctionStart=0;
	
	public static final int STATE_START=0;
	public static final int STATE_RUNOUT=1;
	public static final int STATE_ONCE=2;
	public static final int STATE_TWICE=3;
	public static final int STATE_CLOSED=4;
	
	public void setAbilityCode(int code)
	{
		state=code;
		tickDown=60000/Host.TICK_TIME;
	}
	
	private MOB invoker=null;
	public MOB invoker(){return invoker;}
	public void setInvoker(MOB mob)
	{
		invoker=mob;
	}
		

	public boolean tick(Tickable ticking, int tickID)
	{
		if((--tickDown)<=0)
		{
			if((state==STATE_START)&&((System.currentTimeMillis()-auctionStart)<(5*60000)))
			{
				if(((System.currentTimeMillis()-auctionStart)>(3*60000))
				&&((highBidder==null)||(highBidder==invoker)))
					setAbilityCode(STATE_RUNOUT);
				else
					setAbilityCode(STATE_START);
				return true;
			}
			setAbilityCode(state+1);
			Vector V=new Vector();
			V.addElement("AUCTION");
			V.addElement("CHANNEL");
			MOB M=invoker();
			switch(state)
			{
			case STATE_RUNOUT:
				V.addElement("The auction for "+auctioning.name()+" is almost done. The current bid is "+bid+".");
				break;
			case STATE_ONCE:
				V.addElement(bid+" gold for "+auctioning.name()+" going ONCE!");
				break;
			case STATE_TWICE:
				V.addElement(bid+" gold for "+auctioning.name()+" going TWICE!");
				break;
			case STATE_CLOSED:
				{
					if((highBidder!=null)&&(highBidder!=invoker()))
					{
						V.addElement(auctioning.name()+" SOLD to "+highBidder.name()+" for "+bid+" gold.");
						try{ExternalPlay.doCommand(M,V);}catch(Exception e){}
						if(Money.totalMoney(highBidder)<bid)
						{
							highBidder.tell("You can no longer cover your bid.  Please contact "+M.name()+" about this matter immediately.");
							M.tell(highBidder.name()+" can not cover the bid any longer! Please contact "+highBidder.charStats().himher()+" immediately.");
						}
						else
						{
							Money.subtractMoney(highBidder,highBidder,bid);
							Money.giveMoney(M,M,bid);
							if((auctioning instanceof Item)
							   &&(highBidder.location()!=null)
							   &&(highBidder.location().isInhabitant(highBidder)))
							{
								((Item)auctioning).remove();
								highBidder.location().bringItemHere((Item)auctioning);
								ExternalPlay.get(highBidder,null,(Item)auctioning,false);
							}
							else
							{
								M.tell(bid+" gold has been transferred to you as payment from "+highBidder.name()+".  Please contact "+highBidder.name()+" about receipt of "+auctioning.name()+".");
								highBidder.tell(bid+" gold has been transferred to "+M.name()+".  Please contact "+M.name()+" about receipt of "+auctioning.name()+".");
							}
						}
					}
				}
				setInvoker(null);
				V=new Vector();
				V.addElement("AUCTION");
				V.addElement("CLOSE");
				try{ExternalPlay.doCommand(M,V);}catch(Exception e){}
				return false;
			}
			try{ExternalPlay.doCommand(M,V);}catch(Exception e){}
		}
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto)
	{
		Vector V=new Vector();
		V.addElement("AUCTION");
		V.addElement("CHANNEL");
		if(target!=null)
		{
			setInvoker(mob);
			auctioning=target;
			bid=Util.s_int(Util.combine(commands,0));
			auctionStart=System.currentTimeMillis();
			setAbilityCode(STATE_START);
			ExternalPlay.startTickDown(this,Host.QUEST_TICK,1);
			V.addElement("New lot: "+auctioning.name()+".  The opening bid is "+bid+".");
		}
		else
		{
			if(state>0)	setAbilityCode(STATE_RUNOUT);
			int b=0;
			String sb="";
			if(commands!=null){ sb=Util.combine(commands,0); b=Util.s_int(sb);}
			
			if(b>Money.totalMoney(mob))
			{
				mob.tell("You don't have enough total money on hand to cover that bid.");
				return false;
			}
			
			if(b>highBid)
			{
				if((highBidder!=null)&&(highBidder!=mob))
					highBidder.tell("You have been outbid for "+auctioning.name()+".");
				
				highBidder=mob;
				if(highBid<0) highBid=0;
				bid=highBid+1;
				highBid=b;
			}
			else
			if((b<bid)||(b==0))
			{
				mob.tell("Your bid of "+b+" is insufficient."+((bid>0)?" The current high bid is "+bid+".":""));
				return false;
			}
			else
				bid=b;
			V.addElement("A new bid has been entered for "+auctioning.name()+". The current bid is "+bid+".");
		}
		try{ExternalPlay.doCommand(invoker(),V);}catch(Exception e){}
		return true;
	}
}
