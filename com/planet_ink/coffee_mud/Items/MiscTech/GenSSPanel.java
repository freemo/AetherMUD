package com.planet_ink.coffee_mud.Items.MiscTech;
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
public class GenSSPanel extends GenShipContainer
	implements ShipComponent.ShipPanel
{
	public String ID(){	return "GenSSPanel";}
	public GenSSPanel()
	{
		super();
		setName("a generic space ship panel");
		baseEnvStats.setWeight(2);
		setDescription("");
		baseGoldValue=5;
		containType=Container.CONTAIN_SSCOMPONENTS;
		setLidsNLocks(true,true,false,false);
		capacity=500;
		setMaterial(RawMaterial.RESOURCE_STEEL);
		recoverEnvStats();
	}

	protected int panelType=ShipComponent.ShipPanel.COMPONENT_PANEL_ANY;
	public int panelType(){return panelType;}
	public void setPanelType(int type){panelType=type;}
	
	public String displayText(){
		if(isOpen())
			return name()+" is opened here.";
		return "";
	}
	public boolean canContain(Environmental E)
	{
		if(!super.canContain(E)) return false;
		if(E instanceof ShipComponent)
		{
			switch(panelType())
			{
			case ShipComponent.ShipPanel.COMPONENT_PANEL_ANY:
				return true;
			case ShipComponent.ShipPanel.COMPONENT_PANEL_ENGINE:
				return E instanceof ShipComponent.ShipEngine;
			case ShipComponent.ShipPanel.COMPONENT_PANEL_POWER:
				return E instanceof ShipComponent.ShipPowerSource;
			case ShipComponent.ShipPanel.COMPONENT_PANEL_SENSOR:
				return E instanceof ShipComponent.ShipSensor;
			case ShipComponent.ShipPanel.COMPONENT_PANEL_WEAPON:
				return E instanceof ShipComponent.ShipWeapon;
			case ShipComponent.ShipPanel.COMPONENT_PANEL_COMPUTER:
				return E instanceof Software;
			case ShipComponent.ShipPanel.COMPONENT_PANEL_ENVIRO:
				return E instanceof ShipComponent.ShipEnviroControl;
			default:
			    return true;
			}
		}
		return true;
	}

	public boolean isGeneric(){return true;}
}
