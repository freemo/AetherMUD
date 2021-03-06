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
package com.syncleus.aethermud.game.Abilities.Diseases;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Commands.interfaces.Command;
import com.syncleus.aethermud.game.Common.interfaces.*;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.collections.XVector;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.MUDCmdProcessor;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class Disease_Depression extends Disease {
    private final static String localizedName = CMLib.lang().L("Depression");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Depression)");

    @Override
    public String ID() {
        return "Disease_Depression";
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
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public boolean putInCommandlist() {
        return false;
    }

    @Override
    public int difficultyLevel() {
        return 4;
    }

    @Override
    protected int DISEASE_TICKS() {
        return 900;
    }

    @Override
    protected int DISEASE_DELAY() {
        return 20;
    }

    @Override
    protected String DISEASE_DONE() {
        return L("You feel better.");
    }

    @Override
    protected String DISEASE_START() {
        return L("^G<S-NAME> seem(s) depressed.^?");
    }

    @Override
    protected String DISEASE_AFFECT() {
        return L("<S-NAME> moap(s).");
    }

    @Override
    public int abilityCode() {
        return 0;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!super.okMessage(myHost, msg))
            return false;
        if (affected instanceof MOB) {
            if (msg.source() != affected)
                return true;
            if (msg.source().location() == null)
                return true;
            final MOB mob = (MOB) affected;
            if (((msg.amITarget(mob)) || (msg.amISource(mob)))
                && (msg.tool() instanceof Social)
                && (msg.tool().Name().equals("MATE <T-NAME>")
                || msg.tool().Name().equals("SEX <T-NAME>"))) {
                mob.tell(L("You don't really feel like doing it right now."));
                return false;
            }
        }
        return true;
    }

    @Override
    public void affectPhyStats(Physical E, PhyStats stats) {
        super.affectPhyStats(E, stats);
        stats.setAttackAdjustment(stats.attackAdjustment() - 10);
    }

    public void affectChatStats(MOB E, CharStats stats) {
        super.affectCharStats(E, stats);
        stats.setStat(CharStats.STAT_SAVE_JUSTICE, stats.getStat(CharStats.STAT_SAVE_JUSTICE) - 20);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if (affected == null)
            return false;
        if (!(affected instanceof MOB))
            return true;

        final MOB mob = (MOB) affected;
        if (CMLib.dice().rollPercentage() == 1)
            mob.tell(L("You are hungry."));
        if (mob.isInCombat()
            && (CMLib.dice().rollPercentage() < 10)) {
            mob.tell(L("Whats the point in fighting, really?"));
            mob.makePeace(true);
        }
        if ((!mob.isInCombat())
            && (mob.session() != null)
            && (mob.session().getIdleMillis() > 10000)
            && ((CMLib.dice().rollPercentage() == 1) || (CMLib.flags().isSitting(mob)))) {
            final Command C = CMClass.getCommand("Sleep");
            try {
                C.execute(mob, new XVector<String>("Sleep"), MUDCmdProcessor.METAFLAG_FORCED);
            } catch (final Exception e) {
            }
        }
        if ((mob.curState().getFatigue() < CharState.FATIGUED_MILLIS)
            && (mob.maxState().getFatigue() > Long.MIN_VALUE / 2))
            mob.curState().setFatigue(CharState.FATIGUED_MILLIS);
        return true;
    }

}

