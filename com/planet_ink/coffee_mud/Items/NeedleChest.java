package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class NeedleChest extends LargeChest
{
	public NeedleChest()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		Trap t=(Trap)CMClass.getAbility("Trap_Open");
		t.baseEnvStats().setAbility(Trap.TRAP_NEEDLE);
		t.recoverEnvStats();
		t.setTrapped(this,t,true);
		isLocked=false;
		setMaterial(EnvResource.RESOURCE_OAK);
	}

	public Environmental newInstance()
	{
		return new NeedleChest();
	}
}
