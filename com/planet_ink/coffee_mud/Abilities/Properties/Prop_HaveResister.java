package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

// this ability is the very picture of the infectuous msg.
// It lobs itself onto other qualified objects, and withdraws
// again when it will.  Don't lothe the HaveResister, LOVE IT.
public class Prop_HaveResister extends Property
{
	public String ID() { return "Prop_HaveResister"; }
	public String name(){ return "Resistance due to ownership";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public boolean bubbleAffect(){return true;}
	private CharStats adjCharStats=null;

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		adjCharStats=new DefaultCharStats();
		setAdjustments(this,adjCharStats);
	}

	private void ensureStarted()
	{
		if(adjCharStats==null)
			setMiscText(text());
	}

	public static void adjCharStats(CharStats affectedStats,
									CharStats adjCharStats)
	{
		affectedStats.setStat(CharStats.SAVE_MAGIC,affectedStats.getStat(CharStats.SAVE_MAGIC)+adjCharStats.getStat(CharStats.SAVE_MAGIC));
		affectedStats.setStat(CharStats.SAVE_GAS,affectedStats.getStat(CharStats.SAVE_GAS)+adjCharStats.getStat(CharStats.SAVE_GAS));
		affectedStats.setStat(CharStats.SAVE_FIRE,affectedStats.getStat(CharStats.SAVE_FIRE)+adjCharStats.getStat(CharStats.SAVE_FIRE));
		affectedStats.setStat(CharStats.SAVE_ELECTRIC,affectedStats.getStat(CharStats.SAVE_ELECTRIC)+adjCharStats.getStat(CharStats.SAVE_ELECTRIC));
		affectedStats.setStat(CharStats.SAVE_MIND,affectedStats.getStat(CharStats.SAVE_MIND)+adjCharStats.getStat(CharStats.SAVE_MIND));
		affectedStats.setStat(CharStats.SAVE_JUSTICE,affectedStats.getStat(CharStats.SAVE_JUSTICE)+adjCharStats.getStat(CharStats.SAVE_JUSTICE));
		affectedStats.setStat(CharStats.SAVE_COLD,affectedStats.getStat(CharStats.SAVE_COLD)+adjCharStats.getStat(CharStats.SAVE_COLD));
		affectedStats.setStat(CharStats.SAVE_ACID,affectedStats.getStat(CharStats.SAVE_ACID)+adjCharStats.getStat(CharStats.SAVE_ACID));
		affectedStats.setStat(CharStats.SAVE_WATER,affectedStats.getStat(CharStats.SAVE_WATER)+adjCharStats.getStat(CharStats.SAVE_WATER));
		affectedStats.setStat(CharStats.SAVE_UNDEAD,affectedStats.getStat(CharStats.SAVE_UNDEAD)+adjCharStats.getStat(CharStats.SAVE_UNDEAD));
		affectedStats.setStat(CharStats.SAVE_DISEASE,affectedStats.getStat(CharStats.SAVE_DISEASE)+adjCharStats.getStat(CharStats.SAVE_DISEASE));
		affectedStats.setStat(CharStats.SAVE_POISON,affectedStats.getStat(CharStats.SAVE_POISON)+adjCharStats.getStat(CharStats.SAVE_POISON));
		affectedStats.setStat(CharStats.SAVE_PARALYSIS,affectedStats.getStat(CharStats.SAVE_PARALYSIS)+adjCharStats.getStat(CharStats.SAVE_PARALYSIS));
		affectedStats.setStat(CharStats.SAVE_TRAPS,affectedStats.getStat(CharStats.SAVE_TRAPS)+adjCharStats.getStat(CharStats.SAVE_TRAPS));
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		ensureStarted();
		adjCharStats(affectedStats,adjCharStats);
		super.affectCharStats(affectedMOB,affectedStats);
	}


	public static boolean checkProtection(Ability me, String protType)
	{
		int prot=getProtection(me,protType);
		if(prot==0) return false;
		if(prot<5) prot=5;
		if(prot>95) prot=95;
		if(Dice.rollPercentage()<prot)
			return true;
		return false;
	}

	public static int getProtection(Ability me, String protType)
	{
		int z=me.text().toUpperCase().indexOf(protType.toUpperCase());
		if(z<0) return 0;
		int x=me.text().indexOf("%",z+protType.length());
		if(x<0)
			return 50;
		else
		{
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(me.text().charAt(x)))
					tot+=Util.s_int(""+me.text().charAt(x))*mul;
				else
				{
					if(me.text().charAt(x)=='-')
						mul=mul*-1;
					x=-1;
				}
				mul=mul*10;
			}
			return tot;
		}
	}
	public static void setAdjustments(Ability me, CharStats adjCharStats)
	{
		adjCharStats.setStat(CharStats.SAVE_MAGIC,getProtection(me,"magic"));
		adjCharStats.setStat(CharStats.SAVE_GAS,getProtection(me,"gas"));
		adjCharStats.setStat(CharStats.SAVE_FIRE,getProtection(me,"fire"));
		adjCharStats.setStat(CharStats.SAVE_ELECTRIC,getProtection(me,"elec"));
		adjCharStats.setStat(CharStats.SAVE_MIND,getProtection(me,"mind"));
		adjCharStats.setStat(CharStats.SAVE_JUSTICE,getProtection(me,"justice"));
		adjCharStats.setStat(CharStats.SAVE_COLD,getProtection(me,"cold"));
		adjCharStats.setStat(CharStats.SAVE_ACID,getProtection(me,"acid"));
		adjCharStats.setStat(CharStats.SAVE_WATER,getProtection(me,"water"));
		adjCharStats.setStat(CharStats.SAVE_UNDEAD,getProtection(me,"evil"));
		adjCharStats.setStat(CharStats.SAVE_DISEASE,getProtection(me,"disease"));
		adjCharStats.setStat(CharStats.SAVE_POISON,getProtection(me,"poison"));
		adjCharStats.setStat(CharStats.SAVE_PARALYSIS,getProtection(me,"paralyze"));
		adjCharStats.setStat(CharStats.SAVE_TRAPS,getProtection(me,"traps"));
	}

	public static void resistAffect(CMMsg msg, MOB mob, Ability me)
	{
		if(mob.location()==null) return;
		if(mob.amDead()) return;
		if(!msg.amITarget(mob)) return;

		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(msg.tool()!=null)
		   &&(msg.tool() instanceof Weapon))
		{
			int recovery=msg.value();
			recovery=(int)Math.round(Util.mul(recovery,Math.random()));
			if(recovery<=0) recovery=0;
			if(Prop_HaveResister.checkProtection(me,"weapons"))
				mob.curState().adjHitPoints(recovery,mob.maxState());
			else
			{
				Weapon W=(Weapon)msg.tool();
				if(((W.weaponType()==Weapon.TYPE_BASHING)&&(Prop_HaveResister.checkProtection(me,"blunt")))
				 ||((W.weaponType()==Weapon.TYPE_PIERCING)&&(Prop_HaveResister.checkProtection(me,"pierce")))
				 ||((W.weaponType()==Weapon.TYPE_SLASHING)&&(Prop_HaveResister.checkProtection(me,"slash"))))
					mob.curState().adjHitPoints(recovery,mob.maxState());
			}
			return;
		}
	}

	public String accountForYourself()
	{
		String id="The owner gains resistances: "+text();
		return id;
	}

	public static boolean isOk(CMMsg msg, Ability me, MOB mob)
	{
		if(!Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			return true;

		if(Util.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
		{
			if(msg.tool() instanceof Ability)
			{
				Ability A=(Ability)msg.tool();
				if((A.ID().equals("Spell_Summon"))
				   ||(A.ID().equals("Spell_Gate")))
				{
					if(Prop_HaveResister.checkProtection(me,"teleport"))
					{
						msg.source().tell("You can't seem to fixate on '"+mob.name()+"'.");
						return false;
					}
				}
				else
				if((A.classificationCode()==Ability.PRAYER)
				&&(Util.bset(A.flags(),Ability.FLAG_HOLY))
				&&(!Util.bset(A.flags(),Ability.FLAG_UNHOLY)))
				{
					if(Prop_HaveResister.checkProtection(me,"holy"))
					{
						mob.location().show(msg.source(),mob,CMMsg.MSG_OK_VISUAL,"Holy energies from <S-NAME> are repelled from <T-NAME>.");
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected!=null)
		&&(affected instanceof Item)
		&&(((Item)affected).owner() instanceof MOB))
		{
			MOB mob=(MOB)((Item)affected).owner();
			if((msg.amITarget(mob))&&(msg.value()<=0)&&(mob.location()!=null))
			{
				if(!Prop_HaveResister.isOk(msg,this,mob))
					return false;
				Prop_HaveResister.resistAffect(msg,mob,this);
			}
		}
		return true;
	}
}