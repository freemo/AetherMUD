/**
 * Copyright 2017 Syncleus, Inc.
 * with portions copyright 2004-2017 Bo Zimmerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.planet_ink.game.Abilities.Prayers;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.CharStats;
import com.planet_ink.game.Common.interfaces.Faction;
import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.Weapon;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMProps;
import com.planet_ink.game.core.interfaces.Environmental;
import com.planet_ink.game.core.interfaces.Physical;
import com.planet_ink.game.core.interfaces.Tickable;

import java.util.List;


public class Prayer_SnakeStaff extends Prayer {
    private final static String localizedName = CMLib.lang().L("Snake Staff");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Snake Staff)");
    protected volatile Item theStaff = null;

    @Override
    public String ID() {
        return "Prayer_SnakeStaff";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return localizedStaticDisplay;
    }

    @Override
    protected int canAffectCode() {
        return 0;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_SELF;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_HOLYPROTECTION;
    }

    @Override
    public long flags() {
        return Ability.FLAG_NEUTRAL;
    }

    @Override
    public void setAffectedOne(Physical affected) {
        super.setAffectedOne(affected);
    }

    @Override
    public void unInvoke() {
        if (affected instanceof Item) {
            Physical affected = this.affected;
            super.unInvoke();
            affected.delEffect(this);
            return;
        }
        final MOB mob = (MOB) affected;
        if (theStaff != null) {
            final MOB invoker = invoker();
            if ((invoker != null) && (canBeUninvoked())) {
                if ((invoker.location() != null) && (mob != null))
                    invoker.location().show(mob, invoker, CMMsg.MSG_OK_ACTION, L("<S-NAME> reform(s) into @x1 in <T-YOUPOSS> hands.", theStaff.name()));
                invoker.addItem(theStaff);
                theStaff.setRawWornCode(0);
                theStaff.recoverPhyStats();
                invoker.recoverPhyStats();
                theStaff.wearIfPossible(invoker);
                if (theStaff.rawWornCode() != 0)
                    invoker.executeMsg(invoker, CMClass.getMsg(invoker, theStaff, CMMsg.MSG_WIELD | CMMsg.MASK_ALWAYS, null));
                theStaff.recoverPhyStats();
                invoker.recoverPhyStats();
            }
            theStaff = null;
        }
        super.unInvoke();
        if ((canBeUninvoked()) && (mob != null)) {
            if (mob.amDead())
                mob.setLocation(null);
            mob.destroy();
        }
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (tickID == Tickable.TICKID_MOB) {
            if (affected instanceof MOB) {
                final MOB mob = (MOB) affected;
                if (!mob.isInCombat())
                    if ((mob.amFollowing() == null)
                        || (mob.location() == null)
                        || (mob.amDead())
                        || ((invoker != null)
                        && ((mob.location() != invoker.location()) || (!CMLib.flags().isInTheGame(invoker, true))))) {
                        unInvoke();
                    }
            }
        }
        return super.tick(ticking, tickID);
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        super.executeMsg(myHost, msg);
        if ((affected instanceof MOB)
            && (msg.amISource((MOB) affected) || msg.amISource(((MOB) affected).amFollowing()) || (msg.source() == invoker()))
            && (msg.sourceMinor() == CMMsg.TYP_QUIT)) {
            unInvoke();
            if (msg.source().playerStats() != null)
                msg.source().playerStats().setLastUpdated(0);
        }
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        Item target = mob.fetchWieldedItem();
        if ((auto) && (givenTarget instanceof Item))
            target = (Item) givenTarget;
        if (target == null) {
            mob.tell(L("You aren't wielding a staff!"));
            return false;
        }
        if (target.fetchEffect(this.ID()) != null) {
            mob.tell(mob, target, null, L("<T-NAME> <T-IS-ARE> already affected by @x1.", name()));
            return false;
        }

        if ((!(target instanceof Weapon)) || (((Weapon) target).weaponClassification() != Weapon.CLASS_STAFF)) {
            mob.tell(L("@x1 is not a staff!", target.name()));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), L("^S<S-NAME> @x1 for <T-NAME> to defend <S-HIM-HER>.^?", prayForWord(mob)));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);

                final MOB monster = determineMonster(mob, mob.phyStats().level() + ((getXLEVELLevel(mob) + getX1Level(mob)) / 2), target.name());
                final Prayer_SnakeStaff A = (Prayer_SnakeStaff) super.beneficialAffect(mob, monster, asLevel, 10);
                if (A != null) {
                    if (monster.isInCombat())
                        monster.makePeace(true);
                    CMLib.commands().postFollow(monster, mob, true);
                    invoker = mob;
                    if (monster.amFollowing() != mob)
                        mob.tell(L("@x1 seems unwilling to follow you.", target.name(mob)));
                    A.theStaff = target;
                    mob.delItem(A.theStaff);
                    target.recoverPhyStats();
                    monster.recoverPhyStats();
                    mob.recoverPhyStats();
                }
            }
        } else
            return beneficialWordsFizzle(mob, target, L("^S<S-NAME> @x1 <T-NAME> to defend <S-HIM-HER>, but nothing happens.^?", prayForWord(mob)));
        // return whether it worked
        return success;
    }

    public MOB determineMonster(MOB caster, int level, String staffName) {

        final MOB newMOB = CMClass.getMOB("GenMob");
        newMOB.basePhyStats().setAbility(CMProps.getMobHPBase());
        newMOB.basePhyStats().setLevel(level);
        newMOB.basePhyStats().setWeight(500);
        newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
        newMOB.baseCharStats().setMyRace(CMClass.getRace("Snake"));
        newMOB.baseCharStats().setStat(CharStats.STAT_GENDER, 'M');
        newMOB.baseCharStats().getMyRace().startRacing(newMOB, false);
        newMOB.setName(L("a bunch of snakes"));
        newMOB.setDisplayText(L("a bunch of nasty snakes writhe here"));
        newMOB.setDescription(L("The seem like they might be magical."));
        newMOB.recoverPhyStats();
        newMOB.recoverCharStats();
        newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
        newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
        newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
        newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
        newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
        newMOB.addBehavior(CMClass.getBehavior("CombatAbilities"));
        Ability A = CMClass.getAbility("Poison");
        if (A != null) {
            A.setProficiency(100);
            newMOB.addAbility(A);
        }
        CMLib.factions().setAlignment(newMOB, Faction.Align.NEUTRAL);
        newMOB.recoverCharStats();
        newMOB.recoverPhyStats();
        newMOB.recoverMaxState();
        newMOB.resetToMaxState();
        newMOB.text();
        newMOB.bringToLife(caster.location(), true);
        CMLib.beanCounter().clearZeroMoney(newMOB, null);
        newMOB.setMoneyVariation(0);
        newMOB.location().showOthers(newMOB, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> forms from @x1!", staffName));
        caster.location().recoverRoomStats();
        newMOB.setStartRoom(null);
        return (newMOB);
    }
}
