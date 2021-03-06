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
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Spell_ManaBurn extends Spell {

    private final static String localizedName = CMLib.lang().L("Mana Burn");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Mana Burn)");
    int curMana = 0;

    @Override
    public String ID() {
        return "Spell_ManaBurn";
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
        adjustMana();
        return super.okMessage(myHost, msg);
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        adjustMana();
        super.executeMsg(myHost, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        adjustMana();
        return super.tick(ticking, tickID);
    }

    public void adjustMana() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        if (canBeUninvoked()) {
            if (mob.curState().getMana() < curMana)
                mob.curState().adjMana(mob.curState().getMana() - curMana, mob.maxState());
            curMana = mob.curState().getMana();
        }

    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();

        mob.tell(L("You feel less drained."));
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            if (target instanceof MOB) {
                if (((MOB) target).curState().getMana() < ((MOB) target).maxState().getMana() / 2)
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

        int levelDiff = target.phyStats().level() - (mob.phyStats().level() + (2 * getXLEVELLevel(mob)));
        if (levelDiff < 0)
            levelDiff = 0;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        // now see if it worked
        boolean success = proficiencyCheck(mob, -((target.charStats().getStat(CharStats.STAT_INTELLIGENCE)) + (levelDiff * 5)), auto);
        if (success) {
            final String str = auto ? "" : L("^S<S-NAME> incant(s) hotly at <T-NAMESELF>^?");
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), str);
            final CMMsg msg2 = CMClass.getMsg(mob, target, this, CMMsg.MSK_CAST_MALICIOUS_VERBAL | CMMsg.TYP_MIND | (auto ? CMMsg.MASK_ALWAYS : 0), null);
            if ((mob.location().okMessage(mob, msg)) && (mob.location().okMessage(mob, msg2))) {
                mob.location().send(mob, msg);
                mob.location().send(mob, msg2);
                if ((msg.value() <= 0) && (msg2.value() <= 0)) {
                    target.curState().adjMana(-50, target.maxState());
                    curMana = target.curState().getMana();
                    success = maliciousAffect(mob, target, asLevel, -levelDiff, -1) != null;
                    if (success)
                        mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> seem(s) drained!"));
                }
            }
        }
        if (!success)
            return maliciousFizzle(mob, target, L("<S-NAME> incant(s) hotly at <T-NAMESELF>, but nothing happens."));

        // return whether it worked
        return success;
    }
}
