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
package com.syncleus.aethermud.game.Abilities.Druid;

import com.syncleus.aethermud.game.Abilities.StdAbility;
import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;
import java.util.Vector;


public class Druid_RecoverVoice extends StdAbility {
    private final static String localizedName = CMLib.lang().L("Recover Voice");
    private static final String[] triggerStrings = I(new String[]{"VRECOVER", "RECOVERVOICE"});

    @Override
    public String ID() {
        return "Druid_RecoverVoice";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_SELF;
    }

    @Override
    protected int canAffectCode() {
        return 0;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_FITNESS;
    }

    public List<Ability> returnOffensiveAffects(MOB caster, Physical fromMe) {
        final MOB newMOB = CMClass.getFactoryMOB();
        final Vector<Ability> offenders = new Vector<Ability>(1);

        for (int a = 0; a < fromMe.numEffects(); a++) // personal
        {
            final Ability A = fromMe.fetchEffect(a);
            if (A != null) {
                newMOB.recoverPhyStats();
                A.affectPhyStats(newMOB, newMOB.phyStats());
                if ((!CMLib.flags().canSpeak(newMOB))
                    && ((A.invoker() == null)
                    || ((A.invoker() != null)
                    && (A.invoker().phyStats().level() <= (caster.phyStats().level() + 10 + (2 * getXLEVELLevel(caster)))))))
                    offenders.addElement(A);
            }
        }
        newMOB.destroy();
        return offenders;
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            if (target instanceof MOB) {
                if (returnOffensiveAffects(mob, (target)).size() == 0)
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;
        final boolean success = proficiencyCheck(mob, 0, auto);

        final List<Ability> offensiveAffects = returnOffensiveAffects(mob, mob);
        if ((!success) || (offensiveAffects.size() == 0))
            mob.tell(L("You failed in your vocal meditation."));
        else {
            final CMMsg msg = CMClass.getMsg(mob, null, null, CMMsg.TYP_GENERAL | CMMsg.MASK_ALWAYS | CMMsg.MASK_MAGIC, null);
            if (mob.location().okMessage(mob, msg)) {
                for (int a = offensiveAffects.size() - 1; a >= 0; a--)
                    offensiveAffects.get(a).unInvoke();
            }
        }
        return success;
    }
}

