package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_ImprovedResists extends Paladin
{
	public String ID() { return "Paladin_ImprovedResists"; }
	public String name(){ return "Paladin`s Resistance";}
	public Environmental newInstance(){	return new Paladin_ImprovedResists();}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((affected!=null)&&(affected.getAlignment()>650))
		{
			int amount=(int)Math.round(Util.mul(Util.div(profficiency(),100.0),affected.envStats().level()));
			affectableStats.setStat(CharStats.SAVE_ACID,affectableStats.getStat(CharStats.SAVE_ACID)+amount);
			affectableStats.setStat(CharStats.SAVE_COLD,affectableStats.getStat(CharStats.SAVE_COLD)+amount);
			affectableStats.setStat(CharStats.SAVE_ELECTRIC,affectableStats.getStat(CharStats.SAVE_ELECTRIC)+amount);
			affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_FIRE)+amount);
			affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+amount);
			affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+amount);
			affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+amount);
			affectableStats.setStat(CharStats.SAVE_MAGIC,affectableStats.getStat(CharStats.SAVE_MAGIC)+amount);
			affectableStats.setStat(CharStats.SAVE_JUSTICE,affectableStats.getStat(CharStats.SAVE_JUSTICE)+amount);
			affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+amount);
			affectableStats.setStat(CharStats.SAVE_WATER,affectableStats.getStat(CharStats.SAVE_WATER)+amount);
			affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+amount);
			affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+amount);
		}
	}
}
