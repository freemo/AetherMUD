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
import com.syncleus.aethermud.game.Abilities.interfaces.DiseaseAffect;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharState;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class Disease_Blahs extends Disease {
    private final static String localizedName = CMLib.lang().L("Blahs");
    private final static String localizedStaticDisplay = CMLib.lang().L("(The Blahs)");

    @Override
    public String ID() {
        return "Disease_Blahs";
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
        return 99999;
    }

    @Override
    protected int DISEASE_DELAY() {
        return 20;
    }

    @Override
    protected String DISEASE_DONE() {
        return L("You feel a little better.");
    }

    @Override
    protected String DISEASE_START() {
        return L("^G<S-NAME> get(s) the blahs.^?");
    }

    @Override
    protected String DISEASE_AFFECT() {
        return L("<S-NAME> sigh(s).");
    }

    @Override
    public int spreadBitmap() {
        return DiseaseAffect.SPREAD_CONSUMPTION;
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

            if ((msg.amISource((MOB) affected))
                && (msg.sourceMessage() != null)
                && (msg.tool() == null)
                && ((msg.sourceMinor() == CMMsg.TYP_SPEAK)
                || (msg.sourceMinor() == CMMsg.TYP_TELL)
                || (CMath.bset(msg.sourceMajor(), CMMsg.MASK_CHANNEL)))) {
                final Ability A = CMClass.getAbility("Blah");
                if (A != null) {
                    A.setProficiency(100);
                    A.invoke(msg.source(), null, true, 0);
                    A.setAffectedOne(msg.source());
                    if (!A.okMessage(myHost, msg))
                        return false;
                }
            }
        } else {

        }
        return true;
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
        if ((mob.curState().getFatigue() < CharState.FATIGUED_MILLIS)
            && (mob.maxState().getFatigue() > Long.MIN_VALUE / 2))
            mob.curState().setFatigue(CharState.FATIGUED_MILLIS);
        return true;
    }

}
