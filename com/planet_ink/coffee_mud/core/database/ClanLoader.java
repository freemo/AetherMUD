package com.planet_ink.coffee_mud.system.database;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */
public class ClanLoader
{
	private static int currentRecordPos=1;
	private static int recordCount=0;

	public static void updateBootStatus(String loading)
	{
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: Loading "+loading+" ("+currentRecordPos+" of "+recordCount+")");
	}

	public static void DBRead()
	{
		DBConnection D=null;
	    Clan C=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCLAN");
			recordCount=DBConnector.getRecordCount(D,R);
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String name=DBConnections.getRes(R,"CMCLID");
				C=Clans.getClanType(Util.s_int(DBConnections.getRes(R,"CMTYPE")));
				C.setName(name);
				C.setPremise(DBConnections.getRes(R,"CMDESC"));
				C.setAcceptanceSettings(DBConnections.getRes(R,"CMACPT"));
				C.setPolitics(DBConnections.getRes(R,"CMPOLI"));
				C.setRecall(DBConnections.getRes(R,"CMRCLL"));
				C.setDonation(DBConnections.getRes(R,"CMDNAT"));
				C.setStatus(Util.s_int(DBConnections.getRes(R, "CMSTAT")));
				C.setMorgue(DBConnections.getRes(R,"CMMORG"));
				C.setTrophies(Util.s_int(DBConnections.getRes(R, "CMTROP")));
				Clans.addClan(C);
		        updateBootStatus("Clans");
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("Clan",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment
	}

	public static void DBUpdate(Clan C)
	{
		String str="UPDATE CMCLAN SET "
				+"CMDESC='"+C.getPremise()+"',"
				+"CMACPT='"+C.getAcceptanceSettings()+"',"
				+"CMPOLI='"+C.getPolitics()+"',"
				+"CMRCLL='"+C.getRecall()+"',"
				+"CMDNAT='"+C.getDonation()+"',"
				+"CMSTAT="+C.getStatus()+","
				+"CMMORG='"+C.getMorgue()+"',"
				+"CMTROP="+C.getTrophies()+""
				+" WHERE CMCLID='"+C.ID()+"'";
		DBConnector.update(str);
	}

	public static void DBCreate(Clan C)
	{
		if(C.ID().length()==0) return;
		String str="INSERT INTO CMCLAN ("
			+"CMCLID,"
			+"CMTYPE,"
			+"CMDESC,"
			+"CMACPT,"
			+"CMPOLI,"
			+"CMRCLL,"
			+"CMDNAT,"
			+"CMSTAT,"
			+"CMMORG,"
			+"CMTROP"
			+") values ("
			+"'"+C.ID()+"',"
			+""+C.getType()+","
			+"'"+C.getPremise()+"',"
			+"'"+C.getAcceptanceSettings()+"',"
			+"'"+C.getPolitics()+"',"
			+"'"+C.getRecall()+"',"
			+"'"+C.getDonation()+"',"
			+""+C.getStatus()+","
			+"'"+C.getMorgue()+"',"
			+""+C.getTrophies()
			+")";
			DBConnector.update(str);
	}

	public static void DBDelete(Clan C)
	{
		DBConnector.update("DELETE FROM CMCLAN WHERE CMCLID='"+C.ID()+"'");
	}

}