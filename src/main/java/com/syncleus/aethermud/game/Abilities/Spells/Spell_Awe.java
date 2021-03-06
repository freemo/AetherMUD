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
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Spell_Awe extends Spell {

    private final static String localizedName = CMLib.lang().L("Awe");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Awe spell)");

    @Override
    public String ID() {
        return "Spell_Awe";
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
        return CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_SELF;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_ENCHANTMENT;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (((msg.targetMajor() & CMMsg.MASK_MALICIOUS) > 0)
            && (!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS))
            && ((msg.amITarget(affected)))) {
            final MOB target = (MOB) msg.target();
            if ((!target.isInCombat())
                && (msg.source().getVictim() != target)
                && (msg.source().location() == target.location())
                && (CMLib.dice().rollPercentage() > ((msg.source().phyStats().level() - (target.phyStats().level() + (2 * getXLEVELLevel(invoker())))) * 10))) {
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
                mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> seem(s) less awesome."));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
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
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> invoke(s) a spell.^?"));
            if (R.okMessage(mob, msg)) {
                R.send(mob, msg);
                R.show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> seem(s) awesome!"));
                beneficialAffect(mob, target, asLevel, 0);
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably."));

        // return whether it worked
        return success;
    }
}
