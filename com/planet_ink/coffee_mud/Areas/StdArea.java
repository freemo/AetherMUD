package com.planet_ink.coffee_mud.Areas;
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
public class StdArea implements Area
{
	public String ID(){	return "StdArea";}
	protected String name="the area";
	protected String description="";
	protected String miscText="";
	protected String archPath="";
	protected String imageName="";
	protected int techLevel=0;
	protected int climateID=Area.CLIMASK_NORMAL;
	protected Vector properRooms=null;
	protected Vector metroRooms=null;
	protected boolean mobility=true;
	protected long tickStatus=Tickable.STATUS_NOT;
	private Boolean roomSemaphore=new Boolean(true);
	private int[] statData=null;
	protected boolean stopTicking=false;

    protected Vector children=null;
    protected Vector parents=null;
    protected Vector childrenToLoad=new Vector();
    protected Vector parentsToLoad=new Vector();

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();
	protected String author="";
	public void setAuthorID(String authorID){author=authorID;}
	public String getAuthorID(){return author;}

	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();
	protected Vector subOps=new Vector();
	protected Climate climateObj=new DefaultClimate();
	public void setClimateObj(Climate obj){climateObj=obj;}
	public Climate getClimateObj()
	{
		return climateObj;
	}
	protected TimeClock myClock=DefaultTimeClock.globalClock;
	public void setTimeObj(TimeClock obj){myClock=obj;}
	public TimeClock getTimeObj(){return myClock;}

	public StdArea()
	{
		DefaultTimeClock.globalClock.setLoadName("GLOBAL");
	}
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return name;
	}
	public void setName(String newName){name=newName;}
	public String Name(){return name;}
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
		envStats=baseEnvStats.cloneStats();
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.affectEnvStats(this,envStats);
		}
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}
	public int getTechLevel(){return techLevel;}
	public void setTechLevel(int level){techLevel=level;}

	public String getArchivePath(){return archPath;}
	public void setArchivePath(String pathFile){archPath=pathFile;}

	public String image(){return imageName;}
	public void setImage(String newImage){imageName=newImage;}
	
	public boolean getMobility(){return mobility;}
	public void toggleMobility(boolean onoff){mobility=onoff;}
	public boolean amISubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(((String)subOps.elementAt(s)).equalsIgnoreCase(username))
				return true;
		}
		return false;
	}
	public String getSubOpList()
	{
		StringBuffer list=new StringBuffer("");
		for(int s=subOps.size()-1;s>=0;s--)
		{
			String str=((String)subOps.elementAt(s));
			list.append(str);
			list.append(";");
		}
		return list.toString();
	}
	public void setSubOpList(String list)
	{
		subOps=Util.parseSemicolons(list,true);
	}
	public void addSubOp(String username){subOps.addElement(username);}
	public void delSubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(((String)subOps.elementAt(s)).equalsIgnoreCase(username))
				subOps.removeElementAt(s);
		}
	}

	public Environmental newInstance()
	{
		try{
			return (Environmental)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdArea();
	}
	public boolean isGeneric(){return false;}
	protected void cloneFix(StdArea E)
	{
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();

		parents=null;
		if(E.parents!=null)
			parents=(Vector)E.parents.clone();
		children=null;
		if(E.children!=null)
			children=(Vector)E.children.clone();
		affects=new Vector();
		behaviors=new Vector();
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				behaviors.addElement(B);
		}
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if(A!=null)
				affects.addElement(A);
		}
		setSubOpList(E.getSubOpList());
	}
	public Environmental copyOf()
	{
		try
		{
			StdArea E=(StdArea)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public String displayText(){return "";}
	public void setDisplayText(String newDisplayText){}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,true);
	}
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			CoffeeMaker.setPropertiesStr(this,newMiscText,true);
	}

	public String description()
	{ return description;}
	public void setDescription(String newDescription)
	{ description=newDescription;}
	public int climateType(){return climateID;}
	public void setClimateType(int newClimateType){	climateID=newClimateType;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okMessage(this,msg)))
				return false;
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(!A.okMessage(this,msg)))
				return false;
		}
		if((!mobility)||(!Sense.allowsMovement(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE))
				return false;
		}
		if(parents!=null)
		for(int i=0;i<parents.size();i++)
			if(!((Area)parents.elementAt(i)).okMessage(myHost,msg))
				return false;
		
		if(getTechLevel()==Area.TECH_HIGH)
		{
			if((Util.bset(msg.sourceCode(),CMMsg.MASK_MAGIC))
			||(Util.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
			||(Util.bset(msg.othersCode(),CMMsg.MASK_MAGIC)))
			{
				Room room=null;
				if((msg.target()!=null)
				&&(msg.target() instanceof MOB)
				&&(((MOB)msg.target()).location()!=null))
					room=((MOB)msg.target()).location();
				else
				if((msg.source()!=null)
				&&(msg.source().location()!=null))
					room=msg.source().location();
				if(room!=null)
				{
					if(room.getArea()==this)
						room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work here.");
					else
						room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work there.");
				}

				return false;
			}
		}
		else
		if(getTechLevel()==Area.TECH_LOW)
		{
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Electronics))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_BUY:
				case CMMsg.TYP_CLOSE:
				case CMMsg.TYP_DEPOSIT:
				case CMMsg.TYP_DROP:
				case CMMsg.TYP_EXAMINESOMETHING:
				case CMMsg.TYP_GET:
				case CMMsg.TYP_GIVE:
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_PUT:
				case CMMsg.TYP_SELL:
				case CMMsg.TYP_VALUE:
				case CMMsg.TYP_REMOVE:
				case CMMsg.TYP_VIEW:
				case CMMsg.TYP_WITHDRAW:
					break;
				default:
					{
						Room room=null;
						if((msg.target()!=null)
						&&(msg.target() instanceof MOB)
						&&(((MOB)msg.target()).location()!=null))
							room=((MOB)msg.target()).location();
						else
						if((msg.source()!=null)
						&&(msg.source().location()!=null))
							room=msg.source().location();
						if(room!=null)
							room.showHappens(CMMsg.MSG_OK_VISUAL,"Technology doesn't seem to work here.");
						return false;
					}
				}
			}
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)
				B.executeMsg(this,msg);
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.executeMsg(this,msg);
		}
		if((msg.sourceMinor()==CMMsg.TYP_RETIRE)
		&&(amISubOp(msg.source().Name())))
			delSubOp(msg.source().Name());
		
		if(parents!=null)
		for(int i=0;i<parents.size();i++)
			((Area)parents.elementAt(i)).executeMsg(myHost,msg);
	}

	public void tickControl(boolean start)
	{
		if(start)
		{
			stopTicking=false;
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_AREA,1);
		}
		else
			stopTicking=true;

	}

	public long getTickStatus(){ return tickStatus;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(stopTicking) return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==MudHost.TICK_AREA)
		{
			getClimateObj().tick(this,tickID);
			getTimeObj().tick(this,tickID);
			for(int b=0;b<numBehaviors();b++)
			{
				tickStatus=Tickable.STATUS_BEHAVIOR+b;
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(ticking,tickID);
			}

			int a=0;
			while(a<numEffects())
			{
				Ability A=fetchEffect(a);
				if(A!=null)
				{
					tickStatus=Tickable.STATUS_AFFECT+a;
					int s=affects.size();
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(envStats().sensesMask()>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|envStats().sensesMask());
		int disposition=envStats().disposition()
			&((Integer.MAX_VALUE-(EnvStats.IS_SLEEPING|EnvStats.IS_HIDDEN)));
		if((affected instanceof Room)&&(CoffeeUtensils.hasASky((Room)affected)))
		{
			if((getClimateObj().weatherType((Room)affected)==Climate.WEATHER_BLIZZARD)
			   ||(getClimateObj().weatherType((Room)affected)==Climate.WEATHER_DUSTSTORM)
			   ||(getTimeObj().getTODCode()==TimeClock.TIME_NIGHT))
				disposition=disposition|EnvStats.IS_DARK;
		}
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+envStats().weight());
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
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
	public int numEffects()
	{
		return affects.size();
	}
	public Ability fetchEffect(int index)
	{
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
			   return A;
		}
		return null;
	}
	public boolean inMetroArea(Area A)
	{
		if(A==this) return true;
		if(getNumChildren()==0) return false;
		for(int i=0;i<getNumChildren();i++)
			if(getChild(i).inMetroArea(A))
				return true;
		return false;
	}

	public void fillInAreaRooms()
	{
		for(Enumeration r=getProperMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			R.clearSky();
			if(R.roomID().length()>0)
			{
				if(R instanceof GridLocale)
					((GridLocale)R).buildGrid();
			}
		}
		clearMaps();
		for(Enumeration r=getProperMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			R.clearSky();
			R.giveASky(0);
		}
		clearMaps();
	}

	public void fillInAreaRoom(Room R)
	{
		if(R==null) return;
		R.clearSky();
		if(R.roomID().length()>0)
		{
			if(R instanceof GridLocale)
				((GridLocale)R).buildGrid();
		}
		R.giveASky(0);
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
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
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

	public int[] getAreaIStats()
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return null;
		getAreaStats();
		return statData;
	}
	public StringBuffer getAreaStats()
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return new StringBuffer("");
		StringBuffer s=(StringBuffer)Resources.getResource("HELP_"+Name().toUpperCase());
		if(s!=null) return s;
		s=new StringBuffer("");
		s.append(description()+"\n\r");
		if(author.length()>0)
			s.append("Author         : "+author+"\n\r");
		s.append("Number of rooms: "+numberOfProperIDedRooms()+"\n\r");

		Vector levelRanges=new Vector();
		Vector alignRanges=new Vector();
		statData=new int[Area.AREASTAT_NUMBER];
		statData[Area.AREASTAT_POPULATION]=0;
		statData[Area.AREASTAT_MINLEVEL]=Integer.MAX_VALUE;
		statData[Area.AREASTAT_MAXLEVEL]=Integer.MIN_VALUE;
		statData[Area.AREASTAT_AVGLEVEL]=0;
		statData[Area.AREASTAT_MEDLEVEL]=0;
		statData[Area.AREASTAT_AVGALIGN]=0;
		statData[Area.AREASTAT_TOTLEVEL]=0;
		statData[Area.AREASTAT_INTLEVEL]=0;
		long totalAlignments=0;
		for(Enumeration r=getProperMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB mob=R.fetchInhabitant(i);
				if((mob!=null)&&(mob.isMonster()))
				{
					int lvl=mob.baseEnvStats().level();
					levelRanges.addElement(new Integer(lvl));
					alignRanges.addElement(new Integer(mob.getAlignment()));
					totalAlignments+=mob.getAlignment();
					statData[Area.AREASTAT_POPULATION]++;
					statData[Area.AREASTAT_TOTLEVEL]+=lvl;
					if(!Sense.isAnimalIntelligence(mob))
						statData[Area.AREASTAT_INTLEVEL]+=lvl;
					if(lvl<statData[Area.AREASTAT_MINLEVEL])
						statData[Area.AREASTAT_MINLEVEL]=lvl;
					if(lvl>statData[Area.AREASTAT_MAXLEVEL])
						statData[Area.AREASTAT_MAXLEVEL]=lvl;
				}
			}
		}
		if((statData[Area.AREASTAT_POPULATION]==0)||(levelRanges.size()==0))
		{
			statData[Area.AREASTAT_MINLEVEL]=0;
			statData[Area.AREASTAT_MAXLEVEL]=0;
			s.append("Population     : 0\n\r");
		}
		else
		{
			Collections.sort(levelRanges);
			Collections.sort(alignRanges);
			statData[Area.AREASTAT_MEDLEVEL]=((Integer)levelRanges.elementAt((int)Math.round(Math.floor(Util.div(levelRanges.size(),2.0))))).intValue();
			statData[Area.AREASTAT_MEDALIGN]=((Integer)alignRanges.elementAt((int)Math.round(Math.floor(Util.div(alignRanges.size(),2.0))))).intValue();
			statData[Area.AREASTAT_AVGLEVEL]=(int)Math.round(Util.div(statData[Area.AREASTAT_TOTLEVEL],statData[Area.AREASTAT_POPULATION]));
			statData[Area.AREASTAT_AVGALIGN]=(int)Math.round(new Long(totalAlignments).doubleValue()/new Integer(statData[Area.AREASTAT_POPULATION]).doubleValue());
			s.append("Population     : "+statData[Area.AREASTAT_POPULATION]+"\n\r");
			Behavior B=CoffeeUtensils.getLegalBehavior(this);
			if(B!=null)
			{
			    Area A2=CoffeeUtensils.getLegalObject(this);
				Vector V=new Vector();
				V.addElement(new Integer(Law.MOD_RULINGCLAN));
				if(B.modifyBehavior(A2,CMClass.sampleMOB(),V)
				&&(V.size()>0)
				&&(V.firstElement() instanceof String))
				{
					Clan C=Clans.getClan(((String)V.firstElement()));
					if(C!=null)
						s.append("Controlled by  : "+C.typeName()+" "+C.name()+"\n\r");
				}
			}
			s.append("Level range    : "+statData[Area.AREASTAT_MINLEVEL]+" to "+statData[Area.AREASTAT_MAXLEVEL]+"\n\r");
			s.append("Average level  : "+statData[Area.AREASTAT_AVGLEVEL]+"\n\r");
			s.append("Median level   : "+statData[Area.AREASTAT_MEDLEVEL]+"\n\r");
			s.append("Avg. Alignment : "+statData[Area.AREASTAT_AVGALIGN]+" ("+CommonStrings.alignmentStr(statData[Area.AREASTAT_AVGALIGN])+")\n\r");
			s.append("Med. Alignment : "+statData[Area.AREASTAT_MEDALIGN]+" ("+CommonStrings.alignmentStr(statData[Area.AREASTAT_MEDALIGN])+")\n\r");
		}
		Resources.submitResource("HELP_"+Name().toUpperCase(),s);
		return s;
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

	public void clearMaps()
	{
		synchronized(roomSemaphore)
		{
			properRooms=null;
			metroRooms=null;
		}
	}

	public int properSize()
	{
		synchronized(roomSemaphore)
		{
			if(properRooms!=null)
				return properRooms.size();
			else
				makeProperMap();
			return properRooms.size();
		}
	}
	public int metroSize()
	{
		synchronized(roomSemaphore)
		{
			if(metroRooms!=null)
				return metroRooms.size();
			else
				makeMetroMap();
			return metroRooms.size();
		}
	}
	public int numberOfProperIDedRooms()
	{
		int num=0;
		for(Enumeration e=getProperMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(R.roomID().length()>0)
				if(R instanceof GridLocale)
					num+=((GridLocale)R).xSize()*((GridLocale)R).ySize();
				else
					num++;
		}
		return num;
	}
	public Room getRandomProperRoom()
	{
		synchronized(roomSemaphore)
		{
			if(properRooms==null) makeProperMap();
			if(properSize()==0) return null;
			return (Room)properRooms.elementAt(Dice.roll(1,properRooms.size(),-1));
		}
	}
	public Room getRandomMetroRoom()
	{
		synchronized(roomSemaphore)
		{
			if(metroRooms==null) makeMetroMap();
			if(metroSize()==0) return null;
			return (Room)metroRooms.elementAt(Dice.roll(1,metroRooms.size(),-1));
		}
	}
	
	public Enumeration getProperMap()
	{
		synchronized(roomSemaphore)
		{
			if(properRooms!=null) return properRooms.elements();
			makeProperMap();
			return properRooms.elements();
		}
	}
	public Enumeration getMetroMap()
	{
		synchronized(roomSemaphore)
		{
			if(metroRooms!=null) return metroRooms.elements();
			makeMetroMap();
			return metroRooms.elements();
		}
	}
	private void makeProperMap()
	{
		synchronized(roomSemaphore)
		{
			if(properRooms!=null) return;
			Vector myMap=new Vector();
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea()==this)
					myMap.addElement(R);
			}
			properRooms=myMap;
		}
	}

	private void makeMetroMap()
	{
		synchronized(roomSemaphore)
		{
			if(metroRooms!=null) return;
			if(properRooms==null) 
				makeProperMap();
			if(getNumChildren()<=0)
			{
				metroRooms=properRooms;
				return;
			}
			Vector myMap=(Vector)properRooms.clone();
			for(int i=0;i<getNumChildren();i++)
			{
				Area A=getChild(i);
				if(A!=null)
				for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
					myMap.addElement(e.nextElement());
			}
			metroRooms=myMap;
		}
	}

	public Vector getSubOpVectorList()
	{
		return subOps;
	}

    public void addChildToLoad(String str) { childrenToLoad.addElement(str);}
    public void addParentToLoad(String str) { parentsToLoad.addElement(str);}

	// Children
	public void initChildren() 
	{
	    if(children==null) 
		{
	        children=new Vector();
	        for(int i=0;i<childrenToLoad.size();i++) 
			{
	          Area A=CMMap.getArea((String)childrenToLoad.elementAt(i));
	          if(A==null)
	            continue;
			children.addElement(A);
			}
		}
	}
	public Enumeration getChildren() { initChildren(); return children.elements(); }
	public String getChildrenList() {
	        initChildren();
	        StringBuffer str=new StringBuffer("");
	        for(Enumeration e=getChildren(); e.hasMoreElements();) {
	                Area A=(Area)e.nextElement();
	                if(str.length()>0) str.append(";");
	                str.append(A.name());
	        }
	        return str.toString();
	}

	public int getNumChildren() { initChildren(); return children.size(); }
	public Area getChild(int num) { initChildren(); return (Area)children.elementAt(num); }
	public Area getChild(String named) {
	        initChildren();
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                       return A;
	        }
	        return null;
	}
	public boolean isChild(Area named) {
	        initChildren();
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if(A.equals(named))
	                       return true;
	        }
	        return false;
	}
	public boolean isChild(String named) {
	        initChildren();
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                        return true;
	        }
	        return false;
	}
	public void addChild(Area Adopted) {
	        initChildren();
	        // So areas can load ok, the code needs to be able to replace 'dummy' children with 'real' ones
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if(A.Name().equalsIgnoreCase(Adopted.Name())){
	                        children.setElementAt(Adopted, i);
	                        return;
	                }
	        }
	        children.addElement(Adopted);
	}
	public void removeChild(Area Disowned) { initChildren(); children.removeElement(Disowned); }
	public void removeChild(int Disowned) { initChildren(); children.removeElementAt(Disowned); }
	// child based circular reference check
	public boolean canChild(Area newChild) {
	        initParents();
	        // Someone asked this area if newChild can be a child to them,
	        // which means this is a parent to someone.  If newChild is a
	        // parent, directly or indirectly, return false.
	        if(parents.contains(newChild))
	        {
	                return false; // It is directly a parent
	        }
	        for(int i=0;i<parents.size();i++) {
	                // check with all the parents about how they feel
	                Area rent=(Area)parents.elementAt(i);
	                // as soon as any parent says false, dump that false back to them
	                if(!(rent.canChild(newChild)))
	                {
	                        return false;
	                }
	        }
	        // no parent is the same as newChild, nor is it indirectly a parent.
	        // Go for it!
	        return true;
	}

	// Parent
	public void initParents() {
	        if (parents == null) {
	                parents = new Vector();
	                for (int i = 0; i < parentsToLoad.size(); i++) {
	                        Area A = CMMap.getArea( (String) parentsToLoad.elementAt(i));
	                        if (A == null)
	                                continue;
	                        parents.addElement(A);
	                }
	        }
	}
	public Enumeration getParents() { initParents(); return parents.elements(); }
	public String getParentsList() {
	        initParents();
	        StringBuffer str=new StringBuffer("");
	        for(Enumeration e=getParents(); e.hasMoreElements();) {
	                Area A=(Area)e.nextElement();
	                if(str.length()>0) str.append(";");
	                str.append(A.name());
	        }
	        return str.toString();
	}

	public int getNumParents() { initParents(); return parents.size(); }
	public Area getParent(int num) { initParents(); return (Area)parents.elementAt(num); }
	public Area getParent(String named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                       return A;
	        }
	        return null;
	}
	public boolean isParent(Area named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if(A.equals(named))
	                       return true;
	        }
	        return false;
	}
	public boolean isParent(String named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                        return true;
	        }
	        return false;
	}
	public void addParent(Area Adopted) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if(A.Name().equalsIgnoreCase(Adopted.Name())){
	                        parents.setElementAt(Adopted, i);
	                        return;
	                }
	        }
	        parents.addElement(Adopted);
	}
	public void removeParent(Area Disowned) { initParents();parents.removeElement(Disowned); }
	public void removeParent(int Disowned) { initParents();parents.removeElementAt(Disowned); }
	public boolean canParent(Area newParent) {
	        initChildren();
	        // Someone asked this area if newParent can be a parent to them,
	        // which means this is a child to someone.  If newParent is a
	        // child, directly or indirectly, return false.
	        if(children.contains(newParent))
	        {
	                return false; // It is directly a child, so it can't Parent
	        }
	        for(int i=0;i<children.size();i++) {
	                // check with all the children about how they feel
	                Area child=(Area)children.elementAt(i);
	                // as soon as any child says false, dump that false back to them
	                if(!(child.canParent(newParent)))
	                {
	                        return false;
	                }
	        }
	        // no child is the same as newParent, nor is it indirectly a child.
	        // Go for it!
	        return true;
	}




	private static final String[] CODES={"CLASS","CLIMATE","DESCRIPTION","TEXT","TECHLEVEL"};
	public String[] getStatCodes(){return CODES;}
	private int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+climateType();
		case 2: return description();
		case 3: return text();
		case 4: return ""+getTechLevel();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClimateType(Util.s_int(val)); break;
		case 2: setDescription(val); break;
		case 3: setMiscText(val); break;
		case 4: setTechLevel(Util.s_int(val)); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof Area)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
