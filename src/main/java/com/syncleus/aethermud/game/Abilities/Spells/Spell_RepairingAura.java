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
package com.planet_ink.game.Abilities.Spells;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.Wearable;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;
import com.planet_ink.game.core.interfaces.Tickable;

import java.util.List;
import java.util.Vector;


public class Spell_RepairingAura extends Spell {

    public static final int REPAIR_MAX = 30;
    private final static String localizedName = CMLib.lang().L("Repairing Aura");
    public int repairDown = REPAIR_MAX;
    public int adjustedLevel = 1;

    @Override
    public String ID() {
        return "Spell_RepairingAura";
    }

    @Override
    public String name() {
        return localizedName;
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
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_ABJURATION;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int overrideMana() {
        return 50;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_BONUS);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        repairDown -= adjustedLevel;
        if ((repairDown <= 0) && (affected instanceof Item)) {
            repairDown = REPAIR_MAX;
            final Item I = (Item) affected;
            if ((I.subjectToWearAndTear()) && (I.usesRemaining() < 100)) {
                if (I.owner() instanceof Room)
                    ((Room) I.owner()).showHappens(CMMsg.MSG_OK_VISUAL, I, L("<S-NAME> is magically repairing itself."));
                else if (I.owner() instanceof MOB)
                    ((MOB) I.owner()).tell(L("@x1 is magically repairing itself.", I.name()));
                I.setUsesRemaining(I.usesRemaining() + 1 + (super.getXLEVELLevel(invoker()) / 3));
            }
        }
        return true;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Physical target = getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_ANY);
        if (target == null)
            return false;
        if (target.fetchEffect(this.ID()) != null) {
            mob.tell(L("@x1 is already repairing!", target.name(mob)));
            return false;
        }
        if ((!(target instanceof Item)) && (!(target instanceof MOB))) {
            mob.tell(L("@x1 would not be affected by this spell.", target.name(mob)));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);
        Item realTarget = null;
        if (target instanceof Item)
            realTarget = (Item) target;
        else if (target instanceof MOB) {
            final Vector<Item> choices = new Vector<Item>();
            final Vector<Item> inventory = new Vector<Item>();
            final MOB M = (MOB) target;
            Item I = null;
            for (int i = 0; i < M.numItems(); i++) {
                I = M.getItem(i);
                if ((I != null) && (I.subjectToWearAndTear()) && (I.fetchEffect(ID()) == null)) {
                    if (I.amWearingAt(Wearable.IN_INVENTORY))
                        inventory.addElement(I);
                    else
                        choices.addElement(I);
                }
            }
            Vector<Item> chooseFrom = inventory;
            if (choices.size() < 3)
                inventory.addAll(choices);
            else
                chooseFrom = choices;
            if (chooseFrom.size() < 1)
                success = false;
            else
                realTarget = chooseFrom.elementAt(CMLib.dice().roll(1, chooseFrom.size(), -1));
        }

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, somanticCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?"));
            final CMMsg msg2 = (target == realTarget) ? null : CMClass.getMsg(mob, target, this, somanticCastCode(mob, target, auto), null);
            if (mob.location().okMessage(mob, msg)
                && (realTarget != null)
                && ((msg2 == null) || mob.location().okMessage(mob, msg2))) {
                mob.location().send(mob, msg);
                if (msg2 != null)
                    mob.location().send(mob, msg2);
                mob.location().show(mob, realTarget, CMMsg.MSG_OK_ACTION, L("<T-NAME> attain(s) a repairing aura."));
                beneficialAffect(mob, realTarget, asLevel, 0);
                final Spell_RepairingAura A = (Spell_RepairingAura) realTarget.fetchEffect(ID());
                if (A != null)
                    A.adjustedLevel = adjustedLevel(mob, asLevel);
                realTarget.recoverPhyStats();
                mob.recoverPhyStats();
                mob.location().recoverRoomStats();
            }
        } else
            beneficialVisualFizzle(mob, target, L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting, but nothing happens."));

        // return whether it worked
        return success;
    }
}