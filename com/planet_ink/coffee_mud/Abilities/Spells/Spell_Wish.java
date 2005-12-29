package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_Wish extends Spell
{
	public String ID() { return "Spell_Wish"; }
	public String name(){return "Wish";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ALTERATION;}
	public long flags(){return Ability.FLAG_NOORDERING;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	protected Environmental maybeAdd(Environmental E, Vector foundAll, Environmental foundThang)
	{
		if((E!=null)
		&&((foundThang==null)
		   ||((foundThang.ID().equals(E.ID()))&&(foundThang.name().equals(E.name())))))
		{
			if(foundThang==null) foundThang=E;
			foundAll.addElement(E);
		}
		return foundThang;
	}

	private void bringThangHere(MOB mob, Room here, Environmental target)
	{
		if(target instanceof MOB)
		{
			mob.location().show((MOB)target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> teleport(s) to "+here.displayText()+".");
			here.bringMobHere((MOB)target,false);
		}
		else
		if(target instanceof Item)
		{
			Item item=(Item)target;
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is teleport to "+here.displayText()+"!");
			item.unWear();
			item.setContainer(null);
			item.removeFromOwnerContainer();
			here.addItemRefuse(item,Item.REFUSE_PLAYER_DROP);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" appears out of the java plain!");
		}
	}

	public void wishDrain(MOB mob, int expLoss, boolean conLoss)
	{
		if(mob==null) return;
		CMLib.combat().postExperience(mob,null,null,-expLoss,false);
		if(conLoss)
		{
			mob.tell("Your wish drains you of "+(expLoss)+" experience points and a point of constitution.");
			mob.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)-1);
			mob.baseCharStats().setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,mob.baseCharStats().getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-1);
			mob.recoverCharStats();
			if(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)<1)
				CMLib.combat().postDeath(mob,mob,null);
		}
		else
			mob.tell("Your wish drains "+(expLoss)+" experience points.");
	}
	
	public void age(MOB mob)
	{
	    Ability A=CMClass.getAbility("Chant_SpeedAging");
	    if(A!=null){ A.setAbilityCode(65536); A.invoke(mob,mob,true,0);}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isMonster())
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> sigh(s).");
			CMLib.commands().postSay(mob,null,"My wishes never seem to come true.",false,false);
			return false;
		}

		String myWish=CMParms.combine(commands,0);
		if(((!auto)&&(mob.envStats().level()<20))||(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)<2))
		{
			mob.tell("You are too weak to wish.");
			return false;
		}
		if(myWish.toUpperCase().trim().startsWith("FOR ")) myWish=myWish.trim().substring(3);
		if(myWish.length()==0)
		{
			mob.tell("What would you like to wish for?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int baseLoss=25;
		CMMsg msg=CMClass.getMsg(mob,null,this,affectType(auto),"^S<S-NAME> wish(es) for '"+myWish+"'!!^?");
		boolean success=profficiencyCheck(mob,0,auto);
		if(!success)
		{
			CMLib.combat().postExperience(mob,null,null,-baseLoss,false);
			beneficialWordsFizzle(mob,null,"<S-NAME> wish(es) for '"+myWish+"', but the spell fizzles.");
			return false;
		}
		else
		if(mob.location().okMessage(mob,msg))
		{
			// cast wish bless were cast on me
			// cast wish to have restoration cast on me
			// cast wish to cast bless on me
			// cast wish to cast disintegrate on orc
			// cast wish to cast geas on orc to kill bob
			Log.sysOut("Wish",mob.Name()+" wished for "+myWish+".");

			mob.location().send(mob,msg);
			StringBuffer wish=new StringBuffer(myWish);
			for(int i=0;i<wish.length();i++)
				if(!Character.isLetterOrDigit(wish.charAt(i)))
					wish.setCharAt(i,' ');
			myWish=wish.toString().trim().toUpperCase();
			Vector wishV=CMParms.parse(myWish);
			myWish=" "+myWish+" ";
			if(wishV.size()==0)
			{
				CMLib.combat().postExperience(mob,null,null,-baseLoss,false);
				beneficialWordsFizzle(mob,null,"<S-NAME> make(s) a wish comes true! Nothing happens!");
				return false;
			}

			// do locate object first.. its the most likely
			String objectWish=myWish;
			String[] redundantStarts={"CREATE","TO CREATE","ANOTHER","THERE WAS","I HAD","I COULD HAVE","MAY I HAVE","CAN I HAVE","CAN YOU","CAN I","MAKE","TO MAKE","GIVE","ME","TO HAVE","TO GET","A NEW","SOME MORE","MY OWN","A","PLEASE","THE","I OWNED"};
			String[] redundantEnds={"TO APPEAR","OF MY OWN","FOR ME","BE","CREATED","PLEASE","HERE"};
			int i=0;
			while(i<redundantStarts.length)
			{
				if(objectWish.startsWith(" "+redundantStarts[i]+" "))
				{	objectWish=objectWish.substring(1+redundantStarts[i].length()); i=-1;}
				i++;
			}
			i=0;
			while(i<redundantEnds.length){
				if(objectWish.endsWith(" "+redundantEnds[i]+" "))
				{	objectWish=objectWish.substring(0,objectWish.length()-(1+redundantEnds[i].length())); i=-1;}i++;}
			String goldWish=objectWish.toUpperCase();
			objectWish=objectWish.toLowerCase().trim();

			String[] redundantGoldStarts={"A PILE OF","A STACK OF","PILE OF","STACK OF"};
			i=0;
			while(i<redundantGoldStarts.length)
			{
				if(goldWish.startsWith(" "+redundantGoldStarts[i]+" "))
				{	goldWish=goldWish.substring(1+redundantGoldStarts[i].length()); i=-1;}
				i++;
			}
			Vector goldCheck=CMParms.parse(goldWish.trim().toLowerCase());
			if((goldCheck.size()>1)
			&&(CMath.isNumber((String)goldCheck.firstElement()))
			&&(CMath.s_int((String)goldCheck.firstElement())>0)
			&&(CMLib.english().matchAnyCurrencySet(CMParms.combine(goldCheck,1))!=null))
			{
				Coins newItem=(Coins)CMClass.getItem("StdCoins");
				newItem.setCurrency(CMLib.english().matchAnyCurrencySet(CMParms.combine(goldCheck,1)));
				newItem.setDenomination(CMLib.english().matchAnyDenomination(newItem.getCurrency(),CMParms.combine(goldCheck,1)));
				newItem.setNumberOfCoins(CMath.s_long((String)goldCheck.firstElement()));
				newItem.setContainer(null);
				newItem.wearAt(0);
				newItem.recoverEnvStats();
				mob.location().addItemRefuse(newItem,Item.REFUSE_PLAYER_DROP);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");
				mob.location().recoverRoomStats();
				int experienceRequired=(int)Math.round(CMath.div(newItem.getTotalValue(),10.0));
				if(experienceRequired<=0)
					experienceRequired=0;
				wishDrain(mob,(baseLoss+experienceRequired),false);
				return true;
			}

			Vector thangsFound=new Vector();
			Environmental foundThang=null;
			Environmental E=mob.location().fetchFromRoomFavorItems(null,objectWish,Item.WORNREQ_UNWORNONLY);
            ShopKeeper SK=null;
			foundThang=maybeAdd(E,thangsFound,foundThang);
			try
			{
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room room=(Room)r.nextElement();
					if(CMLib.flags().canAccess(mob,room))
					{
						E=room.fetchFromRoomFavorMOBs(null,objectWish,Item.WORNREQ_UNWORNONLY);
						foundThang=maybeAdd(E,thangsFound,foundThang);
					}
				}
		    }catch(NoSuchElementException nse){}
		    try
		    {
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room room=(Room)r.nextElement();
					if(CMLib.flags().canAccess(mob,room))
					for(int m=0;m<room.numInhabitants();m++)
					{
						MOB mob2=room.fetchInhabitant(m);
						if(mob2!=null)
						{
							E=mob2.fetchInventory(objectWish);
							foundThang=maybeAdd(E,thangsFound,foundThang);
                            SK=CMLib.coffeeShops().getShopKeeper(mob2);
							if(SK!=null)
							{
								E=SK.getShop().getStock(objectWish,mob,SK.whatIsSold(),mob2.getStartRoom());
								foundThang=maybeAdd(E,thangsFound,foundThang);
							}
						}
					}
				}
		    }catch(NoSuchElementException nse){}
			if((thangsFound.size()>0)&&(foundThang!=null))
			{
				// yea, we get to DO something!
				int experienceRequired=100*(foundThang.envStats().level()-1);
				if(foundThang instanceof MOB)
				{
					MOB newMOB=(MOB)foundThang.copyOf();
					newMOB.setStartRoom(null);
					newMOB.setLocation(mob.location());
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(mob.location(),true);
					newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
					newMOB.setFollowing(mob);
				}
				else
				if((foundThang instanceof Item)
				   &&(!(foundThang instanceof ArchonOnly)))
				{
					Item newItem=(Item)foundThang.copyOf();
					newItem.setContainer(null);
					newItem.wearAt(0);
					mob.location().addItemRefuse(newItem,Item.REFUSE_PLAYER_DROP);
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");
					mob.location().recoverRoomStats();
				}
				if(experienceRequired<=0)
					experienceRequired=0;
				wishDrain(mob,(baseLoss+experienceRequired),false);
				return true;
			}

			// anything else may refer to another person or item
			Environmental target=null;
			String possName=((String)wishV.elementAt(0)).trim();
			if(wishV.size()>2)
			{
				possName=CMParms.combine(wishV,0,2);
				if(target==null) target=mob.location().fetchFromRoomFavorMOBs(null,possName,Item.WORNREQ_UNWORNONLY);
				if(target==null) target=mob.fetchInventory(possName);
				if(target==null) possName=((String)wishV.elementAt(0)).trim();
			}
			if(target==null) target=mob.location().fetchFromRoomFavorMOBs(null,possName,Item.WORNREQ_UNWORNONLY);
			if(target==null) target=mob.fetchInventory(possName);
			if((target==null)
			||(possName.equalsIgnoreCase("FOR"))
			||(possName.equalsIgnoreCase("TO"))
			||(possName.equalsIgnoreCase("BE"))
			||(possName.equalsIgnoreCase("WOULD"))
			||(possName.equalsIgnoreCase("A"))
			||(possName.equalsIgnoreCase("THE"))
			||(possName.equalsIgnoreCase("AN"))
			||(possName.equalsIgnoreCase("I")))
			{
				if(possName.equalsIgnoreCase("I"))
				{
					wishV.removeElementAt(0);
					myWish=" "+CMParms.combine(wishV,0).toUpperCase()+" ";
				}
				target=mob;
			}
			else
			{
				wishV.removeElementAt(0);
				myWish=" "+CMParms.combine(wishV,0).toUpperCase().trim()+" ";
			}

			if((target!=null)
			&&(target!=mob)
			&&(target instanceof MOB)
			&&(!((MOB)target).isMonster())
			&&(!mob.mayIFight((MOB)target)))
			{
				mob.tell("You cannot cast wish on "+target.name()+" until "+mob.charStats().heshe()+" permits you. You must both toggle your playerkill flags on.");
				return false;
			}

			// a wish for recall
			if((myWish.startsWith(" TO BE RECALLED "))
			||(myWish.startsWith(" TO RECALL "))
			||(myWish.startsWith(" RECALL "))
			||(myWish.startsWith(" BE RECALLED "))
			||(myWish.startsWith(" WAS RECALLED "))
			||(myWish.startsWith(" WOULD RECALL "))
			||(myWish.endsWith(" WAS RECALLED "))
			||(myWish.endsWith(" WOULD RECALL "))
			||(myWish.endsWith(" TO RECALL "))
			||(myWish.endsWith(" BE RECALLED "))
			||(myWish.endsWith(" RECALL ")))
			{
				Room recallRoom=mob.getStartRoom();
				if((recallRoom==null)&&(target instanceof MOB)&&(((MOB)target).getStartRoom()!=null))
					recallRoom=((MOB)target).getStartRoom();
				if(recallRoom!=null)
				{
					wishDrain(mob,baseLoss,false);
					bringThangHere(mob,recallRoom,target);
					return true;
				}
			}

			// a wish for death or destruction
			if((myWish.startsWith(" TO DIE "))
			||(myWish.startsWith(" TO BE DESTROYED "))
			||(myWish.startsWith(" TO CROAK "))
			||(myWish.startsWith(" WAS DEAD "))
			||(myWish.startsWith(" WAS GONE "))
			||(myWish.startsWith(" WOULD GO AWAY "))
			||(myWish.startsWith(" WAS BANISHED "))
			||(myWish.startsWith(" WOULD DIE "))
			||(myWish.startsWith(" WOULD BE DEAD "))
			||(myWish.startsWith(" WAS DESTROYED "))
			||(myWish.startsWith(" DEATH "))
			||(myWish.startsWith(" FOR DEATH "))
			||(myWish.startsWith(" DESTRUCTION "))
			||(myWish.startsWith(" TO BE BANISHED "))
			||(myWish.startsWith(" TO BE DEAD "))
			||(myWish.startsWith(" TO BE GONE "))
			||(myWish.startsWith(" TO DISAPPEAR "))
			||(myWish.startsWith(" TO VANISH "))
			||(myWish.startsWith(" TO BE INVISIBLE "))
			||(myWish.startsWith(" TO GO AWAY "))
			||(myWish.startsWith(" TO GO TO HELL ")))
			{
				if(target instanceof Item)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" quietly vanishes.");
					((Item)target).destroy();
				}
				else
				if(target instanceof MOB)
				{
					int exp=mob.getExperience();
					//int hp=((MOB)target).curState().getHitPoints();
					CMLib.combat().postDeath(mob,(MOB)target,null);
					if((!CMSecurity.isDisabled("EXPERIENCE"))
					&&!mob.charStats().getCurrentClass().expless()
					&&!mob.charStats().getMyRace().expless()
					&&(mob.getExperience()>exp))
					{
						baseLoss=mob.getExperience()-exp;
						mob.setExperience(exp);
					}
				}
				wishDrain(mob,baseLoss,false);
				return true;
			}

			// a wish for movement
			String locationWish=myWish;
			String[] redundantStarts2={"TO GO TO",
									  "TO TELEPORT TO",
									  "TO TRANSPORT TO",
									  "TO TRANSFER TO",
									  "TO PORTAL TO",
									  "WOULD TELEPORT TO",
									  "WOULD TRANSPORT TO",
									  "WOULD TRANSFER TO",
									  "WOULD PORTAL TO",
									  "WOULD GO TO",
									  "TO PORTAL TO",
									  "TO BE TELEPORTED TO",
									  "TO BE TRANSPORTED TO",
									  "TO BE TRANSFERRED TO",
									  "TO BE PORTALLED TO",
									  "TO BE PORTALED TO",
									  "TO BE TELEPORTED",
									  "TO BE TRANSPORTED",
									  "TO BE TRANSFERRED",
									  "TO BE PORTALLED",
									  "TO BE PORTALED",
									  "TO APPEAR IN ",
									  "TO BE IN",
									  "TO APPEAR AT",
									  "TO BE AT",
									  "TO GO",
									  "TO MOVE TO",
									  "TO MOVE",
									  "TO BE AT",
									  "TO BE IN",
									  "TO BE",
									  "TO TRAVEL",
									  "TO WALK TO",
									  "TO WALK",
									  "TO TRAVEL TO",
									  "TO GOTO",
									  "TELEPORTATION TO",
									  "TRANSPORTED TO",
									  "TELEPORTED TO",
									  "TRANSFERRED TO",
									  "WAS TRANSPORTED TO",
									  "WAS TELEPORTED TO",
									  "WAS TRANSFERRED TO",
									  "TELEPORT",
									  "GO",
									  "GO TO",
									  "GOTO",
									  "TRANSFER",
									  "PORTAL",
									  "TELEPORTATION"};
			String[] redundantEnds2={"IMMEDIATELY","PLEASE","NOW","AT ONCE"};
			boolean validStart=false;
			i=0;
			while(i<redundantStarts2.length){
				if(locationWish.startsWith(" "+redundantStarts2[i]+" "))
				{	validStart=true; locationWish=locationWish.substring(1+redundantStarts2[i].length()); i=-1;}i++;}
			i=0;
			while(i<redundantEnds2.length){
				if(locationWish.endsWith(" "+redundantEnds2[i]+" "))
				{	locationWish=locationWish.substring(0,locationWish.length()-(1+redundantEnds2[i].length())); i=-1;}i++;}

			// a wish for teleportation
			if(validStart)
			{
				Room newRoom=null;
				int dir=Directions.getGoodDirectionCode((String)wishV.lastElement());
				if(dir>=0)
					newRoom=mob.location().getRoomInDir(dir);
				if(newRoom==null)
				{
				    try
				    {
						for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
						{
							Room room=(Room)r.nextElement();
							if(CMLib.flags().canAccess(mob,room))
							if(CMLib.english().containsString(room.displayText(),locationWish.trim()))
							{
							   newRoom=room;
							   break;
							}
						}
				    }catch(NoSuchElementException nse){}
				}
				if(newRoom!=null)
				{
					bringThangHere(mob,newRoom,target);
					wishDrain(mob,baseLoss,false);
					return true;
				}
			}

			// temporary stat changes
			if((target instanceof MOB)
			&&((myWish.indexOf(" MORE ")>=0)
			||(myWish.indexOf(" HIGHER ")>=0)
			||(myWish.indexOf(" BIGGER ")>=0)
			||(myWish.indexOf(" TO HAVE ")>=0)))
			{
				MOB tm=(MOB)target;
				if((myWish.indexOf("HIT POINT")>=0)&&(tm.curState().getHitPoints()<tm.maxState().getHitPoints()))
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is healthier!");
					tm.curState().setHitPoints(tm.maxState().getHitPoints());
					wishDrain(mob,baseLoss,true);
					return true;
				}
				else
				if(myWish.indexOf("HIT POINT")>=0)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is healthier!");
					tm.baseState().setHitPoints(tm.baseState().getHitPoints()+2);
					tm.recoverMaxState();
					wishDrain(mob,baseLoss,true);
					return true;
				}
				if((myWish.indexOf("MANA")>=0)&&(tm.curState().getMana()<tm.maxState().getMana()))
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" has more mana!");
					tm.curState().setMana(tm.maxState().getMana());
					wishDrain(mob,baseLoss,true);
					return true;
				}
				else
				if(myWish.indexOf("MANA")>=0)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" has more mana!");
					tm.baseState().setMana(tm.baseState().getMana()+2);
					tm.recoverMaxState();
					wishDrain(mob,baseLoss,true);
					return true;
				}
				if((myWish.indexOf("MOVE")>=0)&&(tm.curState().getMovement()<tm.maxState().getMovement()))
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" has more move points!");
					tm.curState().setMovement(tm.maxState().getMovement());
					wishDrain(mob,baseLoss,true);
					return true;
				}
				else
				if(myWish.indexOf("MOVE")>=0)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" has more move points!");
					tm.baseState().setMovement(tm.baseState().getMovement()+5);
					tm.recoverMaxState();
					wishDrain(mob,baseLoss,true);
					return true;
				}

			}
			if((target instanceof MOB)
			&&(((MOB)target).charStats().getStat(CharStats.STAT_GENDER)!='M')
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" TURN INTO ")>=0)
			||(myWish.indexOf(" CHANGE")>=0)
			||(myWish.indexOf(" BE A")>=0)
			||(myWish.indexOf(" WAS A")>=0)
			||(myWish.indexOf(" TRANSFORM")>=0))
			&&((myWish.indexOf(" MALE ")>=0)
			||(myWish.indexOf(" MAN ")>=0)
			||(myWish.indexOf(" BOY ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				((MOB)target).baseCharStats().setStat(CharStats.STAT_GENDER,'M');
				((MOB)target).recoverCharStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is now male!");
				return true;
			}

			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" WAS ")>=0))
			&&((myWish.indexOf(" LIGHTER ")>=0)
			||(myWish.indexOf(" LOSE WEIGHT ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				int weight=((MOB)target).baseEnvStats().weight();
				weight-=50;
				if(weight<=0) weight=1;
				((MOB)target).baseEnvStats().setWeight(weight);
				((MOB)target).recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is now lighter!");
				return true;
			}
			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" WAS ")>=0))
			&&((myWish.indexOf(" HEAVIER ")>=0)
			||(myWish.indexOf(" GAIN WEIGHT ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				int weight=((MOB)target).baseEnvStats().weight();
				weight+=50;
				((MOB)target).baseEnvStats().setWeight(weight);
				((MOB)target).recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is now heavier!");
				return true;
			}
			if((target instanceof MOB)
			&&((myWish.indexOf(" EXP ")>=0)
			||(myWish.indexOf(" EXPERIENCE ")>=0)))
			{
				int x=myWish.indexOf(" EXP");
				String wsh=myWish.substring(0,x).trim();
				x=wsh.lastIndexOf(" ");
				int amount=25;
				if((x>=0)&&(CMath.isNumber(wsh.substring(x).trim())))
				   amount=CMath.s_int(wsh.substring(x).trim());
				if(target!=mob)
				{
					CMLib.combat().postExperience(mob,null,null,-(amount*4),false);
					mob.tell("Your wish has drained you of "+(amount*4)+" experience points.");
				}
				else
				{
					CMLib.combat().postExperience(mob,null,null,-amount,false);
					mob.tell("Your wish has drained you of "+amount+" experience points.");
				}
				CMLib.combat().postExperience((MOB)target,null,null,amount,false);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" gains experience!");
				return true;
			}

			if((target!=null)
			&&((myWish.indexOf(" LOWER ")>=0)||(myWish.indexOf(" LOSE ")>=0)||(myWish.indexOf(" GAIN ")>=0)||(myWish.indexOf(" HIGHER ")>=0)||(myWish.indexOf(" WAS ")>=0)||(myWish.indexOf(" WOULD BE ")>=0)||(myWish.indexOf(" WOULD BECOME ")>=0)||(myWish.indexOf(" BECAME ")>=0))
			&&((myWish.indexOf(" LEVEL ")>=0)||(myWish.indexOf(" LEVELS ")>=0))
			&&(!CMSecurity.isDisabled("LEVELS")))
			{
				int level=0;
				if(myWish.indexOf(" LOWER ")>=0)
					level=-1;
				else
				if(myWish.indexOf(" HIGHER" )>=0)
					level=1;
				else
				if(myWish.indexOf(" GAIN ")>=0)
				{
					level=1;
					Vector V=CMParms.parse(myWish);
					for(int i2=1;i2<V.size();i2++)
					{
						if(((String)V.elementAt(i2)).equalsIgnoreCase("LEVELS"))
						{
							String s=(String)V.elementAt(i2-1);
							if(CMath.isNumber(s)
							&&((CMath.s_int(s)!=0)||(s.equalsIgnoreCase("0"))))
							{
								level=CMath.s_int(s);
								break;
							}
						}
					}
				}
				else
				if(myWish.indexOf(" LOSE" )>=0)
				{
					level=-1;
					Vector V=CMParms.parse(myWish);
					for(int i2=1;i2<V.size();i2++)
					{
						if(((String)V.elementAt(i2)).equalsIgnoreCase("LEVELS"))
						{
							String s=(String)V.elementAt(i2);
							if(CMath.isNumber(s)
							&&((CMath.s_int(s)!=0)||(s.equalsIgnoreCase("0"))))
							{
								level=-CMath.s_int(s);
								break;
							}
						}
					}
				}
				else
				{
					Vector V=CMParms.parse(myWish);
					for(int i2=0;i2<V.size()-1;i2++)
					{
						if(((String)V.elementAt(i2)).equalsIgnoreCase("LEVEL"))
						{
							String s=(String)V.elementAt(i2+1);
							if(CMath.isNumber(s)
							&&((CMath.s_int(s)!=0)||(s.equalsIgnoreCase("0"))))
							{
								level=CMath.s_int(s)-target.baseEnvStats().level();
								break;
							}
						}
					}
				}
				if(level!=0)
				{
					int levelsLost=level;
					if(levelsLost<0) levelsLost=levelsLost*-1;
					int levelsGained=levelsLost;
					levelsLost*=4;
					if(levelsLost>=mob.baseEnvStats().level())
					{
						levelsLost=mob.baseEnvStats().level()-1;
						levelsGained=levelsLost/4;
						if(level>0) level=levelsGained;
						else level=-levelsGained;
					}
					int newLevel=target.baseEnvStats().level()+level;
					if(target instanceof MOB)
					{
					    if(((newLevel>CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL))
					            ||(((MOB)target).charStats().getCurrentClass().leveless())
					            ||(((MOB)target).charStats().getMyRace().leveless()))
					    &&(newLevel>target.baseEnvStats().level()))
						{
							wishDrain(mob,baseLoss,false);
							mob.tell("That's beyond your power, but you lost exp even for trying.");
							return false;
						}
					}

					if(target instanceof MOB)
					{
						MOB MT=(MOB)target;
						if(level>0)
						{
							for(int i2=0;i2<levelsGained;i2++)
							{
								MT.baseCharStats().getCurrentClass().level(MT);
								MT.recoverEnvStats();
							}
						}
						else
						while(MT.baseEnvStats().level()>newLevel)
						{
							MT.baseCharStats().getCurrentClass().unLevel(MT);
							MT.recoverEnvStats();
						}
					}
					else
					{
						target.baseEnvStats().setLevel(newLevel);
						target.recoverEnvStats();
					}
					wishDrain(mob,baseLoss*levelsLost,true);
					if((mob!=target)||(level>0))
					for(int i2=0;i2<levelsLost;i2++)
						mob.baseCharStats().getCurrentClass().unLevel(mob);
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is now level "+target.envStats().level()+"!");
				}
				return true;
			}



			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" WAS ")>=0))
			&&((myWish.indexOf(" SMALL ")>=0)
			||(myWish.indexOf(" SHORT ")>=0)
			||(myWish.indexOf(" LITTLE ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				int weight=((MOB)target).baseEnvStats().height();
				weight-=12;
				if(weight<=0) weight=5;
				((MOB)target).baseEnvStats().setHeight(weight);
				((MOB)target).recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is now shorter!");
				return true;
			}
			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" WAS ")>=0))
			&&((myWish.indexOf(" LARGE ")>=0)
			||(myWish.indexOf(" BIG ")>=0)
			||(myWish.indexOf(" TALL ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				int weight=((MOB)target).baseEnvStats().height();
				weight+=12;
				((MOB)target).baseEnvStats().setHeight(weight);
				((MOB)target).recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is now taller!");
				return true;
			}

			if((target instanceof MOB)
			&&(((MOB)target).charStats().getStat(CharStats.STAT_GENDER)!='F')
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" TURN INTO ")>=0)
			||(myWish.indexOf(" CHANGE")>=0)
			||(myWish.indexOf(" BE A")>=0)
			||(myWish.indexOf(" WAS A")>=0)
			||(myWish.indexOf(" TRANSFORM")>=0))
			&&((myWish.indexOf(" MALE ")>=0)
			||(myWish.indexOf(" WOMAN ")>=0)
			||(myWish.indexOf(" GIRL ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				((MOB)target).baseCharStats().setStat(CharStats.STAT_GENDER,'F');
				((MOB)target).recoverCharStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is now female!");
				return true;
			}

			// change race
			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" TURN INTO ")>=0)
			||(myWish.indexOf(" CHANGE")>=0)
			||(myWish.indexOf(" BE A")>=0)
			||(myWish.indexOf(" WAS A")>=0)
			||(myWish.indexOf(" TRANSFORM")>=0)))
			{
				Race R=CMClass.getRace((String)wishV.lastElement());
				if(R!=null)
				{
					if(!((MOB)target).isMonster())
					{
						baseLoss+=500;
						mob.baseCharStats().getCurrentClass().unLevel(mob);
					}
					wishDrain(mob,baseLoss,true);
					int oldCat=mob.baseCharStats().ageCategory();
					((MOB)target).baseCharStats().setMyRace(R);
					((MOB)target).baseCharStats().getMyRace().startRacing(((MOB)target),true);
					((MOB)target).baseCharStats().getMyRace().setHeightWeight(((MOB)target).baseEnvStats(),(char)((MOB)target).baseCharStats().getStat(CharStats.STAT_GENDER));
					((MOB)target).confirmWearability();
					((MOB)target).recoverCharStats();
					((MOB)target).recoverEnvStats();
					if(!((MOB)target).isMonster())
						((MOB)target).baseCharStats().setStat(CharStats.STAT_AGE,R.getAgingChart()[oldCat]);
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is now a "+R.name()+"!");
					return true;
				}
			}

			// change class
			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" TURN INTO ")>=0)
			||(myWish.indexOf(" CHANGE")>=0)
			||(myWish.indexOf(" LEARN TO BE")>=0)
			||(myWish.indexOf(" BE A")>=0)
			||(myWish.indexOf(" WAS A")>=0)
			||(myWish.indexOf(" TRANSFORM")>=0)))
			{
				CharClass C=CMClass.findCharClass((String)wishV.lastElement());
				if((C!=null)&&(CMath.bset(C.availabilityCode(),Area.THEME_FANTASY)))
				{
					CharClass oldC=mob.baseCharStats().getCurrentClass();
					baseLoss+=1000;
					wishDrain(mob,baseLoss,true);
					mob.baseCharStats().getCurrentClass().unLevel(mob);
					mob.baseCharStats().getCurrentClass().unLevel(mob);
					mob.baseCharStats().getCurrentClass().unLevel(mob);
					StringBuffer str=new StringBuffer("");
					for(int trait=0;trait<CharStats.NUM_BASE_STATS;trait++)
					{
						int newVal=C.maxStatAdjustments()[trait];
						int amountToLose=oldC.maxStatAdjustments()[trait]-newVal;
						if((amountToLose>0)&&(mob.baseCharStats().getStat(trait)>amountToLose))
						{
							mob.baseCharStats().setStat(trait,mob.baseCharStats().getStat(trait)-amountToLose);
							str.append("\n\rYou lost "+amountToLose+" points of "+CharStats.STAT_DESCS[trait].toLowerCase()+".");
						}
					}
					mob.tell(str.toString()+"\n\r");
					((MOB)target).baseCharStats().setCurrentClass(C);
					if((!((MOB)target).isMonster())&&(((MOB)target).soulMate()==null))
						CMLib.coffeeTables().bump(target,CoffeeTableRow.STAT_CLASSCHANGE);
					((MOB)target).baseCharStats().getCurrentClass().startCharacter((MOB)target,false,true);
					((MOB)target).recoverCharStats();
					((MOB)target).recoverEnvStats();
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" is now a "+C.name(((MOB)target).baseCharStats().getCurrentClassLevel())+"!");
					return true;
				}
			}

			// gaining new abilities!
			if(target instanceof MOB)
			{
				int code=-1;
				int x=myWish.indexOf(" KNOW "); if((x>=0)&&(x+5>code)) code=x+5;
				x=myWish.indexOf(" KNEW "); if((x>=0)&&(x+5>code)) code=x+5;
				x=myWish.indexOf(" LEARN "); if((x>=0)&&(x+6>code)) code=x+6;
				x=myWish.indexOf(" COULD "); if((x>=0)&&(x+6>code)) code=x+6;
				x=myWish.indexOf(" GAIN "); if((x>=0)&&(x+5>code)) code=x+5;
				x=myWish.indexOf(" BE TAUGHT "); if((x>=0)&&(x+10>code)) code=x+10;
				x=myWish.indexOf(" HOW TO "); if((x>=0)&&(x+7>code)) code=x+7;
				x=myWish.indexOf(" ABLE TO "); if((x>=0)&&(x+8>code)) code=x+8;
				x=myWish.indexOf(" CAST "); if((x>=0)&&(x+5>code)) code=x+5;
				x=myWish.indexOf(" SING "); if((x>=0)&&(x+5>code)) code=x+5;
				x=myWish.indexOf(" PRAY FOR "); if((x>=0)&&(x+9>code)) code=x+9;
				if((code>=0)&&(code<myWish.length()))
				{
					MOB tm=(MOB)target;
					Ability A=CMClass.findAbility(myWish.substring(code).trim());
					if((A!=null)
					&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())>0))
					{
						if(CMLib.ableMapper().lowestQualifyingLevel(A.ID())>=25)
						{
							CMLib.combat().postExperience(mob,null,null,-baseLoss,false);
							mob.tell("Your wish has drained you of "+baseLoss+" experience points, but that is beyond your wishing ability.");
							return false;
						}
						if(tm.fetchAbility(A.ID())!=null)
						{
							A=tm.fetchAbility(A.ID());
							CMLib.combat().postExperience(mob,null,null,-baseLoss,false);
							mob.tell("Your wish has drained you of "+baseLoss+" experience points.");
						}
						else
						{
							tm.addAbility(A);
							baseLoss+=500;
							wishDrain(mob,baseLoss,true);
							mob.baseCharStats().getCurrentClass().unLevel(mob);
							mob.baseCharStats().getCurrentClass().unLevel(mob);
						}
						A=tm.fetchAbility(A.ID());
						A.setProfficiency(100);
						A.autoInvocation(tm);
						mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,target.name()+" now knows "+A.name()+"!");
						return true;
					}
				}
			}

			// attributes will be hairy
			int foundAttribute=-1;
			for(int attributes=0;attributes<CharStats.STAT_DESCS.length;attributes++)
			{
				if(CMLib.english().containsString(myWish,CharStats.STAT_DESCS[attributes]))
				{	foundAttribute=attributes; break;}
			}
			if(myWish.indexOf("STRONG")>=0)
				foundAttribute=CharStats.STAT_STRENGTH;
			if(myWish.indexOf(" INTELLIGEN")>=0)
				foundAttribute=CharStats.STAT_INTELLIGENCE;
			if(myWish.indexOf(" SMART")>=0)
				foundAttribute=CharStats.STAT_INTELLIGENCE;
			if(myWish.indexOf(" WISE")>=0)
				foundAttribute=CharStats.STAT_WISDOM;
			if(myWish.indexOf(" FAST")>=0)
				foundAttribute=CharStats.STAT_DEXTERITY;
			if(myWish.indexOf(" DEXTROUS")>=0)
				foundAttribute=CharStats.STAT_DEXTERITY;
			if(myWish.indexOf(" HEALTH")>=0)
				foundAttribute=CharStats.STAT_CONSTITUTION;
			if(myWish.indexOf(" PRETTY")>=0)
				foundAttribute=CharStats.STAT_CHARISMA;
			if(myWish.indexOf(" NICE")>=0)
				foundAttribute=CharStats.STAT_CHARISMA;
			if(myWish.indexOf(" PRETTIER")>=0)
				foundAttribute=CharStats.STAT_CHARISMA;
			if((myWish.indexOf("RESIST")>=0)
			||(myWish.indexOf("IMMUN")>=0))
			{
				if(myWish.indexOf(" PARALY")>=0)
					foundAttribute=CharStats.STAT_SAVE_PARALYSIS;
				if(myWish.indexOf(" FIRE")>=0)
					foundAttribute=CharStats.STAT_SAVE_FIRE;
				if(myWish.indexOf(" FLAMES")>=0)
					foundAttribute=CharStats.STAT_SAVE_FIRE;
				if(myWish.indexOf(" COLD")>=0)
					foundAttribute=CharStats.STAT_SAVE_COLD;
				if(myWish.indexOf(" FROST")>=0)
					foundAttribute=CharStats.STAT_SAVE_COLD;
				if(myWish.indexOf(" GAS")>=0)
					foundAttribute=CharStats.STAT_SAVE_GAS;
				if(myWish.indexOf(" ACID")>=0)
					foundAttribute=CharStats.STAT_SAVE_ACID;
				if(myWish.indexOf(" SPELL ")>=0)
					foundAttribute=CharStats.STAT_SAVE_MAGIC;
				if(myWish.indexOf(" TRAPS ")>=0)
					foundAttribute=CharStats.STAT_SAVE_TRAPS;
				if(myWish.indexOf(" SPELLS ")>=0)
					foundAttribute=CharStats.STAT_SAVE_MAGIC;
				if(myWish.indexOf(" SONGS")>=0)
					foundAttribute=CharStats.STAT_SAVE_MIND;
				if(myWish.indexOf(" CHARMS")>=0)
					foundAttribute=CharStats.STAT_SAVE_MIND;
				if(myWish.indexOf(" ELECTRI")>=0)
					foundAttribute=CharStats.STAT_SAVE_ELECTRIC;
				if(myWish.indexOf(" POISON")>=0)
					foundAttribute=CharStats.STAT_SAVE_POISON;
				if(myWish.indexOf(" DEATH")>=0)
					foundAttribute=CharStats.STAT_SAVE_UNDEAD;
				if(myWish.indexOf(" DISEASE")>=0)
					foundAttribute=CharStats.STAT_SAVE_DISEASE;
				if(myWish.indexOf(" PLAGUE")>=0)
					foundAttribute=CharStats.STAT_SAVE_DISEASE;
				if(myWish.indexOf(" COLDS ")>=0)
					foundAttribute=CharStats.STAT_SAVE_DISEASE;
				if(myWish.indexOf(" SICK")>=0)
					foundAttribute=CharStats.STAT_SAVE_DISEASE;
				if(myWish.indexOf(" UNDEAD")>=0)
					foundAttribute=CharStats.STAT_SAVE_UNDEAD;
				if(myWish.indexOf(" EVIL")>=0)
					foundAttribute=CharStats.STAT_SAVE_UNDEAD;
			}
			if((foundAttribute>=0)
			&&(target instanceof MOB)
			&&((myWish.indexOf(" LESS ")>=0)
			||(myWish.indexOf(" LOWER ")>=0)
			||(myWish.indexOf(" LESS RESIST")>=0)
			||(myWish.indexOf(" LESS IMMUN")>=0)
			||(myWish.indexOf(" NO RESIST")>=0)
			||(myWish.indexOf(" NO IMMUN")>=0)
			||(myWish.indexOf(" LOSE ")>=0)))
			{
				switch(foundAttribute)
				{
				case CharStats.STAT_CHARISMA:
				case CharStats.STAT_CONSTITUTION:
				case CharStats.STAT_DEXTERITY:
				case CharStats.STAT_INTELLIGENCE:
				case CharStats.STAT_STRENGTH:
				case CharStats.STAT_WISDOM:
					baseLoss-=1000;
					break;
				default:
					baseLoss-=10;
					break;
				}
				wishDrain(mob,baseLoss,true);
				if(foundAttribute<=6)
					((MOB)target).baseCharStats().setStat(foundAttribute,((MOB)target).baseCharStats().getStat(foundAttribute)-1);
				else
					((MOB)target).baseCharStats().setStat(foundAttribute,((MOB)target).baseCharStats().getStat(foundAttribute)-33);
				((MOB)target).recoverCharStats();
				mob.recoverCharStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,target.name()+" has lost "+CharStats.STAT_DESCS[foundAttribute].toLowerCase()+".");
				return true;
			}

			if((foundAttribute>=0)
			&&(target instanceof MOB)
			&&((myWish.indexOf(" MORE ")>=0)
			||(myWish.indexOf(" HIGHER ")>=0)
			||(myWish.indexOf("RESIST")>=0)
			||(myWish.indexOf("IMMUN")>=0)
			||(myWish.indexOf(" BIGGER ")>=0)
			||(myWish.indexOf(" TO HAVE ")>=0)
			||(myWish.indexOf(" GAIN ")>=0)
			||(myWish.indexOf(" WAS ")>=0)
			||(myWish.indexOf(" TO BE ")>=0)))
			{
				switch(foundAttribute)
				{
				case CharStats.STAT_CHARISMA:
				case CharStats.STAT_CONSTITUTION:
				case CharStats.STAT_DEXTERITY:
				case CharStats.STAT_INTELLIGENCE:
				case CharStats.STAT_STRENGTH:
				case CharStats.STAT_WISDOM:
					baseLoss+=500;
					break;
				default:
					baseLoss+=10;
					break;
				}
				wishDrain(mob,baseLoss,true);
				if(!CMSecurity.isDisabled("LEVELS"))
					mob.baseCharStats().getCurrentClass().unLevel(mob);
				if(foundAttribute<=6)
					((MOB)target).baseCharStats().setStat(foundAttribute,((MOB)target).baseCharStats().getStat(foundAttribute)+1);
				else
					((MOB)target).baseCharStats().setStat(foundAttribute,((MOB)target).baseCharStats().getStat(foundAttribute)+33);
				mob.recoverCharStats();
				((MOB)target).recoverCharStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,target.name()+" has gained "+CharStats.STAT_DESCS[foundAttribute].toLowerCase()+".");
				return true;
			}

			CMLib.combat().postExperience(mob,null,null,-baseLoss,false);
			Log.sysOut("Wish",mob.Name()+" unsuccessfully wished for '"+CMParms.combine(commands,0)+"'");
			mob.tell("Your attempted wish has cost you "+baseLoss+" experience points, but it did not come true.  You might try rewording your wish next time.");
			return false;
		}
		return success;
	}
}
