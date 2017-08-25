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
import com.planet_ink.game.Abilities.interfaces.MendingSkill;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Items.interfaces.Coins;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.Wearable;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;


public class Prayer_Bless extends Prayer implements MendingSkill {
    private final static String localizedName = CMLib.lang().L("Bless");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Blessed)");

    public static Item getSomething(MOB mob, boolean cursedOnly) {
        final Vector<Item> good = new Vector<Item>();
        final Vector<Item> great = new Vector<Item>();
        Item target = null;
        for (int i = 0; i < mob.numItems(); i++) {
            final Item I = mob.getItem(i);
            if ((I.container() == null) && ((!cursedOnly) || (isCursed(I))))
                if (I.amWearingAt(Wearable.IN_INVENTORY))
                    good.addElement(I);
                else
                    great.addElement(I);
        }
        if (great.size() > 0)
            target = great.elementAt(CMLib.dice().roll(1, great.size(), -1));
        else if (good.size() > 0)
            target = good.elementAt(CMLib.dice().roll(1, good.size(), -1));
        return target;
    }

    public static void endAllOtherBlessings(MOB from, Physical target, int level) {
        final List<Ability> V = CMLib.flags().domainAffects(target, Ability.DOMAIN_BLESSING);
        for (int v = 0; v < V.size(); v++) {
            final Ability A = V.get(v);
            if ((CMLib.ableMapper().lowestQualifyingLevel(A.ID()) < level)
                || (from == A.invoker())
                || (target == from)
                || (target == A.invoker()))
                A.unInvoke();
        }
    }

    public static void endLowerBlessings(Physical target, int level) {
        final List<Ability> V = CMLib.flags().domainAffects(target, Ability.DOMAIN_BLESSING);
        for (int v = 0; v < V.size(); v++) {
            final Ability A = V.get(v);
            if (CMLib.ableMapper().lowestQualifyingLevel(A.ID()) < level)
                A.unInvoke();
        }
    }

    public static void endLowerCurses(Physical target, int level) {
        final List<Ability> V = CMLib.flags().domainAffects(target, Ability.DOMAIN_CURSING);
        for (int v = 0; v < V.size(); v++) {
            final Ability A = V.get(v);
            if (CMLib.ableMapper().lowestQualifyingLevel(A.ID()) <= level)
                A.unInvoke();
        }
    }

    public static boolean isCursed(Item item) {
        if (CMLib.flags().isSeeable(item)) {
            if (!CMLib.flags().isRemovable(item))
                return true;
            if (!CMLib.flags().isDroppable(item))
                return true;
        }
        return CMLib.flags().domainAffects(item, Ability.DOMAIN_CURSING).size() > 0;
    }

    @Override
    public String ID() {
        return "Prayer_Bless";
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
        return Ability.CAN_MOBS | Ability.CAN_ITEMS;
    }

    @Override
    protected int canTargetCode() {
        return Ability.CAN_MOBS | Ability.CAN_ITEMS;
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
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        if (affected == null)
            return;
        affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_GOOD);
        affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_BONUS);
        if (affected instanceof MOB)
            affectableStats.setArmor(affectableStats.armor() - 5 - (2 * getXLEVELLevel(invoker())));
        else if (affected instanceof Item)
            affectableStats.setAbility(affectableStats.ability() + 1);
    }

    @Override
    public void unInvoke() {

        // undo the affects of this spell
        if (!(affected instanceof MOB)) {
            if (canBeUninvoked())
                if ((affected instanceof Item) && (((Item) affected).owner() != null) && (((Item) affected).owner() instanceof MOB))
                    ((MOB) ((Item) affected).owner()).tell(L("The blessing on @x1 fades.", ((Item) affected).name()));
            super.unInvoke();
            return;
        }
        final MOB mob = (MOB) affected;
        if (canBeUninvoked())
            mob.tell(L("Your aura of blessing fades."));
        super.unInvoke();
    }

    @Override
    public boolean supportsMending(Physical item) {
        return (item instanceof MOB)
            && ((Prayer_Bless.getSomething((MOB) item, true) != null)
            || (CMLib.flags().domainAffects(item, Ability.DOMAIN_CURSING).size() > 0));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (target instanceof Coins) {
            mob.tell(L("You can not bless that."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), L(auto ? "<T-NAME> appear(s) blessed!" : "^S<S-NAME> bless(es) <T-NAMESELF>" + inTheNameOf(mob) + ".^?") + CMLib.protocol().msp("bless.wav", 10));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                Item I = getSomething(target, true);
                final HashSet<Item> alreadyDone = new HashSet<Item>();
                while ((I != null) && (!alreadyDone.contains(I))) {
                    alreadyDone.add(I);
                    final CMMsg msg2 = CMClass.getMsg(target, I, null, CMMsg.MASK_ALWAYS | CMMsg.MSG_DROP, L("<S-NAME> release(s) <T-NAME>."));
                    target.location().send(target, msg2);
                    endLowerCurses(I, CMLib.ableMapper().lowestQualifyingLevel(ID()));
                    I.recoverPhyStats();
                    I = getSomething(target, true);
                }
                Prayer_Bless.endAllOtherBlessings(mob, target, CMLib.ableMapper().lowestQualifyingLevel(ID()));
                endLowerCurses(target, CMLib.ableMapper().lowestQualifyingLevel(ID()));
                beneficialAffect(mob, target, asLevel, 0);
                target.recoverPhyStats();
                target.location().recoverRoomStats();
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> @x1 for blessings, but nothing happens.", prayWord(mob)));
        // return whether it worked
        return success;
    }
}