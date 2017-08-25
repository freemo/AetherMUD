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
package com.syncleus.aethermud.game.Abilities.Druid;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Chant_Unbreakable extends Chant {
    private final static String localizedName = CMLib.lang().L("Unbreakable");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Unbreakable)");
    protected int maintainCondition = 100;

    @Override
    public String ID() {
        return "Chant_Unbreakable";
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
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_PRESERVING;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    protected int canAffectCode() {
        return CAN_ITEMS;
    }

    @Override
    protected int canTargetCode() {
        return CAN_ITEMS;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        if (!(affected instanceof Item))
            return;
        if (maintainCondition > 0)
            ((Item) affected).setUsesRemaining(maintainCondition);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if (!(affected instanceof Item))
            return true;
        if (maintainCondition > 0)
            ((Item) affected).setUsesRemaining(maintainCondition);
        return true;
    }

    @Override
    public boolean okMessage(Environmental host, CMMsg msg) {
        if (!super.okMessage(host, msg))
            return false;
        if ((msg.target() == affected)
            && (CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS)
            || ((msg.tool() instanceof Ability) && (((Ability) msg.tool()).abstractQuality() == Ability.QUALITY_MALICIOUS)))) {
            msg.source().tell(L("@x1 is unbreakable!", affected.name()));
            return false;
        }

        return true;
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (canBeUninvoked()) {
            if (((affected != null) && (affected instanceof Item))
                && ((((Item) affected).owner() != null)
                && (((Item) affected).owner() instanceof MOB)))
                ((MOB) ((Item) affected).owner()).tell(L("The enchantment on @x1 fades.", ((Item) affected).name()));
        }
        super.unInvoke();
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Item target = getTarget(mob, mob.location(), givenTarget, commands, Wearable.FILTER_ANY);
        if (target == null)
            return false;

        if (target.fetchEffect(this.ID()) != null) {
            mob.tell(L("@x1 is already unbreakable.", target.name(mob)));
            return false;
        }
        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? L("<T-NAME> appear(s) unbreakable!") : L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (!target.subjectToWearAndTear())
                    maintainCondition = -1;
                else
                    maintainCondition = target.usesRemaining();

                beneficialAffect(mob, target, asLevel, 0);
                mob.location().show(mob, target, CMMsg.MSG_OK_VISUAL, L("<T-NAME> is unbreakable!"));
                target.recoverPhyStats();
                mob.recoverPhyStats();
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));
        // return whether it worked
        return success;
    }
}
