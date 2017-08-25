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
import com.planet_ink.game.Common.interfaces.CharStats;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;


public class Spell_LowerResists extends Spell {

    private final static String localizedName = CMLib.lang().L("Lower Resistance");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Lowered Resistances)");
    int amount = 0;

    @Override
    public String ID() {
        return "Spell_LowerResists";
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
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_ENCHANTMENT;
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        if (canBeUninvoked())
            mob.tell(L("Your cold weakness is now gone."));

        super.unInvoke();

    }

    @Override
    public void affectCharStats(MOB affectedMOB, CharStats affectedStats) {
        super.affectCharStats(affectedMOB, affectedStats);
        for (final int i : CharStats.CODES.SAVING_THROWS())
            affectedStats.setStat(i, affectedStats.getStat(i) - amount);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? L("A shimmering unresistable field appears around <T-NAMESELF>.") : L("^S<S-NAME> invoke(s) a shimmering unresistable field around <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                for (int a = 0; a < target.numEffects(); a++) // personal effects
                {
                    final Ability A = target.fetchEffect(a);
                    if ((!A.isAutoInvoked())
                        && (A.canBeUninvoked())
                        && ((A.classificationCode() & Ability.ALL_DOMAINS) == Ability.DOMAIN_ABJURATION)) {
                        final int x = target.numEffects();
                        A.unInvoke();
                        if (x > target.numEffects())
                            a--;
                    }
                }
                amount = ((mob.phyStats().level() + (2 * getXLEVELLevel(mob))) - target.phyStats().level()) * 5;
                if (amount < 5)
                    amount = 5;
                success = maliciousAffect(mob, target, asLevel, 0, -1) != null;
            }
        } else
            maliciousFizzle(mob, target, L("<S-NAME> attempt(s) to invoke lowered resistances on <T-NAMESELF>, but fail(s)."));

        return success;
    }
}