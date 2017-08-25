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
import com.syncleus.aethermud.game.Behaviors.interfaces.LegalBehavior;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.Law;
import com.syncleus.aethermud.game.Common.interfaces.LegalWarrant;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;
import java.util.Vector;


public class Prayer_Condemnation extends Prayer {
    private final static String localizedName = CMLib.lang().L("Condemnation");

    @Override
    public String ID() {
        return "Prayer_Condemnation";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_EVANGELISM;
    }

    @Override
    protected int canTargetCode() {
        return Ability.CAN_MOBS;
    }

    @Override
    protected int canAffectCode() {
        return 0;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_OTHERS;
    }

    @Override
    public long flags() {
        return Ability.FLAG_UNHOLY;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        LegalBehavior B = null;
        if (mob.location() != null)
            B = CMLib.law().getLegalBehavior(mob.location());

        final MOB target = getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;
        List<LegalWarrant> warrants = new Vector<LegalWarrant>();
        if (B != null)
            warrants = B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()), target);

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if ((success) && (warrants.size() > 0)) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> @x1 to condemn <T-NAMESELF>.^?", prayForWord(mob)));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (msg.value() <= 0)
                    for (int i = 0; i < warrants.size(); i++) {
                        final LegalWarrant W = warrants.get(i);
                        if (W.punishment() < Law.PUNISHMENT_HIGHEST)
                            W.setPunishment(W.punishment() + 1);
                    }
            }

        } else
            beneficialWordsFizzle(mob, target, L("<S-NAME> @x1 to condemn <T-NAMESELF>, but nothing happens.", prayForWord(mob)));

        // return whether it worked
        return success;
    }
}
