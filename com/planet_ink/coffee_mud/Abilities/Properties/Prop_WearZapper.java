package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WearZapper extends Property
{
	public String ID() { return "Prop_WearZapper"; }
	public String name(){ return "Restrictions to wielding/wearing/holding";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	Prop_WearZapper BOB=new Prop_WearZapper();	BOB.setMiscText(text()); return BOB;}

	public String accountForYourself()
	{
		return "Wearing restricted as follows: "+MUDZapper.zapperDesc(miscText);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(affected==null) return false;
		if(!(affected instanceof Item)) return false;
		Item myItem=(Item)affected;

		MOB mob=msg.source();
		if(mob.location()==null)
			return true;

		if(msg.amITarget(myItem))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
			if((!MUDZapper.zapperCheck(text(),mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,myItem,CMMsg.MSG_OK_VISUAL,"<O-NAME> flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case CMMsg.TYP_WEAR:
			if((!MUDZapper.zapperCheck(text(),mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,myItem,CMMsg.MSG_OK_VISUAL,"<O-NAME> flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case CMMsg.TYP_WIELD:
			if((!MUDZapper.zapperCheck(text(),mob))&&(Prop_SpellAdder.didHappen(100,this)))
			{
				mob.location().show(mob,null,myItem,CMMsg.MSG_OK_VISUAL,"<O-NAME> flashes and falls out of <S-HIS-HER> hands!");
				return false;
			}
			break;
		case CMMsg.TYP_GET:
			break;
		case CMMsg.TYP_DROP:
		case CMMsg.TYP_THROW:
			break;
		default:
			break;
		}
		return true;
	}
}