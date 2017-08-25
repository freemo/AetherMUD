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
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.collections.XVector;
import com.syncleus.aethermud.game.core.interfaces.Environmental;

import java.util.List;
import java.util.Vector;


public class Sell extends StdCommand {
    private final String[] access = I(new String[]{"SELL"});

    public Sell() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        Vector<String> origCmds = new XVector<String>(commands);
        final Environmental shopkeeper = CMLib.english().parseShopkeeper(mob, commands, "Sell what to whom?");
        if (shopkeeper == null)
            return false;
        if (commands.size() == 0) {
            CMLib.commands().doCommandFail(mob, origCmds, L("Sell what?"));
            return false;
        }

        final int maxToDo = CMLib.english().calculateMaxToGive(mob, commands, true, mob, false);
        if (maxToDo < 0)
            return false;

        String whatName = CMParms.combine(commands, 0);
        final Vector<Item> V = new Vector<Item>();
        boolean allFlag = commands.get(0).equalsIgnoreCase("all");
        if (whatName.toUpperCase().startsWith("ALL.")) {
            allFlag = true;
            whatName = "ALL " + whatName.substring(4);
        }
        if (whatName.toUpperCase().endsWith(".ALL")) {
            allFlag = true;
            whatName = "ALL " + whatName.substring(0, whatName.length() - 4);
        }
        int addendum = 1;
        String addendumStr = "";
        boolean doBugFix = true;
        while (doBugFix || ((allFlag) && (addendum <= maxToDo))) {
            doBugFix = false;
            final Item itemToDo = mob.fetchItem(null, Wearable.FILTER_UNWORNONLY, whatName + addendumStr);
            if (itemToDo == null)
                break;
            if ((CMLib.flags().canBeSeenBy(itemToDo, mob))
                && (!V.contains(itemToDo)))
                V.add(itemToDo);
            addendumStr = "." + (++addendum);
        }

        if (V.size() == 0)
            CMLib.commands().doCommandFail(mob, origCmds, L("You don't seem to have '@x1'.", whatName));
        else {
            for (int v = 0; v < V.size(); v++) {
                final Item thisThang = V.get(v);
                final CMMsg newMsg = CMClass.getMsg(mob, shopkeeper, thisThang, CMMsg.MSG_SELL, L("<S-NAME> sell(s) <O-NAME> to <T-NAMESELF>."));
                if (mob.location().okMessage(mob, newMsg))
                    mob.location().send(mob, newMsg);
            }
        }
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
        return false;
    }

}
