package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bardness extends CombatAbilities
{
	private int tickDown=0;
	public Bardness()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

	public Behavior newInstance()
	{
		return new Bardness();
	}

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		if(!mob.baseCharStats().getMyClass().ID().equals("Bard"))
		{
			mob.baseCharStats().setMyClass(CMClass.getCharClass("Bard"));
			mob.recoverCharStats();
		}
		// now equip character...
		newCharacter(mob);
	}
}