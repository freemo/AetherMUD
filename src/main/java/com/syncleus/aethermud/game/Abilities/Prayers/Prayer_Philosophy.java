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
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Prayer_Philosophy extends Prayer {
    private final static String localizedName = CMLib.lang().L("Philosophy");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Philosophy spell)");

    @Override
    public String ID() {
        return "Prayer_Philosophy";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public long flags() {
        return Ability.FLAG_HOLY;
    }

    @Override
    public String displayText() {
        return localizedStaticDisplay;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_COMMUNING;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_OTHERS;
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        super.affectCharStats(affected, affectableStats);
        int increase = 1;
        if (affectableStats.getCurrentClass().baseClass().equals("Fighter"))
            increase = 1;
        else if (affectableStats.getCurrentClass().baseClass().equals("Mage"))
            increase = 2;
        else if (affectableStats.getCurrentClass().baseClass().equals("Thief"))
            increase = 1;
        else if (affectableStats.getCurrentClass().baseClass().equals("Bard"))
            increase = 1;
        else if (affectableStats.getCurrentClass().baseClass().equals("Cleric"))
            increase = 3;
        else if (affectableStats.getCurrentClass().baseClass().equals("Druid"))
            increase = 3;
        increase += (super.getXLEVELLevel(invoker()) + 2) / 3;
        affectableStats.setStat(CharStats.STAT_WISDOM, affectableStats.getStat(CharStats.STAT_WISDOM) + increase);
    }

    @Override
    public void unInvoke() {
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();
        if (canBeUninvoked())
            mob.tell(L("You stop pondering life and the mysteries of the universe."));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        // now see if it worked
        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> give(s) <T-NAMESELF> something to think about.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> start(s) pondering the mysteries of the universe."));
                beneficialAffect(mob, target, asLevel, 0);
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> give(s) <T-NAMESELF> something to think about, but it just confuses <T-HIM-HER>."));

        // return whether it worked
        return success;
    }
}
