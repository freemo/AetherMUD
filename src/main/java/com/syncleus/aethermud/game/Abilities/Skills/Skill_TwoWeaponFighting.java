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
package com.syncleus.aethermud.game.Abilities.Skills;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class Skill_TwoWeaponFighting extends StdSkill {
    private final static String localizedName = CMLib.lang().L("Two Weapon Fighting");
    protected volatile boolean attackedSinceLastTick = false;

    @Override
    public String ID() {
        return "Skill_TwoWeaponFighting";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return "";
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_SELF;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_MARTIALLORE;
    }

    @Override
    public boolean isAutoInvoked() {
        return true;
    }

    @Override
    public boolean canBeUninvoked() {
        return false;
    }

    protected Weapon getFirstWeapon(MOB mob) {
        final Item I = mob.fetchWieldedItem();
        if (I instanceof Weapon)
            return (Weapon) I;
        return null;
    }

    protected Weapon getSecondWeapon(MOB mob) {
        final Item I = mob.fetchHeldItem();
        if ((I instanceof Weapon) && (!I.amWearingAt(Wearable.WORN_WIELD)))
            return (Weapon) I;
        return null;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        if (affected instanceof MOB) {
            final MOB mob = (MOB) affected;

            if ((getSecondWeapon(mob) != null) && (getFirstWeapon(mob) != null) && (mob.isInCombat())) {
                final int xlvl = super.getXLEVELLevel(invoker());
                affectableStats.setSpeed(affectableStats.speed() + 1.0 + (0.1 * xlvl));
                affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() - (affectableStats.attackAdjustment() / (5 + xlvl)));
                affectableStats.setDamage(affectableStats.damage() - (affectableStats.damage() / (20 + xlvl)));
            }
        }
    }

    @Override
    public void executeMsg(Environmental host, CMMsg msg) {
        if ((msg.source() == affected)
            && (msg.target() instanceof MOB)
            && (msg.sourceMinor() == CMMsg.TYP_WEAPONATTACK))
            attackedSinceLastTick = true;
        super.executeMsg(host, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if ((tickID == Tickable.TICKID_MOB) && (affected instanceof MOB)) {
            final MOB mob = (MOB) affected;
            if ((mob != null) && (mob.isInCombat())) {
                if (mob.isAttributeSet(MOB.Attrib.AUTODRAW))
                    CMLib.commands().postDraw(mob, true, true);

                final Item primaryWeapon = getFirstWeapon(mob);
                final Item weapon = getSecondWeapon(mob);
                if ((weapon != null) // try to wield anything!
                    && (primaryWeapon != null)
                    && attackedSinceLastTick
                    && (mob.rangeToTarget() >= 0)
                    && (mob.rangeToTarget() >= weapon.minRange())
                    && (mob.rangeToTarget() <= weapon.maxRange())
                    && (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
                    && (!mob.amDead())
                    && (mob.curState().getHitPoints() > 0)
                    && (CMLib.flags().isStanding(mob))
                    && (proficiencyCheck(mob, 0, false))
                    && (!mob.getVictim().amDead())) {
                    primaryWeapon.setRawWornCode(Wearable.WORN_HELD);
                    weapon.setRawWornCode(Wearable.WORN_WIELD);
                    mob.recoverPhyStats();
                    CMLib.combat().postAttack(mob, mob.getVictim(), weapon);
                    weapon.setRawWornCode(Wearable.WORN_HELD);
                    primaryWeapon.setRawWornCode(Wearable.WORN_WIELD);
                    mob.recoverPhyStats();
                    if (CMLib.dice().rollPercentage() == 1)
                        helpProficiency(mob, 0);
                }
            }
            attackedSinceLastTick = false;
        }
        return super.tick(ticking, tickID);
    }
}
