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
package com.syncleus.aethermud.game.Abilities.Fighter;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Fighter_Charge extends FighterSkill {
    private final static String localizedName = CMLib.lang().L("Charge");
    private static final String[] triggerStrings = I(new String[]{"CHARGE"});
    private final static String localizedStaticDisplay = CMLib.lang().L("(Charging!!)");
    public boolean done = false;
    protected int code = 0;

    @Override
    public String ID() {
        return "Fighter_Charge";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public String displayText() {
        return localizedStaticDisplay;
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return Ability.CAN_MOBS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_ACROBATIC;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT;
    }

    @Override
    public int minRange() {
        return 1;
    }

    @Override
    public int maxRange() {
        return adjustedMaxInvokerRange(2);
    }

    @Override
    public int abilityCode() {
        return code;
    }

    @Override
    public void setAbilityCode(int c) {
        code = c;
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        if ((affected != null)
            && (affected instanceof MOB)
            && (msg.amISource((MOB) affected))
            && (msg.targetMinor() == CMMsg.TYP_WEAPONATTACK))
            done = true;
        super.executeMsg(myHost, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (tickID == Tickable.TICKID_MOB) {
            if (done)
                unInvoke();
        }
        return super.tick(ticking, tickID);
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        final int xlvl = getXLEVELLevel(invoker());
        affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + (2 * (affected.phyStats().level() + xlvl)));
        affectableStats.setDamage(affectableStats.damage() + (affected.phyStats().level()) + abilityCode() + xlvl);
        affectableStats.setArmor(affectableStats.armor() + (2 * (xlvl + affected.phyStats().level())));
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if ((mob != null) && (target != null)) {
            if ((mob.isInCombat()) && (mob.rangeToTarget() <= 0))
                return Ability.QUALITY_INDIFFERENT;
            if ((CMLib.flags().isSitting(mob)) || (mob.riding() != null))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final boolean notInCombat = !mob.isInCombat();
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if ((mob.isInCombat())
            && (mob.rangeToTarget() <= 0)) {
            mob.tell(L("You can not charge while in melee!"));
            return false;
        }

        if ((CMLib.flags().isSitting(mob)) || (mob.riding() != null)) {
            mob.tell(L("You must be on your feet to charge!"));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        // now see if it worked
        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, CMMsg.MASK_MALICIOUS | CMMsg.MSG_ADVANCE, L("^F^<FIGHT^><S-NAME> charge(s) at <T-NAMESELF>!^</FIGHT^>^?"));
            CMLib.color().fixSourceFightColor(msg);
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (mob.getVictim() == target) {
                    mob.setRangeToTarget(0);
                    target.setRangeToTarget(0);
                    beneficialAffect(mob, mob, asLevel, 2);
                    mob.recoverPhyStats();
                    if (notInCombat) {
                        done = true;
                        CMLib.combat().postAttack(mob, target, mob.fetchWieldedItem());
                    } else
                        done = false;
                    if (mob.getVictim() == null)
                        mob.setVictim(null); // correct range
                    if (target.getVictim() == null)
                        target.setVictim(null); // correct range
                }
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> attempt(s) to charge <T-NAME>, but then give(s) up."));

        // return whether it worked
        return success;
    }
}
