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
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Spell_Sleep extends Spell {

    private final static String localizedName = CMLib.lang().L("Sleep");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Sleep spell)");

    @Override
    public String ID() {
        return "Spell_Sleep";
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
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!(affected instanceof MOB))
            return true;

        final MOB mob = (MOB) affected;

        // when this spell is on a MOBs Affected list,
        // it should consistantly prevent the mob
        // from trying to do ANYTHING except sleep
        if ((msg.amISource(mob))
            && (!msg.sourceMajor(CMMsg.MASK_ALWAYS))
            && (msg.sourceMajor() > 0)) {
            mob.tell(L("You are way too drowsy."));
            return false;
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        // when this spell is on a MOBs Affected list,
        // it should consistantly put the mob into
        // a sleeping state, so that nothing they do
        // can get them out of it.
        affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_SLEEPING);
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();
        if (canBeUninvoked()) {
            if ((!mob.amDead()) && (mob.location() != null))
                mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> do(es)n't seem so drowsy any more."));
            CMLib.commands().postStand(mob, true);
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
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        // sleep has a 3 level difference for PCs, so check for this.
        int levelDiff = target.phyStats().level() - (mob.phyStats().level() + (2 * getXLEVELLevel(mob)));
        if (levelDiff < 0)
            levelDiff = 0;
        if (levelDiff > 2)
            levelDiff = 2;

        if ((!auto) && target.isInCombat()) {
            mob.tell(L("@x1 is in combat, and would not be affected.", target.name(mob)));
            return false;
        }

        // if they can't hear the sleep spell, it
        // won't happen
        if ((!auto) && (!CMLib.flags().canBeHeardSpeakingBy(mob, target))) {
            mob.tell(L("@x1 can't hear your words.", target.charStats().HeShe()));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, -((target.charStats().getStat(CharStats.STAT_INTELLIGENCE) * 2)), auto);

        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> whisper(s) to <T-NAMESELF>.^?"));
            final MOB oldVictim = mob.getVictim();
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (msg.value() <= 0) {
                    success = maliciousAffect(mob, target, asLevel, 3 - levelDiff, CMMsg.MSK_CAST_MALICIOUS_VERBAL | CMMsg.TYP_MIND | (auto ? CMMsg.MASK_ALWAYS : 0)) != null;
                    if (success)
                        if (target.location() == mob.location())
                            target.location().show(target, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> fall(s) asleep!!"));
                }
                if (oldVictim == null)
                    mob.setVictim(null);
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> whisper(s) to <T-NAMESELF>, but the spell fades."));

        // return whether it worked
        return success;
    }
}
