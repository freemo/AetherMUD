package com.planet_ink.coffee_mud.Abilities.Properties;

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
public class Prop_SafePet extends Property
{
	boolean disabled=false;
	public String ID() { return "Prop_SafePet"; }
	public String name(){ return "Unattackable Pets";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}

	public String accountForYourself()
	{ return "Unattackable";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS)&&(msg.amITarget(affected))&&(affected!=null)&&(!disabled)))
		{
			msg.source().tell("Ah, leave "+affected.name()+" alone.");
			if(affected instanceof MOB)
				((MOB)affected).makePeace();
			return false;
		}
		else
		if((affected!=null)&&(affected instanceof MOB)&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))&&(msg.amISource((MOB)affected)))
			disabled=true;
		return super.okMessage(myHost,msg);
	}
}
