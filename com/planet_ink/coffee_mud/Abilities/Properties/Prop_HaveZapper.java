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
public class Prop_HaveZapper extends Property
{
	public String ID() { return "Prop_HaveZapper"; }
	public String name(){ return "Restrictions to ownership";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}

	public String accountForYourself()
	{
		return "Ownership restricted as follows: "+CMLib.masking().maskDesc(text());
	}

    public boolean didHappen(int defaultPct)
    {
        int x=text().indexOf("%");
        if(x<0)
        {
            if(CMLib.dice().rollPercentage()<=defaultPct)
                return true;
            return false;
        }
        int mul=1;
        int tot=0;
        while((--x)>=0)
        {
            if(Character.isDigit(text().charAt(x)))
                tot+=CMath.s_int(""+text().charAt(x))*mul;
            else
                x=-1;
            mul=mul*10;
        }
        if(CMLib.dice().rollPercentage()<=tot)
            return true;
        return false;
    }
    
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null) return false;

		MOB mob=msg.source();
		if(mob.location()==null)
			return true;

		if(msg.amITarget(affected))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
			break;
		case CMMsg.TYP_WEAR:
			break;
		case CMMsg.TYP_WIELD:
			break;
		case CMMsg.TYP_GET:
			if((!CMLib.masking().maskCheck(text(),mob,false))&&(didHappen(100)))
			{
				mob.location().show(mob,null,affected,CMMsg.MSG_OK_ACTION,CMParms.getParmStr(text(),"MESSAGE","<O-NAME> flashes and flies out of <S-HIS-HER> hands!"));
				return false;
			}
			break;
		case CMMsg.TYP_EAT:
		case CMMsg.TYP_DRINK:
			if((!CMLib.masking().maskCheck(text(),mob,false))&&(didHappen(100)))
			{
				mob.location().show(mob,null,affected,CMMsg.MSG_OK_ACTION,CMParms.getParmStr(text(),"MESSAGE","<O-NAME> flashes and falls out <S-HIS-HER> mouth!"));
				return false;
			}
			break;
		default:
			break;
		}
		return true;
	}
}
