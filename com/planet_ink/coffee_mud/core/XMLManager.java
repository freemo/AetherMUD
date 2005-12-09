package com.planet_ink.coffee_mud.utils;

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
public class XMLManager
{
	/**
	 * Returns the double value of a string without crashing
 	 * 
	 * <br><br><b>Usage:</b> dSize = WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
	 * @param double String to convert
	 * @return double Double value of the string
	 */
	public static double s_double(String DOUBLE)
	{
		double sdouble=0;
		try{ sdouble=Double.parseDouble(DOUBLE); }
		catch(Exception e){ return 0;}
		return sdouble;
	}

	/**
	 * Returns the integer value of a string without crashing
 	 * 
	 * <br><br><b>Usage:</b> int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	private static int s_int(String INT)
	{
		int sint=0;
		try{ sint=Integer.parseInt(INT); }
		catch(java.lang.NumberFormatException e){ return 0;}
		return sint;
	}
	
	/**
	 * Returns the long value of a string without crashing
 	 * 
	 * <br><br><b>Usage:</b> int num=s_long(CMD.substring(14));
	 * @param LONG Long value of string
	 * @return long Long value of the string
	 */
	private  static long s_long(String LONG)
	{
		long slong=0;
		try{ slong=Long.parseLong(LONG); }
		catch(java.lang.NumberFormatException e){ return 0;}
		return slong;
	}
	
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to search for
	 * @param Data String to searh
	 * @return String Information corresponding to the tname
	 */
	public static String convertXMLtoTag(String TName, String Data)
	{
	    if(Data.length()==0)
			return "<"+TName+" />";
		return "<"+TName+">"+Data+"</"+TName+">";
	}
	
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to search for
	 * @param Data String to searh
	 * @return String Information corresponding to the tname
	 */
	public static String convertXMLtoTag(String TName, int Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}
	
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to search for
	 * @param Data String to searh
	 * @return String Information corresponding to the tname
	 */
	public static String convertXMLtoTag(String TName, boolean Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}
	
	/**
	 * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
	 * 
  	 * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
	 * @param TName Tag name to search for
	 * @param Data String to searh
	 * @return String Information corresponding to the tname
	 */
	public static String convertXMLtoTag(String TName, long Data)
	{
		return "<"+TName+">"+Data+"</"+TName+">";
	}
	
	/**
	 * Return the contents of an XML tag, given the tag to search for
	 * 
  	 * <br><br><b>Usage:</b> String XML=returnXMLBlock(Response,"PDIModelErrors");
	 * @param Blob String to searh
	 * @param Tag Tag name to search for
	 * @return String Information corresponding to the tname
	 */
	public static String returnXMLBlock(String Blob, String Tag)
	{
		int foundb=Blob.indexOf("<"+Tag+">");
		if(foundb<0) foundb=Blob.indexOf("<"+Tag+" ");
		if(foundb<0) foundb=Blob.indexOf("<"+Tag+"/");
		if(foundb<0) return "";
		
		int founde=Blob.indexOf("/"+Tag+">",foundb)-1;
		if(founde<0) founde=Blob.indexOf("/"+Tag+" ",foundb)-1;
		if(founde<0)
		{
			founde=Blob.indexOf(">",foundb);
			if((founde>0)&&(Blob.charAt(founde-1)!='/')) founde=-1;
		}
		if(founde<0) return "";

		Blob=Blob.substring(foundb,founde).trim();
		return Blob;
	}
	
	public static class XMLpiece
	{
		public String tag="";
		public String value="";
		public Vector contents=new Vector();
        public Vector parms=contents;
        public XMLpiece parent=null;
		public int outerStart=-1;
		public int innerStart=-1;
		public int innerEnd=-1;
		public int outerEnd=-1;
		public void addContent(XMLpiece x)
		{
			if(x==null) return;
			if(contents==null) contents=new Vector();
			contents.addElement(x);
		}
	}
	
	public static String parseOutParms(String blk, Vector parmList)
	{
		blk=blk.trim();
		for(int x=0;x<blk.length();x++)
			if(Character.isWhitespace(blk.charAt(x)))
			{
			    if(!blk.substring(x).trim().startsWith("/"))
			    {
					parmList.addElement(blk.substring(x).trim());
					return blk.substring(0,x).trim();
			    }
		        break;
			}
		return blk;
	}
	
	
	public static String getValFromPieces(Vector V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.value!=null))
			return x.value;
		return "";
	}
	
	public static Vector getContentsFromPieces(Vector V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if((x!=null)&&(x.contents!=null))
			return x.contents;
		return new Vector();
	}
	
	public static Vector getRealContentsFromPieces(Vector V, String tag)
	{
		XMLpiece x=getPieceFromPieces(V,tag);
		if(x!=null)	return x.contents;
		return null;
	}
	
	public static XMLpiece getPieceFromPieces(Vector V, String tag)
	{
		if(V==null) return null;
		for(int v=0;v<V.size();v++)
			if(((XMLpiece)V.elementAt(v)).tag.equalsIgnoreCase(tag))
				return (XMLpiece)V.elementAt(v);
		return null;
	}
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=getBoolFromPieces(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return boolean Information from XML block
	 */
	public static boolean getBoolFromPieces(Vector V, String tag)
	{
		String val=getValFromPieces(V,tag);
		if((val==null)||((val!=null)&&(val.length()==0)))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=getIntFromPieces(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return int Information from XML block
	 */
	public static int getIntFromPieces(Vector V, String tag)
	{
		return s_int(getValFromPieces(V,tag));
	}
	
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=getLongFromPieces(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return long Information from XML block
	 */
	public static long getLongFromPieces(Vector V, String tag)
	{
		return s_long(getValFromPieces(V,tag));
	}
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return double Information from XML block
	 */
	public static double getDoubleFromPieces(Vector V, String tag)
	{
		return s_double(getValFromPieces(V,tag));
	}
	
    public static boolean acceptableTag(StringBuffer str, int start, int end)
    {
        while(Character.isWhitespace(str.charAt(start)))
            start++;
        while(Character.isWhitespace(str.charAt(end)))
            end--;
        if((start>=end)
        ||(end>(start+250))
        ||((str.charAt(start)!='/')&&(!Character.isLetter(str.charAt(start)))))
            return false;
        if(start+1==end) return true;
        if(CoffeeFilter.getTagTable().containsKey(str.substring(start,end).toUpperCase()))
            return false;
        return true;
    }
    
    public static XMLpiece nextXML(StringBuffer buf, XMLpiece parent, int start)
    {
        int end=-1;
        start--;
        while((end<0)||(!acceptableTag(buf,start+1,end)))
        {
            start=buf.indexOf("<",start+1);
            if(start<0) return null;
            end=buf.indexOf(">",start);
            if(end<=start) return null;
            int nextStart=buf.indexOf("<",start+1);
            while((nextStart>=0)&&(nextStart<end))
            {
                start=nextStart;
                nextStart=buf.indexOf("<",start+1);
            }
        }
        Vector parmList=new Vector();
		String tag=parseOutParms(buf.substring(start+1,end).trim(),parmList).toUpperCase().trim();

		if(!tag.startsWith("/"))
		{
			XMLpiece piece=new XMLpiece();
			piece.parms=parmList;
			if(tag.endsWith("/"))
			{
				piece.tag=tag.substring(0,tag.length()-1).trim();
				piece.value="";
				piece.contents=new Vector();
				piece.outerStart=start;
				piece.outerEnd=end;
			}
			else
			{
				piece.tag=tag.trim();
				piece.outerStart=start;
				piece.innerStart=end+1;
				piece.contents=new Vector();
				XMLpiece next=null;
				while(next!=piece)
				{
					next=nextXML(buf,piece,end+1);
					if(next==null) // this was probably a faulty start tag
						return nextXML(buf,parent,end+1);
					else
					if(next!=piece)
					{
						end=next.outerEnd;
						piece.addContent(next);
					}
				}
			}
			return piece;
		}
		tag=tag.substring(1);
		if((parent!=null)&&(tag.equals(parent.tag)))
		{
			parent.value=buf.substring(parent.innerStart,start);
			parent.innerEnd=start;
			parent.outerEnd=end;
			return parent;
		}
		return null;
	}
	
	
	public static Vector parseAllXML(String buf)
	{  return parseAllXML(new StringBuffer(buf));}
		
	public static Vector parseAllXML(StringBuffer buf)
	{
		Vector V=new Vector();
		int end=-1;
		XMLpiece next=nextXML(buf,null,end+1);
		while(next!=null)
		{
			end=next.outerEnd;
			V.addElement(next);
			next=nextXML(buf,null,end+1);
		}
		return V;
	}
	
	
	/**
	 * Return the data value within the first XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow);
	 * @param Blob String to searh
	 * @return String Information from first XML block
	 */
	public static String returnXMLValue(String Blob)
	{
		int start=0;
		
		try{
			while((start<Blob.length())&&(Blob.charAt(start)!='>')) start++;
			if((start>=Blob.length())||(Blob.charAt(start-1)!='>')||(Blob.charAt(start-1)=='/'))
				return "";
		} catch (Throwable t){return "";}
		return Blob.substring(start+1).trim();
	}
	
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public static String returnXMLValue(String Blob, String Tag)
	{
		int start=0;
		Blob=returnXMLBlock(Blob,Tag);
		try{
			while((start<Blob.length())&&(Blob.charAt(start)!='>')) start++;
			if((start>=Blob.length())||(Blob.charAt(start)!='>')||(Blob.charAt(start-1)=='/'))
				return "";
		} catch (Throwable t){return "";}
		return Blob.substring(start+1).trim();
	}
	
	/**
	 * Return the data value within a given XML block
	 * <TAG>Data</TAG>
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Information from XML block
	 */
	public static boolean returnXMLBoolean(String Blob, String Tag)
	{
		String val=returnXMLValue(Blob,Tag);
		if((val==null)||((val!=null)&&(val.length()==0)))
			return false;
		if(val.toUpperCase().trim().startsWith("T"))
			return true;
		return false;
	}


	/**
	 * Return a parameter value within an XML tag
	 * <TAG Parameter="VALUE">
	 * 
  	 * <br><br><b>Usage:</b> String ThisColHead=ReturnXMLParm(ThisRow,"TD");
	 * @param Blob String to search
	 * @param Tag Tag to search for
	 * @return String Parameter value
	 */
	public static String returnXMLParm(String Blob, String Tag)
	{
		int foundb=Blob.indexOf(Tag+"=");
		if(foundb<0)foundb=Blob.indexOf(Tag+" =");
		if(foundb<0)return"";
		try{ while(Blob.charAt(foundb)!='\"') foundb++;
		} catch(Throwable t){return "";}
		foundb++;
		int founde=Blob.indexOf('\"',foundb);
		if(founde<foundb)return"";
		return Blob.substring(foundb,founde);
	}
    
}
