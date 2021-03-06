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
package com.syncleus.aethermud.game.Abilities.Prayers;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Abilities.interfaces.MendingSkill;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.HashSet;
import java.util.List;


public class Prayer_HolyAura extends Prayer implements MendingSkill {
    private final static String localizedName = CMLib.lang().L("Holy Aura");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Holy Aura)");

    @Override
    public String ID() {
        return "Prayer_HolyAura";
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
        return Ability.CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return Ability.CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_OTHERS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_BLESSING;
    }

    @Override
    public long flags() {
        return Ability.FLAG_HOLY;
    }

    @Override
    public boolean supportsMending(Physical item) {
        return (item instanceof MOB)
            && ((Prayer_Bless.getSomething((MOB) item, true) != null)
            || (CMLib.flags().domainAffects(item, Ability.DOMAIN_CURSING).size() > 0));
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        if (affected == null)
            return;
        if (!(affected instanceof MOB))
            return;

        final int xlvl = super.getXLEVELLevel(invoker());
        affectableStats.setArmor(affectableStats.armor() - 10 - (4 * xlvl));
        affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + 10 + (2 * xlvl));
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();

        if (canBeUninvoked())
            if ((mob.location() != null) && (!mob.amDead()))
                mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> holy aura fades."));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), L(auto ? "<T-NAME> become(s) clothed in holiness." : "^S<S-NAME> " + prayForWord(mob) + " to clothe <T-NAMESELF> in holiness.^?") + CMLib.protocol().msp("bless.wav", 10));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                Item I = Prayer_Bless.getSomething(target, true);
                final HashSet<Item> alreadyDone = new HashSet<Item>();
                while ((I != null) && (!alreadyDone.contains(I))) {
                    alreadyDone.add(I);
                    final CMMsg msg2 = CMClass.getMsg(target, I, null, CMMsg.MASK_ALWAYS | CMMsg.MSG_DROP, L("<S-NAME> release(s) <T-NAME>."));
                    target.location().send(target, msg2);
                    Prayer_Bless.endLowerCurses(I, CMLib.ableMapper().lowestQualifyingLevel(ID()));
                    I.recoverPhyStats();
                    I = Prayer_Bless.getSomething(target, true);
                }
                Prayer_Bless.endAllOtherBlessings(mob, target, CMLib.ableMapper().lowestQualifyingLevel(ID()));
                Prayer_Bless.endLowerCurses(target, CMLib.ableMapper().lowestQualifyingLevel(ID()));
                beneficialAffect(mob, target, asLevel, 0);
                target.recoverPhyStats();
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> @x1 for holiness, but nothing happens.", prayWord(mob)));

        // return whether it worked
        return success;
    }
}
