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
package com.syncleus.aethermud.game.Commands;

import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.*;
import com.syncleus.aethermud.game.core.collections.XVector;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Rideable;

import java.util.List;
import java.util.Vector;


public class Sleep extends StdCommand {
    private final String[] access = I(new String[]{"SLEEP", "SL"});

    public Sleep() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        Vector<String> origCmds = new XVector<String>(commands);
        if (CMLib.flags().isSleeping(mob)) {
            CMLib.commands().doCommandFail(mob, origCmds, L("You are already asleep!"));
            return false;
        }
        final Room R = mob.location();
        if (R == null)
            return false;
        if (commands.size() <= 1) {
            final CMMsg msg = CMClass.getMsg(mob, null, null, CMMsg.MSG_SLEEP, L("<S-NAME> lay(s) down and take(s) a nap."));
            if (R.okMessage(mob, msg))
                R.send(mob, msg);
            return false;
        }
        final String possibleRideable = CMParms.combine(commands, 1);
        final Environmental E = R.fetchFromRoomFavorItems(null, possibleRideable);
        if ((E == null) || (!CMLib.flags().canBeSeenBy(E, mob))) {
            CMLib.commands().doCommandFail(mob, origCmds, L("You don't see '@x1' here.", possibleRideable));
            return false;
        }
        String mountStr = null;
        if (E instanceof Rideable)
            mountStr = "<S-NAME> " + ((Rideable) E).mountString(CMMsg.TYP_SLEEP, mob) + " <T-NAME>.";
        else
            mountStr = L("<S-NAME> sleep(s) on <T-NAME>.");
        String sourceMountStr = null;
        if (!CMLib.flags().canBeSeenBy(E, mob))
            sourceMountStr = mountStr;
        else {
            sourceMountStr = CMStrings.replaceAll(mountStr, "<T-NAME>", E.name());
            sourceMountStr = CMStrings.replaceAll(sourceMountStr, "<T-NAMESELF>", E.name());
        }
        final CMMsg msg = CMClass.getMsg(mob, E, null, CMMsg.MSG_SLEEP, sourceMountStr, mountStr, mountStr);
        if (R.okMessage(mob, msg))
            R.send(mob, msg);
        return false;
    }

    @Override
    public double combatActionsCost(final MOB mob, final List<String> cmds) {
        return CMProps.getCommandCombatActionCost(ID());
    }

    @Override
    public double actionsCost(final MOB mob, final List<String> cmds) {
        return CMProps.getCommandActionCost(ID());
    }

    @Override
    public boolean canBeOrdered() {
        return true;
    }

}
