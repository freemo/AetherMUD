package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class GenShield extends GenArmor implements Shield
{
	public GenShield()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a shield";
		displayText="a sturdy round shield sits here.";
		description="Its made of steel, and looks in good shape.";
		properWornBitmap=Item.HELD;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(10);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(15);
		recoverEnvStats();
		material=EnvResource.RESOURCE_OAK;
	}
	public boolean isGeneric(){return true;}

	public Environmental newInstance()
	{
		return new GenShield();
	}
}
