package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
public class GenSSBattery extends GenShipComponent
{
	public String ID(){	return "GenSSBattery";}
	public GenSSBattery()
	{
		super();
		setName("a generic ships battery");
		baseEnvStats.setWeight(2);
		setDisplayText("a generic ships battery sits here.");
		setDescription("");
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_STEEL);
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenSSBattery)) return false;
		return super.sameAs(E);
	}
}
