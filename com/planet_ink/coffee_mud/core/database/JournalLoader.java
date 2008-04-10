package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import java.sql.*;
import java.util.*;


/* 
   Copyright 2000-2008 Bo Zimmerman

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
public class JournalLoader
{
	protected DBConnector DB=null;
	public JournalLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	public synchronized int DBCount(String Journal, String from, String to)
	{
		int ct=0;
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+Journal+"'");
			while(R.next())
			{
				if((from!=null)&&(!from.equalsIgnoreCase(DBConnections.getRes(R,"CMFROM"))))
				   continue;
				if((to!=null)&&(!to.equalsIgnoreCase(DBConnections.getRes(R,"CMTONM"))))
				   continue;
				ct++;
			}
			DB.DBDone(D);
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
			if(D!=null) DB.DBDone(D);
			return ct;
		}
		return ct;
	}
    
    public String DBGetRealName(String possibleName)
    {
        DBConnection D=null;
        String realName=null;
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+possibleName+"'");
            if(R.next())
            {
                realName=DBConnections.getRes(R,"CMJRNL");
                if(realName.length()==0)
                {
                    DB.DBDone(D);
                    return realName=null;
                }
            }
            DB.DBDone(D);
        }
        catch(Exception sqle)
        {
            Log.errOut("Journal",sqle);
            if(D!=null) DB.DBDone(D);
        }
        return realName;
    }
	
	public long DBReadNewJournalDate(String Journal, String name)
	{
		Hashtable TABLE=(Hashtable)Resources.getResource("JOURNALDATECACHE");
		if(TABLE==null)
		{
			TABLE=new Hashtable();
			Resources.submitResource("JOURNALDATECACHE",TABLE);
		}
		synchronized(TABLE)
		{
			Hashtable H=(Hashtable)TABLE.get(Journal);
			if(H!=null)
			{
				Long l=(Long)H.get(name);
				Long l2=(Long)H.get("ALL");
				if((l!=null)&&(l2==null)) return l.longValue();
				if((l2!=null)&&(l==null)) return l2.longValue();
				if((l!=null)&&(l2!=null)) return l.longValue()>l2.longValue()?l.longValue():l2.longValue();
				return 0;
			}
			Vector V=DBRead(Journal);
			H=new Hashtable();
			TABLE.put(Journal,H);
			if(V==null) return 0;
			if(V.size()==0) return 0;
			for(int v=0;v<V.size();v++)
			{
				Vector V2=(Vector)V.elementAt(v);
				String to=(String)V2.elementAt(3);
				String compdate=(String)V2.elementAt(6);
				if(to.equalsIgnoreCase("all"))
				{
					Long l2=(Long)H.get("ALL");
					if((l2==null)||(l2.longValue()<CMath.s_long(compdate)))
					{
						if(H.containsKey("ALL")) H.remove("ALL");
						H.put("ALL",new Long(CMath.s_long(compdate)));
					}
				}
				else
				{
					Long l2=(Long)H.get(to);
					if((l2==null)||(l2.longValue()<CMath.s_long(compdate)))
					{
						if(H.containsKey(to)) H.remove(to);
						H.put(to,new Long(CMath.s_long(compdate)));
					}
					String from=(String)V2.elementAt(1);
					l2=(Long)H.get(from); // from
					if((l2==null)||(l2.longValue()<CMath.s_long(compdate)))
					{
						if(H.containsKey(from)) H.remove(from);
						H.put(from,new Long(CMath.s_long(compdate)));
					}
				}
			}
			return DBReadNewJournalDate(Journal,name);
		}
	}
	
	public synchronized Vector DBRead(String Journal)
	{
		Vector journal=new Vector();
		if(Journal==null)
		{
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				ResultSet R=D.query("SELECT * FROM CMJRNL");
				while(R.next())
				{
					String which=DBConnections.getRes(R,"CMJRNL");
					if(!journal.contains(which))
						journal.addElement(which);
				}
				DB.DBDone(D);
			}
			catch(Exception sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DB.DBDone(D);
				return null;
			}
		}
		else
		{
			//Resources.submitResource("JOURNAL_"+Journal);
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				String str="SELECT * FROM CMJRNL WHERE CMJRNL='"+Journal+"'";
				ResultSet R=D.query(str);
				while(R.next())
				{
					Vector entry=new Vector();
					entry.addElement(DBConnections.getRes(R,"CMJKEY"));
					entry.addElement(DBConnections.getRes(R,"CMFROM"));
					String datestr=DBConnections.getRes(R,"CMDATE");
					entry.addElement(datestr);
					entry.addElement(DBConnections.getRes(R,"CMTONM"));
					entry.addElement(DBConnections.getRes(R,"CMSUBJ"));
					entry.addElement(DBConnections.getRes(R,"CMMSGT"));
					
					int datestrdex=datestr.indexOf("/");
					if(datestrdex>=0)
					{
						entry.addElement(datestr.substring(datestrdex+1));
						entry.setElementAt(datestr.substring(0,datestrdex),2);
					}
					else
						entry.addElement(datestr);
					
					String subject=(String)entry.elementAt(4);
					if((subject.toUpperCase().startsWith("MOTD"))
					||(subject.toUpperCase().startsWith("MOTM"))
					||(subject.toUpperCase().startsWith("MOTY")))
					{
						char c=subject.toUpperCase().charAt(3);
						subject=subject.substring(4);
						entry.setElementAt(subject,4);
						long last=CMath.s_long((String)entry.elementAt(2));
						if(c=='D') last=last+TimeManager.MILI_DAY;
						else
						if(c=='M') last=last+TimeManager.MILI_MONTH;
						else
						if(c=='Y') last=last+TimeManager.MILI_YEAR;
						entry.setElementAt(""+last,6);
					}
					
					journal.addElement(entry);
				}
				DB.DBDone(D);
			}
			catch(Exception sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DB.DBDone(D);
				return null;
			}
				
			Vector oldJournal=journal;
			journal=new Vector();
			while(oldJournal.size()>0)
			{
				Vector useEntry=null;
				long byDate=Long.MAX_VALUE;
				for(int j=0;j<oldJournal.size();j++)
				{
					Vector entry=(Vector)oldJournal.elementAt(j);
					String datestr=(String)entry.elementAt(2);
					long date=0;
					if(datestr.indexOf("/")>=0)
						date=CMath.s_long(datestr.substring(0,datestr.indexOf("/")));
					else
						date=CMath.s_long(datestr);
					
					if(date<byDate)
					{
						byDate=date;
						useEntry=entry;
					}
				}
				if(useEntry!=null)
				{
					oldJournal.removeElement(useEntry);
					journal.addElement(useEntry);
				}
				else
				{
					journal.addElement(oldJournal.elementAt(0));
					oldJournal.removeElementAt(0);
				}
			}
		}
		return journal;
	}
	public synchronized Vector DBReadCached(String Journal)
	{
		if(Journal==null) return DBRead(Journal);
		Vector journal=(Vector)Resources.getResource("JOURNAL_"+Journal);
		if(journal==null)
		{
			journal=DBRead(Journal);
		}
		if(journal!=null)
			Resources.submitResource("JOURNAL_"+Journal,journal);
		return journal;
	}

	public int getFirstMsgIndex(Vector journal, 
									   String from, 
									   String to, 
									   String subj)
	{
		if(journal==null) return -1;
		for(int i=0;i<journal.size();i++)
		{
			Vector V=(Vector)journal.elementAt(i);
			if((from!=null)&&(!((String)V.elementAt(1)).equalsIgnoreCase(from)))
				continue;
			if((to!=null)&&(!((String)V.elementAt(3)).equalsIgnoreCase(to)))
				continue;
			if((subj!=null)&&(!((String)V.elementAt(4)).equalsIgnoreCase(subj)))
				continue;
			return i;
		}
		return -1;
	}
	
	public synchronized void DBDelete(String oldkey)
	{
		DB.update("DELETE FROM CMJRNL WHERE CMJKEY='"+oldkey+"'");
	}
	
	public synchronized void DBUpdateJournal(String key, String subject, String msg)
	{
		DB.update("UPDATE CMJRNL SET CMSUBJ='"+subject+"', CMMSGT='"+msg+"' WHERE CMJKEY='"+key+"'");
	}
	
	public synchronized void DBDeletePlayerData(String name)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
			{
				Vector keys=new Vector();
				ResultSet R=D.query("SELECT * FROM CMJRNL");
				while(R.next())
				{
					String playerID2=DBConnections.getRes(R,"CMJKEY");
					String section2=DBConnections.getRes(R,"CMTONM");
					if(section2.equalsIgnoreCase(name))
						keys.addElement(playerID2);
				}
				for(int i=0;i<keys.size();i++)
				{
					DB.DBDone(D);
					D=DB.DBFetch();
					D.update("DELETE FROM CMJRNL WHERE CMJKEY='"+((String)keys.elementAt(i))+"'",0);
				}
			}
			else
				D.update("DELETE FROM CMJRNL WHERE CMTONM='"+name+"'",0);
		}
		catch(Exception sqle)
		{
			Log.errOut("JournalLoader",sqle.getMessage());
		}
		if(D!=null) DB.DBDone(D);
		
	}
	
	public synchronized void DBDelete(String Journal, int which)
	{
		if(which<0)
		{
			Vector journal=DBRead(Journal);
			if(journal==null) return;
			DB.update("DELETE FROM CMJRNL WHERE CMJRNL='"+Journal+"'");
		}
		else
		if(which==Integer.MAX_VALUE)
		{
			DB.update("DELETE FROM CMJRNL WHERE CMJRNL='"+Journal+"'");
		}
		else
		{
			Vector journal=DBRead(Journal);
			if(journal==null) return;
			if(which>=journal.size()) return;
			Vector entry=(Vector)journal.elementAt(which);
			String oldkey=(String)entry.elementAt(0);
			DB.update("DELETE FROM CMJRNL WHERE CMJKEY='"+oldkey+"'");
		}
	}
	
	public void updateJournalDateCacheIfNecessary(Hashtable H, 
														 String to, 
														 String from,
														 long date)
	{
		if(to.equalsIgnoreCase("all"))
		{
			Long l2=(Long)H.get("ALL");
			if((l2==null)||(l2.longValue()<System.currentTimeMillis()))
			{
				if(H.containsKey("ALL")) H.remove("ALL");
				H.put("ALL",new Long(System.currentTimeMillis()));
			}
		}
		else
		{
			Long l2=(Long)H.get(to);
			if((l2==null)||(l2.longValue()<System.currentTimeMillis()))
			{
				if(H.containsKey(to)) H.remove(to);
				H.put(to,new Long(System.currentTimeMillis()));
			}
			l2=(Long)H.get(from);
			if((l2==null)||(l2.longValue()<System.currentTimeMillis()))
			{
				if(H.containsKey(from)) H.remove(from);
				H.put(from,new Long(System.currentTimeMillis()));
			}
		}
	}
	
	public synchronized void DBWrite(String Journal, 
											String from, 
											String to, 
											String subject, 
											String message, 
											int which)
	{
		String date=System.currentTimeMillis()+"";
		if(which>=0)
		{
			Vector journal=DBRead(Journal);
			if(journal==null) return;
			if(which>=journal.size()) return;
			Vector entry=(Vector)journal.elementAt(which);
			String olddate=(String)entry.elementAt(2);
			int olddatedex=olddate.indexOf("/");
			if(olddatedex>=0) olddate=olddate.substring(0,olddatedex);
			String oldkey=(String)entry.elementAt(0);
			String oldmsg=(String)entry.elementAt(5);
			message=oldmsg+DatabaseEngine.JOURNAL_BOUNDARY
                          +"^yReply from^N: "+from+"%0D"
                          +"^yDate/Time ^N: "+CMLib.time().date2String(System.currentTimeMillis())+"%0D"
                          +message;
			DB.update("UPDATE CMJRNL SET CMDATE='"+olddate+"/"+date+"', CMMSGT='"+message+"' WHERE CMJKEY='"+oldkey+"'");
			Hashtable TABLE=(Hashtable)Resources.getResource("JOURNALDATECACHE");
			if(TABLE!=null)
			{
				synchronized(TABLE)
				{
					Hashtable H=(Hashtable)TABLE.get(Journal);
					if(H!=null)
						updateJournalDateCacheIfNecessary(H,
														  (String)entry.elementAt(3),
														  (String)entry.elementAt(1),
														  System.currentTimeMillis());
				}
			}
		}
		else
		{
			if(subject.length()>255) subject=subject.substring(0,255);
			DB.update(
			"INSERT INTO CMJRNL ("
			+"CMJKEY, "
			+"CMJRNL, "
			+"CMFROM, "
			+"CMDATE, "
			+"CMTONM, "
			+"CMSUBJ, "
			+"CMMSGT "
			+") VALUES ('"
			+(Journal+from+date+Math.random())
			+"','"+Journal
			+"','"+from
			+"','"+date
			+"','"+to
			+"','"+subject
			+"','"+message+"')");
			Hashtable TABLE=(Hashtable)Resources.getResource("JOURNALDATECACHE");
			if(TABLE!=null)
			{
				synchronized(TABLE)
				{
					Hashtable H=(Hashtable)TABLE.get(Journal);
					if(H!=null)
						updateJournalDateCacheIfNecessary(H,to,from,System.currentTimeMillis());
				}
			}
		}
	}
}
