package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Tattoo;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;


/*
   Copyright 2000-2011 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, e\ither express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class StdMOB implements MOB
{
	public String ID(){return "StdMOB";}
	
	public String 		username="";

    protected String 	clanID=null;
    protected WeakReference<Clan>
    					cachedClan=null;
    protected int 		clanRole=0;

	protected CharStats baseCharStats=(CharStats)CMClass.getCommon("DefaultCharStats");
	protected CharStats charStats=(CharStats)CMClass.getCommon("DefaultCharStats");

	protected PhyStats 	phyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected PhyStats 	basePhyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");

	protected PlayerStats playerStats=null;

	protected boolean 	amDead=false;
	protected Room 		location=null;
	protected Room 		lastLocation=null;
	protected Rideable 	riding=null;

	protected Session 	mySession=null;
	protected boolean 	pleaseDestroy=false;
	protected byte[] 	description=null;
	protected String 	displayText="";
	protected String 	rawImageName=null;
	protected String 	cachedImageName=null;
	protected byte[] 	miscText=null;
    protected String[] 	xtraValues=null;
    protected int		possibleWeaponIndex=-1;

	protected long 		tickStatus=Tickable.STATUS_NOT;

	/* containers of items and attributes*/
	protected SVector<Item>		inventory	= new SVector<Item>(1);
	protected SVector<Ability>	abilitees	= new SVector<Ability>(1);
	protected SVector<Ability> 	affects		= new SVector<Ability>(1);
	protected SVector<Behavior>	behaviors	= new SVector<Behavior>(1);
	protected SVector<Tattoo>	tattoos		= new SVector<Tattoo>(1);
	protected SVector<String>	expertises	= new SVector<String>(1);
	protected SVector<Follower> followers	= null;
	protected LinkedList<QMCommand> 
								commandQue	= new LinkedList<QMCommand>();
	protected SVector<ScriptingEngine>
								scripts		= new SVector(1);
    protected SHashtable<String,Faction.FactionData> 
    							factions	= new SHashtable<String,Faction.FactionData>(1);

	// gained attributes
	protected int 		experience=0;
	protected int 		practices=0;
	protected int 		trains=0;
	protected long 		ageHours=0;
	protected int 		money=0;
	protected double 	moneyVariation=0.0;
	protected int 		attributesBitmap=MOB.ATT_NOTEACH;
	protected String 	databaseID="";

    protected int 		tickCounter=0;
    protected int 		recoverTickCounter=1;
    private long 		expirationDate=0;
    private int 		manaConsumeCounter=CMLib.dice().roll(1,10,0);
    private double 		freeActions=0.0;

    // the core state values
    public CharState 	curState=(CharState)CMClass.getCommon("DefaultCharState");
    public CharState 	maxState=(CharState)CMClass.getCommon("DefaultCharState");
    public CharState 	baseState=(CharState)CMClass.getCommon("DefaultCharState");
    private long 		lastTickedDateTime=0;
    private long 		lastCommandTime=System.currentTimeMillis();

    protected Room 		startRoomPossibly=null;
    protected String 	worshipCharID="";
    protected String 	liegeID="";
    protected int 		wimpHitPoint=0;
    protected int 		questPoint=0;
    protected MOB 		victim=null;
    protected MOB 		amFollowing=null;
    protected MOB 		soulMate=null;
    protected int 		atRange=-1;
    protected long 		peaceTime=0;
    protected boolean 	amDestroyed=false;
    protected boolean 	kickFlag=false;
    protected boolean 	imMobile=false;

    public long lastTickedDateTime(){return lastTickedDateTime;}
    public void flagVariableEq(){lastTickedDateTime=-3;}
    public long getAgeHours(){return ageHours;}
	public int getPractices(){return practices;}
	public int getExperience(){return experience;}
	public int getExpNextLevel(){return CMLib.leveler().getLevelExperience(basePhyStats().level());}
	public int getExpPrevLevel()
	{
		if(basePhyStats().level()<=1) return 0;
		int neededLowest=CMLib.leveler().getLevelExperience(basePhyStats().level()-2);
		return neededLowest;
	}
	public int getExpNeededDelevel()
	{
		if(basePhyStats().level()<=1) return 0;
		if((CMSecurity.isDisabled("EXPERIENCE"))
		||(charStats().getCurrentClass().expless())
		||(charStats().getMyRace().expless()))
		    return 0;
		int ExpPrevLevel=getExpPrevLevel();
		if(ExpPrevLevel>getExperience())
			ExpPrevLevel=getExperience()-1000;
		return getExperience()-ExpPrevLevel;
	}
	public int getExpNeededLevel()
	{
		if((CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)>0)
		&&(CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)<=basePhyStats().level()))
			return Integer.MAX_VALUE;
		if((CMSecurity.isDisabled("EXPERIENCE"))
		||(charStats().getCurrentClass().expless())
		||(charStats().getMyRace().expless()))
		    return Integer.MAX_VALUE;
		int ExpNextLevel=getExpNextLevel();
		if(ExpNextLevel<getExperience())
			ExpNextLevel=getExperience()+1000;
		return ExpNextLevel-getExperience();
	}
	public int getTrains(){return trains;}
	public int getMoney(){return money;}
    public double getMoneyVariation() { return moneyVariation;}
	public int getBitmap(){return attributesBitmap;}
	public void setAgeHours(long newVal){ ageHours=newVal;}
	public void setExperience(int newVal){ experience=newVal; }
	public void setExpNextLevel(int newVal){}
	public void setPractices(int newVal){ practices=newVal;}
	public void setTrains(int newVal){ trains=newVal;}
	public void setMoney(int newVal){ money=newVal;}
    public void setMoneyVariation(double newVal){moneyVariation=newVal;}
	public void setBitmap(int newVal){ attributesBitmap=newVal;}
    public String getFactionListing()
    {
        StringBuffer msg=new StringBuffer();
        for(Enumeration e=fetchFactions();e.hasMoreElements();)
        {
            Faction F=CMLib.factions().getFaction((String)e.nextElement());
            msg.append(F.name()+"("+fetchFaction(F.factionID())+");");
        }
        return msg.toString();
    }

	public String getLiegeID(){return liegeID;}
	public String getWorshipCharID(){return worshipCharID;}
	public int getWimpHitPoint(){return wimpHitPoint;}
	public int getQuestPoint(){return questPoint;}
	public void setLiegeID(String newVal){liegeID=newVal;}
	public void setWorshipCharID(String newVal){ worshipCharID=newVal;}
	public void setWimpHitPoint(int newVal){ wimpHitPoint=newVal;}
	public void setQuestPoint(int newVal){ questPoint=newVal;}
	public Deity getMyDeity()
	{
		if(getWorshipCharID().length()==0) return null;
		Deity bob=CMLib.map().getDeity(getWorshipCharID());
		if(bob==null)
			setWorshipCharID("");
		return bob;
	}
	public final Clan getMyClan() { return (cachedClan!=null)?cachedClan.get():null;}

    public void initializeClass(){}
	public CMObject newInstance()
	{
		try
        {
			return (Environmental)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdMOB();
	}

	public Room getStartRoom(){
		return CMLib.map().getRoom(startRoomPossibly);
	}
	public void setStartRoom(Room room){
		startRoomPossibly=room;
	}

	public long peaceTime(){return peaceTime;}

	public void setDatabaseID(String id){databaseID=id;}
	public boolean canSaveDatabaseID(){ return true;}
	public String databaseID(){return databaseID;}

	public String Name()
	{
		return username;
	}
	public void setName(String newName){
		username=newName;
	}
	public String name()
	{
		if(phyStats().newName()!=null) return phyStats().newName();
		return username;
	}
	public String titledName()
	{
	    if((playerStats==null)||(playerStats.getTitles().size()==0))
	        return name();
	    return CMStrings.replaceAll(playerStats.getActiveTitle(),"*",Name());
	}

    public String genericName()
    {
        if(charStats().getStat(CharStats.STAT_AGE)>0)
            return charStats().ageName().toLowerCase()+" "+charStats().raceName().toLowerCase();
        return charStats().raceName().toLowerCase();
    }

	public String image()
    {
        if(cachedImageName==null)
        	cachedImageName=CMProps.getDefaultMXPImage(this);
        if(!baseCharStats().getMyRace().name().equalsIgnoreCase(charStats().raceName()))
            return CMProps.getDefaultMXPImage(this);
        if(cachedImageName==null) return "";
        return cachedImageName;
    }
    public String rawImage()
    {
        if(rawImageName==null)
            return "";
        return rawImageName;
    }
	public void setImage(String newImage)
    {
        if((newImage==null)||(newImage.trim().length()==0))
        	rawImageName=null;
        else
        	rawImageName=newImage;
        cachedImageName=null;
    }

	public StdMOB()
	{
        super();
        CMClass.bumpCounter(this,CMClass.OBJECT_MOB);
		baseCharStats().setMyRace(CMClass.getRace("Human"));
		basePhyStats().setLevel(1);
        xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
	public long expirationDate(){return expirationDate;}
	public void setExpirationDate(long time){expirationDate=time;}
    protected void finalize()
    {
        CMClass.unbumpCounter(this,CMClass.OBJECT_MOB);
    }
    public boolean amDestroyed(){return amDestroyed;}
	protected void cloneFix(MOB M)
	{
		if(M==null) return;
		affects=new SVector(1);
		basePhyStats=(PhyStats)M.basePhyStats().copyOf();
		phyStats=(PhyStats)M.phyStats().copyOf();
		baseCharStats=(CharStats)M.baseCharStats().copyOf();
		charStats=(CharStats)M.charStats().copyOf();
		baseState=(CharState)M.baseState().copyOf();
		curState=(CharState)M.curState().copyOf();
		maxState=(CharState)M.maxState().copyOf();

		pleaseDestroy=false;

		inventory=new SVector<Item>(1);
		followers=null;
		abilitees=new SVector<Ability>(1);
		affects=new SVector<Ability>(1);
		behaviors=new SVector<Behavior>(1);
		scripts=new SVector<ScriptingEngine>(1);
		Item I=null;
		for(int i=0;i<M.numItems();i++)
		{
			I=M.getItem(i);
			if(I!=null)
				addItem((Item)I.copyOf());
		}
		Item I2=null;
		for(int i=0;i<numItems();i++)
		{
			I=getItem(i);
			if((I!=null)
			&&(I.container()!=null)
			&&(!isMine(I.container())))
				for(Enumeration<Item> e=M.items();e.hasMoreElements();)
				{
					I2=(Item)e.nextElement();
					if((I2==I.container())&&(I2 instanceof Container))
					{ I.setContainer((Container)I2); break;}
				}
		}
		Ability A=null;
		for(int i=0;i<M.numLearnedAbilities();i++)
		{
			A=M.fetchAbility(i);
			if(A!=null)
				addAbility((Ability)A.copyOf());
		}
		for(final Enumeration<Ability> a=M.personalEffects();a.hasMoreElements();)
		{
			A=a.nextElement();
			if((A!=null)&&(!A.canBeUninvoked()))
				addEffect((Ability)A.copyOf());
		}
		for(final Enumeration<Behavior> e=M.behaviors();e.hasMoreElements();)
		{
			Behavior B=e.nextElement();
			if(B!=null) // iteration during a clone would just be messed up.
				behaviors.addElement((Behavior)B.copyOf()); 
		}
		ScriptingEngine SE=null;
		for(final Enumeration<ScriptingEngine> e=M.scripts();e.hasMoreElements();)
		{
			SE=e.nextElement();
            if(SE!=null)
                addScript((ScriptingEngine)SE.copyOf());
        }
	}

	public CMObject copyOf()
	{
		try
		{
			StdMOB E=(StdMOB)this.clone();
            CMClass.bumpCounter(E,CMClass.OBJECT_MOB);
            E.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			E.cloneFix(this);
			CMLib.catalog().newInstance(this);
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public boolean isGeneric(){return false;}
	public PhyStats phyStats()
	{
		return phyStats;
	}

	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		if(location()!=null)
			location().affectPhyStats(this,phyStats);
		phyStats().setWeight(phyStats().weight()+(int)Math.round(CMath.div(getMoney(),100.0)));
		if(riding()!=null) riding().affectPhyStats(this,phyStats);
		if(getMyDeity()!=null) getMyDeity().affectPhyStats(this,phyStats);
        int num=0;
		if(charStats!=null)
		{
            num=charStats().numClasses();
			for(int c=0;c<num;c++)
				charStats().getMyClass(c).affectPhyStats(this,phyStats);
			charStats().getMyRace().affectPhyStats(this,phyStats);
		}
        Item item=null;
		num=numItems();
		for(int i=0;i<num;i++)
		{
			item=getItem(i);
			if(item!=null)
			{
				item.recoverPhyStats();
				item.affectPhyStats(this,phyStats);
			}
		}
        Ability effect=null;
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			effect=a.nextElement();
			if(effect!=null)
				effect.affectPhyStats(this,phyStats);
		}
		for(Enumeration e=factions.elements();e.hasMoreElements();)
			((Faction.FactionData)e.nextElement()).affectPhyStats(this,phyStats);
		/* the follower light exception*/
		if(!CMLib.flags().isLightSource(this))
        {
            num=numFollowers();
    		for(int f=0;f<num;f++)
    			if(CMLib.flags().isLightSource(fetchFollower(f)))
    				phyStats.setDisposition(phyStats().disposition()|PhyStats.IS_LIGHTSOURCE);
        }
	}
	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}

	public int baseWeight()
	{
		if(charStats().getMyRace()==baseCharStats().getMyRace())
			return basePhyStats().weight() 
				 + charStats().getStat(CharStats.STAT_WEIGHTADJ);
		return charStats().getMyRace().lightestWeight()
		        + charStats().getStat(CharStats.STAT_WEIGHTADJ)
				+ charStats().getMyRace().weightVariance();
	}

	public int maxCarry()
	{
        if(CMSecurity.isAllowed(this,location(),"CARRYALL"))
            return Integer.MAX_VALUE/2;
		double str=(double)charStats().getStat(CharStats.STAT_STRENGTH);
		double bodyWeight=(double)baseWeight();
		return (int)Math.round(bodyWeight + ((str+10.0)*str*bodyWeight/150.0) + (str*5.0));
	}
    public int maxItems()
    {
        if(CMSecurity.isAllowed(this,location(),"CARRYALL"))
            return Integer.MAX_VALUE/2;
        return (2*Wearable.CODES.TOTAL())
                +(2*charStats().getStat(CharStats.STAT_DEXTERITY))
                +(2*phyStats().level());
    }
	public int maxFollowers()
	{
		return ((int)Math.round(CMath.div(charStats().getStat(CharStats.STAT_CHARISMA)-8,4.0))+1);
	}
	public int totalFollowers()
	{
		int total=numFollowers();
		try{
			for(int i=0;i<total;i++)
				total+=fetchFollower(i).totalFollowers();
		}catch(Throwable t){}
		return total;
	}

	public CharStats baseCharStats(){return baseCharStats;}
	public CharStats charStats(){return charStats;}
	public void recoverCharStats()
	{
		baseCharStats.setClassLevel(baseCharStats.getCurrentClass(),basePhyStats().level()-baseCharStats().combinedSubLevels());
		baseCharStats().copyInto(charStats);

		if(riding()!=null) riding().affectCharStats(this,charStats);
		if(getMyDeity()!=null) getMyDeity().affectCharStats(this,charStats);
        Ability effect=null;
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			effect=a.nextElement();
			if(effect!=null)
				effect.affectCharStats(this,charStats);
		}
        Item item=null;
        int num=numItems();
		for(int i=0;i<num;i++)
		{
			item=getItem(i);
			if(item!=null)
				item.affectCharStats(this,charStats);
		}
		if(location()!=null)
			location().affectCharStats(this,charStats);

        num=charStats.numClasses();
		for(int c=0;c<num;c++)
			charStats.getMyClass(c).affectCharStats(this,charStats);
		charStats.getMyRace().affectCharStats(this,charStats);
		baseCharStats.getMyRace().agingAffects(this,baseCharStats,charStats);
		for(Enumeration e=factions.elements();e.hasMoreElements();)
			((Faction.FactionData)e.nextElement()).affectCharStats(this,charStats);
	    if((playerStats!=null)&&(soulMate==null)&&(playerStats.getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
	    {
	        int chaAdjust=(int)(playerStats.getHygiene()/PlayerStats.HYGIENE_DELIMIT);
	        if((charStats.getStat(CharStats.STAT_CHARISMA)/2)>chaAdjust)
		        charStats.setStat(CharStats.STAT_CHARISMA,charStats.getStat(CharStats.STAT_CHARISMA)-chaAdjust);
	        else
		        charStats.setStat(CharStats.STAT_CHARISMA,charStats.getStat(CharStats.STAT_CHARISMA)/2);
	    }
	}

	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=(CharStats)newBaseCharStats.copyOf();
	}
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(affected instanceof Room)
		{
			if(CMLib.flags().isLightSource(this))
			{
				if(CMLib.flags().isInDark(affected))
					affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_DARK);
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_LIGHTSOURCE);
			}
		}
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}
	public boolean isMarriedToLiege()
	{
		if(getLiegeID().length()==0) return false;
		if(getLiegeID().equals(Name())) return false;
		MOB M=CMLib.players().getLoadPlayer(getLiegeID());
		if(M==null){ setLiegeID(""); return false;}
		if(M.getLiegeID().equals(Name()))
			return true;
		return false;
	}
	public CharState curState(){return curState;}
	public CharState maxState(){return maxState;}
	public CharState baseState(){return baseState;}
	public PlayerStats playerStats()
	{
		if((playerStats==null)&&(soulMate!=null))
			return soulMate.playerStats();
		return playerStats;
	}
	public void setPlayerStats(PlayerStats newStats)
    {
        playerStats=newStats;
    }
	public void setBaseState(CharState newState)
	{
		baseState=(CharState)newState.copyOf();
		maxState=(CharState)newState.copyOf();
	}
	public void resetToMaxState()
	{
		recoverMaxState();
		maxState.copyInto(curState);
	}
	public void recoverMaxState()
	{
		baseState.copyInto(maxState);
		if(charStats.getMyRace()!=null)	charStats.getMyRace().affectCharState(this,maxState);
		if(riding()!=null) riding().affectCharState(this,maxState);
        int num=charStats.numClasses();
        for(int c=0;c<num;c++)
            charStats.getMyClass(c).affectCharState(this,maxState);
        Ability effect=null;
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			effect=a.nextElement();
			if(effect!=null)
				effect.affectCharState(this,maxState);
		}
        Item item=null;
        num=numItems();
		for(int i=0;i<num;i++)
		{
			item=getItem(i);
			if(item!=null)
				item.affectCharState(this,maxState);
		}
		for(Enumeration e=factions.elements();e.hasMoreElements();)
			((Faction.FactionData)e.nextElement()).affectCharState(this,maxState);
		if(location()!=null)
			location().affectCharState(this,maxState);
	}

	public boolean amDead()
	{
		return amDead||pleaseDestroy;
	}
	public boolean amActive()
	{
		return !pleaseDestroy;
	}

    public void dispossess(boolean giveMsg)
    {
        MOB mate=soulMate();
        if(mate==null) return;
        if(mate.soulMate()!=null)
            mate.dispossess(giveMsg);
        Session s=session();
        if(s!=null)
        {
            s.setMob(mate);
            mate.setSession(s);
            setSession(null);
            if(giveMsg)
            {
                mate.tell("^HYour spirit has returned to your body...\n\r\n\r^N");
                CMLib.commands().postLook(mate,true);
            }
            setSoulMate(null);
        }
    }

	public void destroy()
	{
        CMLib.map().registerWorldObjectDestroyed(null,getStartRoom(),this);
        try { CMLib.catalog().changeCatalogUsage(this,false);} catch(Throwable t){}
        if((CMSecurity.isDebugging("MISSINGKIDS"))&&(fetchEffect("Age")!=null)&&CMath.isInteger(fetchEffect("Age").text())&&(CMath.s_long(fetchEffect("Age").text())>Short.MAX_VALUE))
            Log.debugOut("MISSKIDS",new Exception(Name()+" went missing form "+CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(this))));
        if(soulMate()!=null) dispossess(false);
        MOB possessor=CMLib.utensils().getMobPossessingAnother(this);
        if(possessor!=null) possessor.dispossess(false);
        if(session()!=null){ session().kill(false,false,false); try{Thread.sleep(1000);}catch(Exception e){}}
		removeFromGame(session()!=null,true);
		while(numBehaviors()>0)
			delBehavior(fetchBehavior(0));
		while(numEffects()>0)
			delEffect(fetchEffect(0));
		while(numLearnedAbilities()>0)
			delAbility(fetchAbility(0));
		while(numItems()>0)
		{
			Item I=getItem(0);
            if(I!=null)
            {
                I.setOwner(this);
    			I.destroy();
                delItem(I);
            }
		}
        if(kickFlag)
        	CMLib.threads().deleteTick(this,-1);
        kickFlag=false;
        clanID=null;
        cachedClan=null;
        charStats=baseCharStats;
        phyStats=basePhyStats;
        playerStats=null;
        location=null;
        lastLocation=null;
        riding=null;
        mySession=null;
        rawImageName=null;
        cachedImageName=null;
        inventory=new SVector<Item>(1);
        followers=null;
        abilitees=new SVector<Ability>(1);
        affects=new SVector<Ability>(1);
        behaviors=new SVector<Behavior>(1);
        tattoos=new SVector<Tattoo>(1);
        expertises=new SVector<String>(1);
        factions=new SHashtable<String, Faction.FactionData>(1);
        commandQue=new LinkedList<QMCommand>();
        scripts=new SVector<ScriptingEngine>(1);
        curState=maxState;
        worshipCharID="";
        liegeID="";
        victim=null;
        amFollowing=null;
        soulMate=null;
        startRoomPossibly=null;
        amDestroyed=true;
	}

	public void removeFromGame(boolean preserveFollowers, boolean killSession)
	{
		pleaseDestroy=true;
		if((location!=null)&&(location.isInhabitant(this)))
        {
            location().delInhabitant(this);
            if((session()!=null)&&(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN)))
                location().show(this,null,CMMsg.MSG_OK_ACTION,"<S-NAME> vanish(es) in a puff of smoke.");
        }
		setFollowing(null);
		DVector oldFollowers=new DVector(2);
		while(numFollowers()>0)
		{
			MOB follower=fetchFollower(0);
			if(follower!=null)
			{
				if((follower.isMonster())&&(!follower.isPossessing()))
					oldFollowers.addElement(follower,Integer.valueOf(fetchFollowerOrder(follower)));
				follower.setFollowing(null);
				delFollower(follower);
			}
		}

		if(preserveFollowers)
		{
			for(int f=0;f<oldFollowers.size();f++)
			{
				MOB follower=(MOB)oldFollowers.elementAt(f,1);
				if(follower.location()!=null)
				{
					MOB newFol=(MOB)follower.copyOf();
					newFol.basePhyStats().setRejuv(0);
					newFol.text();
					follower.killMeDead(false);
					addFollower(newFol, ((Integer)oldFollowers.elementAt(f,2)).intValue());
				}
			}
            if(killSession&&(session()!=null))
    			session().kill(false,false,false);
		}
		setRiding(null);
	}

	public String getClanID(){return ((clanID==null)?"":clanID);}
	public void setClanID(String clan)
	{
		clanID=clan;
		if((clan==null)||(clan.trim().length()==0))
			cachedClan=null;
		else
			cachedClan=new WeakReference(CMLib.clans().getClan(clan));
	}
	public int getClanRole(){return clanRole;}
	public void setClanRole(int role){clanRole=role;}

	public void bringToLife()
	{
		amDead=false;
		pleaseDestroy=false;

		// will ensure no duplicate ticks, this obj, this id
		kickFlag=true;
		CMLib.threads().startTickDown(this,Tickable.TICKID_MOB,1);
		if(tickStatus==Tickable.STATUS_NOT)
		{
			try{ imMobile=true;
				 tick(this,Tickable.TICKID_MOB); // slap on the butt
			}finally{ imMobile=false;}
		}
	}

	public void bringToLife(Room newLocation, boolean resetStats)
	{
		amDead=false;
		if((miscText!=null)&&(resetStats)&&(isGeneric()))
		{
			if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBCOMPRESS))
				CMLib.coffeeMaker().resetGenMOB(this,CMLib.coffeeMaker().getGenMOBTextUnpacked(this,CMLib.encoder().decompressString(miscText)));
			else
				CMLib.coffeeMaker().resetGenMOB(this,CMLib.coffeeMaker().getGenMOBTextUnpacked(this,CMStrings.bytesToStr(miscText)));
		}
		if(CMLib.map().getStartRoom(this)==null)
			setStartRoom(isMonster()?newLocation:CMLib.login().getDefaultStartRoom(this));
		setLocation(newLocation);
		if(location()==null)
		{
			setLocation(CMLib.map().getStartRoom(this));
			if(location()==null)
			{
				Log.errOut("StdMOB",username+" cannot get a location.");
				return;
			}
		}
		if(!location().isInhabitant(this))
			location().addInhabitant(this);
		pleaseDestroy=false;

		// will ensure no duplicate ticks, this obj, this id
		kickFlag=true;
		CMLib.threads().startTickDown(this,Tickable.TICKID_MOB,1);

        Ability A=null;
		for(int a=0;a<numLearnedAbilities();a++)
		{
			A=fetchAbility(a);
			if(A!=null) A.autoInvocation(this);
		}
        if(location()==null)
        {
        	Log.errOut("StdMOB",name()+" of "+CMLib.map().getExtendedRoomID(newLocation)+" was auto-destroyed!");
            destroy();
            return;
        }
    	CMLib.factions().updatePlayerFactions(this,location());
		if(tickStatus==Tickable.STATUS_NOT)
		{
			try { 
				imMobile=true;
				tick(this,Tickable.TICKID_MOB); // slap on the butt
			} catch(Throwable t) {
				t.printStackTrace();
			} finally { imMobile=false; }
		}
        if(location()==null)
        {
        	Log.errOut("StdMOB",name()+" of "+CMLib.map().getExtendedRoomID(newLocation)+" was auto-destroyed by its tick!!");
            destroy();
            return;
        }

		location().recoverRoomStats();
		if((!isGeneric())&&(resetStats))
			resetToMaxState();

        if(location()==null)
        {
        	Log.errOut("StdMOB",name()+" of "+CMLib.map().getExtendedRoomID(newLocation)+" was auto-destroyed by its room recover!!");
            destroy();
            return;
        }

        if(isMonster())
        {
            Item dropItem=CMLib.catalog().getDropItem(this,true);
            if(dropItem!=null)
                addItem(dropItem);
        }

        CMLib.map().registerWorldObjectLoaded(null,getStartRoom(),this);
        location().showOthers(this,null,CMMsg.MSG_BRINGTOLIFE,null);
		if(CMLib.flags().isSleeping(this))
			tell("(You are asleep)");
		else
			CMLib.commands().postLook(this,true);
		inventory.trimToSize();
		abilitees.trimToSize();
		affects.trimToSize();
		behaviors.trimToSize();
	}

	public boolean isInCombat()
	{
		if(victim==null) return false;
        try{
    		Room vicR=victim.location();
    		if((vicR==null)
    		||(location()==null)
    		||(vicR!=location())
    		||(victim.amDead()))
    		{
    			if((victim instanceof StdMOB)
    			&&(((StdMOB)victim).victim==this))
                    victim.setVictim(null);
    			setVictim(null);
    			return false;
    		}
    		return true;
        }catch(NullPointerException n){}
        return false;
	}
	public boolean mayIFight(MOB mob)
	{
		if(mob==null) return false;
		if(location()==null) return false;
		if(mob.location()==null) return false;
		if(mob.amDead()) return false;
		if(mob.curState().getHitPoints()<=0) return false;
		if(amDead()) return false;
		if(curState().getHitPoints()<=0) return false;
		if(mob.isMonster())
		{
			MOB fol=mob.amFollowing();
			if(fol!=null) return mayIFight(fol);
			return true;
		}
		else
		if(isMonster())
		{
			MOB fol=amFollowing();
			if(fol!=null) return fol.mayIFight(mob);
			return true;
		}
		if((mob.soulMate()!=null)||(soulMate()!=null))
			return true;
		if(mob==this) return true;
		if(CMProps.getVar(CMProps.SYSTEM_PKILL).startsWith("ALWAYS"))
			return true;
		if(CMProps.getVar(CMProps.SYSTEM_PKILL).startsWith("NEVER"))
			return false;
		if(CMLib.clans().isCommonClanRelations(getClanID(),mob.getClanID(),Clan.REL_WAR))
			return true;
		if(CMath.bset(getBitmap(),MOB.ATT_PLAYERKILL))
		{
			if(CMSecurity.isAllowed(this,location(),"PKILL")
			||(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
				return true;
			return false;
		}
		else
		if(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL))
		{
			if(CMSecurity.isAllowed(mob,location(),"PKILL")
			||(CMath.bset(getBitmap(),MOB.ATT_PLAYERKILL)))
				return true;
			return false;
		}
		else
			return false;
	}
	public boolean mayPhysicallyAttack(MOB mob)
	{
		if((!mayIFight(mob))
		||(location()!=mob.location())
		||(!CMLib.flags().isInTheGame(this,false))
		||(!CMLib.flags().isInTheGame(mob,false)))
		   return false;
		return true;
	}
	public void setAtRange(int newRange){atRange=newRange;}
	public int rangeToTarget(){return atRange;}
	public int maxRange(){return maxRange(null);}
	public int minRange(){return maxRange(null);}
	public int maxRange(Environmental tool)
	{
		int max=0;
		if(tool!=null)
			max=tool.maxRange();
		if((location()!=null)&&(location().maxRange()<max))
			max=location().maxRange();
		return max;
	}
	public int minRange(Environmental tool)
	{
		if(tool!=null) return tool.minRange();
		return 0;
	}

	public void makePeace()
	{
		MOB myVictim=victim;
		setVictim(null);
		for(int f=0;f<numFollowers();f++)
		{
			MOB M=fetchFollower(f);
			if((M!=null)&&(M.isInCombat()))
				M.makePeace();
		}
		if(myVictim!=null)
		{
			MOB oldVictim=myVictim.getVictim();
			if(oldVictim==this)
				myVictim.makePeace();
		}
	}

	public MOB getVictim()
	{
		if(!isInCombat())
			return null;
		return victim;
	}

	public void setVictim(MOB mob)
	{
		if(mob==null)
		{
			setAtRange(-1);
			if(victim!=null)
			    synchronized(commandQue){commandQue.clear();}
		}
		if(victim==mob) return;
		if(mob==this) return;
		victim=mob;
		recoverPhyStats();
		recoverCharStats();
		recoverMaxState();
		if(mob!=null)
		{
			if((mob.location()==null)
			||(location()==null)
			||(mob.amDead())
			||(amDead())
			||(mob.location()!=location())
			||(!location().isInhabitant(this))
			||(!location().isInhabitant(mob)))
			{
				if(victim!=null)
					victim.setVictim(null);
				victim=null;
				setAtRange(-1);
			}
			else
			{
                if(Log.combatChannelOn())
                {
                	Item I=fetchWieldedItem();
                	Item VI=mob.fetchWieldedItem();
                	Log.combatOut("STRT",Name()+":"+phyStats().getCombatStats()+":"+curState().getCombatStats()+":"+((I==null)?"null":I.name())+":"+mob.Name()+":"+mob.phyStats().getCombatStats()+":"+mob.curState().getCombatStats()+":"+((VI==null)?"null":VI.name()));

                }
				mob.recoverCharStats();
				mob.recoverPhyStats();
				mob.recoverMaxState();
			}
		}
	}
	public DeadBody killMeDead(boolean createBody)
	{
		Room deathRoom=null;
		if(isMonster())
			deathRoom=location();
		else
			deathRoom=CMLib.login().getDefaultBodyRoom(this);
		if(location()!=null) location().delInhabitant(this);
		DeadBody Body=null;
		if(createBody)
			Body=charStats().getMyRace().getCorpseContainer(this,deathRoom);
		amDead=true;
		makePeace();
		setRiding(null);
        synchronized(commandQue){commandQue.clear();}
        Ability A=null;
		for(int a=numEffects()-1;a>=0;a--)
		{
			A=fetchEffect(a);
			if(A!=null) A.unInvoke();
		}
		setLocation(null);
		if(isMonster())
		{
			while(numFollowers()>0)
			{
				MOB follower=fetchFollower(0);
				if(follower!=null)
				{
					follower.setFollowing(null);
					delFollower(follower);
				}
			}
			setFollowing(null);
		}
		if((!isMonster())&&(soulMate()==null))
			bringToLife(CMLib.login().getDefaultDeathRoom(this),true);
		if(deathRoom!=null)
			deathRoom.recoverRoomStats();
		return Body;
	}

	public Room location()
	{
		if(location==null) return lastLocation;
		return location;
	}
	public void setLocation(Room newRoom)
	{
		lastLocation=location;
		location=newRoom;
	}
	public Rideable riding(){return riding;}
	public void setRiding(Rideable ride)
	{
		final Rideable amRiding=riding();
		if((ride!=null)&&(amRiding!=null)&&(amRiding==ride)&&(amRiding.amRiding(this)))
			return;
		if((amRiding!=null)&&(amRiding.amRiding(this)))
			amRiding.delRider(this);
		riding=ride;
		if((ride!=null)&&(!ride.amRiding(this)))
			ride.addRider(this);
	}
	public Session session()
	{
		return mySession==null?null:mySession.isFake()?null:mySession;
	}
	public void setSession(Session newSession)
	{
		mySession=newSession;
		setBitmap(getBitmap());
	}
	public Weapon myNaturalWeapon()
	{
		Weapon W=null;
		if((charStats()!=null)&&(charStats().getMyRace()!=null))
			W=charStats().getMyRace().myNaturalWeapon();
		else
			W=CMClass.getWeapon("Natural");
		if(W.subjectToWearAndTear())
			W.setUsesRemaining(100);
		return W;
	}

    public String displayName(MOB viewer)
    {
        if((CMProps.getBoolVar(CMProps.SYSTEMB_INTRODUCTIONSYSTEM))
        &&(playerStats()!=null)
        &&(viewer!=null)
        &&(viewer.playerStats()!=null)
        &&(!viewer.playerStats().isIntroducedTo(Name())))
            return CMLib.english().startWithAorAn(genericName()).toLowerCase();
        return name();
    }

	public String displayText(MOB viewer)
	{
		if((displayText.length()==0)
	    ||(!name().equals(Name()))
	    ||(!titledName().equals(Name()))
	    ||(CMLib.flags().isSleeping(this))
	    ||(CMLib.flags().isSitting(this))
	    ||(riding()!=null)
	    ||((amFollowing()!=null)&&(amFollowing().fetchFollowerOrder(this)>0))
	    ||((this instanceof Rideable)&&(((Rideable)this).numRiders()>0)&&CMLib.flags().hasSeenContents(this))
	    ||(isInCombat()))
		{
			StringBuffer sendBack=null;
			if(!displayName(viewer).equals(Name()))
				sendBack=new StringBuffer(displayName(viewer));
			else
				sendBack=new StringBuffer(titledName());
			sendBack.append(" ");
			sendBack.append(CMLib.flags().dispositionString(this,CMFlagLibrary.flag_is));
			sendBack.append(" here");
			if(riding()!=null)
			{
				sendBack.append(" "+riding().stateString(this)+" ");
				if(riding()==viewer)
					sendBack.append("YOU");
				else
				if(!CMLib.flags().canBeSeenBy(riding(),viewer))
				{
					if(riding() instanceof Item)
						sendBack.append("something");
					else
						sendBack.append("someone");
				}
				else
					sendBack.append(riding().name());
			}
			else
			if((this instanceof Rideable)
			   &&(((Rideable)this).numRiders()>0)
			   &&(((Rideable)this).stateStringSubject(((Rideable)this).fetchRider(0)).length()>0))
			{
				Rideable me=(Rideable)this;
				String first=me.stateStringSubject(me.fetchRider(0));
				sendBack.append(" "+first+" ");
				for(int r=0;r<me.numRiders();r++)
				{
					Rider rider=me.fetchRider(r);
					if((rider!=null)&&(me.stateStringSubject(rider).equals(first)))
					{
						if(r>0)
						{
							sendBack.append(", ");
							if(r==me.numRiders()-1)
								sendBack.append("and ");
						}
						if(rider==viewer)
							sendBack.append("you");
						else
						if(!CMLib.flags().canBeSeenBy(riding(),viewer))
						{
							if(riding() instanceof Item)
								sendBack.append("something");
							else
								sendBack.append("someone");
						}
						else
							sendBack.append(rider.name());
					}

				}
			}
			if((isInCombat())&&(CMLib.flags().canMove(this))&&(!CMLib.flags().isSleeping(this)))
			{
				sendBack.append(" fighting ");
				if(getVictim()==viewer)
					sendBack.append("YOU");
				else
				if(!CMLib.flags().canBeSeenBy(getVictim(),viewer))
					sendBack.append("someone");
				else
					sendBack.append(getVictim().name());
			}
			if((amFollowing()!=null)&&(amFollowing().fetchFollowerOrder(this)>0))
			{
				List<MOB> whoseAhead=CMLib.combat().getFormationFollowed(this);
			    if((whoseAhead!=null)&&(whoseAhead.size()>0))
			    {
				    sendBack.append(", behind ");
				    for(int v=0;v<whoseAhead.size();v++)
				    {
				        MOB ahead=(MOB)whoseAhead.get(v);
						if(v>0)
						{
							sendBack.append(", ");
							if(v==whoseAhead.size()-1)
								sendBack.append("and ");
						}
						if(ahead==viewer)
							sendBack.append("you");
						else
						if(!CMLib.flags().canBeSeenBy(ahead,viewer))
							sendBack.append("someone");
						else
							sendBack.append(ahead.name());
				    }
			    }
			}
			sendBack.append(".");
			return sendBack.toString();
		}
		return displayText;
	}

	public String displayText()
	{
		return displayText;
	}
	public void setDisplayText(String newDisplayText)
	{
		displayText=newDisplayText;
	}
	public String description()
	{
		if((description==null)||(description.length==0))
			return "";
		else
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBDCOMPRESS))
			return CMLib.encoder().decompressString(description);
		else
			return CMStrings.bytesToStr(description);
	}
	public void setDescription(String newDescription)
	{
		if(newDescription.length()==0)
			description=null;
		else
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBDCOMPRESS))
			description=CMLib.encoder().compressString(newDescription);
		else
			description=CMStrings.strToBytes(newDescription);
	}
	public void setMiscText(String newText)
	{
		if(newText.length()==0)
			miscText=null;
		else
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBCOMPRESS))
			miscText=CMLib.encoder().compressString(newText);
		else
			miscText=CMStrings.strToBytes(newText);
	}
	public String text()
	{
		if((miscText==null)||(miscText.length==0))
			return "";
		else
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBCOMPRESS))
			return CMLib.encoder().decompressString(miscText);
		else
			return CMStrings.bytesToStr(miscText);
	}
	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}

	public String healthText(MOB viewer)
	{
	    String mxp="^<!ENTITY vicmaxhp \""+maxState().getHitPoints()+"\"^>^<!ENTITY vichp \""+curState().getHitPoints()+"\"^>^<Health^>^<HealthText \""+name()+"\"^>";
		if((charStats()!=null)&&(charStats().getMyRace()!=null))
			return mxp+charStats().getMyRace().healthText(viewer,this)+"^</HealthText^>";
		return mxp+CMLib.combat().standardMobCondition(viewer,this)+"^</HealthText^>";
	}

    public double actions(){return freeActions;}
    public void setActions(double remain){freeActions=remain;}
    public int commandQueSize(){return commandQue.size();}
	public boolean dequeCommand()
	{
        while((!pleaseDestroy)&&((session()==null)||(!session().killFlag())))
        {
        	QMCommand doCommand=null;
            synchronized(commandQue)
            {
                if(commandQue.size()==0) return false;
                QMCommand cmd=commandQue.getFirst();
                double diff=actions()-cmd.tickDelay;
				if(diff>=0.0)
                {
                    long nextTime=lastCommandTime
                                 +Math.round(cmd.tickDelay
                                             /phyStats().speed()
                                             *CMProps.getTickMillisD());
                    if((System.currentTimeMillis()<nextTime)&&(session()!=null))
                        return false;
					cmd=commandQue.removeFirst();
                    setActions(diff);
                    doCommand=cmd;
                }
            }
            if(doCommand!=null)
            {
                lastCommandTime=System.currentTimeMillis();
                doCommand(doCommand.commandObj,doCommand.commandVector,doCommand.metaFlags);
                synchronized(commandQue)
                {
	                if(commandQue.size()>0)
	                {
	                	QMCommand cmd = commandQue.getFirst();
	                	Object O=cmd.commandObj;
	                	cmd.tickDelay = calculateTickDelay(O,cmd.commandVector,0.0);
	                }
	                else
	                	return false;
	                return true;
                }
            }

            QMCommand cmd=null;
            synchronized(commandQue)
            {
                if(commandQue.size()==0) return false;
                cmd=commandQue.getFirst();
                if(System.currentTimeMillis()<cmd.nextCheck)
                    return false;
            }
            if(cmd != null)
            {
                double diff=actions()-cmd.tickDelay;
                Object O=cmd.commandObj;
                Vector commands=new XVector(cmd.commandVector);
                cmd.nextCheck=cmd.nextCheck+1000;
                cmd.seconds+=1;
                int secondsElapsed=cmd.seconds;
                int metaFlags=cmd.metaFlags;
                try
                {
	                if(O instanceof Command)
	                {
	                    if(!((Command)O).preExecute(this,commands,metaFlags,secondsElapsed,-diff))
	                    {
	                        commandQue.remove(cmd);
	                        return true;
	                    }
	                }
	                else
	                if(O instanceof Ability)
	                {
	                    if(!CMLib.english().preEvoke(this,commands,secondsElapsed,-diff))
	                    {
	                        commandQue.remove(cmd);
	                        return true;
	                    }
	                }
                }
                catch(Exception e)
                {
                    return false;
                }
            }
		}
        return false;
	}

	public void doCommand(List commands, int metaFlags)
	{
		CMObject O=CMLib.english().findCommand(this,commands);
		if(O!=null)
			doCommand(O,commands, metaFlags);
		else
			CMLib.commands().handleUnknownCommand(this,commands);
	}

    protected void doCommand(Object O, List commands, int metaFlags)
	{
		try
		{
			if(O instanceof Command)
				((Command)O).execute(this,new XVector(commands), metaFlags);
			else
			if(O instanceof Social)
				((Social)O).invoke(this,new XVector(commands),null,false);
			else
			if(O instanceof Ability)
				CMLib.english().evoke(this,new XVector(commands));
			else
				CMLib.commands().handleUnknownCommand(this,commands);
		}
		catch(java.io.IOException io)
		{
			Log.errOut("StdMOB",CMParms.toStringList(commands));
			if(io.getMessage()!=null)
				Log.errOut("StdMOB",io.getMessage());
			else
				Log.errOut("StdMOB",io);
			tell("Oops!");
		}
		catch(Exception e)
		{
			Log.errOut("StdMOB",CMParms.toStringList(commands));
			Log.errOut("StdMOB",e);
			tell("Oops!");
		}
	}

    protected double calculateTickDelay(Object command, List<String> commands, double tickDelay)
    {
        if(tickDelay<=0.0)
        {
            if(command==null){ tell("Huh?!"); return -1.0;}
            if(command instanceof Command)
                tickDelay=((Command)command).checkedActionsCost(this, commands);
            else
            if(command instanceof Ability)
            	tickDelay = ((Ability)command).checkedCastingTime(this,commands);
            else
                tickDelay=1.0;
        }
        return tickDelay;
    }

    public void prequeCommand(Vector commands, int metaFlags, double tickDelay)
    {
        if(commands==null) return;
        CMObject O=CMLib.english().findCommand(this,commands);
        if(O==null){ CMLib.commands().handleUnknownCommand(this,commands); return;}
        tickDelay=calculateTickDelay(O,commands,tickDelay);
        if(tickDelay<0.0) return;
        if(tickDelay==0.0)
            doCommand(O,commands,metaFlags);
        else
        synchronized(commandQue)
        {
        	QMCommand cmd = new QMCommand();
        	cmd.nextCheck=System.currentTimeMillis()-1;
        	cmd.seconds=-1;
        	cmd.tickDelay=tickDelay;
        	cmd.metaFlags=metaFlags;
        	cmd.commandObj=O;
        	cmd.commandVector=commands;
            commandQue.addFirst(cmd);
        }
        dequeCommand();
    }

	public void enqueCommand(List<String> commands, int metaFlags, double tickDelay)
	{
		if(commands==null) return;
		CMObject O=CMLib.english().findCommand(this,commands);
        if(O==null){ CMLib.commands().handleUnknownCommand(this,commands); return;}
        tickDelay=calculateTickDelay(O,commands,tickDelay);
        if(tickDelay<0.0) return;
        if(tickDelay==0.0)
            doCommand(commands,metaFlags);
        else
        synchronized(commandQue)
        {
        	QMCommand cmd = new QMCommand();
        	cmd.nextCheck=System.currentTimeMillis()-1;
        	cmd.seconds=-1;
        	cmd.tickDelay=tickDelay;
        	cmd.metaFlags=metaFlags;
        	cmd.commandObj=O;
        	cmd.commandVector=commands;
            commandQue.addLast(cmd);
        }
        dequeCommand();
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((getMyDeity()!=null)&&(!getMyDeity().okMessage(this,msg)))
			return false;

		if(charStats!=null)
		{
			for(int c=0;c<charStats().numClasses();c++)
				if(!charStats().getMyClass(c).okMessage(this,msg))
					return false;
			if(!charStats().getMyRace().okMessage(this, msg))
				return false;
		}

		MsgListener ML=null;
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			ML=a.nextElement();
			if((ML!=null)&&(!ML.okMessage(this,msg)))
				return false;
		}

        int num=numItems();
        for(int i=num-1;i>=0;i--)
		{
			ML=getItem(i);
            if((ML!=null)&&(!ML.okMessage(this,msg)))
				return false;
		}

        num=numBehaviors();
        for(int b=0;b<num;b++)
		{
			ML=fetchBehavior(b);
            if((ML!=null)&&(!ML.okMessage(this,msg)))
				return false;
		}

        num=numScripts();
        for(int s=0;s<num;s++)
        {
            ML=fetchScript(s);
            if((ML!=null)&&(!ML.okMessage(this,msg)))
                return false;
        }

        Faction.FactionData factionData=null;
        for(Enumeration e=factions.elements();e.hasMoreElements();)
        {
        	factionData=(Faction.FactionData)e.nextElement();
        	if(!factionData.getFaction().okMessage(myHost, msg))
        		return false;
        	if(!factionData.okMessage(myHost, msg))
        		return false;
        }

		MOB mob=msg.source();
		if((msg.sourceCode()!=CMMsg.NO_EFFECT)&&(msg.amISource(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(CMSecurity.isAllowed(this,location(),"IMMORT")))
			{
				curState().setHitPoints(1);
				if((msg.tool()!=null)
				&&(msg.tool()!=this)
				&&(msg.tool() instanceof MOB))
				   ((MOB)msg.tool()).tell(name()+" is immortal, and can not die.");
				tell("You are immortal, and can not die.");
				return false;
			}

			if(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
			{
				int srcCode=msg.sourceMajor();
			    int srcMinor = msg.sourceMinor();
				if(amDead())
				{
					tell("You are DEAD!");
					return false;
				}

				if(CMath.bset(srcCode,CMMsg.MASK_MALICIOUS))
				{
					if((msg.target()!=this)&&(msg.target()!=null)&&(msg.target() instanceof MOB))
					{
						MOB target=(MOB)msg.target();
						if((amFollowing()!=null)&&(target==amFollowing()))
						{
							tell("You like "+amFollowing().charStats().himher()+" too much.");
							return false;
						}
						if((getLiegeID().length()>0)&&(target.Name().equals(getLiegeID())))
						{
							if(isMarriedToLiege())
								tell("You are married to '"+getLiegeID()+"'!");
							else
								tell("You are serving '"+getLiegeID()+"'!");
							return false;
						}
						CMLib.combat().establishRange(this,(MOB)msg.target(),msg.tool());
					}
				}


				if(CMath.bset(srcCode,CMMsg.MASK_EYES))
				{
					if(CMLib.flags().isSleeping(this))
					{
						tell("Not while you are sleeping.");
						return false;
					}
					if(!(msg.target() instanceof Room))
						if(!CMLib.flags().canBeSeenBy(msg.target(),this))
						{
							if(msg.target() instanceof Item)
								tell("You don't see "+msg.target().name()+" here.");
							else
								tell("You can't see that!");
							return false;
						}
				}
				if(CMath.bset(srcCode,CMMsg.MASK_MOUTH))
				{
					if(((srcMinor!=CMMsg.TYP_LIST)||mob.amDead()||CMLib.flags().isSleeping(mob))
					&&(!CMLib.flags().aliveAwakeMobile(this,false)))
						return false;
					if(CMath.bset(srcCode,CMMsg.MASK_SOUND))
					{
						if((msg.tool()==null)
						||(!(msg.tool() instanceof Ability))
						||(!((Ability)msg.tool()).isNowAnAutoEffect()))
						{
							if(CMLib.flags().isSleeping(this))
							{
								tell("Not while you are sleeping.");
								return false;
							}
							if(!CMLib.flags().canSpeak(this))
							{
								tell("You can't make sounds!");
								return false;
							}
							if(CMLib.flags().isAnimalIntelligence(this))
							{
								tell("You aren't smart enough to speak.");
								return false;
							}
						}
					}
					else
					{
						if((!CMLib.flags().canBeSeenBy(msg.target(),this))
						&&(!(isMine(msg.target())&&(msg.target() instanceof Item))))
						{
							mob.tell("You don't see '"+msg.target().name()+"' here.");
							return false;
						}
						if(!CMLib.flags().canTaste(this))
						{
							tell("You can't eat or drink!");
							return false;
						}
					}
				}
				if(CMath.bset(srcCode,CMMsg.MASK_HANDS))
				{
					if((!CMLib.flags().canBeSeenBy(msg.target(),this))
					&&(!(isMine(msg.target())&&(msg.target() instanceof Item)))
					&&(!((isInCombat())&&(msg.target()==victim)))
					&&(CMath.bset(msg.targetCode(),CMMsg.MASK_HANDS)))
					{
						mob.tell("You don't see '"+msg.target().name()+"' here.");
						return false;
					}
					if(!CMLib.flags().aliveAwakeMobile(this,false))
						return false;

					if((CMLib.flags().isSitting(this))
					&&(msg.sourceMinor()!=CMMsg.TYP_SITMOVE)
					&&(msg.sourceMinor()!=CMMsg.TYP_BUY)
					&&(msg.sourceMinor()!=CMMsg.TYP_BID)
					&&(msg.targetCode()!=CMMsg.MSG_OK_VISUAL)
					&&((msg.sourceMessage()!=null)||(msg.othersMessage()!=null))
					&&((!CMLib.utensils().reachableItem(this,msg.target()))
					||(!CMLib.utensils().reachableItem(this,msg.tool()))))
					{
						tell("You need to stand up!");
						return false;
					}
				}

				if(CMath.bset(srcCode,CMMsg.MASK_MOVE))
				{
					boolean sitting=CMLib.flags().isSitting(this);
					if((sitting)
					&&((msg.sourceMinor()==CMMsg.TYP_LEAVE)
					||(msg.sourceMinor()==CMMsg.TYP_ENTER)))
						sitting=false;

					if(((CMLib.flags().isSleeping(this))||(sitting))
					&&(msg.sourceMinor()!=CMMsg.TYP_STAND)
					&&(msg.sourceMinor()!=CMMsg.TYP_SITMOVE)
					&&(msg.sourceMinor()!=CMMsg.TYP_SLEEP))
					{
						tell("You need to stand up!");
						if((msg.sourceMinor()!=CMMsg.TYP_WEAPONATTACK)
						&&(msg.sourceMinor()!=CMMsg.TYP_THROW))
							return false;
					}
					if((!CMLib.flags().canMove(this))||(imMobile))
					{
						tell("You can't move!");
						return false;
					}
				}

				// limb check
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_PULL:
				case CMMsg.TYP_PUSH:
				case CMMsg.TYP_GET:
				case CMMsg.TYP_REMOVE:
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
					if(charStats().getBodyPart(Race.BODY_ARM)==0)
					{
						tell("You need arms to do that.");
						return false;
					}
					break;
				case CMMsg.TYP_DELICATE_HANDS_ACT:
					if((charStats().getBodyPart(Race.BODY_HAND)==0)
					&&(msg.othersMinor()!=CMMsg.NO_EFFECT))
					{
						tell("You need hands to do that.");
						return false;
					}
					break;
				case CMMsg.TYP_JUSTICE:
					if((charStats().getBodyPart(Race.BODY_HAND)==0)
					&&(msg.target() instanceof Item))
					{
						tell("You need hands to do that.");
						return false;
					}
					break;
				case CMMsg.TYP_FILL:
				case CMMsg.TYP_GIVE:
				case CMMsg.TYP_HANDS:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_PUT:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_WRITE:
					if(charStats().getBodyPart(Race.BODY_HAND)==0)
					{
						tell("You need hands to do that.");
						return false;
					}
					break;
				case CMMsg.TYP_DRINK:
					if(charStats().getBodyPart(Race.BODY_HAND)==0)
					{
						if((msg.target()!=null)
						&&(isMine(msg.target())))
						{
							tell("You need hands to do that.");
							return false;
						}
					}
					break;
				}

				// activity check
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_JUSTICE:
					if((msg.target()!=null)
					&&(isInCombat())
					&&(msg.target() instanceof Item))
					{
						tell("Not while you are fighting!");
						return false;
					}
					break;
				case CMMsg.TYP_THROW:
					if(charStats().getBodyPart(Race.BODY_ARM)==0)
					{
						tell("You need arms to do that.");
						return false;
					}
					break;
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
					if(isInCombat())
					{
						if((msg.target()!=null)
						&&((msg.target() instanceof Exit)||(msg.source().isMine(msg.target()))))
							break;
						tell("Not while you are fighting!");
						return false;
					}
					break;
				case CMMsg.TYP_LEAVE:
					if((isInCombat())&&(location()!=null)&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC)))
						for(int i=0;i<location().numInhabitants();i++)
						{
							MOB M=location().fetchInhabitant(i);
							if((M!=null)
							&&(M!=this)
							&&(M.getVictim()==this)
							&&(CMLib.flags().aliveAwakeMobile(M,true))
							&&(CMLib.flags().canSenseEnteringLeaving(mob,M)))
							{
								tell("Not while you are fighting!");
								return false;
							}
						}
					break;
				case CMMsg.TYP_BUY:
				case CMMsg.TYP_BID:
				case CMMsg.TYP_DELICATE_HANDS_ACT:
				case CMMsg.TYP_FILL:
				case CMMsg.TYP_LIST:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_SLEEP:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_VALUE:
				case CMMsg.TYP_SELL:
				case CMMsg.TYP_VIEW:
				case CMMsg.TYP_READ:
					if(isInCombat()&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC)))
					{
						tell("Not while you are fighting!");
						return false;
					}
					break;
				case CMMsg.TYP_REBUKE:
					if((msg.target()==null)||(!(msg.target() instanceof Deity)))
					{
						if(msg.target()!=null)
						{
							if((msg.target() instanceof MOB)
							&&(!CMLib.flags().canBeHeardSpeakingBy(this,(MOB)msg.target())))
							{
								tell(msg.target().name()+" can't hear you!");
								return false;
							}
							else
							if((msg.target() instanceof MOB)
							&&(((MOB)msg.target()).amFollowing()==msg.source())
							&&(msg.source().isFollowedBy((MOB)msg.target())))
							{
								// should work.
							}
							else
							if((!((msg.target() instanceof MOB)
							&&(((MOB)msg.target()).getLiegeID().equals(Name()))))
							&&(!msg.target().Name().equals(getLiegeID())))
							{
								tell(msg.target().name()+" does not serve you, and you do not serve "+msg.target().name()+".");
								return false;
							}
							else
							if((msg.target() instanceof MOB)
							&&(((MOB)msg.target()).getLiegeID().equals(Name()))
							&&(getLiegeID().equals(msg.target().Name()))
							&&(((MOB)msg.target()).isMarriedToLiege()))
							{
								tell("You cannot rebuke "+msg.target().name()+".  You must get an annulment or a divorce.");
								return false;
							}
						}
						else
						if(getLiegeID().length()==0)
						{
							tell("You aren't serving anyone!");
							return false;
						}
					}
					else
					if(getWorshipCharID().length()==0)
					{
						tell("You aren't worshipping anyone!");
						return false;
					}
					break;
				case CMMsg.TYP_SERVE:
					if(msg.target()==null) return false;
					if(msg.target()==this)
					{
						tell("You can't serve yourself!");
						return false;
					}
					if(msg.target() instanceof Deity)
						break;
					if((msg.target() instanceof MOB)
					&&(!CMLib.flags().canBeHeardSpeakingBy(this,(MOB)msg.target())))
					{
						tell(msg.target().name()+" can't hear you!");
						return false;
					}
					if(getLiegeID().length()>0)
					{
						tell("You are already serving '"+getLiegeID()+"'.");
						return false;
					}
					if((msg.target() instanceof MOB)
					&&(((MOB)msg.target()).getLiegeID().equals(Name())))
					{
						tell("You can not serve each other!");
						return false;
					}
					break;
				case CMMsg.TYP_CAST_SPELL:
					if(charStats().getStat(CharStats.STAT_INTELLIGENCE)<5)
					{
						tell("You aren't smart enough to do magic.");
						return false;
					}
					break;
				default:
					break;
				}
			}
		}

		if((msg.sourceCode()!=CMMsg.NO_EFFECT)
		&&(msg.amISource(this))
		&&(msg.target()!=null)
		&&(msg.target()!=this)
		&&(!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS))
		&&(msg.target() instanceof MOB)
		&&(location()!=null)
		&&(location()==((MOB)msg.target()).location()))
		{
			MOB target=(MOB)msg.target();
			// and now, the consequences of range
			if(((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)&&(rangeToTarget()>maxRange(msg.tool())))
			||((msg.sourceMinor()==CMMsg.TYP_THROW)&&(rangeToTarget()>2)&&(maxRange(msg.tool())<=0)))
			{
				String newstr="<S-NAME> advance(s) at ";
				msg.modify(this,target,null,CMMsg.MSG_ADVANCE,newstr+target.name(),CMMsg.MSG_ADVANCE,newstr+"you",CMMsg.MSG_ADVANCE,newstr+target.name());
				boolean ok=location().okMessage(this,msg);
				if(ok) setAtRange(rangeToTarget()-1);
				if(victim!=null)
				{
					victim.setAtRange(rangeToTarget());
					victim.recoverPhyStats();
				}
				else
					setAtRange(-1);
				recoverPhyStats();
				return ok;
			}
			else
			if(msg.targetMinor()==CMMsg.TYP_RETREAT)
			{
				if(curState().getMovement()<25)
				{
					tell("You are too tired.");
					return false;
				}
				if((location()!=null)
				   &&(rangeToTarget()>=location().maxRange()))
				{
					tell("You cannot retreat any further.");
					return false;
				}
				curState().adjMovement(-25,maxState());
				setAtRange(rangeToTarget()+1);
				if(victim!=null)
				{
					victim.setAtRange(rangeToTarget());
					victim.recoverPhyStats();
				}
				else
					setAtRange(-1);
				recoverPhyStats();
			}
			else
			if((msg.tool()!=null)
			&&(msg.sourceMinor()!=CMMsg.TYP_BUY)
		    &&(msg.sourceMinor()!=CMMsg.TYP_BID)
			&&(msg.sourceMinor()!=CMMsg.TYP_SELL)
			&&(msg.sourceMinor()!=CMMsg.TYP_VIEW))
			{
				int useRange=-1;
				Environmental tool=msg.tool();
				if(getVictim()!=null)
				{
					if(getVictim()==target)
						useRange=rangeToTarget();
					else
					{
						if(target.getVictim()==this)
							useRange=target.rangeToTarget();
						else
							useRange=maxRange(tool);
					}
				}
				if((useRange>=0)&&(maxRange(tool)<useRange))
				{
					mob.tell("You are too far away from "+target.name()+" to use "+tool.name()+".");
					return false;
				}
				else
				if((useRange>=0)&&(minRange(tool)>useRange))
				{
					mob.tell("You are too close to "+target.name()+" to use "+tool.name()+".");
					if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
					&&(tool instanceof Weapon)
					&&(!((Weapon)tool).amWearingAt(Wearable.IN_INVENTORY)))
						CMLib.commands().postRemove(this,(Weapon)msg.tool(),false);
					return false;
				}
			}
		}

		if((msg.targetCode()!=CMMsg.NO_EFFECT)&&(msg.amITarget(this)))
		{
			if((amDead())||(location()==null))
				return false;
			if(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			{
                if(Log.combatChannelOn())
                    Log.combatOut(msg.source().Name()+":"+Name()+":"+CMMsg.TYPE_DESCS[msg.targetMinor()]+":"+((msg.tool()!=null)?msg.tool().Name():"null"));

				if((msg.amISource(this))
				&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
				&&((msg.tool()==null)||(!(msg.tool() instanceof Ability))||(!((Ability)msg.tool()).isNowAnAutoEffect())))
				{
					mob.tell("You like yourself too much.");
					if(victim==this){ victim=null; setAtRange(-1);}
					return false;
				}

				if((!mayIFight(mob))
				&&((!(msg.tool() instanceof Ability))
                   ||(((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_POISON)
				       &&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_DISEASE))
                   ||((mob==this)&&(mob.isMonster()))))
				{
					mob.tell("You may not attack "+name()+".");
					mob.setVictim(null);
					if(victim==mob) setVictim(null);
					return false;
				}

				if((!isMonster())&&(!mob.isMonster())
				&&(soulMate()==null)
				&&(mob.soulMate()==null)
				&&(!CMSecurity.isAllowed(this,location(),"PKILL"))&&(!CMSecurity.isAllowed(mob,mob.location(),"PKILL"))
				&&(mob.phyStats().level()>phyStats().level()+CMProps.getPKillLevelDiff())
				&&((!(msg.tool() instanceof Ability))
				   ||(((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_DISEASE))
				{
					mob.tell("That is not EVEN a fair fight.");
					mob.setVictim(null);
					if(victim==mob) setVictim(null);
					return false;
				}

				if((amFollowing()==mob)
				&&(!(msg.tool() instanceof DiseaseAffect)))
					setFollowing(null);

				if(isInCombat())
				{
					if((rangeToTarget()>0)
					&&(getVictim()!=msg.source())
					&&(msg.source().getVictim()==this)
					&&(msg.source().rangeToTarget()==0))
					{
					    setVictim(msg.source());
						setAtRange(0);
					}
				}

				if((msg.targetMinor()!=CMMsg.TYP_WEAPONATTACK)&&(msg.value()<=0))
				{
					int chanceToFail=Integer.MIN_VALUE;
					for(int c : CharStats.CODES.SAVING_THROWS())
						if(msg.targetMinor()==CharStats.CODES.CMMSGMAP(c))
						{	chanceToFail=charStats().getSave(c); break;}
					if(chanceToFail>Integer.MIN_VALUE)
					{
				        int diff = (phyStats().level()-msg.source().phyStats().level());
				        int diffSign = diff < 0 ? -1 : 1;
						chanceToFail+=(diffSign * (diff * diff));
						if(chanceToFail<5)
							chanceToFail=5;
						else
						if(chanceToFail>95)
						   chanceToFail=95;

						if(CMLib.dice().rollPercentage()<chanceToFail)
						{
                            CMLib.combat().resistanceMsgs(msg,msg.source(),this);
							msg.setValue(msg.value()+1);
						}
					}
				}
			}

			if((rangeToTarget()>=0)&&(!isInCombat()))
				setAtRange(-1);

			switch(msg.targetMinor())
			{
			case CMMsg.TYP_CLOSE:
			case CMMsg.TYP_DRINK:
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_EAT:
			case CMMsg.TYP_FILL:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_HOLD:
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_UNLOCK:
			case CMMsg.TYP_WEAR:
			case CMMsg.TYP_WIELD:
				mob.tell(mob,this,null,"You can't do that to <T-NAMESELF>.");
				return false;
			case CMMsg.TYP_PULL:
			    if((!CMLib.flags().isBoundOrHeld(this))&&(!CMLib.flags().isSleeping(this)))
			    {
					mob.tell(mob,this,null,"You can't do that to <T-NAMESELF>.");
					return false;
			    }
			    if(phyStats().weight()>(mob.maxCarry()/2))
			    {
			        mob.tell(mob,this,null,"<T-NAME> is too big for you to pull.");
			        return false;
			    }
			    break;
			case CMMsg.TYP_PUSH:
			    if((!CMLib.flags().isBoundOrHeld(this))&&(!CMLib.flags().isSleeping(this)))
			    {
					mob.tell(mob,this,null,"You can't do that to <T-NAMESELF>.");
					return false;
			    }
			    if(phyStats().weight()>mob.maxCarry())
			    {
			        mob.tell(mob,this,null,"<T-NAME> is too heavy for you to push.");
			        return false;
			    }
			    break;
			case CMMsg.TYP_MOUNT:
			case CMMsg.TYP_DISMOUNT:
				if(!(this instanceof Rideable))
				{
					mob.tell(mob,this,null,"You can't do that to <T-NAMESELF>.");
					return false;
				}
				break;
			case CMMsg.TYP_GIVE:
				if(msg.tool()==null) return false;
				if(!(msg.tool() instanceof Item)) return false;
				if(CMSecurity.isAllowed(this,location(),"ORDER")
				||(CMSecurity.isAllowed(this,location(),"CMDMOBS")&&(isMonster()))
				||(CMSecurity.isAllowed(this,location(),"CMDROOMS")&&(isMonster())))
					return true;
				if(getWearPositions(Wearable.WORN_ARMS)==0)
				{
					msg.source().tell(name()+" is unable to accept that from you.");
					return false;
				}
				if((!CMLib.flags().canBeSeenBy((Item)msg.tool(),this))&&(!CMath.bset(msg.targetCode(),CMMsg.MASK_ALWAYS)))
				{
					mob.tell(name()+" can't see what you are giving.");
					return false;
				}
				int GC=msg.targetCode()&CMMsg.MASK_ALWAYS;
				CMMsg msg2=CMClass.getMsg(msg.source(),msg.tool(),null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
				if(!location().okMessage(msg.source(),msg2))
					return false;
				if((msg.target()!=null)&&(msg.target() instanceof MOB))
				{
					msg2=CMClass.getMsg((MOB)msg.target(),msg.tool(),null,GC|CMMsg.MSG_GET,null,GC|CMMsg.MSG_GET,"GIVE",GC|CMMsg.MSG_GET,null);
					if(!location().okMessage(msg.target(),msg2))
					{
						mob.tell(msg.target().name()+" cannot seem to accept "+msg.tool().name()+".");
						return false;
					}
				}
				break;
			case CMMsg.TYP_FOLLOW:
				if(totalFollowers()+mob.totalFollowers()>=maxFollowers())
				{
					mob.tell(name()+" can't accept any more followers.");
					return false;
				}
                if((CMProps.getIntVar(CMProps.SYSTEMI_FOLLOWLEVELDIFF)>0)
				&&(!isMonster())
				&&(!mob.isMonster())
				&&(!CMSecurity.isAllowed(this,location(),"ORDER"))
				&&(!CMSecurity.isAllowed(mob,mob.location(),"ORDER")))
                {
					if(phyStats.level() > (mob.phyStats().level() + CMProps.getIntVar(CMProps.SYSTEMI_FOLLOWLEVELDIFF)))
					{
						mob.tell(name() + " is too advanced for you.");
						return false;
					}
					if(phyStats.level() < (mob.phyStats().level() - CMProps.getIntVar(CMProps.SYSTEMI_FOLLOWLEVELDIFF)))
					{
						mob.tell(name() + " is too inexperienced for you.");
						return false;
					}
                }
				break;
			}
		}
		if((msg.source()!=this)&&(msg.target()!=this))
		{
			if((msg.othersMinor()==CMMsg.TYP_DEATH)
            &&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			{
			    if((followers!=null)
			    &&(followers.contains(msg.source()))
				&&(CMLib.dice().rollPercentage()==1)
			    &&(fetchEffect("Disease_Depression")==null)
			    &&(!CMSecurity.isDisabled("AUTODISEASE")))
				{
				    Ability A=CMClass.getAbility("Disease_Depression");
				    if(A!=null) A.invoke(this,this,true,0);
				}
			}
		}
		return true;
	}

	public void tell(MOB source, Environmental target, Environmental tool, String msg)
	{
		if((mySession!=null)&&(msg!=null))
		{
			Session S=mySession;
			if(S!=null)
				S.stdPrintln(source,target,tool,msg);
		}
	}

	public void tell(String msg)
	{
		tell(this,this,null,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(getMyDeity()!=null)
		   getMyDeity().executeMsg(this,msg);

		if(charStats!=null)
		{
			for(int c=0;c<charStats().numClasses();c++)
				charStats().getMyClass(c).executeMsg(this,msg);
			charStats().getMyRace().executeMsg(this,msg);
		}

        MsgListener ML=null;
        for(int b=0;b<numBehaviors();b++)
		{
			ML=fetchBehavior(b);
			if(ML!=null) ML.executeMsg(this,msg);
		}

        for(int s=0;s<numScripts();s++)
        {
            ML=fetchScript(s);
            if(ML!=null) ML.executeMsg(this,msg);
        }
        
		MOB mob=msg.source();

		boolean asleep=CMLib.flags().isSleeping(this);
		boolean canseesrc=CMLib.flags().canBeSeenBy(msg.source(),this);
		boolean canhearsrc=(msg.targetMinor()==CMMsg.TYP_SPEAK)
							?CMLib.flags().canBeHeardSpeakingBy(msg.source(),this)
							:CMLib.flags().canBeHeardMovingBy(msg.source(),this);

		// first do special cases...
		if(msg.amITarget(this)&&(!amDead))
		switch(msg.targetMinor())
		{
			case CMMsg.TYP_HEALING: CMLib.combat().handleBeingHealed(msg); break;
			case CMMsg.TYP_SNIFF: CMLib.commands().handleBeingSniffed(msg); break;
			case CMMsg.TYP_DAMAGE: CMLib.combat().handleBeingDamaged(msg); break;
			default: break;
		}


		// now go on to source activities
		if((msg.sourceCode()!=CMMsg.NO_EFFECT)&&(msg.amISource(this)))
		{
			if((CMath.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
			&&(msg.target() instanceof MOB)
			&&(getVictim()!=msg.target())
			&&((!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS))
				||(!(msg.tool() instanceof DiseaseAffect))))
			{
                CMLib.combat().establishRange(this,(MOB)msg.target(),msg.tool());
                if((msg.tool() instanceof Weapon)
				||(!CMLib.flags().aliveAwakeMobileUnbound((MOB)msg.target(),true)))
                {
					setVictim((MOB)msg.target());
                }
			}

			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_LIFE: CMLib.commands().handleComeToLife(this,msg); break;
			case CMMsg.TYP_PANIC: CMLib.commands().postFlee(this,""); break;
			case CMMsg.TYP_EXPCHANGE: CMLib.leveler().handleExperienceChange(msg); break;
            case CMMsg.TYP_FACTIONCHANGE:
                if(msg.othersMessage()!=null)
                {
	                if((msg.value()==Integer.MAX_VALUE)||(msg.value()==Integer.MIN_VALUE))
	                    removeFaction(msg.othersMessage());
	                else
		                adjustFaction(msg.othersMessage(),msg.value());
                }
                break;
			case CMMsg.TYP_DEATH:
				CMLib.combat().handleDeath(msg);
			break;
			case CMMsg.TYP_REBUKE:
				if(((msg.target()==null)&&(getLiegeID().length()>0))
				||((msg.target()!=null)
				   &&(msg.target().Name().equals(getLiegeID()))
				   &&(!isMarriedToLiege())))
					setLiegeID("");
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				break;
			case CMMsg.TYP_SERVE:
				if((msg.target()!=null)&&(!(msg.target() instanceof Deity)))
					setLiegeID(msg.target().Name());
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				break;
			case CMMsg.TYP_LOOK:
            case CMMsg.TYP_EXAMINE:
                if(msg.target()==this)
                    CMLib.commands().handleBeingLookedAt(msg);
                break;
			case CMMsg.TYP_READ:
				if((CMLib.flags().canBeSeenBy(this,mob))&&(msg.amITarget(this)))
					tell("There is nothing written on "+name());
				break;
			case CMMsg.TYP_SIT: CMLib.commands().handleSit(msg); break;
			case CMMsg.TYP_SLEEP: CMLib.commands().handleSleep(msg); break;
			case CMMsg.TYP_QUIT:
				tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				break;
			case CMMsg.TYP_STAND: CMLib.commands().handleStand(msg); break;
			case CMMsg.TYP_RECALL:  CMLib.commands().handleRecall(msg); break;
			case CMMsg.TYP_FOLLOW:
				if((msg.target()!=null)&&(msg.target() instanceof MOB))
				{
					setFollowing((MOB)msg.target());
					tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				}
				break;
			case CMMsg.TYP_NOFOLLOW:
				setFollowing(null);
				tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				break;
			default:
				// you pretty much always know what you are doing, if you can do it.
				tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				break;
			}
		}
		else
		if((msg.targetCode()!=CMMsg.NO_EFFECT)&&(msg.amITarget(this)))
		{
			int targetMajor=msg.targetMajor();
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_HEALING:
            case CMMsg.TYP_DAMAGE:
                // handled as special cases above
                break;
            case CMMsg.TYP_GIVE:
                if(msg.tool() instanceof Item)
                    CMLib.commands().handleBeingGivenTo(msg);
                break;
            case CMMsg.TYP_LOOK:
            case CMMsg.TYP_EXAMINE:
                if(CMLib.flags().canBeSeenBy(this,mob))
                    CMLib.commands().handleBeingLookedAt(msg);
                break;
            case CMMsg.TYP_REBUKE:
                if((msg.source().Name().equals(getLiegeID())&&(!isMarriedToLiege())))
                    setLiegeID("");
                break;
            case CMMsg.TYP_SPEAK:
                if((CMProps.getBoolVar(CMProps.SYSTEMB_INTRODUCTIONSYSTEM))
                &&(!asleep)&&(canhearsrc))
                    CMLib.commands().handleIntroductions(msg.source(),this,msg.targetMessage());
                break;
            default:
                if((CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))&&(!amDead))
                    CMLib.combat().handleBeingAssaulted(msg);
                else
                if(CMath.bset(targetMajor,CMMsg.MASK_CHANNEL))
                {
                    int channelCode=((msg.targetCode()-CMMsg.MASK_CHANNEL)-CMMsg.TYP_CHANNEL);
                    if((playerStats()!=null)
                    &&(!CMath.bset(getBitmap(),MOB.ATT_QUIET))
                    &&(!CMath.isSet(playerStats().getChannelMask(),channelCode)))
                        tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
                }
                break;
			}

			// now do the says
			if((CMath.bset(targetMajor,CMMsg.MASK_SOUND))
			&&(canhearsrc)&&(!asleep))
			{
				if((msg.targetMinor()==CMMsg.TYP_SPEAK)
				 &&(msg.source()!=null)
				 &&(playerStats()!=null)
				 &&(!msg.source().isMonster())
				 &&(CMLib.flags().canBeHeardSpeakingBy(msg.source(), this)))
					playerStats().setReplyTo(msg.source(),PlayerStats.REPLY_SAY);
				
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			}
			else
			if((CMath.bset(targetMajor,CMMsg.MASK_ALWAYS))
			||(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			||(msg.targetMinor()==CMMsg.TYP_HEALING))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			else
			if((CMath.bset(targetMajor,CMMsg.MASK_EYES))
			&&((!asleep)&&(canseesrc)))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			else
			if(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			else
			if(((CMath.bset(targetMajor,CMMsg.MASK_HANDS))
				||(CMath.bset(targetMajor,CMMsg.MASK_MOVE))
				||((CMath.bset(targetMajor,CMMsg.MASK_MOUTH))
				   &&(!CMath.bset(targetMajor,CMMsg.MASK_SOUND))))
			&&(!asleep)&&((canhearsrc)||(canseesrc)))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
		}
		else
		if((msg.othersCode()!=CMMsg.NO_EFFECT)
		&&(!msg.amISource(this))
		&&(!msg.amITarget(this)))
		{
			int othersMajor=msg.othersMajor();
			int othersMinor=msg.othersMinor();

			if(CMath.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)
			&&(msg.target() instanceof MOB)
			&&((!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS))
				||(!(msg.tool() instanceof DiseaseAffect))))
                CMLib.combat().makeFollowersFight(this,(MOB)msg.target(),msg.source());

			if((othersMinor==CMMsg.TYP_ENTER) // exceptions to movement
			||(othersMinor==CMMsg.TYP_FLEE)
			||(othersMinor==CMMsg.TYP_LEAVE))
			{
				if(((!asleep)||(msg.othersMinor()==CMMsg.TYP_ENTER))
				&&(CMLib.flags().canSenseEnteringLeaving(msg.source(), this)))
					tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if(CMath.bset(othersMajor,CMMsg.MASK_CHANNEL))
			{
		        int channelCode=((msg.othersCode()-CMMsg.MASK_CHANNEL)-CMMsg.TYP_CHANNEL);
				if((playerStats()!=null)
				&&(!CMath.bset(getBitmap(),MOB.ATT_QUIET))
				&&(!CMath.isSet(playerStats().getChannelMask(),channelCode)))
					tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if((CMath.bset(othersMajor,CMMsg.MASK_SOUND))
			&&(!asleep)
			&&(canhearsrc))
			{
                if((CMProps.getBoolVar(CMProps.SYSTEMB_INTRODUCTIONSYSTEM))
                &&(msg.othersMinor()==CMMsg.TYP_SPEAK))
                    CMLib.commands().handleIntroductions(msg.source(),this,msg.othersMessage());
				tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if(((CMath.bset(othersMajor,CMMsg.MASK_EYES))
			||(CMath.bset(othersMajor,CMMsg.MASK_HANDS))
			||(CMath.bset(othersMajor,CMMsg.MASK_ALWAYS)))
			&&((!asleep)&&(canseesrc)))
			{
				tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if(((CMath.bset(othersMajor,CMMsg.MASK_MOVE))
				||((CMath.bset(othersMajor,CMMsg.MASK_MOUTH))&&(!CMath.bset(othersMajor,CMMsg.MASK_SOUND))))
			&&(!asleep)
			&&((canseesrc)||(canhearsrc)))
				tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());

			if((msg.othersMinor()==CMMsg.TYP_DEATH)
            &&(msg.sourceMinor()==CMMsg.TYP_DEATH)
            &&(location()!=null))
                CMLib.combat().handleObserveDeath(this,victim,msg);
			else
			if(msg.sourceMinor()==CMMsg.TYP_LIFE)
				CMLib.commands().handleObserveComesToLife(this,msg.source(),msg);
		}

        int num=numItems();
		for(int i=num-1;i>=0;i--)
		{
		    ML=getItem(i);
			if(ML!=null)
			    ML.executeMsg(this,msg);
		}

		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			ML=a.nextElement();
			if(ML!=null)
			    ML.executeMsg(this,msg);
		}

        Faction.FactionData factionData=null;
        for(Enumeration e=factions.elements();e.hasMoreElements();)
        {
        	factionData=(Faction.FactionData)e.nextElement();
        	factionData.getFaction().executeMsg(myHost, msg);
        	factionData.executeMsg(myHost, msg);
        }
	}

	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}

	public long getTickStatus(){return tickStatus;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(pleaseDestroy) return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==Tickable.TICKID_MOB)
		{
			boolean isMonster=isMonster();
			if(amDead)
			{
				boolean isOk=!pleaseDestroy;
				tickStatus=Tickable.STATUS_DEAD;
				if(isMonster)
				{
					if((phyStats().rejuv()<Integer.MAX_VALUE)
					&&(basePhyStats().rejuv()>0))
					{
						phyStats().setRejuv(phyStats().rejuv()-1);
						if((phyStats().rejuv()<0)||(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN)))
						{
							tickStatus=Tickable.STATUS_REBIRTH;
							cloneFix(CMClass.staticMOB(ID()));
							bringToLife(CMLib.map().getStartRoom(this),true);
							location().showOthers(this,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
						}
					}
					else
					{
						tickStatus=Tickable.STATUS_END;
 						if(soulMate()==null) destroy();
						isOk=false;

					}
				}
				tickStatus=Tickable.STATUS_NOT;
				lastTickedDateTime=System.currentTimeMillis();
				return isOk;
			}
			else
			if(location()!=null)
			{
				// handle variable equipment!
				if((lastTickedDateTime<0)
				&&isMonster
                &&location().getMobility()
                &&(location().getArea().getAreaState()<Area.STATE_FROZEN))
				{
					if(lastTickedDateTime==-1)
						lastTickedDateTime=CMLib.utensils().processVariableEquipment(this);
					else
						lastTickedDateTime++;
				}

				tickStatus=Tickable.STATUS_ALIVE;
				if((--recoverTickCounter)<=0)
				{
					curState().recoverTick(this,maxState);
					recoverTickCounter = CMProps.getIntVar(CMProps.SYSTEMI_RECOVERRATE);
				}
				if(!isMonster)
                    curState().expendEnergy(this,maxState,false);

				if((!CMLib.flags().canBreathe(this))&&(!CMLib.flags().isGolem(this)))
				{
					location().show(this,this,CMMsg.MSG_OK_VISUAL,("^Z<S-NAME> can't breathe!^.^?")+CMProps.msp("choke.wav",10));
					CMLib.combat().postDamage(this,this,null,(int)Math.round(CMath.mul(Math.random(),basePhyStats().level()+2)),CMMsg.MASK_ALWAYS|CMMsg.TYP_GAS,-1,null);
				}

                if(commandQueSize()==0) setActions(actions()-Math.floor(actions()));
                setActions(actions()+(CMLib.flags().isSitting(this)?phyStats().speed()/2.0:phyStats().speed()));

				if(isInCombat())
				{
                    if(CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM)==CombatLibrary.COMBAT_DEFAULT)
                        setActions(actions()+1.0); // bonus action is employed in default system
                    tickStatus=Tickable.STATUS_FIGHT;
                    peaceTime=0;
                    CMLib.combat().tickCombat(this);
				}
				else
				{
					peaceTime+=CMProps.getTickMillis();
					if(CMath.bset(getBitmap(),MOB.ATT_AUTODRAW)
					&&(peaceTime>=SHEATH_TIME)
					&&(CMLib.flags().aliveAwakeMobileUnbound(this,true)))
						CMLib.commands().postSheath(this,true);
				}

				tickStatus=Tickable.STATUS_OTHER;
				if(!isMonster)
				{
					if(CMLib.flags().isSleeping(this))
						curState().adjFatigue(-CharState.REST_PER_TICK,maxState());
					else
					if(!CMSecurity.isAllowed(this,location(),"IMMORT"))
					{
						curState().adjFatigue(CMProps.getTickMillis(),maxState());
				        if((curState().getFatigue()>CharState.FATIGUED_MILLIS)
						&&(!isMonster)
                     	&&(CMLib.dice().rollPercentage()==1)
                     	&&(!CMSecurity.isDisabled("AUTODISEASE")))
                     	{
                        	Ability theYawns = CMClass.getAbility("Disease_Yawning");
                        	if(theYawns!=null) theYawns.invoke(this, this, true,0);
                     	}
				        if((curState().getFatigue()>(CharState.FATIGUED_EXHAUSTED_MILLIS))
						&&(!isMonster)
                     	&&(CMLib.dice().rollPercentage()==1))
                     	{
							location().show(this,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fall(s) asleep from exhaustion!!");
							basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_SLEEPING);
							phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_SLEEPING);
                     	}
					}
				}
                else
                while((!amDead())&&dequeCommand())
                	{}

				if((riding()!=null)&&(CMLib.map().roomLocation(riding())!=location()))
					setRiding(null);

				if((!isMonster)&&(soulMate()==null))
				{
                    CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_TICKSONLINE);
                    if(((++tickCounter)*CMProps.getTickMillis())>60000)
                    {
                        tickCounter=0;
                        if(inventory!=null) inventory.trimToSize();
                        if(affects!=null) affects.trimToSize();
                        if(abilitees!=null) abilitees.trimToSize();
                        if(followers!=null) followers.trimToSize();
                        CMLib.commands().tickAging(this);
                    }
				}
			}

            Tickable T=null;
            int c=0;
            Enumeration e=null;
            for(e=affects.elements();e.hasMoreElements();c++)
			{
				T=(Tickable)e.nextElement();
				tickStatus=Tickable.STATUS_AFFECT+(c++);
				if(!T.tick(ticking,tickID))
					((Ability)T).unInvoke();
			}

            manaConsumeCounter=CMLib.commands().tickManaConsumption(this,manaConsumeCounter);

            c=0;
            for(e=behaviors.elements();e.hasMoreElements();c++)
            {
				T=(Tickable)e.nextElement();
				tickStatus=Tickable.STATUS_BEHAVIOR+(c++);
				if(T!=null) T.tick(ticking,tickID);
			}

            c=0;
            for(e=scripts.elements();e.hasMoreElements();c++)
            {
                T=(Tickable)e.nextElement();
                tickStatus=Tickable.STATUS_SCRIPT+(c++);
                if(T!=null) T.tick(ticking,tickID);

            }
            e=null;
            
            Faction.FactionData factionData=null;
            if(isMonster)
            {
	            for(e=factions.elements();e.hasMoreElements();)
	            {
	                factionData=(Faction.FactionData)e.nextElement();
	                if(factionData.requiresUpdating())
	                {
	                	String factionID = factionData.getFaction().factionID();
	                    Faction F=CMLib.factions().getFaction(factionID);
	                    if(F!=null)
	                    {
	                    	int oldValue = factionData.value();
	                    	F.updateFactionData(this, factionData);
	                    	factionData.setValue(oldValue);
	                    }
	                    else
	                        removeFaction(factionID);
	                }
	            }
            }
            
            c=0;
            for(e=factions.elements();e.hasMoreElements();)
            {
                tickStatus=Tickable.STATUS_OTHER+(c++);
            	((Faction.FactionData)e.nextElement()).tick(ticking, tickID);
            }

            int num=charStats().numClasses();
			tickStatus=Tickable.STATUS_CLASS;
			for(c=0;c<num;c++)
				charStats().getMyClass(c).tick(ticking,tickID);
			tickStatus=Tickable.STATUS_RACE;
			charStats().getMyRace().tick(ticking,tickID);
			tickStatus=Tickable.STATUS_END;
			
			for(Tattoo tattoo: tattoos)
				if((tattoo!=null)
				&&(tattoo.tickDown>0))
				{
					if(tattoo.tickDown==1)
						delTattoo(tattoo);
					else
						tattoo.tickDown--;
				}
		}

		if(lastTickedDateTime>=0) lastTickedDateTime=System.currentTimeMillis();
		tickStatus=Tickable.STATUS_NOT;
		return !pleaseDestroy;
	}

	public boolean isMonster()
	{
	    return (mySession==null)||(mySession.isFake());
	}
	public boolean isPossessing()
	{
	    try
	    {
			for(Session S : CMLib.sessions().allIterable())
		        if((S.mob()!=null)&&(S.mob().soulMate()==this))
		            return true;
	    }
	    catch(Exception e){}
	    return false;
	}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void addItem(Item item)
	{
		item.setOwner(this);
		inventory.addElement(item);
		item.recoverPhyStats();
	}
	public void addItem(Item item, ItemPossessor.Expire expire) { addItem(item);}
	
	public void delItem(Item item)
	{
		inventory.removeElement(item);
		item.recoverPhyStats();
	}
	public int numItems()
	{
		return inventory.size();
	}
	public Enumeration<Item> items() { return inventory.elements();}
	public boolean isContent(Item I) { return inventory.contains(I);}
	public List<Item> findItems(Item goodLocation, String itemName)
	{
		List<Item> items=CMLib.english().fetchAvailableItems(inventory,itemName,goodLocation,Wearable.FILTER_ANY,true);
		if(items.size()==0)
			items=CMLib.english().fetchAvailableItems(inventory,itemName,goodLocation,Wearable.FILTER_ANY,false);
		return items;
	}
	public Item getItem(int index)
	{
		try
		{
            return (Item)inventory.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Item fetchFromInventory(Item goodLocation,
                                   String itemName,
                                   int wornFilter,
                                   boolean allowCoins,
                                   boolean respectLocationAndWornCode)
	{
	    SVector inv=inventory;
	    if(!allowCoins)
	    {
	        inv=inv.copyOf();
	        for(int v=inv.size()-1;v>=0;v--)
	            if(inv.elementAt(v) instanceof Coins)
	                inv.removeElementAt(v);
	    }
        Item item=null;
        if(respectLocationAndWornCode)
        {
    		item=CMLib.english().fetchAvailableItem(inv,itemName,goodLocation,wornFilter,true);
    		if(item==null) item=CMLib.english().fetchAvailableItem(inv,itemName,goodLocation,wornFilter,false);
        }
        else
        {
            item=(Item)CMLib.english().fetchEnvironmental(inv,itemName,true);
            if(item==null) item=(Item)CMLib.english().fetchEnvironmental(inv,itemName,false);
        }
		return item;
	}
	public Item findItem(String itemName){ return fetchFromInventory(null,itemName,Wearable.FILTER_ANY,true,false);}
	public Item findItem(Item goodLocation, String itemName){ return fetchFromInventory(goodLocation,itemName,Wearable.FILTER_ANY,true,true);}
	public Item fetchCarried(Item goodLocation, String itemName){ return fetchFromInventory(goodLocation,itemName,Wearable.FILTER_UNWORNONLY,true,true);}
	public Item fetchWornItem(String itemName){ return fetchFromInventory(null,itemName,Wearable.FILTER_WORNONLY,true,true);}
	
	public List<Item> findItems(String itemName)
	{ 
        List V=CMLib.english().fetchEnvironmentals(inventory,itemName,true);
        if((V!=null)&&(V.size()>0)) return V;
        V=CMLib.english().fetchEnvironmentals(inventory,itemName,false);
        if(V!=null) return V;
        return new Vector(1);
	}

	public void addFollower(MOB follower, int order)
	{
		if(follower!=null)
		{
			if(followers==null) 
				followers=new SVector<Follower>();
			else
			for(Follower F : followers)
				if(F.follower==follower)
				{
					F.marchingOrder=order;
					return;
				}
			followers.add(new Follower(follower,order));
		}
	}

	public void delFollower(MOB follower)
	{
		if((follower!=null)&&(followers!=null))
		{
			for(Follower F : followers)
				if(F.follower==follower)
					followers.remove(F);
		}
	}
	public int numFollowers()
	{
		return (followers==null)?0:followers.size();
	}
	public int fetchFollowerOrder(MOB thisOne)
	{
		try
		{
			if(followers==null) 
				return -1;
			for(Follower F : followers)
				if(F.follower==thisOne)
					return F.marchingOrder;
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return -1;
	}
	public MOB fetchFollower(String named)
	{
		if(followers==null) 
			return null;
		List<MOB> list = new ConvertingList<Follower,MOB>(followers,Follower.converter);
		MOB mob=(MOB)CMLib.english().fetchEnvironmental(list,named,true);
		if(mob==null)
			mob=(MOB)CMLib.english().fetchEnvironmental(list,named,false);
		return mob;
	}
	public MOB fetchFollower(int index)
	{
		try
		{
			if(followers==null) 
				return null;
			return followers.get(index).follower;
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	
	public boolean isFollowedBy(MOB thisOne)
	{
		if(followers!=null) 
			for(Follower F : followers)
				if(F.follower==thisOne)
					return true;
		return false;
	}

	public boolean willFollowOrdersOf(MOB mob)
	{
        if((amFollowing()==mob)
        ||((isMonster()&&CMSecurity.isAllowed(mob,location(),"ORDER")))
        ||(getLiegeID().equals(mob.Name()))
        ||(CMLib.law().doesOwnThisProperty(mob,CMLib.map().getStartRoom(this))))
            return true;
        if((!isMonster())
        &&(CMSecurity.isAllowedEverywhere(mob,"ORDER"))
        &&((!CMSecurity.isASysOp(this))||CMSecurity.isASysOp(mob)))
            return true;
		if((getClanID().length()>0)&&(getClanID().equals(mob.getClanID())))
		{
			final Clan C=getMyClan();
			if(C!=null)
			{
				if((C.getAuthority(mob.getClanRole(),Clan.Function.ORDER_UNDERLINGS)!=Clan.Authority.CAN_NOT_DO)
				&&(C.doesOutRank(mob.getClanRole(), getClanRole())))
					return true;
				else
				if((isMonster())
				&&(C.getAuthority(mob.getClanRole(),Clan.Function.ORDER_CONQUERED)!=Clan.Authority.CAN_NOT_DO)
				&&(getStartRoom()!=null))
				{
					LegalBehavior B=CMLib.law().getLegalBehavior(getStartRoom());
					if((B!=null)&&(B.rulingOrganization().equals(mob.getClanID())))
						return true;
				}
			}
		}
		return false;
	}

    public MOB amUltimatelyFollowing()
    {
        MOB following=amFollowing;
        if(following==null) return null;
        HashSet seen=new HashSet();
        while((following!=null)&&(following.amFollowing()!=null)&&(!seen.contains(following)))
        {
            seen.add(following);
            following=following.amFollowing();
        }
        return following;
    }
	public MOB amFollowing()
	{
	    MOB following=amFollowing;
		if(following!=null)
		{
			if(!following.isFollowedBy(this))
				amFollowing=null;
		}
		return amFollowing;
	}
	public void setFollowing(MOB mob)
	{
		if((amFollowing!=null)&&(amFollowing!=mob))
		{
			if(amFollowing.isFollowedBy(this))
				amFollowing.delFollower(this);
		}
		if(mob!=null)
		{
			if(!mob.isFollowedBy(this))
				mob.addFollower(this,-1);
		}
		amFollowing=mob;
	}

	public Set<MOB> getRideBuddies(Set<MOB> list)
	{
		if(list==null) return list;
		if(!list.contains(this)) list.add(this);
		if(riding()!=null)
			riding().getRideBuddies(list);
		return list;
	}

	public Set<MOB> getGroupMembers(Set<MOB> list)
	{
		if(list==null) return list;
		if(!list.contains(this)) list.add(this);
		MOB following = amFollowing();
		if((following!=null)&&(!list.contains(following)))
			following.getGroupMembers(list);
		if(followers!=null)
			for(Follower F : followers)
				if((F.follower!=null)&&(!list.contains(F.follower)))
					F.follower.getGroupMembers(list);
		return list;
	}

	public boolean isSavable()
	{
		if((!isMonster())&&(soulMate()==null))
            return false;
        if(!CMLib.flags().isSavable(this))
            return false;
        if(CMLib.utensils().getMobPossessingAnother(this)!=null)
            return false;
		MOB followed=amFollowing();
		if(followed!=null)
			if(!followed.isMonster())
				return false;
		return true;

	}
	public void setSavable(boolean truefalse){ CMLib.flags().setSavable(this, truefalse);}

	public MOB soulMate()
	{
		return soulMate;
	}

	public void setSoulMate(MOB mob)
	{
		soulMate=mob;
	}

	public void addAbility(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numLearnedAbilities();a++)
		{
			Ability A=fetchAbility(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		abilitees.addElement(to);
	}
	public void delAbility(Ability to)
	{
		abilitees.removeElement(to);
	}
	public int numLearnedAbilities()
	{
		return abilitees.size();
	}
	
	public Enumeration<Ability> abilities()
	{
		final Clan C=getMyClan();
		final MultiEnumeration multi =
			new MultiEnumeration(
					new Enumeration[] {abilitees.elements(),new IteratorEnumeration(charStats().getMyRace().racialAbilities(this).iterator())});
		if(C!=null)
			multi.addEnumeration(new IteratorEnumeration(C.clanAbilities(this).iterator()));
		return multi;
	}
	
	public int numAbilities()
	{
		final Clan C=getMyClan();
		return abilitees.size()
			  +charStats().getMyRace().racialAbilities(this).size()
			  +((C==null)?0:C.clanAbilities(this).size());
	}

	public Ability fetchAbility(int index)
	{
		try
		{
			if(index<abilitees.size())
				return (Ability)abilitees.elementAt(index);
			final List<Ability> racialAbilities=charStats().getMyRace().racialAbilities(this);
			if(index<abilitees.size() + racialAbilities.size())
				return (Ability)charStats().getMyRace().racialAbilities(this).get(index-abilitees.size());
			final Clan C=getMyClan();
			return (C==null)?null:C.clanAbilities(this).get(index-abilitees.size()-racialAbilities.size());
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	
	public Ability fetchAbility(String ID)
	{
		for(Enumeration<Ability> a=abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		return null;
	}
	public Ability findAbility(String ID)
	{
		final Clan C=getMyClan();
		final Race R=charStats().getMyRace();
		Ability A=(Ability)CMLib.english().fetchEnvironmental(abilitees,ID,true);
        if(A==null) A=(Ability)CMLib.english().fetchEnvironmental(R.racialAbilities(this),ID,true);
        if((A==null)&&(C!=null)) A=(Ability)CMLib.english().fetchEnvironmental(C.clanAbilities(this),ID,true);
        if(A==null) A=(Ability)CMLib.english().fetchEnvironmental(abilitees,ID,false);
		if(A==null) A=(Ability)CMLib.english().fetchEnvironmental(R.racialAbilities(this),ID,false);
        if((A==null)&&(C!=null)) A=(Ability)CMLib.english().fetchEnvironmental(C.clanAbilities(this),ID,false);
		if(A==null) A=fetchAbility(ID);
		return A;
	}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addPriorityEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		if(affects.size()==0)
			affects.addElement(to);
		else
			affects.insertElementAt(to,0);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) 
			return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		if(affects.removeElement(to))
			to.setAffectedOne(null);
	}

	public int numAllEffects()
	{
		final Clan C=getMyClan();
		return affects.size()
			  +charStats().getMyRace().numRacialEffects(this)
			  +((C==null)?0:C.numClanEffects(this));
	}

	public int numEffects()
	{
		return affects.size();
	}
	
	public Ability fetchEffect(int index)
	{
		try
		{
			if(index<affects.size())
				return affects.elementAt(index);
			if(index<abilitees.size() + charStats().getMyRace().numRacialEffects(this))
				return charStats().getMyRace().racialEffects(this).get(index-affects.size());
			final Clan C=getMyClan();
			return (C==null)?null:C.clanAbilities(this).get(index-affects.size()-charStats().getMyRace().numRacialEffects(this));
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
        Ability A=null;
		for(final Enumeration<Ability> a = effects();a.hasMoreElements();)
		{
			A=a.nextElement();
			if((A!=null)&&(A.ID().equals(ID)))
				return A;
		}
		return null;
	}
	
	public Enumeration<Ability> personalEffects()
	{
		return affects.elements();
	}
	
	public Enumeration<Ability> effects()
	{
		final Clan C=getMyClan();
		final MultiEnumeration multi =
			new MultiEnumeration(
					new Enumeration[] {affects.elements(),new IteratorEnumeration(charStats().getMyRace().racialEffects(this).iterator())});
		if(C!=null)
			multi.addEnumeration(new IteratorEnumeration(C.clanEffects(this).iterator()));
		return multi;
	}
	
	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		if(fetchBehavior(to.ID())!=null) return;
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
    public Enumeration<Behavior> behaviors() { return behaviors.elements();}
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		for(Behavior B : behaviors)
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		return null;
	}

	private void clearExpertiseCache()
	{
		Ability A=null;
        for(Enumeration<Ability> a=abilities();a.hasMoreElements();)
        {
            A=a.nextElement();
			if(A!=null) A.clearExpertiseCache();
		}
		for(final Enumeration<Ability> a = personalEffects();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(A!=null) A.clearExpertiseCache();
		}
	}
	
	/** Manipulation of the expertise list */
	public void addExpertise(String of)
	{
		if(fetchExpertise(of)!=null)
			return;
		expertises.add(of);
		clearExpertiseCache();
	}
	public void delExpertise(String of)
	{
		of=fetchExpertise(of);
		if(of!=null)
		{
			if(expertises.remove(of))
				clearExpertiseCache();
		}
	}
	public int numExpertises(){return expertises.size();}
	
	public Enumeration<String> uniqueExpertises()
	{
		try{
			if(expertises.size()==0) 
				return expertises.elements();
			SVector<String> exCopy=expertises.copyOf();
			String exper=null, experRoot=null, expTest=null;
			int num=-1,end=-1,num2=-1;
			HashSet remove=new HashSet();
			for(int i1=exCopy.size()-1;i1>=0;i1--)
			{
				exper=(String)exCopy.elementAt(i1);
				if((exper.length()==0)||(remove.contains(exper))) continue;
				end=exper.length();
				while(Character.isDigit(exper.charAt(end-1))) end--;
				if(end<exper.length())
				{
					num=CMath.s_int(exper.substring(end));
					experRoot=exper.substring(0,end);
					for(int i2=i1-1;i2>=0;i2--)
					{
						expTest=(String)exCopy.elementAt(i2);
						if((!remove.contains(expTest))
						&&(expTest.startsWith(experRoot))
						&&(CMath.isInteger(expTest.substring(experRoot.length()))))
						{
							num2=CMath.s_int(expTest.substring(experRoot.length()));
							if(num<num2)
							{
								remove.add(exper);
								exper=expTest;
								num=num2;
							}
							else
								remove.add(expTest);
						}
					}
				}
                else
                {
                    ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(exper);
                    if(def==null) continue;
                    int x=def.name.lastIndexOf(' ');
                    if(x<0) continue;
                    if(CMath.isRomanNumeral(def.name.substring(x+1).trim())
                    &&(exper.endsWith(def.name.substring(x+1).trim())))
                    {
                        num=CMath.convertFromRoman(def.name.substring(x+1).trim());
                        experRoot=exper.substring(0,exper.length()-def.name.substring(x+1).trim().length());
                        for(int i2=i1-1;i2>=0;i2--)
                        {
                            expTest=(String)exCopy.elementAt(i2);
                            if((!remove.contains(expTest))
                            &&(expTest.startsWith(experRoot))
                            &&(CMath.isRomanNumeral(expTest.substring(experRoot.length()))))
                            {
                                num2=CMath.convertFromRoman(expTest.substring(experRoot.length()));
                                if(num<num2)
                                {
                                    remove.add(exper);
                                    exper=expTest;
                                    num=num2;
                                }
                                else
                                    remove.add(expTest);
                            }
                        }
                    }
                }
			}
			exCopy.removeAll(remove);
			return exCopy.elements();
		}catch(Exception e){}
		return expertises.elements();
	}
	public String fetchExpertise(int x){try{return (String)expertises.elementAt(x);}catch(Exception e){} return null;}
	public String fetchExpertise(String of){
		try{
            String X=null;
			for(int i=0;i<numExpertises();i++)
            {
			    X=fetchExpertise(i);
                if((X!=null)&&(X.equalsIgnoreCase(of)))
                    return X;
            }
		}catch(Exception e){}
		return null;
	}

    /** Manipulation of the scripts list */
    public void addScript(ScriptingEngine S)
    {
        if(S==null) return;
        if(!scripts.contains(S)) 
        {
            for(ScriptingEngine S2 : scripts)
                if(S2.getScript().equalsIgnoreCase(S.getScript()))
                    return;
	        scripts.addElement(S);
        }
    }
    public void delScript(ScriptingEngine S)
    {
        if(S==null) return;
    	scripts.removeElement(S);
    }
    public int numScripts(){return (scripts==null)?0:scripts.size();}
    public Enumeration<ScriptingEngine> scripts() { return (scripts==null)?EmptyEnumeration.INSTANCE:scripts.elements();}
    public ScriptingEngine fetchScript(int x){try{return (ScriptingEngine)scripts.elementAt(x);}catch(Exception e){} return null;}

	/** Manipulation of the tatoo list */
	public void addTattoo(Tattoo of)
	{
		if((of==null)
		||(of.tattooName==null)
		||(of.tattooName.length()==0)
		||findTattoo(of.tattooName)!=null)
			return;
		tattoos.addElement(of);
	}
	public void delTattoo(Tattoo of)
	{
		if((of==null)
		||(of.tattooName==null)
		||(of.tattooName.length()==0))
			return;
		Tattoo tat = findTattoo(of.tattooName); 
		if(tat==null)
			return;
		tattoos.remove(tat);
	}
	public Enumeration<Tattoo> tattoos() { return tattoos.elements();}
	public Tattoo findTattoo(String of)
	{
		if((of==null)||(of.length()==0)) 
			return null;
		of=of.toUpperCase().trim();
		for(Tattoo s : tattoos)
			if(s.tattooName.equals(of))
				return s;
		return null;
	}

    /** Manipulation of the factions list */
    public void addFaction(String which, int start)
    {
        which=which.toUpperCase();
        Faction F=CMLib.factions().getFaction(which);
        if(F==null) return;
        if(start>F.maximum()) start=F.maximum();
        if(start<F.minimum()) start=F.minimum();
        Faction.FactionData data=factions.get(which);
        if(data==null)
        {
            data=F.makeFactionData(this);
        	factions.put(which,data);
        }
        data.setValue(start);
    }
    public void adjustFaction(String which, int amount)
    {
        which=which.toUpperCase();
        Faction F=CMLib.factions().getFaction(which);
        if(F==null) return;
        if(!factions.containsKey(which))
            addFaction(which,amount);
        else
            addFaction(which,fetchFaction(which) + amount);
    }
    public Enumeration<String> fetchFactions()
    {
        return factions.keys();
    }
    public int fetchFaction(String which)
    {
    	Faction.FactionData data=factions.get(which.toUpperCase());
    	if(data == null)  return Integer.MAX_VALUE;
    	return data.value();
    }
    public void removeFaction(String which)
    {
    	factions.remove(which.toUpperCase());
    }
    public void copyFactions(MOB source)
    {
        for(Enumeration e=source.fetchFactions();e.hasMoreElements();)
        {
            String fID=(String)e.nextElement();
            addFaction(fID,source.fetchFaction(fID));
        }
    }
    public boolean hasFaction(String which)
    {
        Faction F=CMLib.factions().getFaction(which);
        if(F==null) return false;
        return factions.containsKey(F.factionID().toUpperCase());
    }
    public List<String> fetchFactionRanges()
    {
        Faction F=null;
        Faction.FactionRange FR=null;
        Vector V=new Vector();
        for(Enumeration e=fetchFactions();e.hasMoreElements();)
        {
            F=CMLib.factions().getFaction((String)e.nextElement());
            if(F==null) continue;
            FR=CMLib.factions().getRange(F.factionID(),fetchFaction(F.factionID()));
            if(FR!=null) V.addElement(FR.codeName());
        }
        return V;
    }

	public int freeWearPositions(long wornCode, short belowLayer, short layerAttributes)
	{
		int x=getWearPositions(wornCode);
		if(x<=0) return 0;
		x-=fetchWornItems(wornCode,belowLayer,layerAttributes).size();
		if(x<=0) return 0;
		return x;
	}
	public int getWearPositions(long wornCode)
	{
		if((charStats().getWearableRestrictionsBitmap()&wornCode)>0)
			return 0;
		if(wornCode==Wearable.WORN_FLOATING_NEARBY)
			return 6;
		int total;
		int add=0;
		boolean found=false;
		for(int i=0;i<Race.BODY_WEARGRID.length;i++)
		{
			if((Race.BODY_WEARGRID[i][0]>0)
			&&((Race.BODY_WEARGRID[i][0]&wornCode)==wornCode))
			{
				found=true;
				total=charStats().getBodyPart(i);
				if(Race.BODY_WEARGRID[i][1]<0)
				{
					if(total>0) return 0;
				}
				else
				if(total<1)
				{
					return 0;
				}
				else
				if(i==Race.BODY_HAND)
				{
					// casting is ok here since these are all originals
					// that fall below the int/long fall.
					if(wornCode>Integer.MAX_VALUE)
						add+=total;
					else
					switch((int)wornCode)
					{
					case (int)Wearable.WORN_HANDS:
						if(total<2)
							add+=1;
						else
							add+=total/2;
						break;
					case (int)Wearable.WORN_WIELD:
					case (int)Wearable.WORN_RIGHT_FINGER:
					case (int)Wearable.WORN_RIGHT_WRIST:
						add+=1; break;
					case (int)Wearable.WORN_HELD:
					case (int)Wearable.WORN_LEFT_FINGER:
					case (int)Wearable.WORN_LEFT_WRIST:
						add+=total-1;
						break;
					default:
						add+=total; break;
					}
				}
				else
				{
					int num=total/((int)Race.BODY_WEARGRID[i][1]);
					if(num<1)
						add+=1;
					else
						add+=num;
				}
			}
		}
		if(!found) return 1;
		return add;
	}

	public List<Item> fetchWornItems(long wornCode, short aboveOrAroundLayer, short layerAttributes)
	{
		Vector V=new Vector();
		boolean equalOk=(layerAttributes&Armor.LAYERMASK_MULTIWEAR)>0;
		int lay=0;
		for(int i=0;i<numItems();i++)
		{
			Item thisItem=getItem(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(wornCode)))
			{
				if(thisItem instanceof Armor)
				{
					lay=((Armor)thisItem).getClothingLayer();
					if(lay>=(aboveOrAroundLayer-1))
					{
						if(((lay>aboveOrAroundLayer-2)
							&&(lay<aboveOrAroundLayer+2)
							&&((!equalOk)||((((Armor)thisItem).getLayerAttributes()&Armor.LAYERMASK_MULTIWEAR)==0)))
						||(lay>aboveOrAroundLayer))
							V.addElement(thisItem);
					}
				}
				else
					V.addElement(thisItem);
			}
		}
		return V;
	}
    
    public boolean hasOnlyGoldInInventory()
    {
        for(int i=0;i<numItems();i++)
        {
            Item I=getItem(i);
            if(I.amWearingAt(Wearable.IN_INVENTORY)
            &&((I.container()==null)||(I.ultimateContainer().amWearingAt(Wearable.IN_INVENTORY)))
            &&(!(I instanceof Coins)))
                return false;
        }
        return true;
    }
    
	public Item fetchFirstWornItem(long wornCode)
	{
		for(int i=0;i<numItems();i++)
		{
			Item thisItem=getItem(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(wornCode)))
				return thisItem;
		}
		return null;
	}

	public Item fetchWieldedItem()
	{
		if((possibleWeaponIndex>=0)&&(possibleWeaponIndex<numItems()))
		{
			Item thisItem=getItem(possibleWeaponIndex);
			if((thisItem!=null)&&(thisItem.amWearingAt(Wearable.WORN_WIELD)))
				return thisItem;
		}
		for(possibleWeaponIndex=0;possibleWeaponIndex<numItems();possibleWeaponIndex++)
		{
			Item thisItem=getItem(possibleWeaponIndex);
			if((thisItem!=null)&&(thisItem.amWearingAt(Wearable.WORN_WIELD)))
				return thisItem;
		}
		possibleWeaponIndex=-1;
		return null;
	}

	public boolean isMine(Environmental env)
	{
		if(env instanceof Item)
		{
			if(inventory.contains(env)) return true;
			return false;
		}
		else
		if(env instanceof MOB)
		{
			if(isFollowedBy((MOB)env))
				return true;
			return false;
		}
		else
		if(env instanceof Ability)
		{
			if(abilitees.contains(env)) 
				return true;
			if(affects.contains(env)) 
				return true;
			return false;
		}
		return false;
	}

	public void moveItemTo(Item container, ItemPossessor.Expire expire, Move... moveFlags) { moveItemTo(container); }
	
	public void moveItemTo(Item container)
	{
		// caller is responsible for recovering any env
		// stat changes!
		if(CMLib.flags().isHidden(container))
			container.basePhyStats().setDisposition(container.basePhyStats().disposition()&((int)PhyStats.ALLMASK-PhyStats.IS_HIDDEN));

		// ensure its out of its previous place
		Environmental owner=location();
		if(container.owner()!=null)
		{
			owner=container.owner();
			if(container.owner() instanceof Room)
				((Room)container.owner()).delItem(container);
			else
			if(container.owner() instanceof MOB)
				((MOB)container.owner()).delItem(container);
		}
		location().delItem(container);

		container.unWear();

		if(!isMine(container))
			addItem(container);
		container.recoverPhyStats();

		boolean nothingDone=true;
		boolean doBugFix = true;
		while(doBugFix || !nothingDone)
		{
			doBugFix=false;
			nothingDone=true;
			if(owner instanceof Room)
			{
				Room R=(Room)owner;
				for(int i=0;i<R.numItems();i++)
				{
					Item thisItem=R.getItem(i);
					if((thisItem!=null)&&(thisItem.container()==container))
					{
						moveItemTo(thisItem);
						nothingDone=false;
						break;
					}
				}
			}
			else
			if(owner instanceof MOB)
			{
				MOB M=(MOB)owner;
				for(int i=0;i<M.numItems();i++)
				{
					Item thisItem=M.getItem(i);
					if((thisItem!=null)&&(thisItem.container()==container))
					{
						moveItemTo(thisItem);
						nothingDone=false;
						break;
					}
				}
			}
		}
	}

	protected static String[] CODES={"CLASS","LEVEL","ABILITY","TEXT"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+basePhyStats().level();
		case 2: return ""+basePhyStats().ability();
		case 3: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: basePhyStats().setLevel(CMath.s_parseIntExpression(val)); break;
		case 2: basePhyStats().setAbility(CMath.s_parseIntExpression(val)); break;
		case 3: setMiscText(val); break;
		}
	}
    public int getSaveStatIndex(){return (xtraValues==null)?getStatCodes().length:getStatCodes().length-xtraValues.length;}
	public String[] getStatCodes(){return CODES;}
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
    public boolean sameAs(Environmental E)
    {
        if(!(E instanceof StdMOB)) return false;
        String[] codes=getStatCodes();
        for(int i=0;i<codes.length;i++)
            if(!E.getStat(codes[i]).equals(getStat(codes[i])))
                return false;
        return true;
    }
}
