package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class FrozenMountains extends Mountains
{
	public String ID(){return "FrozenMountains";}
	public FrozenMountains()
	{
		super();
		recoverEnvStats();
		domainCondition=Room.CONDITION_COLD;
	}

	public Vector resourceChoices(){return Mountains.roomResources;}
}
