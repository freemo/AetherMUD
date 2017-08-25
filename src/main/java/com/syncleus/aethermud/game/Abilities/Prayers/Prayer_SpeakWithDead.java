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
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Items.interfaces.DeadBody;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.Wearable;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;


public class Prayer_SpeakWithDead extends Prayer {
    private final static String localizedName = CMLib.lang().L("Speak with Dead");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Being Spoken To)");

    @Override
    public String ID() {
        return "Prayer_SpeakWithDead";
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
        return 0;
    }

    @Override
    protected int canTargetCode() {
        return Ability.CAN_ITEMS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_COMMUNING;
    }

    @Override
    public long flags() {
        return Ability.FLAG_HOLY;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Item target = super.getTarget(mob, mob.location(), givenTarget, commands, Wearable.FILTER_UNWORNONLY);
        if (target == null)
            return false;

        if (!(target instanceof DeadBody)) {
            mob.tell(L("You can not speak with @x1.", target.name()));
            return false;
        }

        if (!CMLib.flags().canHear(mob)) {
            mob.tell(L("You can't hear!"));
            return false;
        }

        if ((((DeadBody) target).getSavedMOB() != null) && (CMLib.flags().isAnimalIntelligence(((DeadBody) target).getSavedMOB()))) {
            mob.tell(L("That poor creature, @x1 was never able to speak.", target.name()));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), L("^S<S-NAME> kneel(s) down and speak(s) with <T-NAMESELF>" + inTheNameOf(mob) + ".^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                beneficialAffect(mob, target, asLevel, 0);
                DeadBody body = (DeadBody) target;
                mob.location().showOthers(mob, target, CMMsg.MSG_OK_ACTION, L("<T-NAME> whisper(s) to <S-NAME>."));
                StringBuilder knowledge = new StringBuilder("");
                knowledge.append(L("I was @x1, killed on @x2 by @x3 with @x4.",
                    body.getMobName(),
                    mob.location().getArea().getTimeObj().deriveClock(body.getTimeOfDeath()).getShortTimeDescription(),
                    body.getKillerName(),
                    (body.getKillerTool() == null) ? L("a weapon") : body.getKillerTool().Name()));
                mob.tell(mob, target, null, L("<T-NAME> whisper(s) to you '@x1'.", knowledge.toString()));
                target.recoverPhyStats();
                mob.recoverPhyStats();
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> kneel(s) before <T-NAME> and @x1, but nothing happens.", prayWord(mob)));
        // return whether it worked
        return success;
    }
}