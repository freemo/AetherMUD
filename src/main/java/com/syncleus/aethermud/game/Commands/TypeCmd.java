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
import com.planet_ink.game.Items.interfaces.Computer;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.Wearable;
import com.planet_ink.game.Libraries.interfaces.CMFlagLibrary;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMParms;
import com.planet_ink.game.core.collections.XVector;
import com.planet_ink.game.core.interfaces.Environmental;

import java.util.List;
import java.util.Vector;

public class TypeCmd extends Go {
    private final String[] access = I(new String[]{"TYPE", "="});

    public TypeCmd() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        Vector<String> origCmds = new XVector<String>(commands);
        final Room R = mob.location();
        final boolean consoleMode = (mob.riding() instanceof Computer);
        if ((commands.size() <= 1) || (R == null)) {
            if (consoleMode)
                CMLib.commands().doCommandFail(mob, origCmds, L("Type what into this console?  Have you read the screen?"));
            else
                CMLib.commands().doCommandFail(mob, origCmds, L("Type what into what?"));
            return false;
        }
        Environmental typeIntoThis = (consoleMode) ? mob.riding() : null;
        if (typeIntoThis == null) {
            int x = 1;
            while ((x < commands.size()) && (!commands.get(x).toString().equalsIgnoreCase("into")))
                x++;
            if (x < commands.size() - 1) {
                final String typeWhere = CMParms.combine(commands, x + 1);
                typeIntoThis = mob.location().fetchFromMOBRoomFavorsItems(mob, null, typeWhere, Wearable.FILTER_ANY);
                if (typeIntoThis == null) {
                    final CMFlagLibrary flagLib = CMLib.flags();
                    for (int i = 0; i < R.numItems(); i++) {
                        final Item I = R.getItem(i);
                        if (flagLib.isOpenAccessibleContainer(I)) {
                            typeIntoThis = R.fetchFromRoomFavorItems(I, typeWhere);
                            if (typeIntoThis != null)
                                break;
                        }
                    }
                }
                if (typeIntoThis != null) {
                    while (commands.size() > x)
                        commands.remove(commands.size() - 1);
                } else {
                    CMLib.commands().doCommandFail(mob, origCmds, L("You don't see '@x1' here.", typeWhere.toLowerCase()));
                }
            }
        }

        final String enterWhat = CMParms.combine(commands, 1);
        if (typeIntoThis != null) {
            final String enterStr = L("^W<S-NAME> enter(s) '@x1' into <T-NAME>.^?", enterWhat);
            final CMMsg msg = CMClass.getMsg(mob, typeIntoThis, null, CMMsg.MSG_WRITE, enterStr, CMMsg.MSG_WRITE, enterWhat, CMMsg.MSG_WRITE, null);
            if (mob.location().okMessage(mob, msg))
                mob.location().send(mob, msg);
            return true;
        } else {
            CMLib.commands().doCommandFail(mob, origCmds, L("You don't see '@x1' here.", enterWhat.toLowerCase()));
        }
        return false;
    }

    @Override
    public boolean canBeOrdered() {
        return true;
    }
}