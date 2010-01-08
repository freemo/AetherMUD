package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.Items.MiscMagic.StdWand;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class GenStaff extends GenWeapon implements Wand
{
	public String ID(){	return "GenStaff";}
	protected String secretWord=CMProps.getSListVar(CMProps.SYSTEML_MAGIC_WORDS)[CMLib.dice().roll(1,CMProps.getSListVar(CMProps.SYSTEML_MAGIC_WORDS).length,0)-1];

	public GenStaff()
	{
		super();

		setName("a wooden staff");
		setDisplayText("a wooden staff lies in the corner of the room.");
		setDescription("");
		secretIdentity="";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(4);
		baseGoldValue=1;
		recoverEnvStats();
		wornLogicalAnd=true;
		material=RawMaterial.RESOURCE_OAK;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		weaponType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_STAFF;
		recoverEnvStats();
	}

	protected int maxUses=Integer.MAX_VALUE;
	public int maxUses(){return maxUses;}
	public void setMaxUses(int newMaxUses){maxUses=newMaxUses;}
	
	public boolean isGeneric(){return true;}

	public int value()
	{
		if(usesRemaining()<=0)
			return 0;
		return super.value();
	}
	public void setSpell(Ability theSpell)
	{
		readableText="";
		if(theSpell!=null)
			readableText=theSpell.ID();
		secretWord=StdWand.getWandWord(readableText);
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text){ readableText=text;secretWord=StdWand.getWandWord(readableText);}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		Ability A=getSpell();
		if(A!=null)
			id="'A staff of "+A.name()+"' Charges: "+usesRemaining()+"\n\r"+id;
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	public Ability getSpell()
	{
		return CMClass.getAbility(readableText());
	}

	public String magicWord()
	{
		return secretWord;
	}
	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   String message)
	{
		StdWand.waveIfAble(mob,afftarget,message,this);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if(msg.amITarget(this))
				StdWand.waveIfAble(mob,msg.tool(),msg.targetMessage(),this);
			break;
		case CMMsg.TYP_SPEAK:
			if(msg.sourceMinor()==CMMsg.TYP_SPEAK)
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,msg.targetMessage(),CMMsg.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}
	// wand stats handled by genweapon, filled by readableText
}
