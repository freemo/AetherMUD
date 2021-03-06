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
package com.syncleus.aethermud.game.Abilities.Thief;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Abilities.interfaces.Trap;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;


public class Thief_AvoidTraps extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Avoid Traps");

    @Override
    public String ID() {
        return "Thief_AvoidTraps";
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
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_SELF;
    }

    @Override
    public boolean isAutoInvoked() {
        return true;
    }

    @Override
    public boolean canBeUninvoked() {
        return false;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_DETRAP;
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        super.affectCharStats(affected, affectableStats);
        affectableStats.setStat(CharStats.STAT_SAVE_TRAPS, affectableStats.getStat(CharStats.STAT_SAVE_TRAPS) + (proficiency() / 2) + (getXLEVELLevel(affected) * 5));
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!(affected instanceof MOB))
            return super.okMessage(myHost, msg);
        final MOB mob = (MOB) affected;
        if (msg.amITarget(mob)
            && (!msg.amISource(mob))
            && (msg.tool() instanceof Trap)
            && (CMLib.dice().rollPercentage() > 90))
            helpProficiency(mob, 0);
        return super.okMessage(myHost, msg);
    }
}
