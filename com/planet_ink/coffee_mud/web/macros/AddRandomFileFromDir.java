package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.web.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;



public class AddRandomFileFromDir extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if((parms==null)||(parms.size()==0)) return "";
		StringBuffer buf=new StringBuffer("");
		Vector fileList=new Vector();
		boolean LINKONLY=false;
		for(Enumeration e=parms.elements();e.hasMoreElements();)
			if(((String)e.nextElement()).equalsIgnoreCase("LINKONLY"))
				LINKONLY=true;
		for(Enumeration e=parms.elements();e.hasMoreElements();)
		{
			String filePath=(String)e.nextElement();
			if(filePath.equalsIgnoreCase("LINKONLY")) continue;
			File directory=httpReq.grabFile(filePath);
			if(directory==null) continue;
			if((!filePath.endsWith(""+File.separatorChar))&&(!filePath.endsWith("/")))
				filePath+="/";
			if((directory!=null)&&(directory.canRead())&&(directory.isDirectory()))
			{
				String[] list=directory.list();
				for(int l=0;l<list.length;l++)
					fileList.addElement(filePath+list[l]);
			}
		}
		if(fileList.size()==0) return buf.toString();
		String file=(String)fileList.elementAt(Dice.roll(1,fileList.size(),-1));
		if((file!=null)&&(file.length()>0))
		{
			if(LINKONLY)
				buf.append(file);
			else
				buf.append(httpReq.getPageContent(file));
		}
		return buf.toString();
	}
}
