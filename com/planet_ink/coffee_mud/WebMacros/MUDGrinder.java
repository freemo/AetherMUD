package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.WebMacros.grinder.*;
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
public class MUDGrinder extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		if(parms!=null)
		if(parms.containsKey("AREAMAP"))
		{
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "";
			if(A.properSize()==0)
				GrinderRooms.createLonelyRoom(A,null,0,false);
			GrinderFlatMap map=null;
			if((httpReq.getRequestParameter("MAPSTYLE")!=null)
			&&(httpReq.getRequestParameter("MAPSTYLE").length()>0))
				map=new GrinderMap(A);
			else
				map=new GrinderFlatMap(A);
			map.rePlaceRooms();
			if(!(map instanceof GrinderMap)) map.crowdMap();
			return map.getHTMLTable(httpReq).toString();
		}
        else
        if(parms.containsKey("AREATHUMBNAIL"))
        {
			String AREA = httpReq.getRequestParameter("AREA");
			if (AREA == null) return "";
			if (AREA.length() == 0) return "";
			Area A = CMLib.map().getArea(AREA);
			if (A == null)  return "";
			if (A.properSize() == 0) 
				GrinderRooms.createLonelyRoom(A, null, 0, false);
			GrinderFlatMap map=null;
			if((httpReq.getRequestParameter("MAPSTYLE")!=null)
			&&(httpReq.getRequestParameter("MAPSTYLE").length()>0))
				map=new GrinderMap(A);
			else
				map=new GrinderFlatMap(A);
			map.rePlaceRooms();
			String rS = httpReq.getRequestParameter("ROOMSIZE");
			int roomSize = (rS!=null)?CMath.s_int(rS):4;
			if (roomSize <= 4)
				return map.getHTMLMap(httpReq).toString();
			return map.getHTMLMap(httpReq, roomSize).toString();
        }
        else
        if(parms.containsKey("WORLDMAP"))
        {
			GrinderFlatMap map=null;
			if((httpReq.getRequestParameter("MAPSTYLE")!=null)
			&&(httpReq.getRequestParameter("MAPSTYLE").length()>0))
				map=new GrinderMap();
			else
				map=new GrinderFlatMap();
			map.rePlaceRooms();
			return map.getHTMLMap(httpReq).toString();
        }
		else
		if(parms.containsKey("AREALIST"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Area pickedA=getLoggedArea(httpReq,mob);
			return GrinderAreas.getAreaList(pickedA,mob);
		}
		else
		if(parms.containsKey("DELAREA"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Area A=getLoggedArea(httpReq,mob);
			if(A==null) return "@break@";
			CMLib.utensils().obliterateArea(A.Name());
			Log.sysOut("Grinder",mob.Name()+" obliterated area "+A.Name());
			return "The area "+A.Name()+" has been successfully deleted.";
		}
		else
		if(parms.containsKey("ADDAREA"))
		{
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "false";
			if(AREA.length()==0) return "false";
			Area A=CMLib.map().getArea(AREA);
			if(A==null)
				A=CMLib.database().DBCreateArea(AREA,"StdArea");
			else
				return "false";
			Log.sysOut("Grinder",mob.Name()+" added area "+A.Name());
			return "true";
		}
		else
		if(parms.containsKey("EDITAREA"))
		{
            MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "";
			GrinderAreas.modifyArea(httpReq,parms);
			AREA=httpReq.getRequestParameter("AREA");
			Log.sysOut("Grinder",mob.Name()+" edited area "+A.Name());
		}
		else
		if(parms.containsKey("DELEXIT"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" deleted exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.delExit(R,dir);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITEXIT"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" modified exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.editExit(R,dir,httpReq,parms);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("QUICKFIND"))
		{
			String find=httpReq.getRequestParameter("QUICKFIND");
			if(find==null) return "@break@";
			String AREA=httpReq.getRequestParameter("AREA");
			if(AREA==null) return "";
			if(AREA.length()==0) return "";
			Area A=CMLib.map().getArea(AREA);
			if(A==null) return "";
			for(Enumeration r=A.getProperMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().toUpperCase().endsWith(find.toUpperCase()))
				{
					httpReq.addRequestParameters("ROOM",R.roomID());
					return "";
				}
			}
			for(Enumeration r=A.getProperMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.displayText().toUpperCase().indexOf(find.toUpperCase())>=0)
				{
					httpReq.addRequestParameters("ROOM",R.roomID());
					return "";
				}
			}
			for(Enumeration r=A.getProperMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.description().toUpperCase().indexOf(find.toUpperCase())>=0)
				{
					httpReq.addRequestParameters("ROOM",R.roomID());
					return "";
				}
			}
			return "";
		}
		else
		if(parms.containsKey("LINKEXIT"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			Room R2=CMLib.map().getRoom(httpReq.getRequestParameter("OLDROOM"));
			if(R2==null) return "@break@";
			int dir2=Directions.getGoodDirectionCode(httpReq.getRequestParameter("OLDLINK"));
			if(dir2<0) return "@break@";
			Log.sysOut("Grinder",mob.Name()+" linked exit "+dir+" from "+R.roomID());
			String errMsg=GrinderExits.linkRooms(R,R2,dir,dir2);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("LINKAREA"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			String oldroom=httpReq.getRequestParameter("OLDROOM");
			if(oldroom==null) oldroom="";
			Room R2=CMLib.map().getRoom(oldroom);
			String errMsg="";
			if(R2==null)
				errMsg="No external room with ID '"+oldroom+"' found.";
			else
			{
				errMsg=GrinderExits.linkRooms(R,R2,dir,Directions.getOpDirectionCode(dir));
				Log.sysOut("Grinder",mob.Name()+" linked area "+R.roomID()+" to "+R2.roomID());
			}
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITROOM"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderRooms.editRoom(httpReq,parms,R);
			Log.sysOut("Grinder",mob.Name()+" modified room "+R.roomID());
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITITEM"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderItems.editItem(httpReq,parms,R);
			Log.sysOut("Grinder",mob.Name()+" modified item in room "+R.roomID());
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("EDITMOB"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			String errMsg=GrinderMobs.editMob(httpReq,parms,R);
			Log.sysOut("Grinder",mob.Name()+" modified mob in room "+R.roomID());
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("DELROOM"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			for(int d=0;d<R.rawDoors().length;d++)
				if(R.rawDoors()[d]!=null)
				{
					httpReq.addRequestParameters("ROOM",R.rawDoors()[d].roomID());
					httpReq.addRequestParameters("LINK","");
					break;
				}
			Log.sysOut("Grinder",mob.Name()+" deleted room "+R.roomID());
			String errMsg=GrinderRooms.delRoom(R);
			httpReq.addRequestParameters("ERRMSG",errMsg);
		}
		else
		if(parms.containsKey("ADDROOM"))
		{
			MOB mob=CMLib.map().getLoadPlayer(Authenticate.getLogin(httpReq));
			if(mob==null) return "@break@";
			Room R=CMLib.map().getRoom(httpReq.getRequestParameter("ROOM"));
			if(R==null) return "@break@";
			int dir=Directions.getGoodDirectionCode(httpReq.getRequestParameter("LINK"));
			if(dir<0) return "@break@";
			String copyThisOne=httpReq.getRequestParameter("COPYROOM");
			String errMsg=GrinderRooms.createRoom(R,dir,(copyThisOne!=null)&&(copyThisOne.equalsIgnoreCase("ON")));
			httpReq.addRequestParameters("ERRMSG",errMsg);
			R=R.rawDoors()[dir];
			if(R!=null)
			{
				httpReq.addRequestParameters("ROOM",R.roomID());
				Log.sysOut("Grinder",mob.Name()+" added room "+R.roomID());
			}
			httpReq.addRequestParameters("LINK","");
		}
		return "";
	}

    protected Area getLoggedArea(ExternalHTTPRequests httpReq, MOB mob)
	{
		String AREA=httpReq.getRequestParameter("AREA");
		if(AREA==null) return null;
		if(AREA.length()==0) return null;
		Area A=CMLib.map().getArea(AREA);
		if(A==null) return null;
		if(CMSecurity.isASysOp(mob)||A.amISubOp(mob.Name()))
			return A;
		return null;
	}
}
