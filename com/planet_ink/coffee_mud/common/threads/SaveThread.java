package com.planet_ink.coffee_mud.system.threads;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.exceptions.*;

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
public class SaveThread extends Thread
{
	public static boolean started=false;
	private static boolean shutDown=false;
	public long lastStart=0;
	public long lastStop=0;
	public static long milliTotal=0;
	public static long tickTotal=0;
	public static String status="";

	public SaveThread()
	{
		super("SaveThread");
		setName("SaveThread");
	}

	public void itemSweep()
	{
		status="title sweeping";
		try
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				LandTitle T=CoffeeUtensils.getLandTitle(R);
				if(T!=null)
				{
					status="updating title in "+R.roomID();
					T.updateLot();
					status="title sweeping";
				}
			}
	    }catch(NoSuchElementException nse){}
	}
    
    public void commandJournalSweep()
    {
        status="command journal sweeping";
        try
        {
            for(int j=0;j<ChannelSet.getNumCommandJournals();j++)
            {
                String num=(String)ChannelSet.getCommandJournalFlags(j).get("EXPIRE=");
                if((num!=null)&&(Util.isNumber(num)))
                {
                    status="updating journal "+ChannelSet.getCommandJournalName(j);
                    Vector items=CMClass.DBEngine().DBReadJournal("SYSTEM_"+ChannelSet.getCommandJournalName(j)+"S");
                    if(items!=null)
                    for(int i=items.size()-1;i>=0;i--)
                    {
                        Vector entry=(Vector)items.elementAt(i);
                        long compdate=Util.s_long((String)entry.elementAt(6));
                        compdate=compdate+Math.round(Util.mul(IQCalendar.MILI_DAY,Util.s_double(num)));
                        if(System.currentTimeMillis()>compdate)
                        {
                            String from=(String)entry.elementAt(1);
                            String message=(String)entry.elementAt(5);
                            Log.sysOut("SaveThread","Expired "+ChannelSet.getCommandJournalName(j)+" from "+from+": "+message);
                            CMClass.DBEngine().DBDeleteJournal("SYSTEM_"+ChannelSet.getCommandJournalName(j)+"S",i);
                        }
                    }
                    status="command journal sweeping";
                }
            }
        }catch(NoSuchElementException nse){}
    }
    
	public void shutdown()
	{
		shutDown=true;
		this.interrupt();
	}

	public int savePlayers()
	{
		int processed=0;
		for(Enumeration p=CMMap.players();p.hasMoreElements();)
		{
			MOB mob=(MOB)p.nextElement();
			if(!mob.isMonster())
			{
				status="just saving "+mob.Name();
				CMClass.DBEngine().DBUpdatePlayerStatsOnly(mob);
				if((mob.Name().length()==0)||(mob.playerStats()==null))
					continue;
				status="saving "+mob.Name()+", "+mob.inventorySize()+"items";
                CMClass.DBEngine().DBUpdatePlayerItems(mob);
				status="saving "+mob.Name()+", "+mob.numLearnedAbilities()+"abilities";
                CMClass.DBEngine().DBUpdatePlayerAbilities(mob);
				status="saving "+mob.numFollowers()+" followers of "+mob.Name();
                CMClass.DBEngine().DBUpdateFollowers(mob);
				mob.playerStats().setUpdated(System.currentTimeMillis());
				processed++;
			}
			else
			if((mob.playerStats()!=null)
			&&((mob.playerStats().lastUpdated()==0)
			   ||(mob.playerStats().lastUpdated()<mob.playerStats().lastDateTime())))
			{
				status="just saving "+mob.Name();
                CMClass.DBEngine().DBUpdatePlayerStatsOnly(mob);
				if((mob.Name().length()==0)||(mob.playerStats()==null))
					continue;
				status="just saving "+mob.Name()+", "+mob.inventorySize()+" items";
                CMClass.DBEngine().DBUpdatePlayerItems(mob);
				status="just saving "+mob.Name()+", "+mob.numLearnedAbilities()+" abilities";
                CMClass.DBEngine().DBUpdatePlayerAbilities(mob);
				mob.playerStats().setUpdated(System.currentTimeMillis());
				processed++;
			}
		}
		return processed;
	}

	public boolean autoPurge()
	{
		long[] levels=new long[2001];
		long[] prePurgeLevels=new long[2001];
		for(int i=0;i<levels.length;i++) levels[i]=0;
		for(int i=0;i<prePurgeLevels.length;i++) prePurgeLevels[i]=0;
		String mask=CommonStrings.getVar(CommonStrings.SYSTEM_AUTOPURGE);
		Vector maskV=Util.parseCommas(mask.trim(),false);
		long purgePoint=0;
		for(int mv=0;mv<maskV.size();mv++)
		{
			Vector V=Util.parse(((String)maskV.elementAt(mv)).trim());
			if(V.size()<2) continue;
			long val=Util.s_long((String)V.elementAt(1));
			if(val<=0) continue;
			long prepurge=0;
			if(V.size()>2)
			    prepurge=Util.s_long((String)V.elementAt(2));
			String cond=((String)V.firstElement()).trim();
			int start=0;
			int finish=levels.length-1;
			if(cond.startsWith("<="))
				finish=Util.s_int(cond.substring(2).trim());
			else
			if(cond.startsWith(">="))
				start=Util.s_int(cond.substring(2).trim());
			else
			if(cond.startsWith("=="))
			{
				start=Util.s_int(cond.substring(2).trim());
				finish=start;
			}
			else
			if(cond.startsWith("="))
			{
				start=Util.s_int(cond.substring(1).trim());
				finish=start;
			}
			else
			if(cond.startsWith(">"))
				start=Util.s_int(cond.substring(1).trim())+1;
			else
			if(cond.startsWith("<"))
				finish=Util.s_int(cond.substring(1).trim())-1;

			if((start>=0)&&(finish<levels.length)&&(start<=finish))
			{
				long realVal=System.currentTimeMillis()-(val*IQCalendar.MILI_DAY);
				purgePoint=realVal+(prepurge*IQCalendar.MILI_DAY);
				for(int s=start;s<=finish;s++)
				{
					if(levels[s]==0) levels[s]=realVal;
					if(prePurgeLevels[s]==0) prePurgeLevels[s]=purgePoint;
				}
			}
		}
		status="autopurge process";
		Vector allUsers=CMClass.DBEngine().getUserList();
		Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if(protectedOnes==null) protectedOnes=new Vector();

		for(int u=0;u<allUsers.size();u++)
		{
			Vector user=(Vector)allUsers.elementAt(u);
			String name=(String)user.elementAt(0);
			int level=Util.s_int((String)user.elementAt(3));
			long last=Util.s_long((String)user.elementAt(5));
			long when=Long.MAX_VALUE;
			long warn=Long.MAX_VALUE;
			if(level>levels.length) 
            {
				when=levels[levels.length-1];
				warn=prePurgeLevels[prePurgeLevels.length-1];
			}
			else
			if(level>=0) 
            {
				when=levels[level];
				warn=prePurgeLevels[level];
			}
			else
				continue;
            if(CMSecurity.isDebugging("AUTOPURGE"))
                Log.debugOut("SaveThread",name+" last on "+new IQCalendar(last).d2String()+" will be warned on "+new IQCalendar(warn).d2String()+" and purged on "+new IQCalendar(when).d2String());
	        if((last>when)&&(last<warn))
			{
				boolean protectedOne=false;
				for(int p=0;p<protectedOnes.size();p++)
				{
					String P=(String)protectedOnes.elementAt(p);
					if(P.equalsIgnoreCase(name))
					{
						protectedOne=true;
						break;
					}
				}
				if(!protectedOne)
				{
					Vector warnedOnes=Resources.getFileLineVector(Resources.getFileResource("warnedplayers.ini",false));
					long foundWarning=-1;
					StringBuffer warnStr=new StringBuffer("");
					if((warnedOnes!=null)&&(warnedOnes.size()>0))
						for(int b=0;b<warnedOnes.size();b++)
						{
							String B=((String)warnedOnes.elementAt(b)).trim();
							if(B.trim().length()>0)
							{
								if(B.toUpperCase().startsWith(name.toUpperCase()+" "))
								{
									int lastSpace=B.lastIndexOf(" ");
									foundWarning=Util.s_long(B.substring(lastSpace+1).trim());
								}
								warnStr.append(B+"\n");
							}
						}
					if((foundWarning<0)||(foundWarning<when))
					{
						MOB M=CMMap.getLoadPlayer(name);
						if((M!=null)&&(M.playerStats()!=null))
						{
							warnStr.append(M.name()+" "+M.playerStats().getEmail()+" "+System.currentTimeMillis()+"\n");
							Resources.updateResource("warnedplayers.ini",warnStr);
							Resources.saveFileResource("warnedplayers.ini");
                            if(CMSecurity.isDebugging("AUTOPURGE"))
                                Log.debugOut("SaveThread",name+" is now warned.");
							warnPrePurge(M,when-warn);
						}
					}
                    else
                    if(CMSecurity.isDebugging("AUTOPURGE"))
                        Log.debugOut("SaveThread",name+" has already been warned on "+new IQCalendar(foundWarning).d2String());
				}
                else
                if(CMSecurity.isDebugging("AUTOPURGE"))
                    Log.debugOut("SaveThread",name+" is protected from purge warnings.");
			}

			if(last<when)
			{
                boolean protectedOne=false;
                for(int p=0;p<protectedOnes.size();p++)
                {
                    String P=(String)protectedOnes.elementAt(p);
                    if(P.equalsIgnoreCase(name))
                    { protectedOne=true; break; }
                }
				if(!protectedOne)
				{
					MOB M=CMMap.getLoadPlayer(name);
					if(M!=null)
					{
						CoffeeUtensils.obliteratePlayer(M,true);
						Log.sysOut("SaveThread","AutoPurged user "+name+". Last logged in "+(new IQCalendar(last).d2String())+".");
					}
				}
                else
                if(CMSecurity.isDebugging("AUTOPURGE"))
                    Log.debugOut("SaveThread",name+" is protected from purging.");
			}
		}
		return true;
	}

	private void warnPrePurge(MOB mob, long timeLeft)
	{
		// check for valid recipient
		if(mob==null) return;

		if((mob.playerStats()==null)
		||(mob.playerStats().getEmail().length()==0)) // no email addy to forward TO
			return;

		//  timeLeft is in millis
		String from="AutoPurgeWarning";
		String to=mob.Name();
		String subj=CommonStrings.SYSTEM_MUDNAME+" Autopurge Warning: "+to;
		String textTimeLeft="";
		if(timeLeft>(1000*60*60*24*2))
		{
			int days=new Double(Util.div((double)timeLeft,1000*60*60*24)).intValue();
			textTimeLeft = days + " days";
		}
		else
		{
			int hours=new Double(Util.div((double)timeLeft,1000*60*60)).intValue();
			textTimeLeft = hours + " hours";
		}
		String msg="Your character, "+to+", is going to be autopurged by the system in "+textTimeLeft+".  If you would like to keep this character active, please re-login.  This is an automated message, please do not reply.";

		SMTPclient SC=null;
		try
		{
		    if(CommonStrings.getVar(CommonStrings.SYSTEM_SMTPSERVERNAME).length()>0)
				SC=new SMTPclient(CommonStrings.getVar(CommonStrings.SYSTEM_SMTPSERVERNAME),SMTPclient.DEFAULT_PORT);
		    else
				SC=new SMTPclient(mob.playerStats().getEmail());
		}
		catch(BadEmailAddressException be)
		{
			Log.errOut("SaveThread","Unable to notify "+to+" of impending autopurge.  Invalid email address.");
			return;
		}
		catch(java.io.IOException ioe)
		{
			return;
		}

		String replyTo="AutoPurge";
		String domain=CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN).toLowerCase();
		try
		{
			SC.sendMessage(from+"@"+domain,
						   replyTo+"@"+domain,
						   mob.playerStats().getEmail(),
						   mob.playerStats().getEmail(),
						   subj,
						   CoffeeFilter.simpleOutFilter(msg));
		}
		catch(java.io.IOException ioe)
		{
			Log.errOut("SaveThread","Unable to notify "+to+" of impending autopurge.");
		}
	}

	public void run()
	{
		lastStart=System.currentTimeMillis();
		if(started)
		{
			System.out.println("DUPLICATE SAVETHREAD RUNNING!!");
			return;
		}
		started=true;
        shutDown=false;

		while(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			try{Thread.sleep(1000);}catch(Exception e){}
		while(true)
		{
			try
			{
                while(CMClass.ThreadEngine().isAllSuspended())
                    try{Thread.sleep(2000);}catch(Exception e){}
				if(!CMSecurity.isDisabled("SAVETHREAD"))
				{
					status="checking database health";
					String ok=CMClass.DBEngine().errorStatus();
					if((ok.length()!=0)&&(!ok.startsWith("OK")))
						Log.errOut("Save Thread","DB: "+ok);
					else
					{
						itemSweep();
                        commandJournalSweep();
						autoPurge();
						CoffeeTables.bump(null,CoffeeTables.STAT_SPECIAL_NUMONLINE);
						CoffeeTables.update();
						lastStop=System.currentTimeMillis();
						milliTotal+=(lastStop-lastStart);
						tickTotal++;
						status="sleeping";
						Thread.sleep(MudHost.TIME_SAVETHREAD_SLEEP);
						lastStart=System.currentTimeMillis();
						if(!CMSecurity.isSaveFlag("NOPLAYERS"))
							savePlayers();
						status="not saving players";
						//if(processed>0)
						//	Log.sysOut("SaveThread","Saved "+processed+" mobs.");
					}
				}
				else
				{
					status="sleeping";
					Thread.sleep(MudHost.TIME_SAVETHREAD_SLEEP);
				}
			}
			catch(InterruptedException ioe)
			{
				Log.sysOut("SaveThread","Interrupted!");
				if(shutDown)
				{
					shutDown=false;
					started=false;
					break;
				}
			}
			catch(Exception e)
			{
				Log.errOut("SaveThread",e);
			}
		}

		Log.sysOut("SaveThread","Shutdown complete.");
	}
}
