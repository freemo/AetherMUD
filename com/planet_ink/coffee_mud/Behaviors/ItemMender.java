package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ItemMender extends StdBehavior
{
	public ItemMender()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new ItemMender();
	}
	private int cost(Item item)
	{
		int cost=((100-item.usesRemaining())*2)+item.envStats().level();
		if(Sense.isABonusItems(item))
			cost+=100+(item.envStats().level()*2);
		return cost;
	}
	
	public boolean okAffect(Environmental affecting, Affect affect)
	{
		if(!super.okAffect(affecting,affect))
			return false;
		MOB source=affect.source();
		if(!canFreelyBehaveNormal(affecting))
			return true;
		MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(affect.amITarget(observer))
		&&(affect.targetMinor()==Affect.TYP_GIVE)
		&&(!source.isASysOp(source.location()))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Item))
		{
			Item tool=(Item)affect.tool();
			if(!tool.subjectToWearAndTear())
			{
				ExternalPlay.quickSay(observer,source,"I'm sorry, I can't work on these.",true,false);
				return false;
			}
			else
			if(tool.usesRemaining()>100)
			{
				ExternalPlay.quickSay(observer,source,"Take this thing away from me.  It's so perfect, it's scary.",true,false);
				return false;
			}
			if(source.getMoney()<cost(tool))
			{
				ExternalPlay.quickSay(observer,source,"You'll need "+cost((Item)affect.tool())+" gold coins to repair that.",true,false);
				return false;
			}
			return true;
		}
		return true;
	}
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		MOB source=affect.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB observer=(MOB)affecting;
		
		if((source!=observer)
		&&(affect.amITarget(observer))
		&&(affect.targetMinor()==Affect.TYP_GIVE)
		&&(!source.isASysOp(source.location()))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Item))
		{
			source.setMoney(source.getMoney()-cost((Item)affect.tool()));
			((Item)affect.tool()).setUsesRemaining(100);
			FullMsg newMsg=new FullMsg(observer,source,affect.tool(),Affect.MSG_GIVE,"<S-NAME> give(s) "+affect.tool().name()+" to <T-NAMESELF>.");
			affect.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,source,null,Affect.MSG_SPEAK,"<S-NAME> say(s) 'There she is, good as new!  Thanks for your business' to <T-NAMESELF>.");
			affect.addTrailerMsg(newMsg);
		}
	}
}