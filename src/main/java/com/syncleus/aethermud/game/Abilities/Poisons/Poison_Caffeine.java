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
package com.syncleus.aethermud.game.Abilities.Poisons;

import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;


public class Poison_Caffeine extends Poison {
    private final static String localizedName = CMLib.lang().L("Poison_Hyper");
    private final static String localizedStaticDisplay = CMLib.lang().L("(CAFFEINATED!!)");
    private static final String[] triggerStrings = I(new String[]{"POISONHYPER"});

    @Override
    public String ID() {
        return "Poison_Caffeine";
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
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    protected int POISON_TICKS() {
        return 30;
    } // 0 means no adjustment!

    @Override
    protected int POISON_DELAY() {
        return 5;
    }

    @Override
    protected String POISON_DONE() {
        return "The caffeine runs its course.";
    }

    @Override
    protected String POISON_START() {
        return "^G<S-NAME> seem(s) wired!^?";
    }

    @Override
    protected String POISON_AFFECT() {
        return "^G<S-NAME> twitch(es) spastically.";
    }

    @Override
    protected String POISON_CAST() {
        return "^F^<FIGHT^><S-NAME> caffeinate(s) <T-NAMESELF>!^</FIGHT^>^?";
    }

    @Override
    protected String POISON_FAIL() {
        return "<S-NAME> attempt(s) to caffinate <T-NAMESELF>, but fail(s).";
    }

    @Override
    protected int POISON_DAMAGE() {
        return 0;
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        affectableStats.setStat(CharStats.STAT_DEXTERITY, affectableStats.getStat(CharStats.STAT_DEXTERITY) + 1);
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        affectableStats.setSpeed(affectableStats.speed() + 0.25);
        int oldDisposition = affectableStats.disposition();
        oldDisposition = oldDisposition & (~(PhyStats.IS_SLEEPING | PhyStats.IS_SNEAKING | PhyStats.IS_SITTING | PhyStats.IS_CUSTOM));
        affectableStats.setDisposition(oldDisposition);
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!(affected instanceof MOB))
            return true;

        final MOB mob = (MOB) affected;
        if (msg.amISource(mob) && ((msg.sourceMinor() == CMMsg.TYP_SIT) || (msg.sourceMinor() == CMMsg.TYP_SLEEP))) {
            mob.tell(L("You're too caffeinated for that!"));
            return false;
        }
        return true;
    }
}
