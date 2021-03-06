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
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.Enumeration;
import java.util.List;


public class Chant_AnimalCompanion extends Chant {
    private final static String localizedName = CMLib.lang().L("Animal Companion");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Animal Companion)");

    @Override
    public String ID() {
        return "Chant_AnimalCompanion";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_OTHERS;
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
        return Ability.CAN_MOBS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
    }

    @Override
    protected int overrideMana() {
        return Ability.COST_PCT + 10;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        MOB target = getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;
        if (!CMLib.flags().isAnimalIntelligence(target)) {
            mob.tell(L("This chant only works on animals."));
            return false;
        }
        if (target.amFollowing() != mob) {
            mob.tell(L("This chant only works on animal followers."));
            return false;
        }

        if (mob.isInCombat()) {
            mob.tell(L("Not while you are fighting!"));
            return false;
        }

        if (target.isInCombat()) {
            mob.tell(target, null, null, L("Not while <S-NAME> <S-IS-ARE> fighting!"));
            return false;
        }

        if (CMLib.flags().isSleeping(target) || (!CMLib.flags().canBeHeardSpeakingBy(mob, target)) || (!target.isMonster())) {
            mob.tell(target, null, null, L("<S-NAME> cannot make the oath with you right now!"));
            return false;
        }

        int numLoyal = 0;
        for (int f = 0; f < mob.numFollowers(); f++) {
            final MOB M = mob.fetchFollower(f);
            if ((M != mob)
                && (CMLib.flags().isAnimalIntelligence(M))
                && (M.fetchEffect("Loyalty") != null))
                numLoyal++;
        }
        if (numLoyal > this.getXLEVELLevel(mob)) {
            mob.tell(L("You lack the expertise to swear another oath."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> chant(s) an oath to <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final MOB targetCopy = (MOB) target.copyOf();
                for (Enumeration<Ability> a = target.effects(); a.hasMoreElements(); ) {
                    Ability A = a.nextElement();
                    if ((A != null) && ((A.flags() & Ability.FLAG_CHARMING) != 0) && (A.canBeUninvoked())) {
                        A.unInvoke();
                        mob.makePeace(true);
                        target.makePeace(true);
                        if (target.amFollowing() != mob)
                            target.setFollowing(mob);
                    }
                }
                if (target.amDestroyed() || target.amDead()) {
                    target = targetCopy;
                    target.basePhyStats().setRejuv(PhyStats.NO_REJUV);
                    target.phyStats().setRejuv(PhyStats.NO_REJUV);
                    target.text();
                    target.bringToLife(mob.location(), false);
                } else if (target.location() != mob.location()) {
                    mob.location().bringMobHere(target, true);
                    targetCopy.destroy();
                } else
                    targetCopy.destroy();
                mob.makePeace(true);
                target.makePeace(true);
                if ((target.basePhyStats().rejuv() > 0)
                    && (target.basePhyStats().rejuv() != PhyStats.NO_REJUV)
                    && (target.getStartRoom() != null)) {
                    final MOB oldTarget = target;
                    target = (MOB) target.copyOf();
                    target.basePhyStats().setRejuv(PhyStats.NO_REJUV);
                    target.phyStats().setRejuv(PhyStats.NO_REJUV);
                    target.text();
                    oldTarget.killMeDead(false);
                    target.bringToLife(mob.location(), false);
                }
                if (target.amFollowing() != mob)
                    target.setFollowing(mob);
                Ability A = target.fetchEffect("Loyalty");
                if (A == null) {
                    A = CMClass.getAbility("Loyalty");
                    A.setMiscText("NAME=" + mob.Name());
                    A.setSavable(true);
                    target.addNonUninvokableEffect(A);
                    mob.tell(mob, target, null, L("<T-NAME> is now loyal to you."));
                    mob.location().recoverRoomStats();
                }
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> chant(s) an oath to <T-NAMESELF>, but nothing happens."));

        // return whether it worked
        return success;
    }
}
