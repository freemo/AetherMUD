package com.planet_ink.coffee_mud.core;
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
import java.net.*;
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import com.planet_ink.coffee_mud.core.exceptions.*;


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
public class SMTPclient
{
	/** Default port number */
    public static final int DEFAULT_PORT = 25;
	/** network end of line */
    protected static final String EOL = "\r\n"; 
	/** default timeout */
	protected static final int DEFAULT_TIMEOUT=10000;

	/** Reply buffer */
    protected BufferedReader reply = null;
	/** Send writer */
    protected PrintWriter send = null;
	/** Socket to use */
    protected Socket sock = null;

	static Attribute doMXLookup( String hostName ) 
	{
		try
		{
			Hashtable env = new Hashtable();
			env.put("java.naming.factory.initial",
			        "com.sun.jndi.dns.DnsContextFactory");
			DirContext ictx = new InitialDirContext( env );
			Attributes attrs = ictx.getAttributes( hostName, new String[] { "MX" });
			Attribute attr = attrs.get( "MX" );
			if( attr == null ) return( null );
			return( attr );
		}
		catch(javax.naming.NamingException x)
		{
		}
		return null;
	}
  
    /**
     *   Create a SMTP object pointing to the specified host
     *   @param hostid The host to connect to.
     *   @exception UnknownHostException
     *   @exception IOException
     */
    public SMTPclient() throws UnknownHostException, IOException {
        this("127.0.0.1", DEFAULT_PORT);
    }

	/** Main constructor that initialized  internal structures*/
    public SMTPclient( String hostid, int port) throws UnknownHostException,IOException {
        sock = new Socket( hostid, port );
		reply = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		sock.setSoTimeout(DEFAULT_TIMEOUT);
        send = new PrintWriter( sock.getOutputStream() );
        String rstr = reply.readLine();
        if ((rstr==null)||(!rstr.startsWith("220"))) throw new ProtocolException(rstr);
        while (rstr.indexOf('-') == 3) {
            rstr = reply.readLine();
            if (!rstr.startsWith("220")) throw new ProtocolException(rstr);
        }
    }

	/** Main constructor that initialized  internal structures*/
    public SMTPclient( InetAddress address ) throws IOException {
        this(address, DEFAULT_PORT);
    }

	/** Main constructor that initialized  internal structures*/
    public SMTPclient( InetAddress address, int port ) throws IOException {
        sock = new Socket( address, port );
		sock.setSoTimeout(DEFAULT_TIMEOUT);
		reply = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        send = new PrintWriter( sock.getOutputStream() );
        String rstr = reply.readLine();
        if (!rstr.startsWith("220")) throw new ProtocolException(rstr);
        while (rstr.indexOf('-') == 3) {
            rstr = reply.readLine();
            if (!rstr.startsWith("220")) throw new ProtocolException(rstr);
        }
    }
	
	public SMTPclient (String emailAddress) throws IOException, 
												   BadEmailAddressException
	{
		int x=emailAddress.indexOf("@");
		if(x<0) throw new BadEmailAddressException("Malformed email address");
		String domain=emailAddress.substring(x+1).trim();
		if(domain.length()==0) throw new BadEmailAddressException("Malformed email address");
		Vector addys=new Vector();
		Attribute mx=doMXLookup(domain);
		boolean connected=false;
		try{
			if((mx!=null)&&(mx.size()>0))
			for(NamingEnumeration e=mx.getAll();e.hasMore();)
				addys.addElement(e.next());
		}
		catch(javax.naming.NamingException ne)
		{
		}
		if(addys.size()==0)
			addys.addElement(domain);
		for(Enumeration e=addys.elements();e.hasMoreElements();)
		{
			String hostid=(String)e.nextElement();
			int y=hostid.lastIndexOf(" ");
			if(y>=0) hostid=hostid.substring(y+1).trim();
			try
			{
				sock = new Socket( hostid, DEFAULT_PORT );
				reply = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				sock.setSoTimeout(DEFAULT_TIMEOUT);
				send = new PrintWriter( sock.getOutputStream() );
				String rstr = reply.readLine();
				if ((rstr==null)||(!rstr.startsWith("220"))) throw new ProtocolException(rstr);
				while (rstr.indexOf('-') == 3) {
				    rstr = reply.readLine();
				    if (!rstr.startsWith("220")) throw new ProtocolException(rstr);
				}
				connected=true;
				break;
			}
			catch(Exception ex)
			{
				// just try the next one.
			}
		}
		if(!connected) throw new IOException("Unable to connect to '"+domain+"'.");
	}
	
	public static boolean emailIfPossible(String SMTPServerName, 
	        						     String from,
	        						     String replyTo,
	        						     String to,
	        						     String subject,
	        						     String message)
	{
		try
		{
			SMTPclient SC=null;
		    if(SMTPServerName.length()>0)
				SC=new SMTPclient(SMTPServerName,DEFAULT_PORT);
		    else
				SC=new SMTPclient(to);
		    
			SC.sendMessage(from,
						   replyTo,
						   to,
						   to,
						   subject,
						   message);
			return true;
		}
		catch(Exception ioe)
		{
		}
	    return false;
	}
	
	
	
	/**
	* Send a message
	* 
	* <br><br><b>Usage:</b>  Mailer.sendmsg(S, From, To, Subject, Message);
	* @param S Session object
	* @param froaddress  Address sending from
	* @param to_address Address sending to 
	* @param subject Subject line
	* @param message Message content
	* @return NA
	*/
    public synchronized void sendMessage(String froaddress, 
										 String reply_address,
										 String to_address, 
										 String mockto_address,
										 String subject, 
										 String message)
	throws IOException
	{
		String rstr;
		String sstr;

		InetAddress local;
		try {
		  local = InetAddress.getLocalHost();
		}
		catch (UnknownHostException ioe) {
		  System.err.println("No local IP address found - is your network up?");
		  throw ioe;
		}
        if(com.planet_ink.coffee_mud.core.CMSecurity.isDebugging("SMTPCLIENT"))
            Log.debugOut("SMTPclient","Sending "+froaddress+" ("+reply_address+") to "+to_address+" through "+mockto_address+", subject="+subject+", message="+message);
		String host = local.getHostName();
		send.print("HELO " + host);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
		if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
		sstr = "MAIL FROM:<" + froaddress+">" ;
		send.print(sstr);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
		if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
		sstr = "RCPT TO:<" + to_address+">";
		send.print(sstr);
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
		if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
		send.print("DATA");
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
		if (!rstr.startsWith("354")) throw new ProtocolException(rstr);
		send.print("MIME-Version: 1.0");
		send.print(EOL);
		send.print("Date: " + new IQCalendar().d2SString());
		send.print(EOL);
		send.print("From: " + froaddress);
		send.print(EOL);
		send.print("Subject: " + subject);
		send.print(EOL);
		send.print("Sender: " + froaddress);
		send.print(EOL);
		send.print("Reply-To: " + reply_address);
		send.print(EOL);
		send.print("To: " + mockto_address);
		send.print(EOL);

		// Create Date - we'll cheat by assuming that local clock is right

		send.print("Date: " + msgDateFormat(IQCalendar.getIQInstance()));
		send.print(EOL);
		send.flush();

		// Warn the world that we are on the loose - with the comments header:
//		send.print("Comment: Unauthenticated sender");
//		send.print(EOL);
//		send.print("X-Mailer: JNet SMTP");
//		send.print(EOL);


		// Now send the message proper
		if(message!=null)
		{
			if((message.indexOf("<HTML>")>=0)&&(message.indexOf("</HTML>")>=0))
		    {
//				String BoundryString="---"+Math.random()+"_"+Math.random();
//				send.print("Content-Type: multipart/mixed; boundry="+BoundryString);
//				send.print(EOL);
//				send.print(BoundryString);
//				send.print(EOL);
				send.print("Content-Type: text/html");
				send.print(EOL);
				
			}
			else
			{
				send.print("Content-Type: text/plain");
				send.print(EOL);
			}
			// Sending a blank line ends the header part.
			send.print(EOL);
			send.print(message);
		}
		send.print(EOL);
		send.print(".");
		send.print(EOL);
		send.flush();
		rstr = reply.readLine();
		if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
    }

	/**
	* return members of a list on an email server.
	* 250-First Last <emailaddress>\r
	* 
	* <br><br><b>Usage:</b>  List=Mailer.getListMembers(List);
	* @param list member list
	* @return String List of members
	*/
    public synchronized String getListMembers( String list)
                         throws IOException, ProtocolException {

        String sendString;

        InetAddress local;
        try {
          local = InetAddress.getLocalHost();
        }
        catch (UnknownHostException ioe) {
          System.err.println("No local IP address found - is your network up?");
          throw ioe;
        }
        String host = local.getHostName();
        send.print("HELO " + host);
        send.print(EOL);
        send.flush();
        String rstr = reply.readLine();
        if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
        sendString = "EXPN " + list ;
        send.print(sendString);
        send.print(EOL);
        send.flush();
		rstr="";
		try
		{
			while(true)
			{
				rstr+=reply.readLine();
				sock.setSoTimeout(1000);
			}
		}
		catch(java.io.InterruptedIOException x)
		{ // not really an error, just a control break			
		}
		sock.setSoTimeout(DEFAULT_TIMEOUT);
        if (!rstr.startsWith("250")) throw new ProtocolException(rstr);
		return rstr;
    }

	/**
	* close this socket
	* 
	* <br><br><b>Usage:</b>  this.close();
	* @param NA
	* @return NA
	*/
	public void close() {
      try {
        send.print("QUIT");
        send.print(EOL);
        send.flush();
        sock.close();
      }
      catch (IOException ioe) {
        // As though there's anything I can dof about it now...
      }
    }

    /**
	* close this socket
	* 
	* <br><br><b>Usage:</b>  finalize();
	* @param NA
	* @return NA
	*/
	protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

	/**
	* format the date
	* 
	* <br><br><b>Usage:</b>  msgDateFormat(IQCalendar.getIQInstance())
	* @param NA
	* @return NA
	*/
    private String msgDateFormat(IQCalendar senddate) {
        String formatted = "hold";

        String Day[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String Month[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul","Aug", "Sep", "Oct", "Nov", "Dec"};
		int dow=senddate.get(IQCalendar.DAY_OF_WEEK)-1;
		int date=senddate.get(IQCalendar.DAY_OF_MONTH);
		int m=senddate.get(IQCalendar.MONTH);
		int y=senddate.get(IQCalendar.YEAR);
		int h=senddate.get(IQCalendar.HOUR_OF_DAY);
		int min=senddate.get(IQCalendar.MINUTE);
		int s=senddate.get(IQCalendar.SECOND);
		int zof=senddate.get(IQCalendar.ZONE_OFFSET);
		int dof=senddate.get(IQCalendar.DST_OFFSET);

        formatted = Day[dow] + ", ";
        formatted = formatted + String.valueOf(date) + " ";
        formatted = formatted + Month[m] + " ";
        formatted = formatted + String.valueOf(y) + " ";
        if (h < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(h) + ":";
        if (min < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(min) + ":";
        if (s < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(s) + " ";
        if ((zof + dof) < 0)
            formatted = formatted + "-";
        else
            formatted = formatted + "+";
		
		zof=Math.round(zof/1000); // now in seconds
		zof=Math.round(zof/60); // now in minutes
		
		dof=Math.round(dof/1000); // now in seconds
		dof=Math.round(dof/60); // now in minutes
		
        if ((Math.abs(zof + dof)/60) < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(Math.abs(zof + dof)/60);
        if ((Math.abs(zof + dof)%60) < 10) formatted = formatted + "0";
        formatted = formatted + String.valueOf(Math.abs(zof + dof)%60);

        return formatted;
    }
}
