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
package com.syncleus.aethermud.game.Abilities.Spells;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Spell_ShrinkMouth extends Spell {

    private final static String localizedName = CMLib.lang().L("Shrink Mouth");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Shrunken Mouth)");

    @Override
    public String ID() {
        return "Spell_ShrinkMouth";
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
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return CAN_MOBS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_TRANSMUTATION;
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (canBeUninvoked()) {
            if (affected instanceof MOB) {
                final MOB mob = (MOB) affected;
                if ((mob.location() != null) && (!mob.amDead()))
                    mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> mouth returns to its normal size."));
            }
        }
        super.unInvoke();
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!super.okMessage(myHost, msg))
            return false;
        if ((msg.targetMinor() == CMMsg.TYP_EAT)
            && (affected instanceof MOB)
            && (msg.amISource((MOB) affected))) {
            msg.source().tell(L("Your mouth is too tiny to eat!"));
            return false;
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
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> cast(s) a puckering spell on <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final Ability A = target.fetchEffect("Spell_BigMouth");
                boolean isJustUnInvoking = false;
                if ((A != null) && (A.canBeUninvoked())) {
                    A.unInvoke();
                    isJustUnInvoking = true;
                }
                if ((!isJustUnInvoking) && (msg.value() <= 0)) {
                    beneficialAffect(mob, target, asLevel, 0);
                    if ((!auto) && (target.location() != null))
                        target.location().show(mob, target, CMMsg.MSG_OK_VISUAL, L("<T-YOUPOSS> mouth shrinks!"));
                    CMLib.utensils().confirmWearability(target);
                }
            }
        } else
            beneficialWordsFizzle(mob, target, L("<S-NAME> attempt(s) to cast a puckering spell, but fail(s)."));

        return success;
    }
}
