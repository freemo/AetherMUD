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

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.Session;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMSecurity;
import com.syncleus.aethermud.game.core.CMStrings;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.Enumeration;
import java.util.List;


@SuppressWarnings({"unchecked", "rawtypes"})
public class AutoAffects extends StdCommand {
    private final static Class[][] internalParameters = new Class[][]{{MOB.class}, {StringBuffer.class}, {StringBuilder.class}, {List.class}, {}};
    private final String[] access = I(new String[]{"AUTOAFFECTS", "AUTOAFF", "AAF"});

    public AutoAffects() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    public String getAutoAffects(MOB viewerMOB, Physical P) {
        final StringBuffer msg = new StringBuffer("");
        final int NUM_COLS = 2;
        final int COL_LEN = CMLib.lister().fixColWidth(25.0, viewerMOB);
        int colnum = NUM_COLS;
        for (final Enumeration<Ability> a = P.effects(); a.hasMoreElements(); ) {
            final Ability A = a.nextElement();
            if (A == null)
                continue;
            final String disp = A.name();
            if ((A.displayText().length() == 0)
                && ((!(P instanceof MOB)) || (((MOB) P).fetchAbility(A.ID()) != null))
                && (A.isAutoInvoked())) {
                if (((++colnum) > NUM_COLS) || (disp.length() > COL_LEN)) {
                    msg.append("\n\r");
                    colnum = 0;
                }
                msg.append("^S" + CMStrings.padRightPreserve("^<HELPNAME NAME='" + A.Name() + "'^>" + disp + "^</HELPNAME^>", COL_LEN));
                if (disp.length() > COL_LEN)
                    colnum = 99;
            }
        }
        msg.append("^N\n\r");
        return msg.toString();
    }

    protected void readAutoAffects(MOB mob, Session S, String name) {
        if (S != null) {
            if (CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDMOBS)) {
                if (name.length() > 0) {
                    final Physical P = mob.location().fetchFromMOBRoomFavorsItems(mob, null, name, Wearable.FILTER_ANY);
                    if (P == null)
                        S.colorOnlyPrint(L("You don't see @x1 here.", name));
                    else {
                        if (S == mob.session())
                            S.colorOnlyPrint(L(" \n\r^!@x1 is affected by: ^?", P.name()));
                        final String msg = getAutoAffects(mob, P);
                        if (msg.length() < 5)
                            S.colorOnlyPrintln(L("Nothing!\n\r^N"));
                        else
                            S.colorOnlyPrintln(msg);
                    }
                    return;
                }

            }
            if (S == mob.session())
                S.colorOnlyPrint(L(" \n\r^!Your auto-invoked skills are:^?"));
            final String msg = getAutoAffects(mob, mob);
            if (msg.length() < 5)
                S.colorOnlyPrintln(L(" Non-existant!\n\r^N"));
            else
                S.colorOnlyPrintln(msg);
        }
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        final String name = CMParms.combine(commands, 1);
        Session S = mob.session();
        readAutoAffects(mob, S, name);
        return false;
    }

    @Override
    public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException {
        if (!super.checkArguments(internalParameters, args))
            return Boolean.FALSE;
        if (args.length > 0) {
            if (args[0] instanceof Session) {
                readAutoAffects(mob, (Session) args[0], "");
                return Boolean.TRUE;
            } else if (args[0] instanceof StringBuffer) {
                ((StringBuffer) args[0]).append(getAutoAffects(mob, mob));
                return Boolean.TRUE;
            } else if (args[0] instanceof StringBuilder) {
                ((StringBuilder) args[0]).append(getAutoAffects(mob, mob));
                return Boolean.TRUE;
            } else if (args[0] instanceof List) {
                ((List) args[0]).add(getAutoAffects(mob, mob));
                return Boolean.TRUE;
            }
        }
        return getAutoAffects(mob, mob);
    }

    @Override
    public boolean canBeOrdered() {
        return true;
    }
}

