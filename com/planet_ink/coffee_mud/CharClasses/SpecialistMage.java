package com.planet_ink.coffee_mud.CharClasses;
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
public class SpecialistMage extends Mage
{
	public String ID(){return "SpecialistMage";}
	public String name(){return "Specialist Mage";}
	public String baseClass(){return "Mage";}
	public int domain(){return Ability.DOMAIN_ABJURATION;}
	public int opposed(){return Ability.DOMAIN_ENCHANTMENT;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	public void cloneFix(CharClass C)
	{
		super.cloneFix(C);
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if(A!=null)
			{
				int level=CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID());
				if((!CMLib.ableMapper().getDefaultGain(baseClass(),true,A.ID()))
				&&(level>0)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL))
				{
					boolean secret=CMLib.ableMapper().getSecretSkill(ID(),true,A.ID());
					if((A.classificationCode()&Ability.ALL_DOMAINS)==opposed())
					{
						if(CMLib.ableMapper().getDefaultGain(baseClass(),true,A.ID()))
							CMLib.ableMapper().addCharAbilityMapping(ID(),level,A.ID(),0,"",false,secret);
						else
							CMLib.ableMapper().delCharAbilityMapping(ID(),A.ID());
					}
					else
					if((A.classificationCode()&Ability.ALL_DOMAINS)==domain()&&(!secret))
						CMLib.ableMapper().addCharAbilityMapping(ID(),level,A.ID(),25,true);
					else
						CMLib.ableMapper().addCharAbilityMapping(ID(),level,A.ID(),0,"",false,secret);
				}
			}
		}
	}

	public int availabilityCode(){return 0;}
	public String otherBonuses()
	{
		String chosen=CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[domain()>>5]);
		return "At 5th level, receives 2%/lvl bonus damage from "+chosen+".  At 10th level, receives double duration on your "+chosen+" magic, and half duration from malicious "+chosen+" magic.";
	}
	public String otherLimitations()
	{
		String opposed=CMStrings.capitalizeAndLower(Ability.DOMAIN_DESCS[opposed()>>5]);
		return "Unable to cast "+opposed+" spells.  Receives 2%/lvl penalty damage from "+opposed+".  Receives double duration from malicious "+opposed+" magic, half duration on other "+opposed+" effects.";
	}

	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		return super.qualifiesForThisClass(mob,quiet);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if((msg.tool()==null)||(!(msg.tool() instanceof Ability)))
		   return super.okMessage(myChar,msg);
		int domain=((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS;
		if(msg.amISource(myChar)
		&&(myChar.isMine(msg.tool())))
		{
			if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
			&&(domain==opposed())
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,msg.tool().ID())))
			{
				if(CMLib.dice().rollPercentage()>
				   (myChar.charStats().getStat(CharStats.STAT_INTELLIGENCE)*((myChar.charStats().getCurrentClass()==this)?1:2)))
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fizzle(s) a spell.");
					return false;
				}
			}
			if((myChar.charStats().getClassLevel(this)>=5)
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==domain()))
			{
                int classLevel=myChar.charStats().getClassLevel(this);
                if(classLevel>30) classLevel=30;
				int recovery=(int)Math.round(CMath.mul(msg.value(),CMath.mul(0.02,classLevel)));
				msg.setValue(msg.value()-recovery);
			}
		}
		else
		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability))
		{
			if((domain==domain())
			&&(myChar.charStats().getClassLevel(this)>=5))
			{
				int recovery=(int)Math.round(CMath.div((msg.value()),1.0+CMath.mul(0.02,myChar.charStats().getClassLevel(this))));
				msg.setValue(msg.value()-recovery);
			}
			else
			if(domain==opposed())
			{
				int recovery=(int)Math.round(CMath.mul((msg.value()),1.0+CMath.mul(0.02,30-myChar.charStats().getClassLevel(this))));
				msg.setValue(msg.value()-recovery);
			}
		}

		return super.okMessage(myChar,msg);
	}

	public int classDurationModifier(MOB myChar,
									 Ability skill,
									 int duration)
	{
		if(myChar==null) return duration;
		boolean lessTen=myChar.charStats().getClassLevel(this)<10;

		int domain=skill.classificationCode()&Ability.ALL_DOMAINS;
		if((skill.invoker()==myChar)
		||(skill.quality()!=Ability.MALICIOUS))
		{
			if(domain==opposed())
				return duration/2;
			else
			if(domain==domain())
			{
				if(!lessTen)
				{
					if(duration>=(Integer.MAX_VALUE/2))
						return duration;
					return duration*2;
				}
			}
		}
		else
		{
			if(domain==opposed())
			{
				if(duration>=(Integer.MAX_VALUE/2))
					return duration;
				return duration*2;
			}
			else
			if(domain==domain())
			{
				if(!lessTen)
					return duration/2;
			}
		}
		return duration;
	}
}
