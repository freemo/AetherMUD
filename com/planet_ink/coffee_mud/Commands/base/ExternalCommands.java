package com.planet_ink.coffee_mud.Commands.base;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.IOException;
public class ExternalCommands implements ExternalCommand
{
	CommandProcessor processor=null;

	public ExternalCommands(CommandProcessor newProcessor)
	{
		processor=newProcessor;
	}
	public boolean wear(MOB mob, Item item)
	{
		return processor.itemUsage.wear(mob,item);
	}
	public boolean remove(MOB mob, Item item)
	{
		return processor.itemUsage.remove(mob,item);
	}
	public void resetRoom(Room room)
	{
		processor.reset.room(room);
	}
	public String getOpenRoomID(String areaName)
	{
		return processor.reset.getOpenRoomID(areaName);
	}
	public void postAttack(MOB attacker, MOB target, Item weapon)
	{
		processor.theFight.postAttack(attacker,target,weapon);
	}
	public Ability getToEvoke(MOB mob, Vector commands)
	{
		return processor.abilityEvoker.getToEvoke(mob,commands);
	}
	public void postDamage(MOB attacker, 
						   MOB target, 
						   Environmental weapon, 
						   int damage,
						   int messageCode,
						   int damageType,
						   String allDisplayMessage)
	{
		processor.theFight.postDamage(attacker,target,weapon,damage,messageCode,damageType,allDisplayMessage);
	}
	public String standardHitWord(int weaponType, int damageAmount)
	{
		return processor.theFight.standardHitWord(weaponType,damageAmount);
	}
	public String standardMissString(int weaponType, int weaponClassification, String weaponName, boolean useExtendedMissString)
	{
		return processor.theFight.standardMissString(weaponType,weaponClassification,weaponName,useExtendedMissString);
	}
	public StringBuffer niceLister(MOB mob, Vector items, boolean useName)
	{
		return processor.scoring.niceLister(mob,items,useName);
	}
	public void standIfNecessary(MOB mob)
	{
		processor.movement.standIfNecessary(mob);
	}
	public void look(MOB mob, Vector commands, boolean quiet)
	{
		processor.basicSenses.look(mob,commands,quiet);
	}
	public void resistanceMsgs(Affect affect, MOB source, MOB target)
	{
		processor.theFight.resistanceMsgs(affect,source,target);
	}
	public void strike(MOB source, MOB target, Weapon weapon, boolean success)
	{
		processor.theFight.strike(source,target,weapon,success);
	}
	public void die(MOB source, MOB target)
	{
		processor.theFight.die(source,target);
	}
	public boolean isHit(MOB attacker, MOB target)
	{
		return processor.theFight.isHit(attacker,target);
	}
	public long adjustedAttackBonus(MOB mob)
	{
		return processor.theFight.adjustedAttackBonus(mob);
	}
	public Hashtable properTargets(Ability A, MOB caster, boolean beRuthless)
	{
		return processor.theFight.properTargets(A,caster,beRuthless);
	}
	public String standardMobCondition(MOB mob)
	{
		return processor.theFight.standardMobCondition(mob);
	}
	public boolean move(MOB mob, int directionCode, boolean flee)
	{
		return processor.movement.move(mob,directionCode,flee);
	}
	public void flee(MOB mob, String direction)
	{
		processor.movement.flee(mob,direction);
	}
	public void roomAffectFully(Affect msg, Room room, int dirCode)
	{
		processor.movement.roomAffectFully(msg,room,dirCode);
	}
	public StringBuffer getEquipment(MOB seer, MOB mob)
	{
		return processor.scoring.getEquipment(seer,mob);
	}
	public void doCommand(MOB mob, Vector commands)
		throws Exception
	{
		processor.doCommand(mob,commands);
	}
	public String shortAlignmentStr(int al)
	{
		return processor.scoring.shortAlignmentStr(al);
	}
	public String alignmentStr(int al)
	{
		return processor.scoring.alignmentStr(al);
	}
	public StringBuffer getInventory(MOB seer, MOB mob)
	{
		return processor.scoring.getInventory(seer,mob);
	}
	public int getMyDirCode(Exit exit, Room room, int testCode)
	{
		return processor.movement.getMyDirCode(exit,room,testCode);
	}
	public boolean drop(MOB mob, Environmental dropThis)
	{
		return processor.itemUsage.drop(mob,dropThis);
	}
	public void read(MOB mob, Environmental thisThang, String theRest)
	{
		processor.itemUsage.read(mob,thisThang,theRest);
	}
	public void quickSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag)
	{
		processor.socialProcessor.quickSay(mob,target,text,isPrivate,tellFlag);
	}
	public StringBuffer getScore(MOB mob)
	{
		return processor.scoring.getScore(mob);
	}
	public boolean get(MOB mob, Item container, Item getThis, boolean quiet)
	{
		return processor.itemUsage.get(mob,container,getThis,quiet);
	}
	public void follow(MOB mob, MOB tofollow, boolean quiet)
	{
		processor.grouping.processFollow(mob,tofollow, quiet);
	}
	public boolean login(MOB mob)
		throws IOException
	{
		return processor.frontDoor.login(mob);
	}
}