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
import com.syncleus.aethermud.game.Exits.interfaces.Exit;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Chant_UnderwaterAction extends Chant {
    private final static String localizedName = CMLib.lang().L("Underwater Action");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Underwater Action)");

    @Override
    public String ID() {
        return "Chant_UnderwaterAction";
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
        return Ability.ACODE_CHANT | Ability.DOMAIN_WATERCONTROL;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_OTHERS;
    }

    @Override
    public void unInvoke() {
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();
        if (canBeUninvoked())
            mob.tell(L("Your fluidic instincts disappear."));
    }

    @Override
    public boolean okMessage(Environmental myHost, CMMsg msg) {
        if (!super.okMessage(myHost, msg))
            return false;
        if ((msg.source() == affected)
            && ((msg.targetMinor() == CMMsg.TYP_ENTER) || (msg.targetMinor() == CMMsg.TYP_LEAVE))) {
            if (msg.target() instanceof Room) {
                Room R = (Room) msg.target();
                if (R == msg.source().location()) {
                    if (msg.tool() instanceof Exit) {
                        int dir = CMLib.map().getExitDir(R, (Exit) msg.tool());
                        if (dir >= 0) {
                            R = R.getRoomInDir(dir);
                        }
                    }
                    if (R == msg.source().location())
                        return true;
                }
                if (CMLib.flags().isWateryRoom(R))
                    msg.source().phyStats().setDisposition(msg.source().phyStats().disposition() | PhyStats.IS_SWIMMING);
            }
        }
        return true;
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            final Room R = mob.location();
            if (R != null) {
                if (CMLib.flags().isWateryRoom(R))
                    return Ability.QUALITY_BENEFICIAL_SELF;
                return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        if (affected instanceof MOB) {
            final MOB M = (MOB) affected;
            final Room R = M.location();
            if ((R != null) && (!CMLib.flags().isWaterWorthy(affected))) {
                if (CMLib.flags().isWateryRoom(R)) {
                    affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_SWIMMING);
                    if (!M.isInCombat())
                        affectableStats.setSpeed(affectableStats.speed() + 1.0);
                }
            }
        }
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> attain(s) fluidic movement!"));
                beneficialAffect(mob, target, asLevel, 0);
            }
        } else
            beneficialWordsFizzle(mob, target, L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));

        return success;
    }
}
