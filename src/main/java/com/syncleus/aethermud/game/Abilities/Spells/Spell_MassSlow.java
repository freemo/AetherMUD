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
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;
import java.util.Set;


public class Spell_MassSlow extends Spell {

    private final static String localizedName = CMLib.lang().L("Mass Slow");

    @Override
    public String ID() {
        return "Spell_MassSlow";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return "";
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    protected int canAffectCode() {
        return 0;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_ALTERATION;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Set<MOB> h = properTargets(mob, givenTarget, auto);
        if (h == null) {
            mob.tell(L("There doesn't appear to be anyone here worth slowing down."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, -20, auto);

        if (success) {
            if (mob.location().show(mob, null, this, somanticCastCode(mob, null, auto), auto ? "" : L("^S<S-NAME> whisper(s) and wave(s) <S-HIS-HER> arms.^?"))) {
                for (final Object element : h) {
                    final MOB target = (MOB) element;

                    // if they can't hear the slow spell, it
                    // won't happen
                    if (CMLib.flags().canBeHeardSpeakingBy(mob, target)) {
                        final MOB oldVictim = mob.getVictim();
                        final CMMsg msg = CMClass.getMsg(mob, target, this, somanticCastCode(mob, target, auto), null);
                        if ((mob.location().okMessage(mob, msg)) && (target.fetchEffect(this.ID()) == null)) {
                            mob.location().send(mob, msg);
                            if (msg.value() <= 0) {
                                final Spell_Slow spell = new Spell_Slow();
                                spell.setProficiency(proficiency());
                                success = spell.maliciousAffect(mob, target, asLevel, 2, CMMsg.MSK_CAST_MALICIOUS_SOMANTIC | CMMsg.TYP_MIND | (auto ? CMMsg.MASK_ALWAYS : 0)) != null;
                                if (success)
                                    target.location().show(target, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> move(s) more slowly!!"));
                            }
                        }
                        if (oldVictim == null)
                            mob.setVictim(null);
                    } else
                        maliciousFizzle(mob, target, L("<T-NAME> seem(s) unaffected by the Slow spell from <S-NAME>."));
                }
            }
        } else
            return maliciousFizzle(mob, null, L("<S-NAME> whisper(s) a spell slowly, but the spell fizzles."));

        // return whether it worked
        return success;
    }
}
