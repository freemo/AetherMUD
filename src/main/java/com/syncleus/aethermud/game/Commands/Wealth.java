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

import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMParms;

import java.util.List;

/*
    Written by Robert Little - The Looking Glass
   Copyright 2010-2017 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Wealth extends Inventory {
    @SuppressWarnings("rawtypes")
    private final static Class[][] internalParameters = new Class[][]{{MOB.class}};
    private final String[] access = I(new String[]{"WEALTH"});

    public Wealth() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public StringBuilder getInventory(MOB seer, MOB mob, String mask, boolean longInv) {
        final StringBuilder msg = new StringBuilder("");
        final InventoryList list = fetchInventory(seer, mob);
        if (list.moneyItems.size() == 0)
            msg.append(L("\n\r^HMoney:^N None!\n\r"));
        else
            msg.append(getShowableMoney(list));
        return msg;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        final StringBuilder msg = getInventory(mob, mob, CMParms.combine(commands, 1), false);
        if (msg.length() == 0)
            mob.tell(L("You have no money on you."));
        else if (!mob.isMonster())
            mob.session().wraplessPrintln(msg.toString());
        return false;
    }

    @Override
    public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException {
        if (!super.checkArguments(internalParameters, args))
            return "";
        final MOB M = (MOB) args[0];
        return getInventory(M, mob, null, false).toString();
    }

    public int ticksToExecute() {
        return 0;
    }

    @Override
    public boolean canBeOrdered() {
        return true;
    }
}