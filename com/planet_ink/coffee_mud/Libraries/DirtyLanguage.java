package com.planet_ink.coffee_mud.Libraries;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
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
public class DirtyLanguage extends StdLibrary implements LanguageLibrary
{
    public String ID(){return "DirtyLanguage";}
	
	protected String language="en";
    protected String country="TX";
    protected Locale currentLocale=null;
    protected Hashtable translatorSections=null;
    protected Hashtable parserSections=null;
	
	public void setLocale(String lang, String state)
	{
		if((lang!=null)&&(state!=null)&&(lang.length()>0)&&(state.length()>0))
		{
			country=state;
			language=lang;
		}
		currentLocale = new Locale(language, country);
        clear();
	}

	public String replaceWithDefinitions(DVector global, DVector local, String str)
	{
		for(int v=0;v<local.size();v++)
			str=CMStrings.replaceAll(str,(String)local.elementAt(v,1),(String)local.elementAt(v,2));
		for(int v=0;v<global.size();v++)
			str=CMStrings.replaceAll(str,(String)global.elementAt(v,1),(String)global.elementAt(v,2));
		return str;
	}
    
    protected Hashtable loadFileSections(String filename)
    {
        Hashtable parserSections=new Hashtable();
        CMFile F=new CMFile(filename,null,false,true);
        if(!F.exists()){ Log.errOut("Language file "+filename+" not found! This mud is in deep doo-doo!"); return null;}
        StringBuffer alldata=F.text();
        Vector V=Resources.getFileLineVector(alldata);
        String s=null;
        DVector currentSection=null;
        DVector globalDefinitions=new DVector(2);
        DVector localDefinitions=new DVector(2);
        for(int v=0;v<V.size();v++)
        {
            s=((String)V.elementAt(v)).trim();
            if((s.startsWith("#"))||(s.trim().length()==0)) continue;
            if(s.startsWith("["))
            {
                int x=s.lastIndexOf("]");
                currentSection=new DVector(3);
                parserSections.put(s.substring(1,x).toUpperCase(),currentSection);
                localDefinitions.clear();
            }
            else
            if(s.toUpperCase().startsWith("DEFINE"))
            {
                int regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                int regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                String variable=s.substring(regstart+1,regend).toUpperCase();
                s=s.substring(regend+1).trim();
                if(!s.toUpperCase().startsWith("AS")){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                String replacement=s.substring(regstart+1,regend);
                replacement=replaceWithDefinitions(globalDefinitions,localDefinitions,replacement);
                if(currentSection!=null)
                {
                    localDefinitions.removeElement(variable);
                    localDefinitions.addElement(variable,replacement);
                }
                else
                {
                    globalDefinitions.removeElement(variable);
                    globalDefinitions.addElement(variable,replacement);
                }
            }
            else
            if(s.toUpperCase().startsWith("REPLACE"))
            {
                String cmd="REPLACE";
                int regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                int regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                String expression=s.substring(regstart+1,regend);
                expression=replaceWithDefinitions(globalDefinitions,localDefinitions,expression);
                s=s.substring(regend+1).trim();
                if(!s.toUpperCase().startsWith("WITH")){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                regstart=s.indexOf('"');
                if(regstart<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                regend=s.indexOf('"',regstart+1);
                while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
                    regend=s.indexOf('"',regend+1);
                if(regend<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
                String replacement=s.substring(regstart+1,regend);
                replacement=replaceWithDefinitions(globalDefinitions,localDefinitions,replacement);
                try
                {
                    Pattern expPattern=Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
                    currentSection.addElement(cmd,expPattern,replacement);
                }
                catch(Exception e){Log.errOut("Scripts",e);}
            }
            else
                Log.errOut("Scripts","Unknown parser command, line "+v);
        }
        return parserSections;
    }
    
	
    public DVector getLanguageParser(String parser)
    {
    	if(parserSections==null)
            parserSections=loadFileSections("resources/parser_"+language.toUpperCase()+"_"+country.toUpperCase()+".properties");
    	return (DVector)parserSections.get(parser);
    }
	
    public DVector getLanguageTranslator(String parser)
    {
        if(parserSections==null)
            parserSections=loadFileSections("resources/translation_"+language.toUpperCase()+"_"+country.toUpperCase()+".properties");
        return (DVector)parserSections.get(parser);
    }
    
	
	public void clear()
	{
        translatorSections=null;
        parserSections=null;
	}
	
    public Vector preCommandParser(Vector CMDS)
    {
        Vector MORE_CMDS=new Vector();
        MORE_CMDS.addElement(CMParms.combineWithTabs(CMDS,0));
        DVector parser=CMLib.lang().getLanguageParser("COMMAND-PRE-PROCESSOR");
        if((parser==null)||(CMDS==null)){ MORE_CMDS.setElementAt(CMDS,0); return MORE_CMDS;}
        Pattern pattern=null;
        Matcher matcher=null;
        String str=null;
        Vector expansion=null;
        String expStr=null;
        int strLen=-1;
        for(int p=0;p<parser.size();p++)
        {
            if(((String)parser.elementAt(p,1)).equals("REPLACE"))
            {
                pattern=(Pattern)parser.elementAt(p,2);
                boolean nothingDone=false;
                while(!nothingDone)
                {
                    nothingDone=true;
                    for(int m=0;m<MORE_CMDS.size();m++)
                    {
                        str=(String)MORE_CMDS.elementAt(m);
                        strLen=str.length();
                        matcher=pattern.matcher(str);
                        if(matcher.find())
                        {
                            str=(String)parser.elementAt(p,3);
                            for(int i=0;i<=matcher.groupCount();i++)
                                str=CMStrings.replaceAll(str,"\\"+i,matcher.group(i));
                            if(!((String)MORE_CMDS.elementAt(m)).equals(str))
                            {
                                str=CMStrings.replaceAll(str,"\\t","\t");
                                expansion=CMParms.parseAny(str,"\\n",false);
                                MORE_CMDS.setElementAt(expansion.elementAt(0),m);
                                expStr=(String)expansion.elementAt(0);
                                if(expStr.length()<=strLen) nothingDone=false;
                                boolean insert=m<MORE_CMDS.size()-1;
                                for(int e=1;e<expansion.size();e++)
                                {
                                    expStr=(String)expansion.elementAt(e);
                                    if(expStr.length()<=strLen) nothingDone=false;
                                    if(insert)
                                        MORE_CMDS.insertElementAt(expStr,m+e);
                                    else
                                        MORE_CMDS.addElement(expStr);
                                }
                            }
                        }
                    }
                }
            }
        }
        for(int m=0;m<MORE_CMDS.size();m++)
            MORE_CMDS.setElementAt(CMParms.parseTabs((String)MORE_CMDS.elementAt(m),false),m);
        return MORE_CMDS;
    }
    
    protected String basicParser(String str, DVector parser)
    {
        if((parser==null)||(str==null)) return null;
        Pattern pattern=null;
        Matcher matcher=null;
        String oldStr=str;
        for(int p=0;p<parser.size();p++)
        {
            if(((String)parser.elementAt(p,1)).equals("REPLACE"))
            {
                pattern=(Pattern)parser.elementAt(p,2);
                matcher=pattern.matcher(str);
                if(matcher.find())
                    for(int i=0;i<=matcher.groupCount();i++)
                        str=CMStrings.replaceAll(str,"\\"+i,matcher.group(i));
            }
        }
        return str.length()>=oldStr.length()?null:str;
        
    }
    
    public String preItemParser(String item)
    {
        DVector parser=CMLib.lang().getLanguageParser("ITEM-PRE-PROCESSOR");
        return basicParser(item,parser);
    }
    public String failedItemParser(String item)
    {
        DVector parser=CMLib.lang().getLanguageParser("ITEM-FAIL-PROCESSOR");
        return basicParser(item,parser);
    }
}
