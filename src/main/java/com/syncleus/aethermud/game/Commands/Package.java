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
package com.planet_ink.game.Commands;

import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Items.interfaces.Coins;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.PackagedItems;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMParms;
import com.planet_ink.game.core.CMProps;
import com.planet_ink.game.core.collections.XVector;
import com.planet_ink.game.core.interfaces.Environmental;
import com.planet_ink.game.core.interfaces.ItemPossessor;

import java.util.List;
import java.util.Vector;


public class Package extends StdCommand {
    private final String[] access = I(new String[]{"PACKAGE"});

    public Package() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        Vector<String> origCmds = new XVector<String>(commands);
        if (commands.size() < 2) {
            CMLib.commands().doCommandFail(mob, origCmds, L("Package what?"));
            return false;
        }
        commands.remove(0);
        String whatName = "";
        if (commands.size() > 0)
            whatName = commands.get(commands.size() - 1);
        final int maxToGet = CMLib.english().calculateMaxToGive(mob, commands, true, mob, false);
        if (maxToGet < 0)
            return false;

        String whatToGet = CMParms.combine(commands, 0);
        boolean allFlag = (commands.size() > 0) ? commands.get(0).equalsIgnoreCase("all") : false;
        if (whatToGet.toUpperCase().startsWith("ALL.")) {
            allFlag = true;
            whatToGet = "ALL " + whatToGet.substring(4);
        }
        if (whatToGet.toUpperCase().endsWith(".ALL")) {
            allFlag = true;
            whatToGet = "ALL " + whatToGet.substring(0, whatToGet.length() - 4);
        }
        final Vector<Item> V = new Vector<Item>();
        int addendum = 1;
        String addendumStr = "";
        do {
            Environmental getThis = null;
            getThis = mob.location().fetchFromRoomFavorItems(null, whatToGet + addendumStr);
            if (getThis == null)
                break;
            if ((getThis instanceof Item)
                && (CMLib.flags().canBeSeenBy(getThis, mob))
                && ((!allFlag) || CMLib.flags().isGettable(((Item) getThis)) || (getThis.displayText().length() > 0))
                && (!V.contains(getThis)))
                V.add((Item) getThis);
            addendumStr = "." + (++addendum);
        }
        while ((allFlag) && (addendum <= maxToGet))
            ;

        if (V.size() == 0) {
            CMLib.commands().doCommandFail(mob, origCmds, L("You don't see '@x1' here.", whatName));
            return false;
        }

        for (int i = 0; i < V.size(); i++) {
            final Item I = V.get(i);
            if ((I instanceof Coins)
                || (CMLib.flags().isEnspelled(I))
                || (CMLib.flags().isOnFire(I))) {
                CMLib.commands().doCommandFail(mob, origCmds, L("Items such as @x1 may not be packaged.", I.name(mob)));
                return false;
            }
        }
        final PackagedItems thePackage = (PackagedItems) CMClass.getItem("GenPackagedItems");
        if (thePackage == null)
            return false;
        if (!thePackage.isPackagable(V)) {
            CMLib.commands().doCommandFail(mob, origCmds, L("All items in a package must be absolutely identical.  Some here are not."));
            return false;
        }
        Item getThis = null;
        for (int i = 0; i < V.size(); i++) {
            getThis = V.get(i);
            if ((!mob.isMine(getThis)) && (!Get.get(mob, null, getThis, true, "get", true)))
                return false;
        }
        if (getThis == null)
            return false;
        final String name = CMLib.english().cleanArticles(getThis.name());
        final CMMsg msg = CMClass.getMsg(mob, getThis, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> package(s) up @x1 <T-NAMENOART>(s).", "" + V.size()));
        if (mob.location().okMessage(mob, msg)) {
            mob.location().send(mob, msg);
            thePackage.setName(name);
            if (thePackage.packageMe(getThis, V.size())) {
                for (int i = 0; i < V.size(); i++)
                    V.get(i).destroy();
                mob.location().addItem(thePackage, ItemPossessor.Expire.Player_Drop);
                mob.location().recoverRoomStats();
                mob.location().recoverRoomStats();
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
        return true;
    }

}