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
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class Thief_MarkInvisibility extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Invisibility to Mark");
    public boolean active = false;
    public MOB mark = null;

    @Override
    public String ID() {
        return "Thief_MarkInvisibility";
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
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_STEALTHY;
    }

    @Override
    public boolean isAutoInvoked() {
        return true;
    }

    @Override
    public boolean canBeUninvoked() {
        return false;
    }

    public MOB getMark(MOB mob) {
        final Thief_Mark A = (Thief_Mark) mob.fetchEffect("Thief_Mark");
        if (A != null)
            return A.mark;
        return null;
    }

    public int getMarkTicks(MOB mob) {
        final Thief_Mark A = (Thief_Mark) mob.fetchEffect("Thief_Mark");
        if ((A != null) && (A.mark != null))
            return A.ticks;
        return -1;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        if (active)
            affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_INVISIBLE);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if (affected instanceof MOB) {
            final MOB mob = (MOB) affected;
            mark = getMark(mob);
            if ((mark != null)
                && (mob.location() != null)
                && (mob.location().isInhabitant(mark))
                && ((mob.fetchAbility(ID()) == null) || proficiencyCheck(mob, 0, false))) {
                if (!active) {
                    active = true;
                    helpProficiency(mob, 0);
                    mob.recoverPhyStats();
                }
            } else if (active) {
                active = false;
                mob.recoverPhyStats();
            }
        }
        return true;
    }
}
