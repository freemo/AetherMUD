package com.planet_ink.coffee_mud.core.database;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
public class DataLoader
{
	protected DBConnector DB=null;
	public DataLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	public static Vector DBReadRaces()
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMGRAC");
			while(R.next())
			{
				Vector V=new Vector();
				V.addElement(DBConnections.getRes(R,"CMRCID"));
				V.addElement(DBConnections.getRes(R,"CMRDAT"));
				rows.addElement(V);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
		return rows;
	}
	public static Vector DBReadClasses()
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCCAC");
			while(R.next())
			{
				Vector V=new Vector();
				V.addElement(DBConnections.getRes(R,"CMCCID"));
				V.addElement(DBConnections.getRes(R,"CMCDAT"));
				rows.addElement(V);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
		return rows;
	}
	public static Vector DBRead(String playerID, String section)
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=null;
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
			else
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'");
			while(R.next())
			{
				String playerID2=DBConnections.getRes(R,"CMPLID");
				String section2=DBConnections.getRes(R,"CMSECT");
				if(section2.equalsIgnoreCase(section))
				{
					Vector V=new Vector();
					V.addElement(playerID2);
					V.addElement(section2);
					V.addElement(DBConnections.getRes(R,"CMPKEY"));
					V.addElement(DBConnections.getRes(R,"CMPDAT"));
					rows.addElement(V);
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
		return rows;
	}
	public static Vector DBReadAllPlayerData(String playerID)
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
			while(R.next())
			{
				String playerID2=DBConnections.getRes(R,"CMPLID");
				if(playerID2.equalsIgnoreCase(playerID))
				{
					Vector V=new Vector();
					V.addElement(playerID2);
					V.addElement(DBConnections.getRes(R,"CMSECT"));
					V.addElement(DBConnections.getRes(R,"CMPKEY"));
					V.addElement(DBConnections.getRes(R,"CMPDAT"));
					rows.addElement(V);
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
		return rows;
	}

	public static int DBCount(String playerID, String section)
	{
		DBConnection D=null;
		int rows=0;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=null;
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
			else
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'");
			while(R.next())
			{
				String section2=DBConnections.getRes(R,"CMSECT");
				if(section2.equalsIgnoreCase(section))
					rows++;
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
		return rows;
	}
	public static Vector DBReadKey(String section, String keyMask)
	{
		DBConnection D=null;
		Vector rows=new Vector();
        Pattern P=Pattern.compile(keyMask, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMSECT='"+section+"'");
			while(R.next())
			{
				String plid=DBConnections.getRes(R,"CMPLID");
				String sect=DBConnections.getRes(R,"CMSECT");
				String key=DBConnections.getRes(R,"CMPKEY");
			    Matcher M=P.matcher(key);
			    if(M.find())
			    {
					Vector V=new Vector();
					V.addElement(plid);
					V.addElement(sect);
					V.addElement(key);
					V.addElement(DBConnections.getRes(R,"CMPDAT"));
					rows.addElement(V);
			    }
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
		return rows;
	}

	public static Vector DBReadKey(String key)
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPKEY='"+key+"'");
			while(R.next())
			{
				String plid=DBConnections.getRes(R,"CMPLID");
				String sect=DBConnections.getRes(R,"CMSECT");
				key=DBConnections.getRes(R,"CMPKEY");
				Vector V=new Vector();
				V.addElement(plid);
				V.addElement(sect);
				V.addElement(key);
				V.addElement(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(V);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
		return rows;
	}

	public static Vector DBRead(String playerID, String section, String key)
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=null;
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
				R=D.query("SELECT * FROM CMPDAT WHERE CMPKEY='"+key+"'");
			else
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"' AND CMPKEY='"+key+"'");
			while(R.next())
			{
				String playerID2=DBConnections.getRes(R,"CMPLID");
				String section2=DBConnections.getRes(R,"CMSECT");
				if((playerID2.equalsIgnoreCase(playerID))
				&&(section2.equalsIgnoreCase(section)))
				{
					Vector V=new Vector();
					V.addElement(playerID2);
					V.addElement(section2);
					V.addElement(DBConnections.getRes(R,"CMPKEY"));
					V.addElement(DBConnections.getRes(R,"CMPDAT"));
					rows.addElement(V);
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
		return rows;
	}
	public static Vector DBRead(String section)
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMSECT='"+section+"'");
			while(R.next())
			{
				Vector V=new Vector();
				V.addElement(DBConnections.getRes(R,"CMPLID"));
				V.addElement(DBConnections.getRes(R,"CMSECT"));
				V.addElement(DBConnections.getRes(R,"CMPKEY"));
				V.addElement(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(V);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
		return rows;
	}

    public static Vector DBRead(String playerID, Vector sections)
    {
        DBConnection D=null;
        Vector rows=new Vector();
        if((sections==null)||(sections.size()==0))
            return rows;
        try
        {
            D=DBConnector.DBFetch();
            if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
            {
                ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
                while(R.next())
                {
                    String section2=DBConnections.getRes(R,"CMSECT");
                    if(sections.contains(section2))
                    {
                        Vector V=new Vector();
                        V.addElement(playerID);
                        V.addElement(section2);
                        V.addElement(DBConnections.getRes(R,"CMPKEY"));
                        V.addElement(DBConnections.getRes(R,"CMPDAT"));
                        rows.addElement(V);
                    }
                }
            }
            else
            {
                StringBuffer orClause=new StringBuffer("");
                for(int i=0;i<sections.size();i++)
                    orClause.append("CMSECT='"+((String)sections.elementAt(i))+"' OR ");
                String clause=orClause.toString().substring(0,orClause.length()-4);
                ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND ("+clause+")");
                while(R.next())
                {
                    Vector V=new Vector();
                    V.addElement(DBConnections.getRes(R,"CMPLID"));
                    V.addElement(DBConnections.getRes(R,"CMSECT"));
                    V.addElement(DBConnections.getRes(R,"CMPKEY"));
                    V.addElement(DBConnections.getRes(R,"CMPDAT"));
                    rows.addElement(V);
                }
            }
        }
        catch(Exception sqle)
        {
            Log.errOut("DataLoader",sqle);
        }
        if(D!=null) DBConnector.DBDone(D);
        // log comment
        return rows;
    }
    
    public static void DBReCreate(String name, String section, String key, String xml)
    {
    	synchronized(("RECREATE"+key).intern())
    	{
			DBConnection D=null;
			try
			{
				D=DBConnector.DBFetch();
				ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPKEY='"+key+"'");
				boolean exists=R.next();
				DBConnector.DBDone(D);
				if(exists)
					DBUpdate(key,xml);
				else
					DBCreate(name,section,key,xml);
				return;
	        }
	        catch(Exception sqle)
	        {
	            Log.errOut("DataLoader",sqle);
	        }
	        if(D!=null) DBConnector.DBDone(D);
    	}
    }
    
    public static void DBUpdate(String key, String xml)
    {
    	DBConnector.update("UPDATE CMPDAT SET CMPDAT='"+xml+"' WHERE CMPKEY='"+key+"'");
    }
    
	public static void DBDelete(String playerID, String section)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
			{
				Vector keys=new Vector();
				ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
				while(R.next())
				{
					String section2=DBConnections.getRes(R,"CMSECT");
					if(section.equalsIgnoreCase(section2))
						keys.addElement(DBConnections.getRes(R,"CMPKEY"));
				}
				for(int i=0;i<keys.size();i++)
				{
					DBConnector.DBDone(D);
					D=DBConnector.DBFetch();
					D.update("DELETE FROM CMPDAT WHERE CMPKEY='"+((String)keys.elementAt(i))+"'",0);
				}
			}
			else
			{
				D.update("DELETE FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'",0);
				try{Thread.sleep(500);}catch(Exception e){}
				if(DBConnector.queryRows("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'")>0)
					Log.errOut("Failed to delete data for player "+playerID+".");
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
	}
	public static void DBDeletePlayer(String playerID)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
			{
				Vector keys=new Vector();
				ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
				while(R.next())
					keys.addElement(DBConnections.getRes(R,"CMPKEY"));
				for(int i=0;i<keys.size();i++)
				{
					DBConnector.DBDone(D);
					D=DBConnector.DBFetch();
					D.update("DELETE FROM CMPDAT WHERE CMPKEY='"+((String)keys.elementAt(i))+"'",0);
				}
			}
			else
			{
				D.update("DELETE FROM CMPDAT WHERE CMPLID='"+playerID+"'",0);
				try{Thread.sleep(500);}catch(Exception e){}
				if(DBConnector.queryRows("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'")>0)
					Log.errOut("Failed to delete data for player "+playerID+".");
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
	}
	public static void DBDelete(String playerID, String section, String key)
	{

		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
				D.update("DELETE FROM CMPDAT WHERE CMPKEY='"+key+"'",0);
			else
			{
				D.update("DELETE FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"' AND CMPKEY='"+key+"'",0);
				try{Thread.sleep(500);}catch(Exception e){}
				if(DBConnector.queryRows("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"' AND CMPKEY='"+key+"'")>0)
					Log.errOut("Failed to delete data for player "+playerID+".");
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
	}
	public static void DBDeleteRace(String raceID)
	{
		DBConnector.update("DELETE FROM CMGRAC WHERE CMRCID='"+raceID+"'");
	}
	public static void DBDeleteClass(String classID)
	{
		DBConnector.update("DELETE FROM CMCCAC WHERE CMCCID='"+classID+"'");
	}
	public static void DBDelete(String section)
	{
		DBConnector.update("DELETE FROM CMPDAT WHERE CMSECT='"+section+"'");
		try{Thread.sleep(500);}catch(Exception e){}
		if(DBConnector.queryRows("SELECT * FROM CMPDAT WHERE CMSECT='"+section+"'")>0)
			Log.errOut("Failed to delete data from section "+section+".");
	}
	public static void DBCreateRace(String raceID, String data)
	{
		DBConnector.update(
		 "INSERT INTO CMGRAC ("
		 +"CMRCID, "
		 +"CMRDAT "
		 +") values ("
		 +"'"+raceID+"',"
		 +"'"+data+" '"
		 +")");
	}
	public static void DBCreateClass(String classID, String data)
	{
		DBConnector.update(
		 "INSERT INTO CMCCAC ("
		 +"CMCCID, "
		 +"CMCDAT "
		 +") values ("
		 +"'"+classID+"',"
		 +"'"+data+" '"
		 +")");
	}
	public static void DBCreate(String player, String section, String key, String data)
	{
		DBConnector.update(
		 "INSERT INTO CMPDAT ("
		 +"CMPLID, "
		 +"CMSECT, "
		 +"CMPKEY, "
		 +"CMPDAT "
		 +") values ("
		 +"'"+player+"',"
		 +"'"+section+"',"
		 +"'"+key+"',"
		 +"'"+data+" '"
		 +")");
	}
}
