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
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Prayer_LightHammer extends Prayer {
    private final static String localizedName = CMLib.lang().L("Hammer of Light");

    @Override
    public String ID() {
        return "Prayer_LightHammer";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_VEXING;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public long flags() {
        return Ability.FLAG_HOLY;
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            if (target instanceof MOB) {
                if (!CMLib.flags().isEvil((target)))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;
        final boolean undead = CMLib.flags().isUndead(target);

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if ((success) && (CMLib.flags().isEvil(target))) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto) | CMMsg.MASK_MALICIOUS, L(auto ? "<T-NAME> is filled with holy light!" : "^S<S-NAME> " + prayWord(mob) + " for a hammer of light to strike <T-NAMESELF>!^?") + CMLib.protocol().msp("spelldam1.wav", 40));
            final CMMsg msg2 = CMClass.getMsg(mob, target, this, CMMsg.MSK_CAST_MALICIOUS_VERBAL | CMMsg.TYP_JUSTICE | (auto ? CMMsg.MASK_ALWAYS : 0), null);
            final Room R = target.location();
            if ((R.okMessage(mob, msg)) && ((R.okMessage(mob, msg2)))) {
                R.send(mob, msg);
                R.send(mob, msg2);
                int harming = CMLib.dice().roll(2, adjustedLevel(mob, asLevel) + 5, adjustedLevel(mob, asLevel));
                if ((msg.value() > 0) || (msg2.value() > 0))
                    harming = (int) Math.round(CMath.div(harming, 2.0));
                if (undead)
                    harming = harming * 2;
                if (CMLib.flags().isEvil(target))
                    CMLib.combat().postDamage(mob, target, this, harming, CMMsg.MASK_ALWAYS | CMMsg.TYP_JUSTICE, Weapon.TYPE_BURSTING, L("^SA hammer of holy light <DAMAGE> <T-NAME>!^?"));
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> @x1, but nothing happens.", prayWord(mob)));

        // return whether it worked
        return success;
    }
}
