package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druidness extends CombatAbilities
{
	public String ID(){return "Druidness";}
	public Behavior newInstance()
	{
		return new Druidness();
	}
	boolean confirmedSetup=false;

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		String className="Druid";
		if((getParms().length()>0)&&(CMClass.getCharClass(getParms())!=null))
			className=getParms();
		if(!mob.baseCharStats().getCurrentClass().ID().equals(className))
		{
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass(className));
			mob.recoverCharStats();
		}
		// now equip character...
		newCharacter(mob);
	}
}