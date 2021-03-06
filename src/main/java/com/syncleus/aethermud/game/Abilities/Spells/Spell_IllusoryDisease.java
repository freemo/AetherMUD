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
import com.syncleus.aethermud.game.Abilities.interfaces.DiseaseAffect;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Spell_IllusoryDisease extends Spell implements DiseaseAffect {

    private final static String localizedName = CMLib.lang().L("Illusory Disease");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Diseased)");
    protected int diseaseTick = 5;

    @Override
    public String ID() {
        return "Spell_IllusoryDisease";
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
        return Ability.ACODE_SPELL | Ability.DOMAIN_ILLUSION;
    }

    @Override
    public int difficultyLevel() {
        return 9;
    }

    @Override
    public int spreadBitmap() {
        return 0;
    }

    @Override
    public boolean isMalicious() {
        return true;
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        super.affectCharStats(affected, affectableStats);
        affectableStats.setStat(CharStats.STAT_STRENGTH, (int) Math.round(CMath.div(affectableStats.getStat(CharStats.STAT_STRENGTH), 2.0)));
    }

    @Override
    public String getHealthConditionDesc() {
        return ""; // not really a condition
    }

    @Override
    public int abilityCode() {
        return 0;
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if ((affected == null) || (invoker == null))
            return false;

        final MOB mob = (MOB) affected;
        if ((--diseaseTick) <= 0) {
            diseaseTick = 5;
            String str = null;
            switch (CMLib.dice().roll(1, 5, 0)) {
                case 1:
                    str = L("<S-NAME> double(s) over and dry heave(s).");
                    break;
                case 2:
                    str = L("<S-NAME> sneeze(s). AAAAAAAAAAAAAACHOOO!!!!");
                    break;
                case 3:
                    str = L("<S-NAME> shake(s) feverishly.");
                    break;
                case 4:
                    str = L("<S-NAME> look(s) around weakly.");
                    break;
                case 5:
                    str = L("<S-NAME> cough(s) and shudder(s) feverishly.");
                    break;
            }
            if (str != null)
                mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, str);
            return true;
        }
        return true;
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        super.unInvoke();

        if (canBeUninvoked())
            mob.tell(L("You begin to feel better."));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> incant(s) at <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (msg.value() <= 0) {
                    mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> get(s) sick!"));
                    success = maliciousAffect(mob, target, asLevel, 0, -1) != null;
                }
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> incant(s) at <T-NAMESELF>, but the spell fizzles."));

        // return whether it worked
        return success;
    }
}
