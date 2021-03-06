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
import com.syncleus.aethermud.game.Races.interfaces.Race;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Chant_Sunray extends Chant {
    private final static String localizedName = CMLib.lang().L("Sunray");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Sunray)");

    @Override
    public String ID() {
        return "Chant_Sunray";
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
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_WEATHER_MASTERY;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        if (affected == null)
            return;
        if (!(affected instanceof MOB))
            return;

        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_NOT_SEE);
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();

        if (canBeUninvoked())
            mob.tell(L("Your vision returns."));
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            if (!mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if (target instanceof MOB) {
                if (((MOB) target).charStats().getBodyPart(Race.BODY_EYE) == 0)
                    return Ability.QUALITY_INDIFFERENT;
                if (!CMLib.flags().canSee((MOB) target))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        if ((!auto) && (target.charStats().getBodyPart(Race.BODY_EYE) == 0)) {
            mob.tell(L("@x1 has no eyes, and would not be affected.", target.name(mob)));
            return false;
        }

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto) | CMMsg.MASK_MALICIOUS, auto ? "" : L("^S<S-NAME> chant(s) to <T-NAMESELF>. A beam of bright sunlight flashes into <T-HIS-HER> eyes!^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (msg.value() <= 0) {
                    mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> go(es) blind!"));
                    maliciousAffect(mob, target, asLevel, 0, -1);
                }
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));

        // return whether it worked
        return success;
    }
}
