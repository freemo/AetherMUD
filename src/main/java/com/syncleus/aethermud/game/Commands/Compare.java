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

import com.syncleus.aethermud.game.Items.interfaces.*;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.collections.XVector;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.ShopKeeper;

import java.util.List;
import java.util.Vector;


public class Compare extends StdCommand {
    private final String[] access = I(new String[]{"COMPARE", "COMP"});

    public Compare() {
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
            CMLib.commands().doCommandFail(mob, origCmds, L("Compare what to what?"));
            return false;
        }
        commands.remove(0);
        Item compareThis = mob.findItem(null, commands.get(0));
        if ((compareThis == null) || (!CMLib.flags().canBeSeenBy(compareThis, mob))) {
            final List<Environmental> V = CMLib.aetherShops().getAllShopkeepers(mob.location(), mob);
            if (V.size() > 0) {
                for (int i = 0; i < V.size(); i++) {
                    final Environmental shopkeeper = V.get(i);
                    final ShopKeeper SK = CMLib.aetherShops().getShopKeeper(shopkeeper);
                    final Environmental itemToDo = SK.getShop().getStock(commands.get(0), mob);
                    if ((itemToDo == null) || (!(itemToDo instanceof Item))) {
                        continue; // next shopkeeper
                    }
                    compareThis = (Item) itemToDo;
                }
                if ((compareThis == null) || (!CMLib.flags().canBeSeenBy(compareThis, mob))) {
                    CMLib.commands().doCommandFail(mob, origCmds, L("You don't have a @x1.", (commands.get(0))));
                    return false;
                }
            } else {
                CMLib.commands().doCommandFail(mob, origCmds, L("You don't have a @x1.", (commands.get(0))));
                return false;
            }
        }
        long compareThisCode = compareThis.rawProperLocationBitmap();
        if (CMath.bset(compareThisCode, Wearable.WORN_HELD)
            && (!CMath.bset(compareThisCode, Wearable.WORN_WIELD))
            && (compareThisCode != Wearable.WORN_HELD))
            compareThisCode = CMath.unsetb(compareThisCode, Wearable.WORN_HELD);
        Item toThis = null;
        if (commands.size() == 1) {
            Item possible = null;
            for (int i = 0; i < mob.numItems(); i++) {
                final Item I = mob.getItem(i);
                if ((I != null)
                    && (I != compareThis)
                    && (I.rawLogicalAnd() == compareThis.rawLogicalAnd())) {
                    long compareThatCode = I.rawProperLocationBitmap();
                    if (CMath.bset(compareThatCode, Wearable.WORN_HELD)
                        && (!CMath.bset(compareThatCode, Wearable.WORN_WIELD))
                        && (compareThatCode != Wearable.WORN_HELD))
                        compareThatCode = CMath.unsetb(compareThatCode, Wearable.WORN_HELD);
                    if (compareThisCode == compareThatCode) {
                        if (!I.amWearingAt(Wearable.IN_INVENTORY)) {
                            toThis = I;
                            break;
                        }
                        if (possible == null)
                            possible = I;
                    }
                }
            }
            if (toThis == null)
                toThis = possible;
            if ((toThis == null) || (!CMLib.flags().canBeSeenBy(toThis, mob))) {
                CMLib.commands().doCommandFail(mob, origCmds, L("Compare a @x1 to what?", compareThis.name()));
                return false;
            }
        } else
            toThis = mob.findItem(null, CMParms.combine(commands, 1));
        if ((toThis == null) || (!CMLib.flags().canBeSeenBy(toThis, mob))) {
            CMLib.commands().doCommandFail(mob, origCmds, L("You don't have a @x1.", (commands.get(1))));
            return false;
        }

        if ((compareThis instanceof Weapon) && (toThis instanceof Weapon)) {
            int cDmg = compareThis.basePhyStats().damage();
            final int tDmg = toThis.basePhyStats().damage();
            cDmg += (int) Math.round(CMath.div(compareThis.basePhyStats().attackAdjustment() - toThis.basePhyStats().attackAdjustment(), 100.0) * cDmg);

            if (cDmg == tDmg)
                mob.tell(L("@x1 and @x2 look about the same.", compareThis.name(), toThis.name()));
            else if (cDmg > tDmg)
                mob.tell(L("@x1 looks better than @x2.", compareThis.name(), toThis.name()));
            else
                mob.tell(L("@x1 looks worse than @x2.", compareThis.name(), toThis.name()));
        } else if ((compareThis instanceof Armor) && (toThis instanceof Armor)) {
            if (!compareThis.compareProperLocations(toThis)) {
                mob.tell(L("@x1 is not worn the same way as @x2, and can't be compared to it.", compareThis.name(), toThis.name()));
                return false;
            }
            if (compareThis.basePhyStats().armor() == toThis.basePhyStats().armor())
                mob.tell(L("@x1 and @x2 look about the same.", compareThis.name(), toThis.name()));
            else if (compareThis.basePhyStats().armor() > toThis.basePhyStats().armor())
                mob.tell(L("@x1 looks better than @x2.", compareThis.name(), toThis.name()));
            else
                mob.tell(L("@x1 looks worse than @x2.", compareThis.name(), toThis.name()));

        } else if ((compareThis instanceof Container) && (toThis instanceof Container)
            && (((Container) compareThis).capacity() > 0) && (((Container) toThis).capacity() > 0)) {
            if (((Container) compareThis).capacity() > ((Container) toThis).capacity())
                mob.tell(L("@x1 looks like it holds more than @x2.", compareThis.name(), toThis.name()));
            else
                mob.tell(L("@x1 looks like it holds less than @x2.", compareThis.name(), toThis.name()));
        } else
            CMLib.commands().doCommandFail(mob, origCmds, L("You can't compare @x1 and @x2.", compareThis.name(), toThis.name()));
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
