package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_DesertTactics extends Fighter_FieldTactics
{
	public String ID() { return "Fighter_DesertTactics"; }
	public String name(){ return "Desert Tactics";}
	private static final Integer[] landClasses = {new Integer(Room.DOMAIN_OUTDOORS_DESERT)};
	public Integer[] landClasses(){return landClasses;}
}
