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
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.*;
import com.syncleus.aethermud.game.core.collections.XVector;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


@SuppressWarnings({"unchecked", "rawtypes"})
public class Spell_AweOther extends Spell {

    private final static String localizedName = CMLib.lang().L("Awe Other");

    @Override
    public String ID() {
        return "Spell_AweOther";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return L("(Awe of " + text() + ")");
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_ENCHANTMENT;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (((msg.targetMajor() & CMMsg.MASK_MALICIOUS) > 0)
            && (!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS))
            && (msg.target() != null)
            && (msg.target().Name().equalsIgnoreCase(text()))) {
            final MOB target = (MOB) msg.target();
            if ((!target.isInCombat())
                && (msg.source().getVictim() != target)
                && (msg.source().location() == target.location())) {
                msg.source().tell(L("You are too much in awe of @x1", target.name(msg.source())));
                if (target.getVictim() == msg.source()) {
                    target.makePeace(true);
                    target.setVictim(null);
                }
                return false;
            }
        }
        return super.okMessage(myHost, msg);
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
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        super.unInvoke();

        if (canBeUninvoked())
            if ((mob.location() != null) && (!mob.amDead()))
                mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> seem(s) less in awe of @x1.", text()));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (commands.size() < 2) {
            mob.tell(L("Invoke awe on whom and of whom?"));
            return false;
        }
        final String aweWhom = CMParms.combine(commands, 1);
        final MOB target = getTarget(mob, new XVector(commands.get(0)), givenTarget);
        if (target == null)
            return false;
        Room R = CMLib.map().roomLocation(target);
        if (R == null)
            R = mob.location();

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> invoke(s) a spell on <T-NAMESELF>.^?"));
            if (R.okMessage(mob, msg)) {
                R.send(mob, msg);
                if (maliciousAffect(mob, target, asLevel, 0, -1) != null) {
                    final Ability A = target.fetchEffect(ID());
                    if (A != null) {
                        A.setMiscText(CMStrings.capitalizeAndLower(aweWhom));
                        R.show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> gain(s) a new awe of @x1!", CMStrings.capitalizeAndLower(aweWhom)));
                    }
                }
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> attempt(s) to invoke a spell on <T-NAMESELF>, but fail(s) miserably."));

        // return whether it worked
        return success;
    }
}
