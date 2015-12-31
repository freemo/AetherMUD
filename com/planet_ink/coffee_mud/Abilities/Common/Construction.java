package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.BuildingSkill.Building;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2015 Bo Zimmerman

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


public class Construction extends BuildingSkill
{
	@Override
	public String ID()
	{
		return "Construction";
	}

	private final static String	localizedName	= CMLib.lang().L("Construction");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CONSTRUCT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "WOODEN";
	}

	@Override
	public String parametersFile()
	{
		return "construction.txt";
	}

	@Override
	protected String getMainResourceName()
	{
		return "Wood";
	}

	@Override
	protected String getClosedLocaleType()
	{
		return "WoodRoom";
	}
	
	@Override
	protected String getMazeLocaleType()
	{
		return "WoodRoomMaze";
	}

	@Override
	protected String getSoundName()
	{
		return "hammer.wav";
	}

	public Construction()
	{
		super();
	}

	@Override
	protected int[][] getBasicMaterials(final MOB mob, int woodRequired)
	{
		final int[] pm={RawMaterial.MATERIAL_WOODEN};
		final int[][] idata=fetchFoundResourceData(mob,
													woodRequired,"wood",pm,
													0,null,null,
													false,
													0,null);
		return idata;
	}
	
}
