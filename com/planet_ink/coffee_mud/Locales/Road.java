package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Road extends StdRoom
{
	public Road()
	{
		super();
		name="a road";
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_PLAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new Road();
	}
}