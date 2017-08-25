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
package com.syncleus.aethermud.game.Abilities.Misc;

import com.syncleus.aethermud.game.Abilities.StdAbility;
import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Prisoner extends StdAbility {
    private final static String localizedName = CMLib.lang().L("Prisoner");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Prisoner's Geas)");

    @Override
    public String ID() {
        return "Prisoner";
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
    public int classificationCode() {
        return Ability.ACODE_PROPERTY;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if ((affected instanceof MOB) && (msg.amISource((MOB) affected))) {
            if (msg.sourceMinor() == CMMsg.TYP_RECALL) {
                if (msg.source().location() != null)
                    msg.source().location().show(msg.source(), null, CMMsg.MSG_OK_ACTION, L("<S-NAME> attempt(s) to recall, but a geas prevents <S-HIM-HER>."));
                return false;
            } else if (msg.sourceMinor() == CMMsg.TYP_FLEE) {
                msg.source().location().show(msg.source(), null, CMMsg.MSG_OK_ACTION, L("<S-NAME> attempt(s) to flee, but a geas prevents <S-HIM-HER>."));
                return false;
            } else if ((msg.tool() instanceof Ability)
                && (msg.targetMinor() == CMMsg.TYP_LEAVE)) {
                msg.source().location().show(msg.source(), null, CMMsg.MSG_OK_ACTION, L("<S-NAME> attempt(s) to escape parole, but a geas prevents <S-HIM-HER>."));
                return false;
            } else if ((msg.targetMinor() == CMMsg.TYP_ENTER)
                && (msg.target() instanceof Room)
                && (msg.source().location() != null)
                && (!msg.source().location().getArea().name().equals(((Room) msg.target()).getArea().name()))) {
                msg.source().location().show(msg.source(), null, CMMsg.MSG_OK_ACTION, L("<S-NAME> attempt(s) to escape parole, but a geas prevents <S-HIM-HER>."));
                return false;
            }
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();

        if (canBeUninvoked())
            mob.tell(L("Your sentence has been served."));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical target, boolean auto, int asLevel) {
        this.startTickDown(mob, target, 0);
        return true;
    }
}
