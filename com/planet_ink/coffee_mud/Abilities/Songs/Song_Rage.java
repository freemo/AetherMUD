package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Rage extends Song
{
	public String ID() { return "Song_Rage"; }
	public String name(){ return "Rage";}
	public int quality(){ return MALICIOUS;}
	protected boolean mindAttack(){return true;}
	public Environmental newInstance(){	return new Song_Rage();}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;
		affectableStats.setDamage(affectableStats.damage()+(int)Math.round(Util.div(affectableStats.damage(),2.0)));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(Util.div(affectableStats.attackAdjustment(),6.0)));
		affectableStats.setArmor(affectableStats.armor()+20);
	}


	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if(affect.amISource(invoker)) return true;
		if(affect.sourceMinor()!=Affect.TYP_FLEE) return true;
		if(affect.source().fetchAffect(this.ID())==null) return true;

		affect.source().tell("You are too enraged to flee.");
		return false;
	}

}
