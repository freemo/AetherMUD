package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Clericness extends CombatAbilities
{
	public String ID(){return "Clericness";}


	boolean confirmedSetup=false;

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		String className="Cleric";
		combatMode=COMBAT_RANDOM;
		String theparms=getParmsMinusCombatMode();
		if((theparms.length()>0)&&((CMClass.getCharClass(theparms)!=null)))
		   className=theparms;
		if(!mob.baseCharStats().getCurrentClass().ID().equals(className))
		{
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass(className));
			mob.recoverCharStats();
		}
		// now equip character...
		newCharacter(mob);
	}
}