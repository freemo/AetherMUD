package com.planet_ink.coffee_mud.core.http.macros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
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
public class MobData extends StdWebMacro
{
	public String name() {return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public static int getShopCardinality(ShopKeeper E, Environmental O)
	{
		Vector V=E.getStoreInventory();
		for(int i=0;i<V.size();i++)
			if(O==(V.elementAt(i)))
				return i;
		return -1;
	}

	public static String senses(Environmental E,
								boolean firstTime,
								ExternalHTTPRequests httpReq,
								Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
		for(int d=0;d<EnvStats.sensesNames.length;d++)
		{
			if(parms.containsKey(EnvStats.sensesNames[d]))
			{
				String parm=httpReq.getRequestParameter(EnvStats.sensesNames[d]);
				if(firstTime)
					parm=(((E.baseEnvStats().sensesMask()&(1<<d))>0)?"on":"");
				if((parm!=null)&&(parm.length()>0))
					str.append("checked");
			}
		}
		return str.toString();
	}

	public static StringBuffer abilities(MOB E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("ABILITIES"))
		{
			Vector theclasses=new Vector();
			if(httpReq.isRequestParameter("ABLES1"))
			{
				int num=1;
				String behav=httpReq.getRequestParameter("ABLES"+num);
				while(behav!=null)
				{
					if(behav.length()>0)
						theclasses.addElement(behav);
					num++;
					behav=httpReq.getRequestParameter("ABLES"+num);
				}
			}
			else
			for(int a=0;a<E.numLearnedAbilities();a++)
			{
				Ability Able=E.fetchAbility(a);
				if((Able!=null)&&(!Able.isBorrowed(E)))
					theclasses.addElement(CMClass.className(Able));
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				String theclass=(String)theclasses.elementAt(i);
				str.append("<TR><TD WIDTH=100%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=ABLES"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=100%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=ABLES"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an Ability");
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				String cnam=((Ability)a.nextElement()).ID();
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			str.append("</SELECT>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer blessings(Deity E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("BLESSINGS"))
		{
			Vector theclasses=new Vector();
			if(httpReq.isRequestParameter("BLESS1"))
			{
				int num=1;
				String behav=httpReq.getRequestParameter("BLESS"+num);
				while(behav!=null)
				{
					if(behav.length()>0)
						theclasses.addElement(behav);
					num++;
					behav=httpReq.getRequestParameter("BLESS"+num);
				}
			}
			else
			for(int a=0;a<E.numBlessings();a++)
			{
				Ability Able=E.fetchBlessing(a);
				if(Able!=null)
					theclasses.addElement(CMClass.className(Able));
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				String theclass=(String)theclasses.elementAt(i);
				str.append("<TR><TD WIDTH=100%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=BLESS"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=100%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=BLESS"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Blessing");
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				String cnam=((Ability)a.nextElement()).ID();
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			str.append("</SELECT>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}
	public static StringBuffer curses(Deity E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("CURSES"))
		{
			Vector theclasses=new Vector();
			if(httpReq.isRequestParameter("CURSE1"))
			{
				int num=1;
				String behav=httpReq.getRequestParameter("CURSE"+num);
				while(behav!=null)
				{
					if(behav.length()>0)
						theclasses.addElement(behav);
					num++;
					behav=httpReq.getRequestParameter("CURSE"+num);
				}
			}
			else
			for(int a=0;a<E.numCurses();a++)
			{
				Ability Able=E.fetchCurse(a);
				if(Able!=null)
					theclasses.addElement(CMClass.className(Able));
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				String theclass=(String)theclasses.elementAt(i);
				str.append("<TR><TD WIDTH=100%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=CURSE"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=100%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=CURSE"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Curse");
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				String cnam=((Ability)a.nextElement()).ID();
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			str.append("</SELECT>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer factions(MOB E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("FACTIONS"))
		{
			Vector theclasses=new Vector();
			Vector theparms=new Vector();
			if(httpReq.isRequestParameter("FACTION1"))
			{
				int num=1;
				String facti=httpReq.getRequestParameter("FACTION"+num);
				String theparm=httpReq.getRequestParameter("FACTDATA"+num);
				if(theparm==null) theparm="";
				while((facti!=null)&&(theparm!=null))
				{
					if(facti.length()>0)
					{
						theclasses.addElement(facti);
						String t=theparm;
						t=Util.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
					num++;
					facti=httpReq.getRequestParameter("FACTION"+num);
					theparm=httpReq.getRequestParameter("FACTDATA"+num);
				}
			}
			else
			for(Enumeration e=E.fetchFactions();e.hasMoreElements();) 
			{
				Faction f=CMLib.factions().getFaction((String)e.nextElement());
				if(f!=null)
				{
					theclasses.addElement(f.factionID());
					theparms.addElement(new Integer(E.fetchFaction(f.factionID())).toString());
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				String theclass=(String)theclasses.elementAt(i);
				Faction F=CMLib.factions().getFaction(theclass);
				if(F==null) continue;
				String theparm=(String)theparms.elementAt(i);
				str.append("<TR><TD WIDTH=50%>");
				str.append("<SELECT ONCHANGE=\"EditFaction(this);\" NAME=FACTION"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+F.name());
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				str.append("<SELECT NAME=FACTDATA"+(i+1)+">");
				if(theparm.length()==0) theparm=""+F.findDefault(E);
				Faction.FactionRange FR=CMLib.factions().getRange(F.factionID(),Util.s_int(theparm));
				if(FR==null)
					str.append("<OPTION VALUE=\""+Util.s_int(theparm)+"\">"+Util.s_int(theparm));
				for(int r=0;r<F.ranges().size();r++)
				{
				    Faction.FactionRange FR2=(Faction.FactionRange)F.ranges().elementAt(r);
				    int value=FR2.low()+(FR2.high()-FR2.low());
				    if(FR2.low()==F.lowest()) value=FR2.low();
				    if(FR2.high()==F.highest()) value=FR2.high();
				    if(FR2==FR) value=Util.s_int(theparm);
					str.append("<OPTION VALUE=\""+value+"\"");
					if(FR2==FR) str.append(" SELECTED");
					str.append(">"+FR2.name());
				}
				str.append("</SELECT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"AddFaction(this);\" NAME=FACTION"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Faction");
	
			Object[] sortedB=null;
			Vector sortMeB=new Vector();
			for(Enumeration fID=CMLib.factions().factionSet().keys();fID.hasMoreElements();)
			{
				Faction F=CMLib.factions().getFaction((String)fID.nextElement());
				if((F!=null)&&(!theclasses.contains(F.factionID())))
					sortMeB.addElement(F.factionID());
			}
			sortedB=(new TreeSet(sortMeB)).toArray();
			for(int r=0;r<sortedB.length;r++)
			{
				String cnam=(String)sortedB[r];
				Faction F=CMLib.factions().getFaction(cnam);
				if(F!=null)
					str.append("<OPTION VALUE=\""+cnam+"\">"+F.name());
			}
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=50%><BR>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer powers(Deity E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("POWERS"))
		{
			Vector theclasses=new Vector();
			if(httpReq.isRequestParameter("POWER1"))
			{
				int num=1;
				String behav=httpReq.getRequestParameter("POWER"+num);
				while(behav!=null)
				{
					if(behav.length()>0)
						theclasses.addElement(behav);
					num++;
					behav=httpReq.getRequestParameter("POWER"+num);
				}
			}
			else
			for(int a=0;a<E.numPowers();a++)
			{
				Ability Able=E.fetchPower(a);
				if(Able!=null)
					theclasses.addElement(CMClass.className(Able));
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				String theclass=(String)theclasses.elementAt(i);
				str.append("<TR><TD WIDTH=100%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=POWER"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=100%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=CURSE"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a Granted Power");
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				String cnam=((Ability)a.nextElement()).ID();
				str.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
			}
			str.append("</SELECT>");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public static StringBuffer shopkeeper(ShopKeeper E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("SHOPINVENTORY"))
		{
			Vector theclasses=new Vector();
			Vector theparms=new Vector();
			Vector theprices=new Vector();
			if(httpReq.isRequestParameter("SHP1"))
			{
				int num=1;
				String MATCHING=httpReq.getRequestParameter("SHP"+num);
				String theparm=httpReq.getRequestParameter("SDATA"+num);
				String theprice=httpReq.getRequestParameter("SPRIC"+num);
				Vector inventory=E.getStoreInventory();
				while((MATCHING!=null)&&(theparm!=null))
				{
					if(MATCHING==null)
						break;
					else
					if(Util.isNumber(MATCHING))
					{
						Environmental O=(Environmental)inventory.elementAt(Util.s_int(MATCHING)-1);
						if(O!=null)
							theclasses.addElement(O);
					}
					else
					if(MATCHING.indexOf("@")>0)
					{
						Environmental O=null;
						for(int m=0;m<RoomData.mobs.size();m++)
						{
							MOB M2=(MOB)RoomData.mobs.elementAt(m);
							if(MATCHING.equals(""+M2))
							{	O=M2;	break;	}
						}
						if(O==null)
							O=RoomData.getItemFromAnywhere(null,MATCHING);
						if(O!=null)
							theclasses.addElement(O);
					}
					else
					{
						Environmental O=null;
						for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
						{
							MOB M2=(MOB)m.nextElement();
							if(CMClass.className(M2).equals(MATCHING)&&(!M2.isGeneric()))
							{	O=(MOB)M2.copyOf(); break;	}
						}
						if(O==null)
						for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
						{
							Ability A2=(Ability)a.nextElement();
							if(CMClass.className(A2).equals(MATCHING))
							{	O=(Ability)A2.copyOf(); break;	}
						}
						if(O==null)
							O=RoomData.getItemFromAnywhere(null,MATCHING);
						if(O!=null)
							theclasses.addElement(O);
					}
					theparms.addElement(theparm);
					theprices.addElement(theprice);
					num++;
					MATCHING=httpReq.getRequestParameter("SHP"+num);
					theparm=httpReq.getRequestParameter("SDATA"+num);
					theprice=httpReq.getRequestParameter("SPRIC"+num);
				}
			}
			else
			{
				Vector V=E.getStoreInventory();
				Vector itemClasses=new Vector();
				Vector mobClasses=new Vector();
				for(int b=0;b<V.size();b++)
				{
					Environmental O=(Environmental)V.elementAt(b);
					if(O instanceof Item) itemClasses.addElement(O);
					if(O instanceof MOB) mobClasses.addElement(O);
					theclasses.addElement(O);
					theparms.addElement(""+E.numberInStock(O));
					theprices.addElement(""+E.stockPrice(O));
				}
				RoomData.contributeItems(itemClasses);
				RoomData.contributeMOBs(mobClasses);
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<theclasses.size();i++)
			{
				Environmental O=(Environmental)theclasses.elementAt(i);
				String theparm=(String)theparms.elementAt(i);
				String theprice=(String)theprices.elementAt(i);
				str.append("<TR><TD WIDTH=50%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=SHP"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				if(E.getStoreInventory().contains(O))
					str.append("<OPTION SELECTED VALUE=\""+(getShopCardinality(E,O)+1)+"\">"+O.Name()+" ("+O.ID()+")");
				else
				if(RoomData.items.contains(O))
					str.append("<OPTION SELECTED VALUE=\""+O+"\">"+O.Name()+" ("+O.ID()+")");
				else
				if(RoomData.mobs.contains(O))
					str.append("<OPTION SELECTED VALUE=\""+O+"\">"+O.Name()+" ("+O.ID()+")");
				else
					str.append("<OPTION SELECTED VALUE=\""+O.ID()+"\">"+O.Name()+" ("+O.ID()+")");
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%><TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0><TR><TD WIDTH=50%>Stock:</TD>");
				str.append("<TD WIDTH=50%><INPUT TYPE=TEXT SIZE=5 NAME=SDATA"+(i+1)+" VALUE=\""+theparm+"\"></TD></TR>");
				if((theprice==null)||(theprice.equals("null")))
			        theprice="-1";
				str.append("<TR><TD WIDTH=50%>Price:</TD><TD WIDTH=50%><INPUT TYPE=TEXT SIZE=5 NAME=SPRIC"+(i+1)+" VALUE=\""+theprice+"\"></TD></TR></TABLE>");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=SHP"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an item");
			for(int i=0;i<RoomData.items.size();i++)
			{
				Item I=(Item)RoomData.items.elementAt(i);
				str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
			}
			for(int i=0;i<RoomData.mobs.size();i++)
			{
				MOB I=(MOB)RoomData.mobs.elementAt(i);
				str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
			}
			StringBuffer bufA=(StringBuffer)Resources.getResource("MUDGRINDER-STORESTUFF");
			if(bufA==null)
			{
				bufA=new StringBuffer("");
				Vector sortMeA=new Vector();
				for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					sortMeA.addElement(CMClass.className(a.nextElement()));
				for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
					sortMeA.addElement(CMClass.className(m.nextElement()));
				CMClass.addAllItemClassNames(sortMeA,true,true);
				Object[] sortedA=(new TreeSet(sortMeA)).toArray();
				for(int r=0;r<sortedA.length;r++)
				{
					String cnam=(String)sortedA[r];
					bufA.append("<OPTION VALUE=\""+cnam+"\">"+cnam);
				}
				Resources.submitResource("MUDGRINDER-STORESTUFF",bufA);
			}
			str.append(bufA);
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=50%>Stock:");
			str.append("<INPUT TYPE=TEXT SIZE=5 NAME=SDATA"+(theclasses.size()+1)+" VALUE=\"1\">");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ROOM");
		if(last==null) return " @break@";
		String mobCode=httpReq.getRequestParameter("MOB");
		if(mobCode==null) return "@break@";

		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		Room R=(Room)httpReq.getRequestObjects().get(last);
		if(R==null)
		{
			R=CMLib.map().getRoom(last);
			if(R==null)
				return "No Room?!";
			CMLib.utensils().resetRoom(R);
			httpReq.getRequestObjects().put(last,R);
		}

		MOB M=(MOB)httpReq.getRequestObjects().get(mobCode);
		if(M==null)
		{
			if(mobCode.equals("NEW"))
				M=CMClass.getMOB("GenMob");
			else
				M=RoomData.getMOBFromCode(R,mobCode);
			if((M==null)||(!M.isEligibleMonster()))
			{
				StringBuffer str=new StringBuffer("No MOB?!");
				str.append(" Got: "+mobCode);
				str.append(", Includes: ");
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M2=R.fetchInhabitant(m);
					if((M2!=null)&&(M2.isEligibleMonster()))
					   str.append(M2.Name()+"="+RoomData.getMOBCode(R,M2));
				}
                return clearWebMacros(str);
			}
			httpReq.getRequestObjects().put(mobCode,M);
		}

		// important generic<->non generic swap!
		String newClassID=httpReq.getRequestParameter("CLASSES");
		if((newClassID!=null)
		&&(!newClassID.equals(CMClass.className(M)))
		&&(CMClass.getMOB(newClassID)!=null))
			M=CMClass.getMOB(newClassID);

		boolean changedClass=((httpReq.isRequestParameter("CHANGEDCLASS"))&&(httpReq.getRequestParameter("CHANGEDCLASS")).equals("true"));
		changedClass=changedClass&&(mobCode.equals("NEW"));
		boolean changedLevel=((httpReq.isRequestParameter("CHANGEDLEVEL"))&&(httpReq.getRequestParameter("CHANGEDLEVEL")).equals("true"));
		boolean firstTime=(!httpReq.isRequestParameter("ACTION"))
				||(!(httpReq.getRequestParameter("ACTION")).equals("MODIFYMOB"))
				||(changedClass);

		if(((changedLevel)||(changedClass))&&(M.isGeneric()))
		{
			M.baseCharStats().getCurrentClass().fillOutMOB(M,Util.s_int(firstTime?"0":httpReq.getRequestParameter("LEVEL")));
			httpReq.addRequestParameters("REJUV",""+M.baseEnvStats().rejuv());
			httpReq.addRequestParameters("ARMOR",""+M.baseEnvStats().armor());
			httpReq.addRequestParameters("DAMAGE",""+M.baseEnvStats().damage());
			httpReq.addRequestParameters("SPEED",""+M.baseEnvStats().speed());
			httpReq.addRequestParameters("ATTACK",""+M.baseEnvStats().attackAdjustment());
			httpReq.addRequestParameters("MONEY",""+CMLib.beanCounter().getMoney(M));
		}

		StringBuffer str=new StringBuffer("");
		String[] okparms={"NAME","CLASSES","DISPLAYTEXT","DESCRIPTION",
						  "LEVEL","ABILITY","REJUV","MISCTEXT",
						  "RACE","GENDER","HEIGHT","WEIGHT",
						  "SPEED","ATTACK","DAMAGE","ARMOR",
						  "ALIGNMENT","MONEY","ISRIDEABLE","RIDEABLETYPE",
						  "MOBSHELD","ISSHOPKEEPER","SHOPKEEPERTYPE","ISGENERIC",
						  "ISBANKER","COININT","ITEMINT","BANKNAME","SHOPPREJ",
						  "ISDEITY","CLEREQ","CLERIT","WORREQ","WORRIT",
						  "CLESIN","WORSIN","CLEPOW","CLANID","TATTOOS","EDUCATIONS",
						  "BUDGET","DEVALRATE","INVRESETRATE","IMAGE",
                          "ISPOSTMAN","POSTCHAIN","POSTMIN","POSTLBS",
                          "POSTHOLD","POSTNEW","POSTHELD","IGNOREMASK"};
		for(int o=0;o<okparms.length;o++)
		if(parms.containsKey(okparms[o]))
		{
			String old=httpReq.getRequestParameter(okparms[o]);
			if(old==null) old="";
			switch(o)
			{
			case 0: // name
				if(firstTime) old=M.Name();
				str.append(old);
				break;
			case 1: // classes
				{
					if(firstTime) old=CMClass.className(M);
					Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-MOBS");
					if(sorted==null)
					{
						Vector sortMe=new Vector();
						for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
							sortMe.addElement(CMClass.className(m.nextElement()));
						sorted=(new TreeSet(sortMe)).toArray();
						Resources.submitResource("MUDGRINDER-MOBS",sorted);
					}
					for(int r=0;r<sorted.length;r++)
					{
						String cnam=(String)sorted[r];
						str.append("<OPTION VALUE=\""+cnam+"\"");
						if(old.equals(cnam))
							str.append(" SELECTED");
						str.append(">"+cnam);
					}
				}
				break;
			case 2: // displaytext
				if(firstTime) old=M.displayText();
				str.append(old);
				break;
			case 3: // description
				if(firstTime) old=M.description();
				str.append(old);
				break;
			case 4: // level
				if(firstTime) old=""+M.baseEnvStats().level();
				str.append(old);
				break;
			case 5: // ability;
				if(firstTime) old=""+M.baseEnvStats().ability();
				str.append(old);
				break;
			case 6: // rejuv;
				if(firstTime) old=""+M.baseEnvStats().rejuv();
				if(old.equals(""+Integer.MAX_VALUE))
					str.append("0");
				else
					str.append(old);
				break;
			case 7: // misctext
				if(firstTime) old=M.text();
				str.append(old);
				break;
			case 8: // race
				if(firstTime) old=""+M.baseCharStats().getMyRace().ID();
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R2=(Race)r.nextElement();
					str.append("<OPTION VALUE=\""+R2.ID()+"\"");
					if(R2.ID().equals(old))
						str.append(" SELECTED");
					str.append(">"+R2.name());
				}
				if((changedClass)||(changedLevel))
				{
					Race R3=CMClass.getRace(old);
					char G=(char)M.baseCharStats().getStat(CharStats.GENDER);
					if((httpReq.isRequestParameter("GENDER"))&&((httpReq.getRequestParameter("GENDER")).length()>0))
						G=(httpReq.getRequestParameter("GENDER")).charAt(0);
					if(R3!=null)
					{
						R3.setHeightWeight(M.baseEnvStats(),G);
						httpReq.addRequestParameters("WEIGHT",""+M.baseEnvStats().weight());
						httpReq.addRequestParameters("HEIGHT",""+M.baseEnvStats().height());
					}
				}
				break;
			case 9: // gender
				if(firstTime) old=""+((char)M.baseCharStats().getStat(CharStats.GENDER));
				if(old.toUpperCase().startsWith("M"))
				{
					str.append("<INPUT TYPE=RADIO NAME=GENDER CHECKED VALUE=M>Male");
					str.append("&nbsp;&nbsp;<INPUT TYPE=RADIO NAME=GENDER VALUE=F>Female");
					str.append("&nbsp;&nbsp;<INPUT TYPE=RADIO NAME=GENDER VALUE=N>Neuter");
				}
				else
				if(old.toUpperCase().startsWith("F"))
				{
					str.append("<INPUT TYPE=RADIO NAME=GENDER VALUE=M>Male");
					str.append("&nbsp;&nbsp;<INPUT TYPE=RADIO CHECKED NAME=GENDER VALUE=F>Female");
					str.append("&nbsp;&nbsp;<INPUT TYPE=RADIO NAME=GENDER VALUE=N>Neuter");
				}
				else
				{
					str.append("<INPUT TYPE=RADIO NAME=GENDER VALUE=M>Male");
					str.append("&nbsp;&nbsp;<INPUT TYPE=RADIO NAME=GENDER VALUE=F>Female");
					str.append("&nbsp;&nbsp;<INPUT CHECKED TYPE=RADIO NAME=GENDER VALUE=N>Neuter");
				}
				break;
			case 10: // height
				if(firstTime) old=""+M.baseEnvStats().height();
				str.append(old);
				break;
			case 11: // weight
				if(firstTime) old=""+M.baseEnvStats().weight();
				str.append(old);
				break;
			case 12: // speed
				if(firstTime) old=""+M.baseEnvStats().speed();
				str.append(old);
				break;
			case 13: // attack
				if(firstTime) old=""+M.baseEnvStats().attackAdjustment();
				str.append(old);
				break;
			case 14: // damage
				if(firstTime) old=""+M.baseEnvStats().damage();
				str.append(old);
				break;
			case 15: // armor
				if(firstTime) old=""+M.baseEnvStats().armor();
				str.append(old);
				break;
			case 16: // alignment
			    if(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null)
			    {
					if(firstTime) old=""+M.fetchFaction(CMLib.factions().AlignID());
					for(int v=1;v<Faction.ALIGN_NAMES.length;v++)
					{
					    str.append("<OPTION VALUE="+Faction.ALIGN_NAMES[v]);
					    if(old.equalsIgnoreCase(Faction.ALIGN_NAMES[v]))
					        str.append(" SELECTED");
					    str.append(">"+Util.capitalizeAndLower(Faction.ALIGN_NAMES[v].toLowerCase()));
					}
			    }
				break;
			case 17: // money
				if(firstTime)
				{ 
				    old=""+CMLib.beanCounter().getMoney(M); 
				    CMLib.beanCounter().clearInventoryMoney(M,null);
				}
				str.append(old);
				break;
			case 18: // is rideable
				if(M instanceof Rideable) return "true";
                return "false";
			case 19: // rideable type
				if((firstTime)&&(M instanceof Rideable))
					old=""+((Rideable)M).rideBasis();
				for(int r=0;r<Rideable.RIDEABLE_DESCS.length;r++)
				{
					str.append("<OPTION VALUE=\""+r+"\"");
					if(r==Util.s_int(old))
						str.append(" SELECTED");
					str.append(">"+Rideable.RIDEABLE_DESCS[r]);
				}
				break;
			case 20: // rideable capacity
				if((firstTime)&&(M instanceof Rideable))
					old=""+((Rideable)M).riderCapacity();
				str.append(old);
				break;
			case 21: // is shopkeeper
				if(M instanceof ShopKeeper) return "true";
                return "false";
			case 22: // shopkeeper type
				if((firstTime)&&(M instanceof ShopKeeper))
					old=""+((ShopKeeper)M).whatIsSold();
				if(M instanceof Banker)
				{
					int r=ShopKeeper.DEAL_BANKER;
					str.append("<OPTION VALUE=\""+r+"\"");
					if(r==Util.s_int(old))
						str.append(" SELECTED");
					str.append(">"+ShopKeeper.SOLDCODES[r]);
					r=ShopKeeper.DEAL_CLANBANKER;
					str.append("<OPTION VALUE=\""+r+"\"");
					if(r==Util.s_int(old))
						str.append(" SELECTED");
					str.append(">"+ShopKeeper.SOLDCODES[r]);
				}
				else
				for(int r=0;r<ShopKeeper.SOLDCODES.length;r++)
				{
					if((r!=ShopKeeper.DEAL_CLANBANKER)&&(r!=ShopKeeper.DEAL_BANKER))
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==Util.s_int(old))
							str.append(" SELECTED");
						str.append(">"+ShopKeeper.SOLDCODES[r]);
					}
				}
				break;
			case 23:
				if(M.isGeneric()) return "true";
                return "false";
			case 24: // is banker
				if(M instanceof Banker) return "true";
                return "false";
			case 25: // coin interest
				if((firstTime)&&(M instanceof Banker))
					old=""+((Banker)M).getCoinInterest();
				str.append(old);
				break;
			case 26: // item interest
				if((firstTime)&&(M instanceof Banker))
					old=""+((Banker)M).getItemInterest();
				str.append(old);
				break;
			case 27: // bank name
				if((firstTime)&&(M instanceof Banker))
					old=""+((Banker)M).bankChain();
				str.append(old);
				break;
			case 28: // prejudice factors
				if((firstTime)&&(M instanceof ShopKeeper))
					old=((ShopKeeper)M).prejudiceFactors();
				str.append(old);
				break;
			case 29: // is deity
				if(M instanceof Deity) return "true";
                return "false";
			case 30: // cleric requirements
				if((firstTime)&&(M instanceof Deity))
					old=((Deity)M).getClericRequirements();
				str.append(old);
				break;
			case 31: // cleric ritual
				if((firstTime)&&(M instanceof Deity))
					old=((Deity)M).getClericRitual();
				str.append(old);
				break;
			case 32: // worship requirements
				if((firstTime)&&(M instanceof Deity))
					old=((Deity)M).getWorshipRequirements();
				str.append(old);
				break;
			case 33: // worship ritual
				if((firstTime)&&(M instanceof Deity))
					old=((Deity)M).getWorshipRitual();
				str.append(old);
				break;
			case 34: // cleric sin
				if((firstTime)&&(M instanceof Deity))
					old=((Deity)M).getClericSin();
				str.append(old);
				break;
			case 35: // worshipper sin
				if((firstTime)&&(M instanceof Deity))
					old=((Deity)M).getWorshipSin();
				str.append(old);
				break;
			case 36: // cleric power
				if((firstTime)&&(M instanceof Deity))
					old=((Deity)M).getClericPowerup();
				str.append(old);
				break;
			case 37: // clanid
				if(firstTime) old=M.getClanID();
				str.append(old);
				break;
			case 38: // tattoos
				if(firstTime)
				{
					old="";
					for(int i=0;i<M.numTattoos();i++)
						old+=M.fetchTattoo(i)+";";
				}
				str.append(old);
				break;
			case 39: // educations
				if(firstTime)
				{
					old="";
					for(int i=0;i<M.numEducations();i++)
						old+=M.fetchEducation(i)+";";
				}
				str.append(old);
				break;
			case 40: // budget
				if((firstTime)&&(M instanceof ShopKeeper))
					old=((ShopKeeper)M).budget();
				str.append(old);
				break;
			case 41: // devaluation rate
				if((firstTime)&&(M instanceof ShopKeeper))
					old=((ShopKeeper)M).devalueRate();
				str.append(old);
				break;
			case 42: // inventory reset rate
				if((firstTime)&&(M instanceof ShopKeeper))
					old=""+((ShopKeeper)M).invResetRate();
				str.append(old);
				break;
			case 43: // image
				if(firstTime)
					old=M.rawImage();
				str.append(old);
				break;
            case 44: // ispostman
                if(M instanceof PostOffice) return "true";
                return "false";
            case 45: // postal chain
                if((firstTime)&&(M instanceof PostOffice))
                    old=((PostOffice)M).postalChain();
                str.append(old);
                break;
            case 46: // minimum postage
                if((firstTime)&&(M instanceof PostOffice))
                    old=""+((PostOffice)M).minimumPostage();
                str.append(old);
                break;
            case 47: // postage per pound
                if((firstTime)&&(M instanceof PostOffice))
                    old=""+((PostOffice)M).postagePerPound();
                str.append(old);
                break;
            case 48: // holding fee per pound
                if((firstTime)&&(M instanceof PostOffice))
                    old=""+((PostOffice)M).holdFeePerPound();
                str.append(old);
                break;
            case 49: // new box fee
                if((firstTime)&&(M instanceof PostOffice))
                    old=""+((PostOffice)M).feeForNewBox();
                str.append(old);
                break;
            case 50: // max held months
                if((firstTime)&&(M instanceof PostOffice))
                    old=""+((PostOffice)M).maxMudMonthsHeld();
                str.append(old);
                break;
            case 51:
                if((firstTime)&&(M instanceof ShopKeeper))
                    old=((ShopKeeper)M).ignoreMask();
                str.append(old);
                break;
			}
			if(firstTime)
				httpReq.addRequestParameters(okparms[o],old.equals("checked")?"on":old);
		}
		str.append(ExitData.dispositions(M,firstTime,httpReq,parms));
		str.append(MobData.senses(M,firstTime,httpReq,parms));
		str.append(AreaData.affectsNBehaves(M,httpReq,parms));
		str.append(factions(M,httpReq,parms));
		str.append(MobData.abilities(M,httpReq,parms));
		if(M instanceof Deity)
		{
			str.append(MobData.blessings((Deity)M,httpReq,parms));
			str.append(MobData.curses((Deity)M,httpReq,parms));
			str.append(MobData.powers((Deity)M,httpReq,parms));
		}
		if(M instanceof ShopKeeper)
			str.append(MobData.shopkeeper((ShopKeeper)M,httpReq,parms));

		if(parms.containsKey("ITEMLIST"))
		{
			Vector classes=new Vector();
			Vector itemlist=null;
			if(httpReq.isRequestParameter("ITEM1"))
			{
				itemlist=RoomData.items;
				for(int i=1;;i++)
				{
					String MATCHING=httpReq.getRequestParameter("ITEM"+i);
					if(MATCHING==null)
						break;
					Item I2=RoomData.getItemFromAnywhere(M,MATCHING);
					if(I2!=null)
						classes.addElement(I2);
				}
			}
			else
			{
				for(int m=0;m<M.inventorySize();m++)
				{
					Item I2=M.fetchInventory(m);
					classes.addElement(I2);
				}
				itemlist=RoomData.contributeItems(classes);
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<classes.size();i++)
			{
				Item I=(Item)classes.elementAt(i);
				str.append("<TR>");
				str.append("<TD WIDTH=90%>");
				str.append("<SELECT ONCHANGE=\"DelItem(this);\" NAME=ITEM"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				if(M.isMine(I))
					str.append("<OPTION SELECTED VALUE=\""+RoomData.getItemCode(classes,I)+"\">"+I.Name()+" ("+I.ID()+")"+((I.container()==null)?"":(" in "+I.container().Name()))+((I.amWearingAt(Item.INVENTORY))?"":" (worn/wielded)"));
				else
				if(itemlist.contains(I))
					str.append("<OPTION SELECTED VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
				else
					str.append("<OPTION SELECTED VALUE=\""+I.ID()+"\">"+I.Name()+" ("+I.ID()+")");
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=EDITITEM"+(i+1)+" VALUE=EDIT ONCLICK=\"EditItem('"+RoomData.getItemCode(classes,I)+"');\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
			str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME=ITEM"+(classes.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a new Item");
			for(int i=0;i<itemlist.size();i++)
			{
				Item I=(Item)itemlist.elementAt(i);
				str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
			}
			StringBuffer mposs=(StringBuffer)Resources.getResource("MUDGRINDER-MOBPOSS");
			if(mposs==null)
			{
				mposs=new StringBuffer("");
				Vector sortMe=new Vector();
				CMClass.addAllItemClassNames(sortMe,true,true);
				Object[] sorted=(new TreeSet(sortMe)).toArray();
				for(int i=0;i<sorted.length;i++)
					mposs.append("<OPTION VALUE=\""+(String)sorted[i]+"\">"+(String)sorted[i]);
				Resources.submitResource("MUDGRINDER-POSS",mposs);
			}
			str.append(mposs);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=BUTTON NAME=ADDITEM VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
			str.append("</TD></TR></TABLE>");
		}

		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
        return clearWebMacros(strstr);
	}
}
