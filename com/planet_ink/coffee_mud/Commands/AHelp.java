package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class AHelp extends StdCommand
{
	public AHelp(){}

	private String[] access={getScr("Ahelp","cmd1"),"AHELP"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String helpStr=Util.combine(commands,1);
		if(MUDHelp.getArcHelpFile().size()==0)
		{
			mob.tell(getScr("Ahelp","aerr"));
			return false;
		}
		StringBuffer thisTag=null;
		if(helpStr.length()==0)
		{
			thisTag=Resources.getFileResource("help"+CMFile.pathSeparator+"arc_help.txt",true);
			if((thisTag!=null)&&(helpStr.equalsIgnoreCase("more")))
			{
				StringBuffer theRest=(StringBuffer)Resources.getResource("arc_help.therest");
				if(theRest==null)
				{
					Vector V=new Vector();
					theRest=new StringBuffer("");

					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PROPERTY))
							V.addElement(A.ID());
					}
					if(V.size()>0)
					{
					    theRest.append(getScr("Ahelp","Properties"));
						theRest.append(CMLister.fourColumns(V));
					}

					V.clear();
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.DISEASE))
							V.addElement(A.ID());
					}
					if(V.size()>0)
					{
					    theRest.append(getScr("Ahelp","Diseases"));
						theRest.append(CMLister.fourColumns(V));
					}

					V.clear();
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.POISON))
							V.addElement(A.ID());
					}
					if(V.size()>0)
					{
					    theRest.append(getScr("Ahelp","Poisons"));
						theRest.append(CMLister.fourColumns(V));
					}

					V.clear();
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SUPERPOWER))
							V.addElement(A.ID());
					}
					if(V.size()>0)
					{
					    theRest.append(getScr("Ahelp","SuperPowers"));
						theRest.append(CMLister.fourColumns(V));
					}

					V.clear();
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.EVILDEED))
							V.addElement(A.ID());
					}
					if(V.size()>0)
					{
					    theRest.append(getScr("Ahelp","EDeeds"));
						theRest.append(CMLister.fourColumns(V));
					}

					V.clear();
					for(Enumeration b=CMClass.behaviors();b.hasMoreElements();)
					{
						Behavior B=(Behavior)b.nextElement();
						if(B!=null) V.addElement(B.ID());
					}
					if(V.size()>0)
					{
					    theRest.append(getScr("Ahelp","Behavior"));
						theRest.append(CMLister.fourColumns(V));
					}
					Resources.submitResource("arc_help.therest",theRest);
				}
				thisTag=new StringBuffer(thisTag.toString());
				thisTag.append(theRest);
			}
		}
		else
			thisTag=MUDHelp.getHelpText(helpStr,MUDHelp.getArcHelpFile(),mob);
		if(thisTag==null)
		{
			mob.tell(getScr("Ahelp","ahelp",helpStr));
			Log.errOut(getScr("Ahelp","errout",mob.name(),helpStr));
		}
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln(thisTag.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"AHELP");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
