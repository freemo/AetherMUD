package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Tinnitus extends Disease
{
	public String ID() { return "Disease_Tinnitus"; }
	public String name(){ return "Tinnitus";}
	public String displayText(){ return "(Tinnitus)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Tinnitus();}

	protected int DISEASE_TICKS(){return 100;}
	protected int DISEASE_DELAY(){return 1;}
	protected String DISEASE_DONE(){return "Your ears stop ringing.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with tinnitus.^?";}
	protected String DISEASE_AFFECT(){return "";}
	protected boolean DISEASE_STD(){return false;}
	protected boolean DISEASE_TOUCHSPREAD(){return false;}
	
	protected boolean ringing=false;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if((affected==null)||(invoker==null)) return false;

		MOB mob=(MOB)affected;
		if((--diseaseTick)<=0)
		{
			diseaseTick=DISEASE_DELAY();
			if(Dice.rollPercentage()>mob.charStats().getStat(CharStats.SAVE_DISEASE))
				ringing=true;
			else
				ringing=false;
			mob.recoverEnvStats();
			return true;
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((affected==null)||(!ringing)) return;
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
	}
}
