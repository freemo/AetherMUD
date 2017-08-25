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
package com.planet_ink.game.Abilities.Spells;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Items.interfaces.Weapon;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMath;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;
import java.util.Set;


public class Spell_ForkedLightning extends Spell {

    private final static String localizedName = CMLib.lang().L("Forked Lightning");

    @Override
    public String ID() {
        return "Spell_ForkedLightning";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int maxRange() {
        return adjustedMaxInvokerRange(2);
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_EVOCATION;
    }

    @Override
    public long flags() {
        return Ability.FLAG_AIRBASED;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Set<MOB> h = properTargets(mob, givenTarget, auto);
        if (h == null) {
            mob.tell(L("There doesn't appear to be anyone here worth electrocuting."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {

            if (mob.location().show(mob, null, this, verbalCastCode(mob, null, auto), L(auto ? "A thunderous crack of lightning erupts!" : "^S<S-NAME> invoke(s) a thunderous crack of forked lightning.^?") + CMLib.protocol().msp("lightning.wav", 40))) {
                for (final Object element : h) {
                    final MOB target = (MOB) element;

                    final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), null);
                    final CMMsg msg2 = CMClass.getMsg(mob, target, this, CMMsg.MSK_CAST_MALICIOUS_VERBAL | CMMsg.TYP_ELECTRIC | (auto ? CMMsg.MASK_ALWAYS : 0), null);
                    if ((mob.location().okMessage(mob, msg)) && ((mob.location().okMessage(mob, msg2)))) {
                        mob.location().send(mob, msg);
                        mob.location().send(mob, msg2);
                        invoker = mob;

                        final int maxDie = (int) Math.round(CMath.div(adjustedLevel(mob, asLevel) + (2 * super.getX1Level(mob)), 2.0));
                        int damage = CMLib.dice().roll(maxDie, 7, 1);
                        if ((msg.value() > 0) || (msg2.value() > 0))
                            damage = (int) Math.round(CMath.div(damage, 2.0));
                        if (target.location() == mob.location())
                            CMLib.combat().postDamage(mob, target, this, damage, CMMsg.MASK_ALWAYS | CMMsg.TYP_ELECTRIC, Weapon.TYPE_STRIKING, L("A bolt <DAMAGE> <T-NAME>!"));
                    }
                }
            }
        } else
            return maliciousFizzle(mob, null, L("<S-NAME> attempt(s) to invoke a ferocious spell, but the spell fizzles."));

        // return whether it worked
        return success;
    }
}