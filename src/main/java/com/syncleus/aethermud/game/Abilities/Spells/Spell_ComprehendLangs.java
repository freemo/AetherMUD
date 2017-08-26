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
import com.syncleus.aethermud.game.core.CMStrings;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Spell_ComprehendLangs extends Spell {

    private final static String localizedName = CMLib.lang().L("Comprehend Languages");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Comprehend Languages)");

    @Override
    public String ID() {
        return "Spell_ComprehendLangs";
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
        return Ability.QUALITY_BENEFICIAL_SELF;
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_DIVINATION;
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        super.unInvoke();

        if (canBeUninvoked())
            mob.tell(L("You no longer feel so comprehensive."));
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        super.executeMsg(myHost, msg);
        if ((affected instanceof MOB)
            && (!msg.amISource((MOB) affected))
            && (msg.tool() instanceof Ability)) {
            if ((msg.tool().ID().equals("Fighter_SmokeSignals"))
                && (msg.sourceMinor() == CMMsg.NO_EFFECT)
                && (msg.targetMinor() == CMMsg.NO_EFFECT)
                && (msg.othersMessage() != null))
                msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, CMMsg.MSG_OK_VISUAL, L("The smoke signals seem to say '@x1'.", msg.othersMessage()), CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null));
            else if (((msg.sourceMinor() == CMMsg.TYP_SPEAK)
                || (msg.sourceMinor() == CMMsg.TYP_TELL)
                || (CMath.bset(msg.sourceMajor(), CMMsg.MASK_CHANNEL)))
                && (msg.sourceMessage() != null)
                && ((((Ability) msg.tool()).classificationCode() & Ability.ALL_ACODES) == Ability.ACODE_LANGUAGE)
                && (((MOB) affected).fetchEffect(msg.tool().ID()) == null)
                && (msg.source().charStats().getMyRace().racialAbilities(msg.source()).find(msg.tool().ID()) == null)) {
                final String str = CMStrings.getSayFromMessage(msg.sourceMessage());
                if (str != null) {
                    if (CMath.bset(msg.sourceMajor(), CMMsg.MASK_CHANNEL))
                        msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, msg.othersCode(), L("@x1 (translated from @x2)", CMStrings.substituteSayInMessage(msg.othersMessage(), str), msg.tool().name())));
                    else if (msg.amITarget(affected) && (msg.targetMessage() != null))
                        msg.addTrailerMsg(CMClass.getMsg(msg.source(), affected, null, CMMsg.NO_EFFECT, msg.targetCode(), CMMsg.NO_EFFECT, L("@x1 (translated from @x2)", CMStrings.substituteSayInMessage(msg.targetMessage(), str), msg.tool().name())));
                    else if ((msg.othersMessage() != null) && (msg.othersMessage().indexOf('\'') > 0)) {
                        String otherMes = msg.othersMessage();
                        if (msg.target() != null)
                            otherMes = CMLib.aetherFilter().fullOutFilter(((MOB) affected).session(), (MOB) affected, msg.source(), msg.target(), msg.tool(), otherMes, false);
                        msg.addTrailerMsg(CMClass.getMsg(msg.source(), affected, null, CMMsg.NO_EFFECT, msg.othersCode(), CMMsg.NO_EFFECT, L("@x1 (translated from @x2)", CMStrings.substituteSayInMessage(otherMes, str), msg.tool().name())));
                    }
                }
            }
        }
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            if (target instanceof MOB) {
                if (((MOB) target).isInCombat())
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        MOB target = mob;
        if ((auto) && (givenTarget != null) && (givenTarget instanceof MOB))
            target = (MOB) givenTarget;
        if (target.fetchEffect(ID()) != null) {
            mob.tell(target, null, null, L("<S-NAME> already <S-HAS-HAVE> comprehension."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? L("<T-NAME> feel(s) more comprehending.") : L("^S<S-NAME> invoke(s) the power of comprehension!^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                beneficialAffect(mob, target, asLevel, 0);
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably."));

        // return whether it worked
        return success;
    }
}
