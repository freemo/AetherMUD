package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_Auction extends Property
{
	public String ID() { return "Prop_Auction"; }
	public String name(){ return "Auction Ticker";}
	protected int canAffectCode(){return 0;}
	public String accountForYourself(){ return "";	}
	public Environmental auctioning=null;
	public MOB highBidder=null;
	protected String currency="";
    private double highBid=Integer.MIN_VALUE;
    private double bid=Integer.MIN_VALUE;
    private int state=-1;
    private long tickDown=0;
    private long auctionStart=0;

    private static final int STATE_START=0;
    private static final int STATE_RUNOUT=1;
    private static final int STATE_ONCE=2;
    private static final int STATE_TWICE=3;
    private static final int STATE_THREE=4;
    private static final int STATE_CLOSED=5;

	public void setAbilityCode(int code)
	{
		state=code;
		tickDown=15000/MudHost.TICK_TIME;
	}

	protected MOB invoker=null;
	public MOB invoker(){return invoker;}
	public void setInvoker(MOB mob)
	{
		invoker=mob;
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((--tickDown)<=0)
		{
			if((state==STATE_START)&&((System.currentTimeMillis()-auctionStart)<(5*15000)))
			{
				if(((System.currentTimeMillis()-auctionStart)>(3*15000))
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
				V.addElement("The auction for "+auctioning.name()+" is almost done. The current bid is "+CMLib.beanCounter().nameCurrencyShort(M,bid)+".");
				break;
			case STATE_ONCE:
				V.addElement(CMLib.beanCounter().nameCurrencyShort(M,bid)+" for "+auctioning.name()+" going ONCE!");
				break;
			case STATE_TWICE:
				V.addElement(CMLib.beanCounter().nameCurrencyShort(M,bid)+" for "+auctioning.name()+" going TWICE!");
				break;
			case STATE_THREE:
				V.addElement(auctioning.name()+" going for "+CMLib.beanCounter().nameCurrencyShort(M,bid)+"! Last chance!");
				break;
			case STATE_CLOSED:
				{
					if((highBidder!=null)&&(highBidder!=invoker()))
					{
						V.addElement(auctioning.name()+" SOLD to "+highBidder.name()+" for "+CMLib.beanCounter().nameCurrencyShort(M,bid)+".");
						M.doCommand(V);
						if(!CMLib.flags().canMove(highBidder))
						{
							highBidder.tell("You have won the auction, but are unable to pay or collect.  Please contact "+M.name()+" about this matter immediately.");
							M.tell(highBidder.name()+" is unable to pay or collect at this time. Please contact "+highBidder.charStats().himher()+" immediately.");
						}
						else
						if(CMLib.beanCounter().getTotalAbsoluteValue(highBidder,currency)<bid)
						{
							highBidder.tell("You can no longer cover your bid.  Please contact "+M.name()+" about this matter immediately.");
							M.tell(highBidder.name()+" can not cover the bid any longer! Please contact "+highBidder.charStats().himher()+" immediately.");
						}
						else
						{
							if((auctioning instanceof Item)
						    &&(CMLib.flags().isInTheGame(highBidder,true))
                            &&(CMLib.flags().isInTheGame(M,true)))
							{
								((Item)auctioning).unWear();
								highBidder.location().bringItemHere((Item)auctioning,Item.REFUSE_PLAYER_DROP);
								if(CMLib.commands().postGet(highBidder,null,(Item)auctioning,false)
                                ||(highBidder.isMine(auctioning)))
                                {
                                    CMLib.beanCounter().subtractMoney(highBidder,currency,bid);
                                    CMLib.beanCounter().addMoney(M,currency,bid);
    								M.tell(CMLib.beanCounter().nameCurrencyShort(M,bid)+" has been transferred to you as payment from "+highBidder.name()+".  The goods have also been transferred in exchange.");
    								highBidder.tell(CMLib.beanCounter().nameCurrencyShort(M,bid)+" has been transferred to "+M.name()+".  You should have received the auctioned goods.  This auction is complete.");
                                    if(auctioning instanceof LandTitle)
                                    {
                                        CMMsg msg=CMClass.getMsg(M,highBidder,auctioning,CMMsg.MASK_GENERAL|CMMsg.TYP_GIVE,null);
                                        auctioning.executeMsg(highBidder,msg);
                                    }
                                }
                                else
                                {
                                    M.giveItem((Item)auctioning);
                                    M.tell("Your transaction could not be completed because "+highBidder.name()+" was unable to collect the item.  Please contact "+highBidder.name()+" about receipt of "+auctioning.name()+" for "+CMLib.beanCounter().nameCurrencyShort(M,bid)+".");
                                    highBidder.tell("Your transaction could not be completed because you were unable to collect the item.  Please contact "+M.name()+" about receipt of "+auctioning.name()+" for "+CMLib.beanCounter().nameCurrencyShort(M,bid)+".");
                                }
							}
							else
							{
								M.tell("Your transaction could not be completed.  Please contact "+highBidder.name()+" about receipt of "+auctioning.name()+" for "+CMLib.beanCounter().nameCurrencyShort(M,bid)+".");
								highBidder.tell("Your transaction could not be completed.  Please contact "+M.name()+" about receipt of "+auctioning.name()+" for "+CMLib.beanCounter().nameCurrencyShort(M,bid)+".");
							}
						}
					}
					if(M!=null)
						M.doCommand(CMParms.parse("AUCTION CLOSE"));
					setInvoker(null);
				}
				return false;
			}
			M.doCommand(V);
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel)
	{
		Vector V=new Vector();
		V.addElement("AUCTION");
		V.addElement("CHANNEL");
		if(target!=null)
		{
			setInvoker(mob);
			auctioning=target;
			String sb=CMParms.combine(commands,0);
		    currency=CMLib.english().numPossibleGoldCurrency(mob,sb);
		    double denomination=CMLib.english().numPossibleGoldDenomination(mob,currency,sb);
		    long num=CMLib.english().numPossibleGold(mob,sb);
		    bid=CMath.mul(denomination,num);
			highBid=bid-1;
			auctionStart=System.currentTimeMillis();
			setAbilityCode(STATE_START);
			CMLib.threads().startTickDown(this,MudHost.TICK_QUEST,1);
			String bidWords=CMLib.beanCounter().nameCurrencyShort(currency,bid);
			V.addElement("New lot: "+auctioning.name()+".  The opening bid is "+bidWords+".");
		}
		else
		{
			if(state>0)	setAbilityCode(STATE_RUNOUT);
			double b=0;
			String sb="";
			String bwords="0";
			String myCurrency=CMLib.beanCounter().getCurrency(mob);
			if(commands!=null)
			{ 
			    sb=CMParms.combine(commands,0);
			    if(sb.length()>0)
			    {
				    myCurrency=CMLib.english().numPossibleGoldCurrency(mob,sb);
				    if(myCurrency!=null)
				    {
					    double denomination=CMLib.english().numPossibleGoldDenomination(mob,currency,sb);
					    long num=CMLib.english().numPossibleGold(mob,sb);
					    b=CMath.mul(denomination,num);
					    bwords=CMLib.beanCounter().getDenominationName(myCurrency,denomination,num);
				    }
				    else
				        myCurrency=CMLib.beanCounter().getCurrency(mob);
			    }
			}
			String bidWords=CMLib.beanCounter().nameCurrencyShort(currency,bid);
			if(bidWords.length()==0) bidWords="0";
			String currencyName=CMLib.beanCounter().getDenominationName(currency);
			if(sb.length()==0)
			{
				mob.tell("Up for auction: "+auctioning.name()+".  The current bid is "+bidWords+".");
				return true;
			}

			if(!myCurrency.equals(currency))
			{
			    mob.tell("This auction is being bid in "+currencyName+" only.");
				return false;
			}
			
			if(b>CMLib.beanCounter().getTotalAbsoluteValue(mob,currency))
			{
				mob.tell("You don't have enough "+currencyName+" on hand to cover that bid.");
				return false;
			}

			if(b>highBid)
			{
				if((highBidder!=null)&&(highBidder!=mob))
				{
					highBidder.tell("You have been outbid for "+auctioning.name()+".");
					mob.tell("You have the high bid for "+auctioning.name()+".");
				}

				highBidder=mob;
				if(highBid<0.0) highBid=0.0;
				bid=highBid+1.0;
				highBid=b;
			}
			else
			if((b<bid)||(b==0))
			{
				mob.tell("Your bid of "+bwords+" is insufficient."+((bid>0)?" The current high bid is "+bidWords+".":""));
				return false;
			}
			else
			if((b==bid)&&(highBidder!=null))
			{
				mob.tell("You must bid higher than "+bidWords+" to have your bid accepted.");
				return false;
			}
			else
			if((b==highBid)&&(highBidder!=null))
			{
				if((highBidder!=null)&&(highBidder!=mob))
				{
					mob.tell("You have been outbid by proxy for "+auctioning.name()+".");
					highBidder.tell("Your high bid for "+auctioning.name()+" has been reached.");
				}
				bid=b;
			}
			else
			{
				bid=b;
				mob.tell("You have been outbid by proxy for "+auctioning.name()+".");
			}
			bidWords=CMLib.beanCounter().nameCurrencyShort(currency,bid);
			V.addElement("A new bid has been entered for "+auctioning.name()+". The current bid is "+bidWords+".");
		}
		if(invoker()!=null) invoker().doCommand(V);
		return true;
	}
}
