package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Prop_WeaponImmunity extends Property
{
	public String ID() { return "Prop_WeaponImmunity"; }
	public String name(){ return "Weapon Immunity";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public Hashtable flags=new Hashtable();

	public String accountForYourself()
	{
		String id="Weapon Immunities for the wearer: "+text();
		return id;
	}
	public void setMiscText(String newValue)
	{
	    super.setMiscText(newValue);
	    flags=new Hashtable();
	    Vector V=CMParms.parse(newValue.toUpperCase());
	    Object c=null;
	    String s=null;
	    for(int v=0;v<V.size();v++)
	    {
	        s=(String)V.elementAt(v);
	        c=new Character(s.charAt(0));
	        if((s.charAt(0)=='-')||(s.charAt(0)=='+'))
	            s=s.substring(1);
	        else
	            c=new Character('+');
	        if(s.startsWith("LEVEL"))
	        {
	            c=((Character)c).charValue()+" "+s.substring(5).trim();
	            s=s.substring(5).trim();
	        }
            flags.put(s,c);
	    }

	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)&&(msg.value()>0))
		{
			MOB M=null;
			if(affected instanceof MOB)
				M=(MOB)affected;
			else
			if((affected instanceof Item)
			&&(!((Item)affected).amWearingAt(Wearable.IN_INVENTORY))
			&&(((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof MOB))
				M=(MOB)((Item)affected).owner();
			if(M==null) return true;
			if(!msg.amITarget(M)) return true;
			if(msg.tool()==null) return true;
			if(flags.size()==0) return true;

			boolean immune=flags.containsKey("ALL")&&(((Character)flags.get("ALL")).charValue()=='+');
			Character foundPlusMinus=null;
			for(int i : CharStats.CODES.SAVING_THROWS())
				if((CharStats.CODES.CMMSGMAP(i)==msg.sourceMinor())
				&&(i!=CharStats.STAT_SAVE_MAGIC))
				{
					foundPlusMinus=(Character)flags.get(CharStats.CODES.NAME(i));
					if(foundPlusMinus!=null)
					{
						if((foundPlusMinus.charValue()=='-')&&(immune))
							immune=false;
						else
						if(foundPlusMinus.charValue()!='-')
							immune=true;
						break;
					}
				}

			if((foundPlusMinus==null)&&(msg.tool() instanceof Weapon))
			{
			    foundPlusMinus=(Character)flags.get(Weapon.TYPE_DESCS[((Weapon)msg.tool()).weaponType()]);
			    foundPlusMinus=(Character)flags.get(Weapon.CLASS_DESCS[((Weapon)msg.tool()).weaponClassification()]);
			    foundPlusMinus=(Character)flags.get((CMLib.flags().isABonusItems(msg.tool()))?"MAGIC":"NONMAGIC");
			    foundPlusMinus=(Character)flags.get(RawMaterial.CODES.NAME(((Weapon)msg.tool()).material()));
				if(foundPlusMinus!=null)
				{
					if((foundPlusMinus.charValue()=='-')&&(immune))
						immune=false;
					else
					if(foundPlusMinus.charValue()!='-')
						immune=true;
				}
				else
				{
				    Object O=flags.get("LEVEL");
					if((O!=null)&&(O instanceof String)&&(((String)O).length()>3))
					{
						String lvl=(String)O;
						foundPlusMinus=new Character(lvl.charAt(0));
						lvl=lvl.substring(2).trim();
						if((foundPlusMinus.charValue()=='-')&&(immune))
						{
							if(msg.tool().envStats().level()>=CMath.s_int(lvl))
								immune=false;
						}
						else
						if(foundPlusMinus.charValue()!='-')
						{
							if(msg.tool().envStats().level()<CMath.s_int(lvl))
								immune=true;
						}
					}
				}
			}

			if((foundPlusMinus==null)&&(msg.tool() instanceof Ability))
			{
				int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES;
				switch(classType)
				{
				case Ability.ACODE_SPELL:
				case Ability.ACODE_PRAYER:
				case Ability.ACODE_CHANT:
				case Ability.ACODE_SONG:
					{
				    	foundPlusMinus=(Character)flags.get("MAGICSKILLS");
						if(foundPlusMinus==null) foundPlusMinus=(Character)flags.get("MAGIC");
						if(foundPlusMinus!=null)
						{
							if((foundPlusMinus.charValue()=='-')&&(immune))
								immune=false;
							else
							if(foundPlusMinus.charValue()!='-')
								immune=true;
						}
					}
					break;
				default:
					break;
				}
			}
			if(immune) msg.setValue(0);
		}
		return true;
	}
}
