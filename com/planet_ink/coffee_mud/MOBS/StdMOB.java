package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;


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
public class StdMOB implements MOB
{
	public String ID(){return "StdMOB";}
	protected String Username="";

	private String clanID=null;
	private int clanRole=0;

	protected CharStats baseCharStats=(CharStats)CMClass.getCommon("DefaultCharStats");
	protected CharStats charStats=(CharStats)CMClass.getCommon("DefaultCharStats");

	protected EnvStats envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
	protected EnvStats baseEnvStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");

	protected PlayerStats playerStats=null;

	protected boolean amDead=false;
	protected Room location=null;
	protected Room lastLocation=null;
	protected Rideable riding=null;

	protected Session mySession=null;
	protected boolean pleaseDestroy=false;
	protected byte[] description=null;
	protected String displayText="";
	protected String imageName=null;
	protected byte[] miscText=null;

	protected long tickStatus=Tickable.STATUS_NOT;

	/* containers of items and attributes*/
	protected Vector inventory=new Vector();
	protected DVector followers=null;
	protected Vector abilities=new Vector();
	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();
	protected Vector tattoos=new Vector();
	protected Vector educations=new Vector();
    protected Hashtable factions=new Hashtable();

	protected DVector commandQue=new DVector(2);

	// gained attributes
	protected int Experience=0;
	protected int ExpNextLevel=1000;
	protected int Practices=0;
	protected int Trains=0;
	protected long AgeHours=0;
	protected int Money=0;
	protected int attributesBitmap=MOB.ATT_NOTEACH;
    
    protected int tickCounter=0;
    private long lastMoveTime=0;
    private int movesSinceTick=0;
    private int manaConsumeCounter=CMLib.dice().roll(1,10,0);

    // the core state values
    public CharState curState=(CharState)CMClass.getCommon("DefaultCharState");
    public CharState maxState=(CharState)CMClass.getCommon("DefaultCharState");
    public CharState baseState=(CharState)CMClass.getCommon("DefaultCharState");
    private long lastTickedDateTime=0;
    public long lastTickedDateTime(){return lastTickedDateTime;}
    public void flagVariableEq(){lastTickedDateTime=-2;}

    // mental characteristics
    protected int Alignment=0;
    protected String WorshipCharID="";
    protected String LiegeID="";
    protected int WimpHitPoint=0;
    protected int QuestPoint=0;
    protected int DeityIndex=-1;

    // location!
    protected Room StartRoom=null;
    
    protected MOB victim=null;
    protected MOB amFollowing=null;
    protected MOB soulMate=null;
    private double speeder=0.0;
    protected int atRange=-1;
    private long peaceTime=0;
    
    public long getAgeHours(){return AgeHours;}
	public int getPractices(){return Practices;}
	public int getExperience(){return Experience;}
	public int getExpNextLevel(){return ExpNextLevel;}
	public int getExpNeededLevel()
	{
		if(!isMonster())
		{
			if((CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)>0)
			&&(CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)<=baseEnvStats().level()))
				return Integer.MAX_VALUE;
		}
		if((CMSecurity.isDisabled("EXPERIENCE"))
		||(charStats().getCurrentClass().expless())
		||(charStats().getMyRace().expless()))
		    return Integer.MAX_VALUE;
		if(ExpNextLevel<=getExperience())
			ExpNextLevel=getExperience()+1000;
		return ExpNextLevel-getExperience();
	}
	public int getTrains(){return Trains;}
	public int getMoney(){return Money;}
	public int getBitmap(){return attributesBitmap;}
	public void setAgeHours(long newVal){ AgeHours=newVal;}
	public void setExperience(int newVal){ Experience=newVal; }
	public void setExpNextLevel(int newVal){ ExpNextLevel=newVal;}
	public void setPractices(int newVal){ Practices=newVal;}
	public void setTrains(int newVal){ Trains=newVal;}
	public void setMoney(int newVal){ Money=newVal;}
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

	public String getLiegeID(){return LiegeID;}
	public String getWorshipCharID(){return WorshipCharID;}
	public int getWimpHitPoint(){return WimpHitPoint;}
	public int getQuestPoint(){return QuestPoint;}
	public void setLiegeID(String newVal){LiegeID=newVal;}
	public void setWorshipCharID(String newVal){ WorshipCharID=newVal;}
	public void setWimpHitPoint(int newVal){ WimpHitPoint=newVal;}
	public void setQuestPoint(int newVal){ QuestPoint=newVal;}
	public Deity getMyDeity()
	{
		if(getWorshipCharID().length()==0) return null;
		Deity bob=CMLib.map().getDeity(getWorshipCharID());
		if(bob==null)
			setWorshipCharID("");
		return bob;
	}

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
	
	public Room getStartRoom(){return StartRoom;}
	public void setStartRoom(Room newVal){StartRoom=newVal;}


	public long peaceTime(){return peaceTime;}

	public String Name()
	{
		return Username;
	}
	public void setName(String newName){Username=newName;}
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return Username;
	}
	public String titledName()
	{
	    if((playerStats==null)||(playerStats.getTitles().size()==0))
	        return name();
	    return Util.replaceAll((String)playerStats.getTitles().firstElement(),"*",Name());
	}

	public String image()
    {
        if(imageName==null)
            imageName=CMProps.getDefaultMXPImage(this);
        if(!baseCharStats().getMyRace().name().equalsIgnoreCase(charStats().raceName()))
            return CMProps.getDefaultMXPImage(this);
        if(imageName==null) return "";
        return imageName;
    }
    public String rawImage()
    {
        if(imageName==null) 
            return "";
        return imageName;
    }
	public void setImage(String newImage)
    {
        if((newImage==null)||(newImage.trim().length()==0))
            imageName=null;
        else
            imageName=newImage;
    }
	
	public StdMOB()
	{
        super();
        CMClass.bumpCounter(CMClass.OBJECT_MOB);
		baseCharStats().setMyRace(CMClass.getRace("Human"));
		baseEnvStats().setLevel(1);
	}
    protected void finalize(){CMClass.unbumpCounter(CMClass.OBJECT_MOB);}

	protected void cloneFix(MOB E)
	{
		affects=new Vector();
		baseEnvStats=(EnvStats)E.baseEnvStats().copyOf();
		envStats=(EnvStats)E.envStats().copyOf();
		baseCharStats=(CharStats)E.baseCharStats().copyOf();
		charStats=(CharStats)E.charStats().copyOf();
		baseState=(CharState)E.baseState().copyOf();
		curState=(CharState)E.curState().copyOf();
		maxState=(CharState)E.maxState().copyOf();

		pleaseDestroy=false;

		inventory=new Vector();
		followers=null;
		abilities=new Vector();
		affects=new Vector();
		behaviors=new Vector();
		for(int i=0;i<E.inventorySize();i++)
		{
			Item I2=E.fetchInventory(i);
			if(I2!=null)
			{
				Item I=(Item)I2.copyOf();
				I.setOwner(this);
				inventory.addElement(I);
			}
		}
		for(int i=0;i<inventorySize();i++)
		{
			Item I2=fetchInventory(i);
			if((I2!=null)
			&&(I2.container()!=null)
			&&(!isMine(I2.container())))
				for(int ii=0;ii<E.inventorySize();ii++)
					if((E.fetchInventory(ii)==I2.container())&&(ii<inventorySize()))
					{ I2.setContainer(fetchInventory(ii)); break;}
		}
		for(int i=0;i<E.numLearnedAbilities();i++)
		{
			Ability A2=E.fetchAbility(i);
			if(A2!=null)
				abilities.addElement(A2.copyOf());
		}
		for(int i=0;i<E.numEffects();i++)
		{
			Ability A=E.fetchEffect(i);
			if((A!=null)&&(!A.canBeUninvoked()))
				addEffect((Ability)A.copyOf());
		}
		for(int i=0;i<E.numBehaviors();i++)
		{
			Behavior B=E.fetchBehavior(i);
			if(B!=null)
				behaviors.addElement(B.copyOf());
		}
	}
	public CMObject copyOf()
	{
		try
		{
			StdMOB E=(StdMOB)this.clone();
            CMClass.bumpCounter(CMClass.OBJECT_MOB);
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public boolean isGeneric(){return false;}
	public EnvStats envStats()
	{
		return envStats;
	}

	public EnvStats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=(EnvStats)baseEnvStats.copyOf();
		if(location()!=null)
			location().affectEnvStats(this,envStats);
		envStats().setWeight(envStats().weight()+(int)Math.round(Util.div(getMoney(),100.0)));
		if(riding()!=null) riding().affectEnvStats(this,envStats);
		if(getMyDeity()!=null) getMyDeity().affectEnvStats(this,envStats);
		if(charStats!=null)
		{
			for(int c=0;c<charStats().numClasses();c++)
				charStats().getMyClass(c).affectEnvStats(this,envStats);
			charStats().getMyRace().affectEnvStats(this,envStats);
		}

		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if(item!=null)
			{
				item.recoverEnvStats();
				item.affectEnvStats(this,envStats);
			}
		}
		for(int a=0;a<numAllEffects();a++)
		{
			Ability effect=fetchEffect(a);
			if(effect!=null)
				effect.affectEnvStats(this,envStats);
		}
		/* the follower light exception*/
		if(!CMLib.flags().isLightSource(this))
		for(int f=0;f<numFollowers();f++)
			if(CMLib.flags().isLightSource(fetchFollower(f)))
				envStats.setDisposition(envStats().disposition()|EnvStats.IS_LIGHTSOURCE);
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=(EnvStats)newBaseEnvStats.copyOf();
	}

	public int baseWeight()
	{
		if(charStats().getMyRace()==baseCharStats().getMyRace())
			return baseEnvStats().weight();
		return charStats().getMyRace().getMaxWeight();
	}
	
	public int maxCarry()
	{
        if(CMSecurity.isAllowed(this,location(),"CARRYALL"))
            return Integer.MAX_VALUE/2;
		double str=new Integer(charStats().getStat(CharStats.STRENGTH)).doubleValue();
		double bodyWeight=new Integer(baseWeight()).doubleValue();
		return (int)Math.round(bodyWeight + ((str+10.0)*str*bodyWeight/150.0) + (str*5.0));
	}
    public int maxItems()
    {
        if(CMSecurity.isAllowed(this,location(),"CARRYALL"))
            return Integer.MAX_VALUE/2;
        return (2*Item.wornOrder.length)
                +(2*charStats().getStat(CharStats.DEXTERITY))
                +(2*envStats().level());
    }
	public int maxFollowers()
	{
		return ((int)Math.round(Util.div(charStats().getStat(CharStats.CHARISMA),4.0))+1);
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
		baseCharStats.setClassLevel(baseCharStats.getCurrentClass(),baseEnvStats().level()-baseCharStats().combinedSubLevels());
		charStats=(CharStats)baseCharStats().copyOf();

		if(riding()!=null) riding().affectCharStats(this,charStats);
		if(getMyDeity()!=null) getMyDeity().affectCharStats(this,charStats);
		for(int a=0;a<numAllEffects();a++)
		{
			Ability effect=fetchEffect(a);
			if(effect!=null)
				effect.affectCharStats(this,charStats);
		}
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if(item!=null)
				item.affectCharStats(this,charStats);
		}
		if(location()!=null)
			location().affectCharStats(this,charStats);

		for(int c=0;c<charStats.numClasses();c++)
			charStats.getMyClass(c).affectCharStats(this,charStats);
		charStats.getMyRace().affectCharStats(this,charStats);
		baseCharStats.getMyRace().agingAffects(this,baseCharStats,charStats);
	    if((playerStats!=null)&&(soulMate==null)&&(playerStats.getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
	    {
	        int chaAdjust=(int)(playerStats.getHygiene()/PlayerStats.HYGIENE_DELIMIT);
	        if((charStats.getStat(CharStats.CHARISMA)/2)>chaAdjust)
		        charStats.setStat(CharStats.CHARISMA,charStats.getStat(CharStats.CHARISMA)-chaAdjust);
	        else
		        charStats.setStat(CharStats.CHARISMA,charStats.getStat(CharStats.CHARISMA)/2);
	    }
	}
	
	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=(CharStats)newBaseCharStats.copyOf();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof Room)
		{
			if(CMLib.flags().isLightSource(this))
			{
				if(CMLib.flags().isInDark(affected))
					affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
			}
		}
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}
	public boolean isMarriedToLiege()
	{
		if(getLiegeID().length()==0) return false;
		if(getLiegeID().equals(Name())) return false;
		MOB M=CMLib.map().getLoadPlayer(getLiegeID());
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
		curState=(CharState)maxState.copyOf();
	}
	public void recoverMaxState()
	{
		maxState=(CharState)baseState.copyOf();
		if(charStats.getMyRace()!=null)	charStats.getMyRace().affectCharState(this,maxState);
		if(riding()!=null) riding().affectCharState(this,maxState);
		for(int a=0;a<numAllEffects();a++)
		{
			Ability effect=fetchEffect(a);
			if(effect!=null)
				effect.affectCharState(this,maxState);
		}
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if(item!=null)
				item.affectCharState(this,maxState);
		}
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
                CMLib.commands().look(mate,true);
            }
            setSoulMate(null);
        }
    }

	public void destroy()
	{
        if(soulMate()!=null) dispossess(false);
        MOB possessor=CMLib.utensils().getMobPossessingAnother(this);
        if(possessor!=null) possessor.dispossess(false);
        if(session()!=null){ session().setKillFlag(true); try{Thread.sleep(1000);}catch(Exception e){}}
		removeFromGame(session()!=null);
		while(numBehaviors()>0)
			delBehavior(fetchBehavior(0));
		while(numEffects()>0)
			delEffect(fetchEffect(0));
		while(numLearnedAbilities()>0)
			delAbility(fetchAbility(0));
		while(inventorySize()>0)
		{
			Item I=fetchInventory(0);
			I.destroy();
			delInventory(I);
		}
        CMLib.threads().deleteTick(this,-1);
        clanID=null;
        charStats=baseCharStats;
        envStats=baseEnvStats;
        playerStats=null;
        location=null;
        lastLocation=null;
        riding=null;
        mySession=null;
        imageName=null;
        inventory=new Vector();
        followers=null;
        abilities=new Vector();
        affects=new Vector();
        behaviors=new Vector();
        tattoos=new Vector();
        educations=new Vector();
        factions=new Hashtable();
        commandQue=new DVector(2);
        curState=maxState;
        WorshipCharID="";
        LiegeID="";
        victim=null;
        amFollowing=null;
        soulMate=null;
        StartRoom=null;
	}

	public void removeFromGame(boolean preserveFollowers)
	{
		pleaseDestroy=true;
		if((location!=null)&&(location.isInhabitant(this)))
        {
            location().delInhabitant(this);
            if(mySession!=null)
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
					oldFollowers.addElement(follower,new Integer(fetchFollowerOrder(follower)));
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
					newFol.baseEnvStats().setRejuv(0);
					newFol.text();
					follower.killMeDead(false);
					addFollower(newFol, ((Integer)oldFollowers.elementAt(f,2)).intValue());
				}
			}
            if(session()!=null)
    			session().setKillFlag(true);
		}
		setRiding(null);
	}

	public String getClanID(){return ((clanID==null)?"":clanID);}
	public void setClanID(String clan){clanID=clan;}
	public int getClanRole(){return clanRole;}
	public void setClanRole(int role){clanRole=role;}

	public void bringToLife()
	{
		amDead=false;
		pleaseDestroy=false;

		// will ensure no duplicate ticks, this obj, this id
		CMLib.threads().startTickDown(this,MudHost.TICK_MOB,1);
		if(tickStatus==Tickable.STATUS_NOT)
			tick(this,MudHost.TICK_MOB); // slap on the butt
	}
	
	public void bringToLife(Room newLocation, boolean resetStats)
	{
		amDead=false;
        Room origStartRoom=getStartRoom();
		if((miscText!=null)&&(resetStats)&&(isGeneric()))
		{
			if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBCOMPRESS))
				CMLib.coffeeMaker().resetGenMOB(this,CMLib.coffeeMaker().getGenMOBTextUnpacked(this,CMLib.encoder().decompressString(miscText)));
			else
				CMLib.coffeeMaker().resetGenMOB(this,CMLib.coffeeMaker().getGenMOBTextUnpacked(this,new String(miscText)));
		}
		if(getStartRoom()==null)
			setStartRoom(isMonster()?newLocation:CMLib.map().getStartRoom(this));
		setLocation(newLocation);
		if(location()==null)
		{
			setLocation(getStartRoom());
			if(location()==null)
			{
				Log.errOut("StdMOB",Username+" cannot get a location.");
				return;
			}
		}
		if(!location().isInhabitant(this))
			location().addInhabitant(this);
		pleaseDestroy=false;

		// will ensure no duplicate ticks, this obj, this id
		CMLib.threads().startTickDown(this,MudHost.TICK_MOB,1);
		if(tickStatus==Tickable.STATUS_NOT)
			tick(this,MudHost.TICK_MOB); // slap on the butt

        Ability A=null;
		for(int a=0;a<numLearnedAbilities();a++)
		{
			A=fetchAbility(a);
			if(A!=null) A.autoInvocation(this);
		}
        if(location()==null)
        {
            Log.errOut("StdMOB","Object: "+this+", was probably destroyed very recently! Life call details below:");
            try{int x=4/0;x=x/0;}catch(Exception e){Log.errOut("StdMOB",e);}
            Log.errOut("StdMOB","Original Start room was: "+CMLib.map().getExtendedRoomID(origStartRoom));
            destroy();
            return;
        }
		location().recoverRoomStats();
		if((!isGeneric())&&(resetStats))
			resetToMaxState();
		Faction F=null;
		for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
		{
		    F=(Faction)e.nextElement();
		    if((!F.hasFaction(this))&&(F.findAutoDefault(this)!=Integer.MAX_VALUE))
		        addFaction(F.factionID(),F.findAutoDefault(this));
		}
		    
		if(CMLib.flags().isSleeping(this))
			tell("(You are asleep)");
		else
			CMLib.commands().look(this,true);
	}

	public boolean isInCombat()
	{
		if(victim==null) return false;
		if((victim.location()==null)
		||(location()==null)
		||(victim.location()!=location())
		||(victim.amDead()))
		{
			if((victim instanceof StdMOB)
			&&(((StdMOB)victim).victim==this))
				victim.setVictim(null);
			setVictim(null);
			return false;
		}
		return true;
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
		if(mob.isMonster()) return true;
		if(isMonster()){
			MOB fol=amFollowing();
			if((fol!=null)&&(!fol.isMonster()))
				return fol.mayIFight(mob);
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
		if(Util.bset(getBitmap(),MOB.ATT_PLAYERKILL))
		{
			if(CMSecurity.isAllowed(this,location(),"PKILL")
			||(Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
				return true;
			return false;
		}
		else
		if(Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL))
		{
			if(CMSecurity.isAllowed(this,location(),"PKILL")
			||(Util.bset(getBitmap(),MOB.ATT_PLAYERKILL)))
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
	public int adjustedAttackBonus(MOB mob)
	{
		double att=new Integer(
				envStats().attackAdjustment()
				+((charStats().getStat(CharStats.STRENGTH)-9)*3)).doubleValue();
		if(curState().getHunger()<1) att=att*.9;
		if(curState().getThirst()<1) att=att*.9;
		if(curState().getFatigue()>CharState.FATIGUED_MILLIS) att=att*.8;
		if(mob==null)
			return (int)Math.round(att);
	    int diff=envStats().level()-mob.envStats().level();
		return (diff*Util.abs(diff))
			   +(int)Math.round(att);
	}

	public int adjustedArmor()
	{
		double arm=new Integer(((charStats().getStat(CharStats.DEXTERITY)-9)*3)
							   +50).doubleValue();
		if((envStats().disposition()&EnvStats.IS_SLEEPING)>0) arm=0.0;
		if(arm>0.0)
		{
			if(curState().getHunger()<1) arm=arm*.85;
			if(curState().getThirst()<1) arm=arm*.85;
			if(curState().getFatigue()>CharState.FATIGUED_MILLIS) arm=arm*.85;
			if((envStats().disposition()&EnvStats.IS_SITTING)>0) arm=arm*.75;
		}
		return (int)Math.round(envStats().armor()-arm);
	}

	public int adjustedDamage(Weapon weapon, MOB target)
	{
		double damageAmount=0.0;
		if(target!=null)
		{
			if((weapon!=null)&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
				damageAmount = new Integer(CMLib.dice().roll(1, weapon.envStats().damage(),1)).doubleValue();
			else
				damageAmount = new Integer(CMLib.dice().roll(1, envStats().damage(), (charStats().getStat(CharStats.STRENGTH) / 3)-2)).doubleValue();
			if(!CMLib.flags().canBeSeenBy(target,this)) damageAmount *=.5;
			if(CMLib.flags().isSleeping(target)) damageAmount *=1.5;
			else
			if(CMLib.flags().isSitting(target)) damageAmount *=1.2;
		}
		else
		if((weapon!=null)&&((weapon.weaponClassification()==Weapon.CLASS_RANGED)||(weapon.weaponClassification()==Weapon.CLASS_THROWN)))
			damageAmount = new Integer(weapon.envStats().damage()+1).doubleValue();
		else
			damageAmount = new Integer(envStats().damage()+(charStats().getStat(CharStats.STRENGTH) / 3)-2).doubleValue();
		if(curState().getHunger() < 1) damageAmount *= .8;
		if(curState().getFatigue()>CharState.FATIGUED_MILLIS) damageAmount *=.8;
		if(curState().getThirst() < 1) damageAmount *= .9;
		if(damageAmount<1.0) damageAmount=1.0;
		return (int)Math.round(damageAmount);
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
				commandQue.clear();
		}
		if(victim==mob) return;
		if(mob==this) return;
        
		victim=mob;
		recoverEnvStats();
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
				mob.recoverCharStats();
				mob.recoverEnvStats();
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
			deathRoom=CMLib.map().getBodyRoom(this);
		if(location()!=null) location().delInhabitant(this);
		DeadBody Body=null;
		if(createBody)
			Body=charStats().getMyRace().getCorpseContainer(this,deathRoom);
		amDead=true;
		makePeace();
		setRiding(null);
		commandQue.clear();
		for(int a=numEffects()-1;a>=0;a--)
		{
			Ability A=fetchEffect(a);
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
			bringToLife(CMLib.map().getDeathRoom(this),true);
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
		if((ride!=null)&&(riding()!=null)&&(riding()==ride)&&(riding().amRiding(this)))
			return;
		if((riding()!=null)&&(riding().amRiding(this)))
			riding().delRider(this);
		riding=ride;
		if((riding()!=null)&&(!riding().amRiding(this)))
			riding().addRider(this);
	}
	public Session session()
	{
		return mySession;
	}
	public void setSession(Session newSession)
	{
		mySession=newSession;
		setBitmap(getBitmap());
	}
	public Weapon myNaturalWeapon()
	{
		if((charStats()!=null)&&(charStats().getMyRace()!=null))
			return charStats().getMyRace().myNaturalWeapon();
		return CMClass.getWeapon("Natural");
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
			if(!name().equals(Name()))
				sendBack=new StringBuffer(name());
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
			    Vector whoseAhead=CMLib.combat().getFormationFollowed(this);
			    if((whoseAhead!=null)&&(whoseAhead.size()>0))
			    {
				    sendBack.append(", behind ");
				    for(int v=0;v<whoseAhead.size();v++)
				    {
				        MOB ahead=(MOB)whoseAhead.elementAt(v);
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
			return new String(description);
	}
	public void setDescription(String newDescription)
	{
		if(newDescription.length()==0)
			description=null;
		else
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBDCOMPRESS))
			description=CMLib.encoder().compressString(newDescription);
		else
			description=newDescription.getBytes();
	}
	public void setMiscText(String newText)
	{
		if(newText.length()==0)
			miscText=null;
		else
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBCOMPRESS))
			miscText=CMLib.encoder().compressString(newText);
		else
			miscText=newText.getBytes();
	}
	public String text()
	{
		if((miscText==null)||(miscText.length==0))
			return "";
		else
		if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBCOMPRESS))
			return CMLib.encoder().decompressString(miscText);
		else
			return new String(miscText);
	}

	public String healthText()
	{
	    String mxp="^<!ENTITY vicmaxhp \""+maxState().getHitPoints()+"\"^>^<!ENTITY vichp \""+curState().getHitPoints()+"\"^>^<Health^>^<HealthText \""+name()+"\"^>";
		if((charStats()!=null)&&(charStats().getMyRace()!=null))
			return mxp+charStats().getMyRace().healthText(this)+"^</HealthText^>";
		return mxp+CMLib.combat().standardMobCondition(this)+"^</HealthText^>";
	}

	public void dequeCommand()
	{
		Vector returnable=null;
		synchronized(commandQue)
		{
			if(commandQue.size()>0)
			{
				Vector topCMD=(Vector)commandQue.elementAt(0,1);
				int topTick=((Integer)commandQue.elementAt(0,2)).intValue();
				commandQue.removeElementAt(0);
				if((--topTick)<2)
					returnable=topCMD;
				else
					commandQue.insertElementAt(0,topCMD,new Integer(topTick));
			}
		}
		if(returnable!=null)
			doCommand(CMLib.english().findCommand(this,returnable),returnable);
	}

	public void doCommand(Vector commands)
	{
		Object O=CMLib.english().findCommand(this,commands);
		if(O!=null) doCommand(O,commands);
	}

	private void doCommand(Object O, Vector commands)
	{
		try
		{
			if(O instanceof Command)
				((Command)O).execute(this,commands);
			else
			if(O instanceof Social)
				((Social)O).invoke(this,commands,null,false);
			else
			if(O instanceof Ability)
				CMLib.english().evoke(this,commands);
			else
				tell("Wha? Huh?");
		}
		catch(java.io.IOException io)
		{
			Log.errOut("StdMOB",Util.toStringList(commands));
			if((io!=null)&&(io.getMessage()!=null))
				Log.errOut("StdMOB",io.getMessage());
			else
				Log.errOut("StdMOB",io);
			tell("Oops!");
		}
		catch(Exception e)
		{
			Log.errOut("StdMOB",Util.toStringList(commands));
			Log.errOut("StdMOB",e);
			tell("Oops!");
		}
	}

	public void enqueCommand(Vector commands, int tickDelay)
	{
		if(commands==null) return;
		int tickDown=1;
		if(tickDelay<1)
		{
			Object O=CMLib.english().findCommand(this,commands);
			if(O==null){ tell("Huh?!"); return;}

			if(O instanceof Command)
				tickDown=((Command)O).ticksToExecute();
			else
			if(O instanceof Ability)
            {
				tickDown=isInCombat()?((Ability)O).combatCastingTime():((Ability)O).castingTime();
                if(!CMLib.english().preEvoke(this,commands))
                    return;
            }

			if(((!isInCombat())&&(tickDown<2))
			||(tickDown==0))
			{
				doCommand(O,commands);
				return;
			}
		}

		synchronized(commandQue)
		{
			if((tickDelay+1)>tickDown) tickDown=tickDelay+1;
			commandQue.addElement(commands,new Integer(tickDown));
		}
	}

	public void establishRange(MOB source, MOB target, Environmental tool)
	{
		// establish and enforce range
		if((source.rangeToTarget()<0))
		{
			if(source.riding()!=null)
			{
				if((target==riding())||(source.riding().amRiding(target)))
					source.setAtRange(0);
				else
				if((source.riding() instanceof MOB)
				   &&(((MOB)source.riding()).isInCombat())
				   &&(((MOB)source.riding()).getVictim()==target)
				   &&(((MOB)source.riding()).rangeToTarget()>=0)
				   &&(((MOB)source.riding()).rangeToTarget()<rangeToTarget()))
				{
					source.setAtRange(((MOB)source.riding()).rangeToTarget());
					recoverEnvStats();
					return;
				}
				else
				for(int r=0;r<source.riding().numRiders();r++)
				{
					Rider rider=source.riding().fetchRider(r);
					if(!(rider instanceof MOB)) continue;
					MOB otherMOB=(MOB)rider;
					if((otherMOB!=null)
					   &&(otherMOB!=this)
					   &&(otherMOB.isInCombat())
					   &&(otherMOB.getVictim()==target)
					   &&(otherMOB.rangeToTarget()>=0)
					   &&(otherMOB.rangeToTarget()<rangeToTarget()))
					{
						source.setAtRange(otherMOB.rangeToTarget());
						source.recoverEnvStats();
						return;
					}
				}
			}

			MOB follow=source.amFollowing();
			if((target.getVictim()==source)&&(target.rangeToTarget()>=0))
				source.setAtRange(target.rangeToTarget());
			else
			if((follow!=null)&&(follow.location()==location()))
			{
				int newRange=follow.fetchFollowerOrder(source);
				if(newRange<0)
				{
					if(follow.rangeToTarget()>=0)
					{
						newRange=follow.rangeToTarget();
						if(newRange<maxRange(tool))
							newRange=maxRange(tool);
					}
					else
						newRange=maxRange(tool);
				}
				else
				{
					if(follow.rangeToTarget()>=0)
						newRange=newRange+follow.rangeToTarget();
				}
				if((location()!=null)&&(location().maxRange()<newRange))
					newRange=location().maxRange();
				source.setAtRange(newRange);
			}
			else
				source.setAtRange(maxRange(tool));
			recoverEnvStats();
		}
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

		Ability A=null;
        for(int i=0;i<numAllEffects();i++)
		{
			A=fetchEffect(i);
			if((A!=null)&&(!A.okMessage(this,msg)))
				return false;
		}

		Item I=null;
        for(int i=inventorySize()-1;i>=0;i--)
		{
			I=fetchInventory(i);
			if((I!=null)&&(!I.okMessage(this,msg)))
				return false;
		}

		Behavior B=null;
        for(int b=0;b<numBehaviors();b++)
		{
			B=fetchBehavior(b);
			if((B!=null)&&(!B.okMessage(this,msg)))
				return false;
		}

		Faction F=null;
        for(Enumeration e=fetchFactions();e.hasMoreElements();) 
        {
            F=CMLib.factions().getFaction((String)e.nextElement());
            if ((F != null) && (!F.okMessage(this, msg)))
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

			if(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			{
				int srcMajor=msg.sourceMajor();

				if(amDead())
				{
					tell("You are DEAD!");
					return false;
				}

				if(Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
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
						establishRange(this,(MOB)msg.target(),msg.tool());
					}
				}


				if(Util.bset(srcMajor,CMMsg.MASK_EYES))
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
				if(Util.bset(srcMajor,CMMsg.MASK_MOUTH))
				{
					if(!CMLib.flags().aliveAwakeMobile(this,false))
						return false;
					if(Util.bset(srcMajor,CMMsg.MASK_SOUND))
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
				if(Util.bset(srcMajor,CMMsg.MASK_HANDS))
				{
					if((!CMLib.flags().canBeSeenBy(msg.target(),this))
					&&(!(isMine(msg.target())&&(msg.target() instanceof Item)))
					&&(!(isInCombat()&&(msg.target()==victim))))
					{
						mob.tell("You don't see '"+msg.target().name()+"' here.");
						return false;
					}
					if(!CMLib.flags().aliveAwakeMobile(this,false))
						return false;

					if((CMLib.flags().isSitting(this))
					&&(msg.sourceMinor()!=CMMsg.TYP_SITMOVE)
					&&(msg.sourceMinor()!=CMMsg.TYP_BUY)
					&&(msg.targetCode()!=CMMsg.MSG_OK_VISUAL)
					&&((msg.sourceMessage()!=null)||(msg.othersMessage()!=null))
					&&((!CMLib.utensils().reachableItem(this,msg.target()))
					||(!CMLib.utensils().reachableItem(this,msg.tool()))))
					{
						tell("You need to stand up!");
						return false;
					}
				}

				if(Util.bset(srcMajor,CMMsg.MASK_MOVE))
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
					if(!CMLib.flags().canMove(this))
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
					if((isInCombat())&&(location()!=null)&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC)))
						for(int i=0;i<location().numInhabitants();i++)
						{
							MOB M=location().fetchInhabitant(i);
							if((M!=null)
							&&(M!=this)
							&&(M.getVictim()==this)
							&&(CMLib.flags().aliveAwakeMobile(M,true))
							&&(CMLib.flags().canSenseMoving(mob,M)))
							{
								tell("Not while you are fighting!");
								return false;
							}
						}
					break;
				case CMMsg.TYP_BUY:
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
					if(isInCombat()&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC)))
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
							&&(!CMLib.flags().canBeHeardBy(this,(MOB)msg.target())))
							{
								tell(msg.target().name()+" can't hear you!");
								return false;
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
					&&(!CMLib.flags().canBeHeardBy(this,(MOB)msg.target())))
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
					if(charStats().getStat(CharStats.INTELLIGENCE)<5)
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
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
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
					victim.recoverEnvStats();
				}
				else
					setAtRange(-1);
				recoverEnvStats();
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
					victim.recoverEnvStats();
				}
				else
					setAtRange(-1);
				recoverEnvStats();
			}
			else
			if((msg.tool()!=null)
			&&(msg.sourceMinor()!=CMMsg.TYP_BUY)
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
					&&(!((Weapon)tool).amWearingAt(Item.INVENTORY)))
						CMLib.commands().remove(this,(Weapon)msg.tool(),false);
					return false;
				}
			}
		}

		if((msg.targetCode()!=CMMsg.NO_EFFECT)&&(msg.amITarget(this)))
		{
			if((amDead())||(location()==null))
				return false;
			if(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			{
				if((msg.amISource(this))
				&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
				&&((msg.tool()==null)||(!(msg.tool() instanceof Ability))||(!((Ability)msg.tool()).isNowAnAutoEffect())))
				{
					mob.tell("You like yourself too much.");
					if(victim==this){ victim=null; setAtRange(-1);}
					return false;
				}

				if((!mayIFight(mob))
				&&((!(msg.tool() instanceof Ability))
				   ||(((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)!=Ability.DISEASE))
				{
					mob.tell("You are not allowed to attack "+name()+".");
					mob.setVictim(null);
					if(victim==mob) setVictim(null);
					return false;
				}

				if((!isMonster())&&(!mob.isMonster())
				&&(soulMate()==null)
				&&(mob.soulMate()==null)
				&&(!CMSecurity.isAllowed(this,location(),"PKILL"))&&(!CMSecurity.isAllowed(mob,mob.location(),"PKILL"))
				&&(mob.envStats().level()>envStats().level()+CMProps.getPKillLevelDiff())
				&&((!(msg.tool() instanceof Ability))
				   ||(((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)!=Ability.DISEASE))
				{
					mob.tell("That is not EVEN a fair fight.");
					mob.setVictim(null);
					if(victim==mob) setVictim(null);
					return false;
				}

				if(amFollowing()==mob) setFollowing(null);

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
					for(int c=0;c<CharStats.affectTypeMap.length;c++)
						if(msg.targetMinor()==CharStats.affectTypeMap[c])
						{	chanceToFail=charStats().getSave(c); break;}
					if(chanceToFail>Integer.MIN_VALUE)
					{
						chanceToFail+=(envStats().level()-msg.source().envStats().level());
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
			    if(envStats().weight()>(mob.maxCarry()/2))
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
			    if(envStats().weight()>mob.maxCarry())
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
				if(getWearPositions(Item.ON_ARMS)==0)
				{
					msg.source().tell(name()+" is unable to accept that from you.");
					return false;
				}
				if((!CMLib.flags().canBeSeenBy(msg.tool(),this))&&(!Util.bset(msg.targetCode(),CMMsg.MASK_GENERAL)))
				{
					mob.tell(name()+" can't see what you are giving.");
					return false;
				}
				int GC=msg.targetCode()&CMMsg.MASK_GENERAL;
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
					if(envStats.level() > (mob.envStats().level() + CMProps.getIntVar(CMProps.SYSTEMI_FOLLOWLEVELDIFF)))
					{
						mob.tell(name() + " is too advanced for you.");
						return false;
					}
					if(envStats.level() < (mob.envStats().level() - CMProps.getIntVar(CMProps.SYSTEMI_FOLLOWLEVELDIFF)))
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
			if(msg.othersMinor()==CMMsg.TYP_DEATH)
			{
			    if((followers!=null)
			    &&(followers.contains(msg.source()))
				&&(CMLib.dice().rollPercentage()==1)
			    &&(fetchEffect("Disease_Depression")==null))
				{
				    A=CMClass.getAbility("Disease_Depression");
				    if(A!=null) A.invoke(this,this,true,0);
				}
			}
		}
		return true;
	}

	public void tell(MOB source, Environmental target, Environmental tool, String msg)
	{
		if(mySession!=null)
			mySession.stdPrintln(source,target,tool,msg);

	}

	public void tell(String msg)
	{
		tell(this,this,null,msg);
	}

    private String relativeCharStatTest(CharStats C, String weakword, String strongword, int stat)
    {
        double d=Util.div(C.getStat(stat),charStats.getStat(stat));
        String prepend="";
        if((d<=0.5)||(d>=3.0)) prepend="much ";
        if(d>=1.6) return name()+" appears "+prepend+weakword+" than the average "+charStats.raceName()+".\n\r";
        if(d<=0.67) return name()+" appears "+prepend+strongword+" than the average "+charStats.raceName()+".\n\r";
        return "";
    }
    
    public void look(MOB viewer, boolean longlook)
    {
        if(CMLib.flags().canBeSeenBy(this,viewer))
        {
            StringBuffer myDescription=new StringBuffer("");
            if(Util.bset(viewer.getBitmap(),MOB.ATT_SYSOPMSGS))
                myDescription.append(ID()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc : "+text()+"\n\r"+description()+"\n\rRoom :'"+((getStartRoom()==null)?"null":getStartRoom().roomID())+"\n\r");
            if(!isMonster())
            {
                String levelStr=null;
                if((!CMSecurity.isDisabled("CLASSES"))
                &&(!charStats().getMyRace().classless())
                &&(!charStats().getCurrentClass().leveless())
                &&(!charStats().getMyRace().leveless())
                &&(!CMSecurity.isDisabled("LEVELS")))
                    levelStr=Util.startWithAorAn(charStats().displayClassLevel(this,false));
                else
                if((!CMSecurity.isDisabled("LEVELS"))
                &&(!charStats().getCurrentClass().leveless())
                &&(!charStats().getMyRace().leveless()))
                    levelStr="level "+charStats().displayClassLevelOnly(this);
                else
                if((!CMSecurity.isDisabled("CLASSES"))
                &&(!charStats().getMyRace().classless()))
                    levelStr=Util.startWithAorAn(charStats().displayClassName());
                if((!CMSecurity.isDisabled("RACES"))
                &&(!charStats.getCurrentClass().raceless()))
                {
                    myDescription.append(name()+" the ");
                    if(charStats.getStat(CharStats.AGE)>0)
                        myDescription.append(charStats.ageName().toLowerCase()+" ");
                    myDescription.append(charStats().raceName());
                }
                else
                    myDescription.append(name()+" ");
                if(levelStr!=null)
                    myDescription.append(" is "+levelStr+".\n\r");
                else
                    myDescription.append("is here.\n\r");
            }
            if(envStats().height()>0)
                myDescription.append(charStats().HeShe()+" is "+envStats().height()+" inches tall and weighs "+baseEnvStats().weight()+" pounds.\n\r");
            if((longlook)&&(viewer.charStats().getStat(CharStats.INTELLIGENCE)>12))
            {
                CharStats C=(CharStats)CMClass.getCommon("DefaultCharStats");
                MOB testMOB=CMClass.getMOB("StdMOB");
                charStats().getMyRace().affectCharStats(testMOB,C);
                myDescription.append(relativeCharStatTest(C,"weaker","stronger",CharStats.STRENGTH));
                myDescription.append(relativeCharStatTest(C,"clumsier","more nimble",CharStats.DEXTERITY));
                myDescription.append(relativeCharStatTest(C,"more sickly","healthier",CharStats.CONSTITUTION));
                myDescription.append(relativeCharStatTest(C,"more repulsive","more attractive",CharStats.CHARISMA));
                myDescription.append(relativeCharStatTest(C,"more naive","wiser",CharStats.WISDOM));
                myDescription.append(relativeCharStatTest(C,"dumber","smarter",CharStats.INTELLIGENCE));
                testMOB.destroy();
            }
            if(!viewer.isMonster())
                myDescription.append(CMProps.mxpImage(this," ALIGN=RIGHT H=70 W=70"));
            myDescription.append(healthText()+"\n\r\n\r");
            myDescription.append(description()+"\n\r\n\r");
            
            StringBuffer eq=CMLib.commands().getEquipment(viewer,this);
            if(eq.length() > 0)
            {
                if((CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)>1)
                ||((viewer!=this)&&(CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)>0)))
                    myDescription.append(charStats().HeShe()+" is wearing "+eq.toString());
                else
                    myDescription.append(charStats().HeShe()+" is wearing:\n\r"+eq.toString());
            }
            viewer.tell(myDescription.toString());
            if(longlook)
            {
                Command C=CMClass.getCommand("Consider");
                try{if(C!=null)C.execute(viewer,Util.makeVector(this));}catch(java.io.IOException e){}
            }
        }
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

        for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)	B.executeMsg(this,msg);
		}

		MOB mob=msg.source();

		boolean asleep=CMLib.flags().isSleeping(this);
		boolean canseesrc=CMLib.flags().canBeSeenBy(msg.source(),this);
		boolean canhearsrc=CMLib.flags().canBeHeardBy(msg.source(),this);

		// first do special cases...
		if((msg.targetCode()!=CMMsg.NO_EFFECT)
		&&(msg.amITarget(this))
		&&(!amDead))
		switch(msg.targetMinor())
		{
			// healing by itself is pure happy
			case CMMsg.TYP_HEALING:
			{
				int amt=msg.value();
				if(amt>0) curState().adjHitPoints(amt,maxState());
			}
			break;
			case CMMsg.TYP_SNIFF:
			{
			    if((playerStats!=null)&&(soulMate==null)&&(playerStats.getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
			    {
			        int x=(int)(playerStats.getHygiene()/PlayerStats.HYGIENE_DELIMIT);
			        if(x<=1) msg.source().tell(name()+" has a slight aroma about "+charStats().himher()+"."); 
			        else
			        if(x<=3) msg.source().tell(name()+" smells pretty sweaty."); 
			        else
			        if(x<=7) msg.source().tell(name()+" stinks pretty bad.");
			        else
			        if(x<15) msg.source().tell(name()+" smells most foul.");
			        else msg.source().tell(name()+" reeks of noxious odors.");
			    }
			}
			break;
			case CMMsg.TYP_DAMAGE:
			{
				int dmg=msg.value();
				synchronized(this)
				{
					if((dmg>0)&&(curState().getHitPoints()>0))
					{
						if((!curState().adjHitPoints(-dmg,maxState()))
						&&(curState().getHitPoints()<1)
						&&(location()!=null))
							CMLib.combat().postDeath(msg.source(),this,msg);
						else
						if((curState().getHitPoints()<getWimpHitPoint())
						&&(getWimpHitPoint()>0)
						&&(isInCombat()))
							CMLib.combat().postPanic(this,msg);
						else
						if((CMProps.getIntVar(CMProps.SYSTEMI_INJPCTHP)>=(int)Math.round(Util.div(curState().getHitPoints(),maxState().getHitPoints())*100.0))
						&&(!CMLib.flags().isGolem(this))
						&&(fetchEffect("Injury")==null))
						{
						    Ability A=CMClass.getAbility("Injury");
						    if(A!=null) A.invoke(this,Util.makeVector(msg),this,true,0);
						}
					}
				}
			}
			break;
			default:
			    break;
		}

		// now go on to source activities
		if((msg.sourceCode()!=CMMsg.NO_EFFECT)&&(msg.amISource(this)))
		{
			if(Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
				if((msg.target() instanceof MOB)
				&&(getVictim()!=msg.target())
				&&((!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
					||(!(msg.tool() instanceof DiseaseAffect))))
				{
					establishRange(this,(MOB)msg.target(),msg.tool());
					setVictim((MOB)msg.target());
				}

			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_PANIC:
				CMLib.commands().flee(this,"");
				break;
			case CMMsg.TYP_EXPCHANGE:
			    if(!CMSecurity.isDisabled("EXPERIENCE")
				&&!charStats().getCurrentClass().expless()
				&&!charStats().getMyRace().expless())
				{
					MOB victim=null;
					if(msg.target() instanceof MOB)
						victim=(MOB)msg.target();

					if(msg.value()>=0)
						charStats().getCurrentClass().gainExperience(this,
																	 victim,
																	 msg.targetMessage(),
																	 msg.value(),
																	 Util.s_bool(msg.othersMessage()));
					else
						charStats().getCurrentClass().loseExperience(this,-msg.value());
				}
				break;
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
				if(!amDead)
				{
					if((!isMonster())&&(soulMate()==null))
					{
						CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_DEATHS);
                        Vector channels=CMLib.channels().getFlaggedChannelNames("DETAILEDDEATHS");
                        Vector channels2=CMLib.channels().getFlaggedChannelNames("DEATHS");
                        if(!CMLib.flags().isCloaked(this))
                        for(int i=0;i<channels.size();i++)
						if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
							CMLib.commands().channel((String)channels.elementAt(i),getClanID(),Name()+" was just killed in "+CMLib.map().getExtendedRoomID(location())+" by "+msg.tool().Name()+".",true);
						else
							CMLib.commands().channel((String)channels.elementAt(i),getClanID(),Name()+" has just died at "+CMLib.map().getExtendedRoomID(location()),true);
                        if(!CMLib.flags().isCloaked(this))
                        for(int i=0;i<channels2.size();i++)
                            if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
                                CMLib.commands().channel((String)channels2.elementAt(i),getClanID(),Name()+" was just killed.",true);
					}
					if(msg.tool() instanceof MOB)
					{
                        MOB killer=(MOB)msg.tool();
						CMLib.combat().justDie(killer,this);
						if((!isMonster())&&(soulMate()==null)&&(killer!=this)&&(!killer.isMonster()))
							CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_PKDEATHS);
                        if((getClanID().length()>0)
                        &&(killer.getClanID().length()>0)
                        &&(!getClanID().equals(killer.getClanID())))
                        {
                            Clan C=CMLib.clans().getClan(killer.getClanID());
                            if(C!=null) C.recordClanKill();
                        }
					}
					else
						CMLib.combat().justDie(null,this);
					tell(this,msg.target(),msg.tool(),msg.sourceMessage());
					if(riding()!=null) riding().delRider(this);
				}
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
                if(msg.target()==this)
                    look(msg.source(),false);
                break;
            case CMMsg.TYP_EXAMINE:
                if(msg.target()==this)
                    look(msg.source(),true);
				break;
			case CMMsg.TYP_READ:
				if((CMLib.flags().canBeSeenBy(this,mob))&&(msg.amITarget(this)))
					tell("There is nothing written on "+name());
				break;
			case CMMsg.TYP_SIT:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SITTING);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				}
				break;
			case CMMsg.TYP_SLEEP:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SLEEPING);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				}
				break;
			case CMMsg.TYP_QUIT:
				tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				break;
			case CMMsg.TYP_STAND:
				{
				int oldDisposition=mob.baseEnvStats().disposition();
				oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
				mob.baseEnvStats().setDisposition(oldDisposition);
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
				tell(this,msg.target(),msg.tool(),msg.sourceMessage());
				}
				break;
			case CMMsg.TYP_RECALL:
				if((msg.target()!=null) && (msg.target() instanceof Room) && (location() != msg.target()))
				{
					tell(msg.source(),null,msg.tool(),msg.targetMessage());
					location().delInhabitant(this);
					((Room)msg.target()).addInhabitant(this);
					((Room)msg.target()).showOthers(mob,null,CMMsg.MSG_ENTER,"<S-NAME> appears out of the Java Plain.");

					setLocation(((Room)msg.target()));
					if((riding()!=null)&&(location()!=CMLib.utensils().roomLocation(riding())))
					{
						int rb=riding().rideBasis();
						if((rb!=Rideable.RIDEABLE_SIT)
						&&(rb!=Rideable.RIDEABLE_SLEEP)
						&&(rb!=Rideable.RIDEABLE_TABLE)
						&&(rb!=Rideable.RIDEABLE_ENTERIN)
						&&(rb!=Rideable.RIDEABLE_LADDER))
						{
							if(riding() instanceof Item)
								location().bringItemHere((Item)riding(),-1);
							else
							if(riding() instanceof MOB)
								location().bringMobHere((MOB)riding(),true);
						}
						else
							setRiding(null);
					}
					recoverEnvStats();
					recoverCharStats();
					recoverMaxState();
					CMLib.commands().look(mob,true);
				}
				break;
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
			case CMMsg.TYP_ENTER:
				lastMoveTime=System.currentTimeMillis();
				movesSinceTick++;
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

			// two special cases were already handled above
			if((msg.targetMinor()!=CMMsg.TYP_HEALING)
			&&(msg.targetMinor()!=CMMsg.TYP_DAMAGE))
			{
				// but there might still be a few more...
				if((Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
				&&(!amDead))
				{
					if((!isInCombat())
					&&(location().isInhabitant(msg.source()))
					&&((!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
						||(!(msg.tool() instanceof DiseaseAffect))))
					{
						establishRange(this,msg.source(),msg.tool());
						setVictim(msg.source());
					}
					if(isInCombat())
					{
                        if((!isMonster())&&(!msg.source().isMonster()))
                            msg.source().session().setLastPKFight();
						if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
						{
							Item weapon=msg.source().myNaturalWeapon();
							if((msg.tool()!=null)&&(msg.tool() instanceof Item))
								weapon=(Item)msg.tool();
							if(weapon!=null)
							{
								CMLib.combat().postWeaponDamage(msg.source(),this,weapon,CMLib.combat().rollToHit(msg.source(),this));
								msg.setValue(1);
							}
							if((soulMate==null)&&(playerStats!=null)&&(location!=null))
							    playerStats.adjHygiene(PlayerStats.HYGIENE_FIGHTDIRTY);
						}
						else
						if((msg.tool()!=null)
						&&(msg.tool() instanceof Item))
							CMLib.combat().postWeaponDamage(msg.source(),this,(Item)msg.tool(),true);
					}
					if(CMLib.flags().isSitting(this)||CMLib.flags().isSleeping(this))
						CMLib.commands().stand(this,true);

				}
				else
				if((msg.targetMinor()==CMMsg.TYP_GIVE)
				 &&(msg.tool()!=null)
				 &&(msg.tool() instanceof Item))
				{
					CMMsg msg2=CMClass.getMsg(msg.source(),msg.tool(),null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
					location().send(this,msg2);
					msg2=CMClass.getMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET,null,CMMsg.MSG_GET,"GIVE",CMMsg.MSG_GET,null);
					location().send(this,msg2);
				}
				else
				if(((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
				&&(CMLib.flags().canBeSeenBy(this,mob)))
                    look(mob,(msg.targetMinor()==CMMsg.TYP_EXAMINE));
				else
				if((msg.targetMinor()==CMMsg.TYP_REBUKE)
				&&(msg.source().Name().equals(getLiegeID())
				&&(!isMarriedToLiege())))
					setLiegeID("");
				else
				if(Util.bset(targetMajor,CMMsg.MASK_CHANNEL))
				{
			        int channelCode=((msg.targetCode()-CMMsg.MASK_CHANNEL)-CMMsg.TYP_CHANNEL);
					if((playerStats()!=null)
					&&(!Util.bset(getBitmap(),MOB.ATT_QUIET))
					&&(!Util.isSet(playerStats().getChannelMask(),channelCode)))
						tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
				}
			}

			// now do the says
			if((Util.bset(targetMajor,CMMsg.MASK_SOUND))
			&&(canhearsrc)&&(!asleep))
			{
				if((msg.targetMinor()==CMMsg.TYP_SPEAK)
				 &&(msg.source()!=null)
				 &&(playerStats()!=null))
					playerStats().setReplyTo(msg.source());
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			}
			else
			if((Util.bset(targetMajor,CMMsg.MASK_GENERAL))
			||(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			||(msg.targetMinor()==CMMsg.TYP_HEALING))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			else
			if((Util.bset(targetMajor,CMMsg.MASK_EYES))
			&&((!asleep)&&(canseesrc)))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			else
			if(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
				tell(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
			else
			if(((Util.bset(targetMajor,CMMsg.MASK_HANDS))
				||(Util.bset(targetMajor,CMMsg.MASK_MOVE))
				||((Util.bset(targetMajor,CMMsg.MASK_MOUTH))
				   &&(!Util.bset(targetMajor,CMMsg.MASK_SOUND))))
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

			if(Util.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)
			&&(msg.target() instanceof MOB)
			&&((!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
				||(!(msg.tool() instanceof DiseaseAffect))))
				fightingFollowers((MOB)msg.target(),msg.source());

			if((othersMinor==CMMsg.TYP_ENTER) // exceptions to movement
			||(othersMinor==CMMsg.TYP_FLEE)
			||(othersMinor==CMMsg.TYP_LEAVE))
			{
				if(((!asleep)||(msg.othersMinor()==CMMsg.TYP_ENTER))
				&&(CMLib.flags().canSenseMoving(msg.source(),this)))
					tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if(Util.bset(othersMajor,CMMsg.MASK_CHANNEL))
			{
		        int channelCode=((msg.othersCode()-CMMsg.MASK_CHANNEL)-CMMsg.TYP_CHANNEL);
				if((playerStats()!=null)
				&&(!Util.bset(getBitmap(),MOB.ATT_QUIET))
				&&(!Util.isSet(playerStats().getChannelMask(),channelCode)))
					tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if((Util.bset(othersMajor,CMMsg.MASK_SOUND))
			&&(!asleep)
			&&(canhearsrc))
			{
				tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if(((Util.bset(othersMajor,CMMsg.MASK_EYES))
			||(Util.bset(othersMajor,CMMsg.MASK_HANDS))
			||(Util.bset(othersMajor,CMMsg.MASK_GENERAL)))
			&&((!asleep)&&(canseesrc)))
			{
				tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());
			}
			else
			if(((Util.bset(othersMajor,CMMsg.MASK_MOVE))
				||((Util.bset(othersMajor,CMMsg.MASK_MOUTH))&&(!Util.bset(othersMajor,CMMsg.MASK_SOUND))))
			&&(!asleep)
			&&((canseesrc)||(canhearsrc)))
				tell(msg.source(),msg.target(),msg.tool(),msg.othersMessage());

			if((msg.othersMinor()==CMMsg.TYP_DEATH)
            &&(victim==msg.source())
            &&(location()!=null))
            {
                Room R=location();
                MOB newTargetM=null;
                HashSet hisGroupH=victim.getGroupMembers(new HashSet());
                HashSet myGroupH=getGroupMembers(new HashSet());
                for(int r=0;r<R.numInhabitants();r++)
                {
                    MOB M=R.fetchInhabitant(r);
                    if((M!=this)
                    &&(M!=victim)
                    &&(M!=null)
                    &&(M.getVictim()==this)
                    &&(!M.amDead())
                    &&(CMLib.flags().isInTheGame(M,true)))
                    {
                        newTargetM=M;
                        break;
                    }
                }
                if(newTargetM==null)
                for(int r=0;r<R.numInhabitants();r++)
                {
                    MOB M=R.fetchInhabitant(r);
                    if((M!=this)
                    &&(M!=victim)
                    &&(M!=null)
                    &&(hisGroupH.contains(M)
                        ||((M.getVictim()!=null)&&(myGroupH.contains(M.getVictim()))))
                    &&(!M.amDead())
                    &&(CMLib.flags().isInTheGame(M,true)))
                    {
                        newTargetM=M;
                        break;
                    }
                }
                setVictim(newTargetM);
            }
		}

		Item I=null;
		for(int i=inventorySize()-1;i>=0;i--)
		{
			I=fetchInventory(i);
			if(I!=null)
				I.executeMsg(this,msg);
		}

		Ability A=null;
        for(int i=0;i<numAllEffects();i++)
		{
			A=fetchEffect(i);
			if(A!=null)
				A.executeMsg(this,msg);
		}

        Faction F=null;
        for(Enumeration e=fetchFactions();e.hasMoreElements();) 
        {
            F=CMLib.factions().getFaction((String)e.nextElement());
            F.executeMsg(this,msg);
        }
	}

	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}

	protected int processVariableEquipment()
	{
		int newLastTickedDateTime=0;
		for(int i=0;i<location().numInhabitants();i++)
		{
			MOB M=location().fetchInhabitant(i);
			if((M!=null)&&(!M.isMonster())&&(CMSecurity.isAllowed(M,location(),"CMDMOBS")))
			{ newLastTickedDateTime=-1; break;}
		}
		if(newLastTickedDateTime==0)
		{
			Vector rivals=new Vector();
			for(int i=0;i<inventorySize();i++)
			{
				Item I=fetchInventory(i);
				if((I!=null)&&(I.baseEnvStats().rejuv()>0)&&(I.baseEnvStats().rejuv()<Integer.MAX_VALUE))
				{
					Vector V=null;
					for(int r=0;r<rivals.size();r++)
					{
						Vector V2=(Vector)rivals.elementAt(r);
						Item I2=(Item)V2.firstElement();
						if(I2.rawWornCode()==I.rawWornCode())
						{ V=V2; break;}
					}
					if(V==null){ V=new Vector(); rivals.addElement(V);}
					V.addElement(I);
				}
			}
			for(int i=0;i<rivals.size();i++)
			{
				Vector V=(Vector)rivals.elementAt(i);
				if((V.size()==1)||(((Item)V.firstElement()).rawWornCode()==0))
				{
					for(int r=0;r<V.size();r++)
					{
						Item I=(Item)V.elementAt(r);
						if(CMLib.dice().rollPercentage()<I.baseEnvStats().rejuv())
							delInventory(I);
						else
						{
							I.baseEnvStats().setRejuv(0);
							I.envStats().setRejuv(0);
						}
					}
				}
				else
				{
					int totalChance=0;
					for(int r=0;r<V.size();r++)
					{
						Item I=(Item)V.elementAt(r);
						totalChance+=I.baseEnvStats().rejuv();
					}
					int chosenChance=CMLib.dice().roll(1,totalChance,0);
					totalChance=0;
					Item chosenI=null;
					for(int r=0;r<V.size();r++)
					{
						Item I=(Item)V.elementAt(r);
						if(chosenChance<=(totalChance+I.baseEnvStats().rejuv()))
						{
							chosenI=I;
							break;
						}
						totalChance+=I.baseEnvStats().rejuv();
					}
					for(int r=0;r<V.size();r++)
					{
						Item I=(Item)V.elementAt(r);
						if(chosenI!=I)
							delInventory(I);
						else
						{
							I.baseEnvStats().setRejuv(0);
							I.envStats().setRejuv(0);
						}
					}
				}
			}
			recoverEnvStats();
			recoverCharStats();
			recoverMaxState();
		}
		return newLastTickedDateTime;
	}

	public int movesSinceLastTick(){return movesSinceTick;}
	public long lastMovedDateTime(){return lastMoveTime;}
	public long getTickStatus(){return tickStatus;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(pleaseDestroy) return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==MudHost.TICK_MOB)
		{
			movesSinceTick=0;
			if(amDead)
			{
				tickStatus=Tickable.STATUS_DEAD;
				if(isMonster())
				{
					if((envStats().rejuv()<Integer.MAX_VALUE)
					&&(baseEnvStats().rejuv()>0))
					{
						envStats().setRejuv(envStats().rejuv()-1);
						if(envStats().rejuv()<0)
						{
							bringToLife(getStartRoom(),true);
							location().showOthers(this,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
						}
					}
					else
					{
						tickStatus=Tickable.STATUS_END;
						if(soulMate()==null) destroy();
						tickStatus=Tickable.STATUS_NOT;
						lastTickedDateTime=System.currentTimeMillis();
						return false;
					}
				}
				tickStatus=Tickable.STATUS_END;
			}
			else
			if(location()!=null)
			{
				// handle variable equipment!
				if((lastTickedDateTime<0)
				&&isMonster()&&location().getMobility()&&location().getArea().getMobility())
				{
					if(lastTickedDateTime==-1)
						lastTickedDateTime=processVariableEquipment();
					else
						lastTickedDateTime++;
				}

				tickStatus=Tickable.STATUS_ALIVE;
				curState().recoverTick(this,maxState);
				if(!isMonster())
					curState().expendEnergy(this,maxState,false);
				if((!CMLib.flags().canBreathe(this))&&(!CMLib.flags().isGolem(this)))
				{
					location().show(this,this,CMMsg.MSG_OK_VISUAL,("^Z<S-NAME> can't breathe!^.^?")+CMProps.msp("choke.wav",10));
					CMLib.combat().postDamage(this,this,null,(int)Math.round(Util.mul(Math.random(),baseEnvStats().level()+2)),CMMsg.MSG_OK_VISUAL,-1,null);
				}
				if(isInCombat())
				{
					tickStatus=Tickable.STATUS_FIGHT;
					peaceTime=0;
					Item weapon=fetchWieldedItem();

					if((Util.bset(getBitmap(),MOB.ATT_AUTODRAW))&&(weapon==null))
					{
						CMLib.commands().draw(this,false,true);
						weapon=fetchWieldedItem();
					}

					double curSpeed=Math.floor(speeder);
					speeder+=CMLib.flags().isSitting(this)?(envStats().speed()/2.0):envStats().speed();
					
					
					int numAttacks=(int)Math.round(Math.floor(speeder-curSpeed));
					
					if(CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM)!=CombatLibrary.COMBAT_DEFAULT)
						while((numAttacks>0)&&(commandQue.size()>0))
						{
                            if(mySession!=null)
    							mySession.dequeCommand();
                            else
                                dequeCommand();
							numAttacks--;
						}
					
					int folrange=(Util.bset(getBitmap(),MOB.ATT_AUTOMELEE)
									&&(amFollowing()!=null)
								    &&(amFollowing().getVictim()==victim)
								    &&(amFollowing().rangeToTarget()>=0)
								    &&(amFollowing().fetchFollowerOrder(this)>=0))?
									amFollowing().fetchFollowerOrder(this)+amFollowing().rangeToTarget():-1;
					if(CMLib.flags().aliveAwakeMobile(this,true))
					{
						for(int s=0;s<numAttacks;s++)
						{
							if((!amDead())
							&&(curState().getHitPoints()>0)
							&&(isInCombat())
							&&((s==0)||(CMLib.flags().isStanding(this))))
							{
								if((weapon!=null)&&(weapon.amWearingAt(Item.INVENTORY)))
									weapon=this.fetchWieldedItem();
								if((!Util.bset(getBitmap(),MOB.ATT_AUTOMELEE)))
									CMLib.combat().postAttack(this,victim,weapon);
								else
								{
									boolean inminrange=(rangeToTarget()>=minRange(weapon));
									boolean inmaxrange=(rangeToTarget()<=maxRange(weapon));
									if((folrange>=0)&&(rangeToTarget()>=0)&&(folrange!=rangeToTarget()))
									{
										if(rangeToTarget()<folrange)
											inminrange=false;
										else
										if(rangeToTarget()>folrange)
										{
											// these settings are ONLY to ensure that neither of the
											// next two conditions evaluate to true.
											inminrange=true;
											inmaxrange=false;
											// we advance
											CMMsg msg=CMClass.getMsg(this,victim,CMMsg.MSG_ADVANCE,"<S-NAME> advances(s) at <T-NAMESELF>.");
											if(location().okMessage(this,msg))
											{
												location().send(this,msg);
												setAtRange(rangeToTarget()-1);
												if((victim!=null)&&(victim.getVictim()==this))
												{
													victim.setAtRange(rangeToTarget());
													victim.recoverEnvStats();
												}
											}
										}
									}
									   
									if((!inminrange)&&(curState().getMovement()>=25))
									{
										CMMsg msg=CMClass.getMsg(this,victim,CMMsg.MSG_RETREAT,"<S-NAME> retreat(s) before <T-NAME>.");
										if(location().okMessage(this,msg))
											location().send(this,msg);
									}
									else
									if((weapon!=null)&&inminrange&&inmaxrange)
										CMLib.combat().postAttack(this,victim,weapon);
								}
							}
							else
								break;
						}

						if(CMLib.dice().rollPercentage()>(charStats().getStat(CharStats.CONSTITUTION)*4))
							curState().adjMovement(-1,maxState());
					}

					if(CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM)==CombatLibrary.COMBAT_DEFAULT)
                        if(mySession!=null)
                            mySession.dequeCommand();
                        else
                            dequeCommand();
					
                    MOB target=getVictim();
					if(!isMonster())
					{
						if((target!=null)&&(!target.amDead())&&(CMLib.flags().canBeSeenBy(target,this)))
							session().print(target.healthText()+"\n\r\n\r");
					}
                    else
                    if((target!=null)
                    &&((amFollowing()==null)||(amFollowing().isMonster()))
                    &&(target.isMonster())
                    &&(!target.amDead())
                    &&(CMLib.dice().rollPercentage()<33)
                    &&(location()!=null))
                    {
                        MOB M=null;
                        Room R=location();
                        MOB nextVictimM=null;
                        for(int m=0;m<R.numInhabitants();m++)
                        {
                            M=R.fetchInhabitant(m);
                            if((M!=null)
                            &&(!M.isMonster())
                            &&(M.getVictim()==this)
                            &&((nextVictimM==null)||(M.rangeToTarget()<nextVictimM.rangeToTarget())))
                                nextVictimM=M;
                        }
                        if(nextVictimM!=null)
                            setVictim(nextVictimM);
                    }
				}
				else
				{
					speeder=0.0;
					peaceTime+=MudHost.TICK_TIME;
					if(Util.bset(getBitmap(),MOB.ATT_AUTODRAW)
					&&(peaceTime>=SHEATH_TIME)
					&&(CMLib.flags().aliveAwakeMobileUnbound(this,true)))
						CMLib.commands().sheath(this,true);
                    if(mySession!=null)
                        mySession.dequeCommand();
                    else
                        dequeCommand();
				}
				tickStatus=Tickable.STATUS_OTHER;
				if(!isMonster())
				{
					if(CMLib.flags().isSleeping(this))
						curState().adjFatigue(-CharState.REST_PER_TICK,maxState());
					else
					if(!CMSecurity.isAllowed(this,location(),"IMMORT"))
					{
						curState().adjFatigue(MudHost.TICK_TIME,maxState());
				        if((curState().getFatigue()>CharState.FATIGUED_MILLIS)
						&&(!isMonster())
                     	&&(CMLib.dice().rollPercentage()==1))
                     	{
                        	Ability theYawns = CMClass.getAbility("Disease_Yawning");
                        	if(theYawns!=null) theYawns.invoke(this, this, true,0);
                     	}
					}
				}

				if((riding()!=null)&&(CMLib.utensils().roomLocation(riding())!=location()))
					setRiding(null);
				if((!isMonster())&&(soulMate()==null))
				{
					CMLib.coffeeTables().bump(this,CoffeeTableRow.STAT_TICKSONLINE);
					if(((++tickCounter)*MudHost.TICK_TIME)>60000)
					{
						tickCounter=0;
						setAgeHours(AgeHours+1); // this is really minutes
						if((baseCharStats.getStat(CharStats.AGE)>0)
						&&(playerStats()!=null)
						&&(playerStats().getBirthday()!=null)
						&&((AgeHours%20)==0))
						{
						    int tage=baseCharStats().getMyRace().getAgingChart()[Race.AGE_YOUNGADULT]+CMClass.globalClock().getYear()-playerStats().getBirthday()[2];
					        int month=CMClass.globalClock().getMonth();
					        int day=CMClass.globalClock().getDayOfMonth();
					        int bday=playerStats().getBirthday()[0];
					        int bmonth=playerStats().getBirthday()[1];
						    while((tage>baseCharStats.getStat(CharStats.AGE))
						    &&((month>bmonth)||((month==bmonth)&&(day>=bday))))
				            {
								if(!CMSecurity.isAllowed(this,location(),"IMMORT"))
								{
							        if((month==bmonth)&&(day==bday))
							            tell("Happy Birthday!");
							        baseCharStats.setStat(CharStats.AGE,baseCharStats.getStat(CharStats.AGE)+1);
							        recoverCharStats();
							        recoverEnvStats();
							        recoverMaxState();
								}
								else
								{
								    playerStats().getBirthday()[2]++;
								    tage--;
								}
						    }
							if(!CMSecurity.isAllowed(this,location(),"IMMORT"))
							{
								if((baseCharStats.ageCategory()>=Race.AGE_VENERABLE)&&(CMLib.dice().rollPercentage()==1)&&(CMLib.dice().rollPercentage()==1))
								{
									Ability A=CMClass.getAbility("Disease_Cancer");
									if((A!=null)&&(fetchEffect(A.ID())==null))
										A.invoke(this,this,true,0);
								}
								else
								if((baseCharStats.ageCategory()>=Race.AGE_ANCIENT)&&(CMLib.dice().rollPercentage()==1))
								{
									Ability A=CMClass.getAbility("Disease_Arthritis");
									if((A!=null)&&(fetchEffect(A.ID())==null))
										A.invoke(this,this,true,0);
								}
								else
								if(CMLib.dice().rollPercentage()<10)
								{
									int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
									for(int i=CharStats.MAX_STRENGTH_ADJ;i<CharStats.MAX_STRENGTH_ADJ+CharStats.NUM_BASE_STATS;i++)
									    if((max+charStats.getStat(i))<=0)
									    {
									        tell("Your max "+CharStats.TRAITS[i].toLowerCase()+" has fallen below 1!");
									        CMLib.combat().postDeath(null,this,null);
									        break;
									    }
								}
							}
						}
					}
				}
			}

			Vector expenseAffects=null;
			if((CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMETIME)>0)
			&&(CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMEAMT)>0)
			&&((--manaConsumeCounter)<=0))
			{
				expenseAffects=new Vector();
				manaConsumeCounter=CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMETIME);
			}

			int a=0;
			while(a<numAllEffects())
			{
				Ability A=fetchEffect(a);
				if(A!=null)
				{
					tickStatus=Tickable.STATUS_AFFECT+a;
					int s=affects.size();
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					else
					if((expenseAffects!=null)
					&&(!A.isAutoInvoked())
					&&(A.canBeUninvoked())
					&&(A.displayText().length()>0)
                    &&(((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
						||((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
						||((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)
						||((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER))
					&&(A.usageCost(this)[0]>0))
						expenseAffects.addElement(A);

					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}

            if((expenseAffects!=null)&&(expenseAffects.size()>0))
            {
                int basePrice=1;
                if(fetchEffect("Prop_MagicBurn1")!=null)
					basePrice=2;  // No way to make a prop that actually increases mana spent

				switch(CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMEAMT))
				{
				case -100: basePrice=basePrice*envStats().level(); break;
				case -200:
					{
						int total=0;
						for(int a1=0;a1<expenseAffects.size();a1++)
						{
							int lql=CMLib.ableMapper().lowestQualifyingLevel(((Ability)expenseAffects.elementAt(a1)).ID());
							if(lql>0)
								total+=lql;
							else
								total+=1;
						}
						basePrice=basePrice*(total/expenseAffects.size());
					}
					break;
				default:
					basePrice=basePrice*CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMEAMT);
					break;
				}

                // 1 per tick per level per msg.  +1 to the affects so that way it's about
                // 3 cost = 1 regen... :)
                int reallyEat=basePrice*(expenseAffects.size()+1);
                while(curState().getMana()<reallyEat)
                {
                    location().show(this,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> strength of will begins to crumble.");
                    //pick one and kill it
                    Ability A=(Ability)expenseAffects.elementAt(CMLib.dice().roll(1,expenseAffects.size(),-1));
                    A.unInvoke();
                    expenseAffects.remove(A);
                    reallyEat=basePrice*expenseAffects.size();
                }
                if(reallyEat>0)
                    curState().adjMana( -reallyEat, maxState());
            }

			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				tickStatus=Tickable.STATUS_BEHAVIOR+b;
				if(B!=null) B.tick(ticking,tickID);
			}

			tickStatus=Tickable.STATUS_CLASS;
			for(int c=0;c<charStats().numClasses();c++)
				charStats().getMyClass(c).tick(ticking,tickID);
			tickStatus=Tickable.STATUS_RACE;
			charStats().getMyRace().tick(ticking,tickID);
			tickStatus=Tickable.STATUS_END;
			synchronized(tattoos)
			{
				try
				{
					String tattoo=null;
					for(int t=0;t<numTattoos();t++)
					{
						tattoo=fetchTattoo(t);
						if((tattoo.length()>0)
						&&(Character.isDigit(tattoo.charAt(0)))
						&&(tattoo.indexOf(" ")>0)
						&&(Util.isNumber(tattoo.substring(0,tattoo.indexOf(" ")))))
						{
							String tat=tattoo.substring(tattoo.indexOf(" ")+1).trim();
							int num=Util.s_int(tattoo.substring(0,tattoo.indexOf(" ")));
							if(num==1)
							{
								tattoos.removeElementAt(t);
								t--;
							}
							else
								tattoos.setElementAt((num-1)+" "+tat,t);
						}
					}
				}
				catch(Exception e)
				{
					Log.errOut("StdMOB","Ticking tattoos.");
					Log.errOut("StdMOB",e);
				}
			}
		}

		if(lastTickedDateTime>=0) lastTickedDateTime=System.currentTimeMillis();
		tickStatus=Tickable.STATUS_NOT;
		return !pleaseDestroy;
	}

	public boolean isMonster()
	{	
	    return (mySession==null);
	}
	public boolean isPossessing()
	{
	    try
	    {
	        Session S=null;
		    for(int s=0;s<CMLib.sessions().size();s++)
		    {
		        S=CMLib.sessions().elementAt(s);
		        if((S.mob()!=null)&&(S.mob().soulMate()==this))
		            return true;
		    }
	    }
	    catch(Exception e){}
	    return false;
	}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void confirmWearability()
	{
		Race R=charStats().getMyRace();
		for(int i=0;i<inventorySize();i++)
		{
			Item item=fetchInventory(i);
			if((item!=null)&&(!item.amWearingAt(Item.INVENTORY)))
			{
				long oldCode=item.rawWornCode();
				item.unWear();
				int msgCode=CMMsg.MSG_WEAR;
				if((oldCode&Item.WIELD)>0)
					msgCode=CMMsg.MSG_WIELD;
				else
				if((oldCode&Item.HELD)>0)
					msgCode=CMMsg.MSG_HOLD;
				CMMsg msg=CMClass.getMsg(this,item,null,CMMsg.NO_EFFECT,null,msgCode,null,CMMsg.NO_EFFECT,null);
				if((R.okMessage(this,msg))&&(item.okMessage(item,msg)))
				   item.wearAt(oldCode);
			}
		}
	}

	public void addInventory(Item item)
	{
		item.setOwner(this);
		inventory.addElement(item);
		item.recoverEnvStats();
	}
	public void delInventory(Item item)
	{
		inventory.removeElement(item);
		item.recoverEnvStats();
	}
	public int inventorySize()
	{
		return inventory.size();
	}
	public Item fetchInventory(int index)
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
                                   int wornCode, 
                                   boolean allowCoins,
                                   boolean respectLocationAndWornCode)
	{
	    Vector inv=inventory;
	    if(!allowCoins)
	    {
	        inv=(Vector)inventory.clone();
	        for(int v=inv.size()-1;v>=0;v--)
	            if(inv.elementAt(v) instanceof Coins)
	                inv.removeElementAt(v);
	    }
        Item item=null;
        if(respectLocationAndWornCode)
        {
    		item=CMLib.english().fetchAvailableItem(inv,itemName,goodLocation,wornCode,true);
    		if(item==null) item=CMLib.english().fetchAvailableItem(inv,itemName,goodLocation,wornCode,false);
        }
        else
        {
            item=(Item)CMLib.english().fetchEnvironmental(inv,itemName,true);
            if(item==null) item=(Item)CMLib.english().fetchEnvironmental(inv,itemName,false);
        }
		return item;
	}
	public Item fetchInventory(String itemName){ return fetchFromInventory(null,itemName,Item.WORN_REQ_ANY,true,false);}
	public Item fetchInventory(Item goodLocation, String itemName){ return fetchFromInventory(goodLocation,itemName,Item.WORN_REQ_ANY,true,true);}
	public Item fetchCarried(Item goodLocation, String itemName){ return fetchFromInventory(goodLocation,itemName,Item.WORN_REQ_UNWORNONLY,true,true);}
	public Item fetchWornItem(String itemName){ return fetchFromInventory(null,itemName,Item.WORN_REQ_WORNONLY,true,true);}
	
	public void addFollower(MOB follower, int order)
	{
		if(follower!=null)
		{
			if(followers==null) followers=new DVector(2);
			if(!followers.contains(follower))
				followers.addElement(follower,new Integer(order));
			else
			{
				int x=followers.indexOf(follower);
				if(x>=0)
					followers.setElementAt(x,2,new Integer(order));
			}
		}
	}

	public void delFollower(MOB follower)
	{
		if((follower!=null)&&(followers!=null)&&(followers.contains(follower)))
		{
			followers.removeElement(follower);
			if(followers.size()==0) followers=null;
		}
	}
	public int numFollowers()
	{
		if(followers==null) return 0;
		return followers.size();
	}
	public int fetchFollowerOrder(MOB thisOne)
	{
		try
		{
			if(followers==null) return -1;
			int x=followers.indexOf(thisOne);
			if(x>=0) return ((Integer)followers.elementAt(x,2)).intValue();
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return -1;
	}
	public MOB fetchFollower(int index)
	{
		try
		{
			if(followers==null) return null;
			return (MOB)followers.elementAt(index,1);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public boolean isFollowedBy(MOB thisOne)
	{
		if(followers==null) return false;
		if(followers.contains(thisOne))
			return true;
		return false;
	}
    
	public boolean willFollowOrdersOf(MOB mob)
	{
        if((amFollowing()==mob)
        ||((isMonster()&&CMSecurity.isAllowed(mob,location(),"ORDER")))
        ||(getLiegeID().equals(mob.Name()))
        ||(CMLib.utensils().doesOwnThisProperty(mob,getStartRoom())))
            return true;
        if((!isMonster())
        &&(CMSecurity.isAllowedEverywhere(mob,"ORDER"))
        &&((!CMSecurity.isASysOp(this))||CMSecurity.isASysOp(mob)))
            return true;
		if((getClanID().length()>0)&&(getClanID().equals(mob.getClanID())))
		{
			Clan C=CMLib.clans().getClan(getClanID());
			if((C!=null)
			&&(C.allowedToDoThis(mob,Clan.FUNC_CLANCANORDERUNDERLINGS)>=0)
			&&(mob.getClanRole()>getClanRole()))
				return true;
		}
		return false;
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

	public HashSet getRideBuddies(HashSet list)
	{
		if(list==null) return list;
		if(!list.contains(this)) list.add(this);
		if(riding()!=null)
			riding().getRideBuddies(list);
		return list;
	}

	public HashSet getGroupMembers(HashSet list)
	{
		if(list==null) return list;
		if(!list.contains(this)) list.add(this);
		if(amFollowing()!=null)
			amFollowing().getGroupMembers(list);
		for(int f=0;f<numFollowers();f++)
		{
			MOB follower=fetchFollower(f);
			if((follower!=null)&&(!list.contains(follower)))
				follower.getGroupMembers(list);
		}
		return list;
	}

	public boolean isEligibleMonster()
	{
		if((!isMonster())&&(soulMate()==null)) 
            return false;
        if(CMLib.utensils().getMobPossessingAnother(this)!=null)
            return false;
		MOB followed=amFollowing();
		if(followed!=null)
			if(!followed.isMonster())
				return false;
		return true;

	}

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
		abilities.addElement(to);
	}
	public void delAbility(Ability to)
	{
		abilities.removeElement(to);
	}
	public int numLearnedAbilities()
	{
		return abilities.size();
	}
	public int numAbilities()
	{
		return abilities.size()+charStats().getMyRace().racialAbilities(this).size();
	}

	public Ability fetchAbility(int index)
	{
		try
		{
			if(index<abilities.size())
				return (Ability)abilities.elementAt(index);
			return (Ability)charStats().getMyRace().racialAbilities(this).elementAt(index-abilities.size());
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchAbility(String ID)
	{
		Ability A=null;
		for(int a=0;a<numLearnedAbilities();a++)
		{
			A=fetchAbility(a);
			if((A!=null)
			&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		for(int a=0;a<charStats().getMyRace().racialAbilities(this).size();a++)
		{
			A=(Ability)charStats().getMyRace().racialAbilities(this).elementAt(a);
			if((A!=null)
			&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		return null;
	}
	public Ability findAbility(String ID)
	{
		Ability A=(Ability)CMLib.english().fetchEnvironmental(abilities,ID,false);
		if(A==null) A=(Ability)CMLib.english().fetchEnvironmental(charStats().getMyRace().racialAbilities(this),ID,false);
		if(A==null) A=fetchAbility(ID);
		return A;
	}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	
	public int numAllEffects()
	{
		return affects.size();//+charStats().getMyRace().racialEffects(this).size();
	}
	
	public int numEffects()
	{
		return affects.size();
	}
	public Ability fetchEffect(int index)
	{
		try
		{
			//if(index<affects.size())
				return (Ability)affects.elementAt(index);
			//return (Ability)charStats().getMyRace().racialEffects(this).elementAt(index-affects.size());
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
		for(int a=0;a<numAllEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
				return A;
		}
		return null;
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
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}


	/** Manipulation of the education list */
	public void addEducation(String of)
	{
		if(educations==null) educations=new Vector();
		if(fetchEducation(of)==null) educations.addElement(of);
	}
	public void delEducation(String of)
	{
		of=fetchEducation(of);
		if(of!=null) educations.removeElement(of);
	}
	public int numEducations(){return (educations==null)?0:educations.size();}
	public String fetchEducation(int x){try{return (String)educations.elementAt(x);}catch(Exception e){} return null;}
	public String fetchEducation(String of){
		try{
			for(int i=0;i<numEducations();i++)
				if(fetchEducation(i).equalsIgnoreCase(of)) return fetchEducation(i);
		}catch(Exception e){}
		return null;
	}

	/** Manipulation of the tatoo list */
	public void addTattoo(String of)
	{
		if(tattoos==null) tattoos=new Vector();
		if(of.length()==0) return;
		if((fetchTattoo(of)==null)&&(of!=null))
			tattoos.addElement(of.toUpperCase().trim());
	}
	public void delTattoo(String of)
	{
		if(tattoos==null) 
			return;
		synchronized(tattoos)
		{
			of=fetchTattoo(of);
			if(of!=null) tattoos.removeElement(of);
		}
	}
	public int numTattoos(){return (tattoos==null)?0:tattoos.size();}
	public String fetchTattoo(int x){try{return (String)tattoos.elementAt(x);}catch(Exception e){} return null;}
	public String fetchTattoo(String of)
	{
		
		try{
			if((of==null)||(of.length()==0)) 
				return null;
			if((Character.isDigit(of.charAt(0)))
			&&(of.indexOf(" ")>0)
			&&(Util.isNumber(of.substring(0,of.indexOf(" ")))))
				of=of.substring(of.indexOf(" ")+1).trim();
			else
				of=of.trim();
			String s=null;
			for(int i=0;i<numTattoos();i++)
			{
				s=fetchTattoo(i);
				if(s.equalsIgnoreCase(of))
					return fetchTattoo(i);
				else
				if((Character.isDigit(s.charAt(0)))
				&&(s.indexOf(" ")>0)
				&&(Util.isNumber(s.substring(0,s.indexOf(" "))))
				&&(of.equalsIgnoreCase(s.substring(s.indexOf(" ")+1).trim())))
					return fetchTattoo(i);
			}
		}catch(Exception e){}
		return null;
	}

    /** Manipulation of the factions list */
    public void addFaction(String which)
    {
        if(factions==null) factions=new Hashtable();
        Faction F=CMLib.factions().getFaction(which);
        if(F==null) return;
        addFaction(F.factionID().toUpperCase(),F.findDefault(this));
    }
    public void addFaction(String which,int start)
    {
        Faction F=CMLib.factions().getFaction(which);
        if(F==null) return;
        if(start>F.maximum()) start=F.maximum();
        if(start<F.minimum()) start=F.minimum();
        factions.put(F.factionID().toUpperCase(),new Integer(start));
    }
    public void adjustFaction(String which,int amount)
    {
        Faction F=CMLib.factions().getFaction(which);
        if(F==null) return;
        if(!factions.containsKey(F.factionID().toUpperCase()))
            addFaction(F.factionID().toUpperCase(),amount);
        else
            addFaction(F.factionID().toUpperCase(),((Integer)factions.get(F.factionID().toUpperCase())).intValue()+amount);
    }
    public Enumeration fetchFactions()
    {
        return factions.keys();
    }
    public int fetchFaction(String which)
    {
		which=which.toUpperCase();
        if(!factions.containsKey(which)) return Integer.MAX_VALUE;
        return ((Integer)factions.get(which)).intValue();
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
    public Vector fetchFactionRanges()
    {
        Faction F=null;
        Faction.FactionRange FR=null;
        Vector V=new Vector();
        for(Enumeration e=fetchFactions();e.hasMoreElements();)
        {
            F=CMLib.factions().getFaction((String)e.nextElement());
            if(F==null) continue;
            FR=CMLib.factions().getRange(F.factionID(),fetchFaction(F.factionID()));
            if(FR!=null) V.addElement(FR.rangeID());
        }
        return V;
    }

	public int freeWearPositions(long wornCode)
	{
		int x=getWearPositions(wornCode);
		if(x<=0) return 0;
		x=x-numWearingHere(wornCode);
		if(x<=0) return 0;
		return x;
	}
	public int getWearPositions(long wornCode)
	{
		if((charStats().getMyRace().forbiddenWornBits()&wornCode)>0)
			return 0;
		if(wornCode==Item.FLOATING_NEARBY)
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
					case (int)Item.ON_HANDS:
						if(total<2)
							add+=1;
						else
							add+=total/2;
						break;
					case (int)Item.WIELD:
					case (int)Item.ON_RIGHT_FINGER:
					case (int)Item.ON_RIGHT_WRIST:
						add+=1; break;
					case (int)Item.HELD:
					case (int)Item.ON_LEFT_FINGER:
					case (int)Item.ON_LEFT_WRIST:
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

	public int numWearingHere(long wornCode)
	{
		int num=0;
		for(int i=0;i<inventorySize();i++)
		{
			Item thisItem=fetchInventory(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(wornCode)))
				num++;
		}
		return num;
	}
	public Item fetchFirstWornItem(long wornCode)
	{
		for(int i=0;i<inventorySize();i++)
		{
			Item thisItem=fetchInventory(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(wornCode)))
				return thisItem;
		}
		return null;
	}

	public Item fetchWieldedItem()
	{
		for(int i=0;i<inventorySize();i++)
		{
			Item thisItem=fetchInventory(i);
			if((thisItem!=null)&&(thisItem.amWearingAt(Item.WIELD)))
				return thisItem;
		}
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
			if((followers!=null)&&(followers.contains(env)))
				return true;
			return false;
		}
		else
		if(env instanceof Ability)
		{
			if(abilities.contains(env)) return true;
			if(affects.contains(env)) return true;
			return false;
		}
		return false;
	}

	public void giveItem(Item thisContainer)
	{
		// caller is responsible for recovering any env
		// stat changes!
		if(CMLib.flags().isHidden(thisContainer))
			thisContainer.baseEnvStats().setDisposition(thisContainer.baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));

		// ensure its out of its previous place
		Environmental owner=location();
		if(thisContainer.owner()!=null)
		{
			owner=thisContainer.owner();
			if(thisContainer.owner() instanceof Room)
				((Room)thisContainer.owner()).delItem(thisContainer);
			else
			if(thisContainer.owner() instanceof MOB)
				((MOB)thisContainer.owner()).delInventory(thisContainer);
		}
		location().delItem(thisContainer);

		thisContainer.unWear();

		if(!isMine(thisContainer))
			addInventory(thisContainer);
		thisContainer.recoverEnvStats();

		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			if(owner instanceof Room)
			{
				Room R=(Room)owner;
				for(int i=0;i<R.numItems();i++)
				{
					Item thisItem=R.fetchItem(i);
					if((thisItem!=null)&&(thisItem.container()==thisContainer))
					{
						giveItem(thisItem);
						nothingDone=false;
						break;
					}
				}
			}
			else
			if(owner instanceof MOB)
			{
				MOB M=(MOB)owner;
				for(int i=0;i<M.inventorySize();i++)
				{
					Item thisItem=M.fetchInventory(i);
					if((thisItem!=null)&&(thisItem.container()==thisContainer))
					{
						giveItem(thisItem);
						nothingDone=false;
						break;
					}
				}
			}
		}while(!nothingDone);
	}
	
	private void fightingFollowers(MOB target, MOB source)
	{
		if((source==null)||(target==null)) return;
		if(source==target) return;
		if((target==this)||(source==this)) return;
		if((target.location()!=location())||(target.location()!=source.location()))
			return;
		if((Util.bset(getBitmap(),MOB.ATT_AUTOASSIST))) return;
		if(isInCombat()) return;

		if((amFollowing()==target)
		||(target.amFollowing()==this)
		||((target.amFollowing()!=null)&&(target.amFollowing()==this.amFollowing())))
		{
			setVictim(source);//CMLib.combat().postAttack(this,source,fetchWieldedItem());
			establishRange(this,source,fetchWieldedItem());
		}
		else
		if((amFollowing()==source)
		||(source.amFollowing()==this)
		||((source.amFollowing()!=null)&&(source.amFollowing()==this.amFollowing())))
		{
			setVictim(target);//CMLib.combat().postAttack(this,source,fetchWieldedItem());
			establishRange(this,target,fetchWieldedItem());
		}
	}

	protected static String[] CODES={"CLASS","LEVEL","ABILITY","TEXT"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+baseEnvStats().level();
		case 2: return ""+baseEnvStats().ability();
		case 3: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: baseEnvStats().setLevel(Util.s_int(val)); break;
		case 2: baseEnvStats().setAbility(Util.s_int(val)); break;
		case 3: setMiscText(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdMOB)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
