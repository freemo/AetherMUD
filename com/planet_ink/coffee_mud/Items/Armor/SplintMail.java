package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

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
public class SplintMail extends StdArmor
{
	public String ID(){	return "SplintMail";}
	public SplintMail()
	{
		super();

		setName("suit of splint mail");
		setDisplayText("a suit of splint mail.");
		setDescription("A suit of splint mail armor including everything to protect the body, legs and arms.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(44);
		baseEnvStats().setWeight(60);
		baseEnvStats().setAbility(0);
		baseGoldValue=160;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

}
