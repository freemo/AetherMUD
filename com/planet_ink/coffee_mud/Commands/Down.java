package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Down extends Go
{
	public Down(){}
	
	private String[] access={"DOWN","D"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		standIfNecessary(mob);
		if(Sense.isSitting(mob))
		{
			mob.tell(getScr("Movement","standandgoerr1"));
			return false;
		}
		move(mob,Directions.DOWN,false,false,false);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
