package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WhoIs extends BaseWho
{
	public WhoIs(){}

	private String[] access={"WHOIS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String mobName=Util.combine(commands,1);
		if((mobName==null)||(mobName.length()==0))
		{
			mob.tell("whois whom?");
			return false;
		}

		if(mobName.startsWith("@"))
		{
			if(!(CMClass.I3Interface().i3online()))
				mob.tell("I3 is unavailable.");
			else
				CMClass.I3Interface().i3who(mob,mobName.substring(1));
			return false;
		}

		StringBuffer msg=new StringBuffer("");
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=(Session)Sessions.elementAt(s);
			MOB mob2=thisSession.mob();
			if((mob2!=null)
			&&(!thisSession.killFlag())
			&&((Sense.isSeen(mob2)||(mob.isASysOp(null))))
			&&(mob2.envStats().level()>0)
			&&(mob2.name().toUpperCase().startsWith(mobName.toUpperCase())))
				msg.append(showWhoShort(mob2));
		}
		if((mobName!=null)&&(msg.length()==0))
			msg.append("That person doesn't appear to be online.\n\r");
		else
		{
			StringBuffer head=new StringBuffer("");
			head.append("^x[");
			head.append(Util.padRight("Race",12)+" ");
			head.append(Util.padRight("Class",12)+" ");
			head.append(Util.padRight("Level",7));
			head.append("] Character name^.^N\n\r");
			mob.tell(head.toString()+msg.toString());
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
