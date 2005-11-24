package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


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
public class GenWallpaper implements Item
{
	public String ID(){	return "GenWallpaper";}
	protected String 	name="some wallpaper";
	protected byte[] 	description=null;
	protected String	readableText="";
	protected EnvStats envStats=new DefaultEnvStats();
	protected boolean destroyed=false;
	protected Environmental owner=null;

    public GenWallpaper()
    {
        super();
        CMClass.bumpCounter(CMClass.OBJECT_ITEM);
    }
	public boolean isGeneric(){return true;}
	public Rideable riding(){return null;}
	public void setRiding(Rideable one){};
	public String image(){return "";}
    public String rawImage(){return "";}
	public void setImage(String newImage){}
	

	public String Name(){ return name;}
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return Name();
	}
	public void setName(String newName){name=newName;}
	public EnvStats envStats()
	{return envStats;}
	public EnvStats baseEnvStats()
	{ return envStats; }
	public void recoverEnvStats()
	{ envStats().setSensesMask(envStats().sensesMask()|EnvStats.SENSE_ITEMNOTGET);}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public boolean isAContainer(){return false;}
    public int numberOfItems(){return 1;}
    protected void finalize(){CMClass.unbumpCounter(CMClass.OBJECT_ITEM);}
	public Environmental newInstance()
	{
		try
        {
			return (Environmental)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new GenWallpaper();
	}
	public boolean subjectToWearAndTear(){return false;}
	public Environmental copyOf()
	{
		try
		{
			GenWallpaper E=(GenWallpaper)this.clone();
            CMClass.bumpCounter(CMClass.OBJECT_ITEM);
			E.destroyed=false;
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public Environmental owner(){return owner;}
	public void setOwner(Environmental E)
	{ owner=E;}
	public long dispossessionTime(){return 0;}
	public void setDispossessionTime(long time){}

	public boolean amDestroyed()
	{return destroyed;}

	public boolean amWearingAt(long wornCode){if(wornCode==Item.INVENTORY)return true; return false;}
	public boolean fitsOn(long wornCode){return false;}
	public void wearIfPossible(MOB mob){}
	public void wearAt(long wornCode){}
	public long rawProperLocationBitmap(){return 0;}
	public boolean rawLogicalAnd(){return false;}
	public void setRawProperLocationBitmap(long newValue){}
	public void setRawLogicalAnd(boolean newAnd){}
	public boolean compareProperLocations(Item toThis){return true;}
	public long whereCantWear(MOB mob){ return 0;}
	public boolean canWear(MOB mob, long wornCode){ return false;}
	public long rawWornCode(){return 0;}
	public void setRawWornCode(long newValue){}
	public void unWear(){}
	public int capacity(){return 0;}
	public void setCapacity(int newValue){}
	public int material(){return EnvResource.RESOURCE_PAPER;}
	public void setMaterial(int newValue){}
	public int baseGoldValue(){return 0;}
	public int value(){return 0;}
	public void setBaseValue(int newValue){}
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats){}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState){}
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public void setMiscText(String newText)
	{
		Vector V=XMLManager.parseAllXML(newText);
		if(V!=null)
		{
			setName(XMLManager.getValFromPieces(V,"NAME"));
			setDescription(XMLManager.getValFromPieces(V,"DESC"));
			CoffeeMaker.setEnvFlags(this,Util.s_int(XMLManager.getValFromPieces(V,"FLAG")));
			setReadableText(XMLManager.getValFromPieces(V,"READ"));
		}
	}
	public String text()
	{	StringBuffer text=new StringBuffer("");
		text.append(XMLManager.convertXMLtoTag("NAME",Name()));
		text.append(XMLManager.convertXMLtoTag("DESC",description()));
		text.append(XMLManager.convertXMLtoTag("FLAG",CoffeeMaker.envFlags(this)));
		text.append(XMLManager.convertXMLtoTag("READ",readableText()));
		return text.toString();
	}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(destroyed) return false;
		return true;
	}
	public Item container(){return null;}
	public Item ultimateContainer(){return this;}
    public void wearEvenIfImpossible(MOB mob){}
	public String rawSecretIdentity(){return "";}
	public String secretIdentity(){return "";}
	public void setSecretIdentity(String newIdentity){}
	public String displayText(){return "";}
	public void setDisplayText(String newDisplayText){}
	public String description()
	{
		if((description==null)||(description.length==0))
			return "You see nothing special about "+name()+".";
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_ITEMDCOMPRESS))
			return CMEncoder.decompressString(description);
		else
			return new String(description);
	}
	public void setDescription(String newDescription)
	{
		if(newDescription.length()==0)
			description=null;
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_ITEMDCOMPRESS))
			description=CMEncoder.compressString(newDescription);
		else
			description=newDescription.getBytes();
	}
	public void setContainer(Item newContainer){}
	public int usesRemaining(){return Integer.MAX_VALUE;}
	public void setUsesRemaining(int newUses){}
	public boolean savable(){return Sense.canBeSaved(this);}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return true;
		else
		if(msg.targetCode()==CMMsg.NO_EFFECT)
			return true;
		else
		if(Util.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
		{
			mob.tell("Please don't do that.");
			return false;
		}
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
        case CMMsg.TYP_EXAMINE:
		case CMMsg.TYP_READ:
		case CMMsg.TYP_SPEAK:
		case CMMsg.TYP_OK_ACTION:
		case CMMsg.TYP_OK_VISUAL:
		case CMMsg.TYP_NOISE:
			return true;
		case CMMsg.TYP_GET:
			if((msg.tool()==null)||(msg.tool() instanceof MOB))
			{
				mob.tell("You can't get "+name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_DROP:
			return true;
		default:
			break;
		}
		mob.tell(mob,this,null,"You can't do that to <T-NAMESELF>.");
		return false;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
        case CMMsg.TYP_EXAMINE:
			if(Sense.canBeSeenBy(this,mob))
			{
				if(description().length()==0)
					mob.tell("You don't see anything special about "+this.name());
				else
					mob.tell(description());
			}
			else
				mob.tell("You can't see that!");
			return;
		case CMMsg.TYP_READ:
			if(Sense.canBeSeenBy(this,mob))
			{
				if((Sense.isReadable(this))&&(readableText()!=null)&&(readableText().length()>0))
				{
					if(readableText().startsWith("FILE=")
						||readableText().startsWith("FILE="))
					{
						StringBuffer buf=Resources.getFileResource(readableText().substring(5));
						if((buf!=null)&&(buf.length()>0))
							mob.tell("It says '"+buf.toString()+"'.");
						else
							mob.tell("There is nothing written on "+name()+".");
					}
					else
						mob.tell("It says '"+readableText()+"'.");
				}
				else
					mob.tell("There is nothing written on "+name()+".");
			}
			else
				mob.tell("You can't see that!");
			return;
		default:
			break;
		}
	}

	public void stopTicking(){destroyed=true;}
	public void destroy()
	{
		if(owner==null) return;
		destroyed=true;
		removeFromOwnerContainer();
        owner=null;
	}

	public void removeFromOwnerContainer()
	{
		if(owner==null) return;
		if (owner instanceof Room)
		{
			Room thisRoom=(Room)owner;
			thisRoom.delItem(this);
		}
		else
		if (owner instanceof MOB)
		{
			MOB mob=(MOB)owner;
			mob.delInventory(this);
		}
	}

	public void addNonUninvokableEffect(Ability to){}
	public void addEffect(Ability to){}
	public void delEffect(Ability to){}
	public int numEffects(){return 0;}
	public Ability fetchEffect(int index){return null;}
	public Ability fetchEffect(String ID){return null;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public int numBehaviors(){return 0;}
	public Behavior fetchBehavior(int index){return null;}
	public Behavior fetchBehavior(String ID){return null;}
	public int maxRange(){return 0;}
	public int minRange(){return 0;}

	private static final String[] CODES={"CLASS","NAME","DESCRIPTION","ISREADABLE","READABLETEXT"};
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
		case 1: return name();
		case 2: return description();
		case 3: return ""+Sense.isReadable(this);
		case 4: return readableText();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setName(val); break;
		case 2: setDescription(val); break;
		case 3: Sense.setReadable(this,Util.s_bool(val));
				break;
		case 4: setReadableText(val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenWallpaper)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
