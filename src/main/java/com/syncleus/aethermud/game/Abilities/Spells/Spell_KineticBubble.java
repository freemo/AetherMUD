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
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Spell_KineticBubble extends Spell {

    private final static String localizedName = CMLib.lang().L("Kinetic Bubble");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Kinetic Bubble)");
    protected int kickBack = 0;

    @Override
    public String ID() {
        return "Spell_KineticBubble";
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
        return Ability.ACODE_SPELL | Ability.DOMAIN_ABJURATION;
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        if (canBeUninvoked()) {
            if ((mob.location() != null) && (!mob.amDead()))
                mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> Kinetic Bubble pops."));
        }

        super.unInvoke();
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        super.executeMsg(myHost, msg);
        if (affected == null)
            return;
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        if (msg.target() == null)
            return;
        if (msg.source() == null)
            return;
        final MOB source = msg.source();
        if (source.location() == null)
            return;

        if (msg.amITarget(mob)) {
            if ((msg.targetMinor() == CMMsg.TYP_DAMAGE)
                && (mob.rangeToTarget() == 0)
                && (msg.source() != mob)
                && (msg.tool() instanceof Weapon)) {
                final CMMsg msg2 = CMClass.getMsg(mob, source, this, verbalCastCode(mob, source, true), null);
                if (source.location().okMessage(mob, msg2)) {
                    source.location().send(mob, msg2);
                    if (invoker == null)
                        invoker = source;
                    if ((msg2.value() <= 0) && (msg.value() > 3)) {
                        final int damage = CMLib.dice().roll(1, (getXLEVELLevel(mob) + msg.value()) / 3, 0);
                        CMLib.combat().postDamage(mob, source, this, damage, CMMsg.MASK_ALWAYS | CMMsg.TYP_CAST_SPELL, Weapon.TYPE_BURSTING, L("The bubble around <S-NAME> <DAMAGES> <T-NAME>!"));
                    }
                }
            }

        }
        return;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, somanticCastCode(mob, target, auto), auto ? L("<T-NAME> <T-IS-ARE> surrounded by a Kinetic Bubble!") : L("^S<S-NAME> invoke(s) a Kinetic Bubble around <T-NAMESELF>!^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                kickBack = 0;
                beneficialAffect(mob, target, asLevel, 0);
            }
        } else
            beneficialWordsFizzle(mob, target, L("<S-NAME> attempt(s) to invoke a Kinetic Bubble, but fail(s)."));

        return success;
    }
}
