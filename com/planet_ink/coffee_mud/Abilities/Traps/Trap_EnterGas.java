package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_EnterGas extends Trap_Enter
{
	public String ID() { return "Trap_EnterGas"; }
	public String name(){ return "Entry Gas Trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapType(){return TRAP_GAS;}
}
