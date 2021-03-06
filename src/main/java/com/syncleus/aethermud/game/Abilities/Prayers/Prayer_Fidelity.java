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
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.Social;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Prayer_Fidelity extends Prayer {
    private final static String localizedName = CMLib.lang().L("Fidelity");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Fidelity)");

    @Override
    public String ID() {
        return "Prayer_Fidelity";
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
        return Ability.ACODE_PRAYER | Ability.DOMAIN_WARDING;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_OTHERS;
    }

    @Override
    public long flags() {
        return Ability.FLAG_HOLY;
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();

        if (canBeUninvoked())
            mob.tell(L("Your magical fidelity subsides."));
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!super.okMessage(myHost, msg))
            return false;
        if (!(affected instanceof MOB))
            return true;

        final MOB myChar = (MOB) affected;
        if (msg.target() instanceof MOB) {
            final MOB mate = (MOB) msg.target();
            if ((msg.amISource(myChar))
                && (!myChar.getLiegeID().equals(mate.Name()))
                && (myChar.isMarriedToLiege())
                && (msg.tool() instanceof Social)
                && (msg.tool().Name().equals("MATE <T-NAME>")
                || msg.tool().Name().equals("SEX <T-NAME>"))) {
                myChar.tell(L("You fidelity geas prevents you from doing that."));
                return false;
            }
        }
        return true;
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
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> @x1 for <T-NAMESELF>.^?", prayWord(mob)));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> gain(s) the fidelity geas!"));
                beneficialAffect(mob, target, asLevel, Ability.TICKS_ALMOST_FOREVER);
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> @x1 for <T-NAMESELF>, but the magic fades.", prayWord(mob)));

        // return whether it worked
        return success;
    }
}
