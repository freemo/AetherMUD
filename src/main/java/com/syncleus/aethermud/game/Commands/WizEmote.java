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

import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Common.interfaces.Clan;
import com.syncleus.aethermud.game.Common.interfaces.Session;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMSecurity;

import java.util.List;


public class WizEmote extends StdCommand {
    private final String[] access = I(new String[]{"WIZEMOTE"});

    public WizEmote() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        if (commands.size() > 2) {
            final String who = commands.get(1);
            final String msg = CMParms.combineQuoted(commands, 2);
            Room R = CMLib.map().getRoom(who);
            if (who.toUpperCase().equals("HERE"))
                R = mob.location();
            final Area A = CMLib.map().findAreaStartsWith(who);
            final Clan C = CMLib.clans().findClan(who);
            if (who.toUpperCase().equals("ALL")) {
                for (final Session S : CMLib.sessions().localOnlineIterable()) {
                    if ((S.mob() != null)
                        && (S.mob().location() != null)
                        && (CMSecurity.isAllowed(mob, S.mob().location(), CMSecurity.SecFlag.WIZEMOTE)))
                        S.stdPrintln("^w" + msg + "^?");
                }
            } else if (R != null) {
                for (final Session S : CMLib.sessions().localOnlineIterable()) {
                    if ((S.mob() != null)
                        && (S.mob().location() == R)
                        && (CMSecurity.isAllowed(mob, R, CMSecurity.SecFlag.WIZEMOTE)))
                        S.stdPrintln("^w" + msg + "^?");
                }
            } else if (A != null) {
                for (final Session S : CMLib.sessions().localOnlineIterable()) {
                    if ((S.mob() != null)
                        && (S.mob().location() != null)
                        && (A.inMyMetroArea(S.mob().location().getArea()))
                        && (CMSecurity.isAllowed(mob, S.mob().location(), CMSecurity.SecFlag.WIZEMOTE)))
                        S.stdPrintln("^w" + msg + "^?");
                }
            } else if (C != null) {
                for (final Session S : CMLib.sessions().localOnlineIterable()) {
                    if ((S.mob() != null)
                        && (S.mob().getClanRole(C.clanID()) != null)
                        && (CMSecurity.isAllowed(mob, S.mob().location(), CMSecurity.SecFlag.WIZEMOTE)))
                        S.stdPrintln("^w" + msg + "^?");
                }
            } else {
                boolean found = false;
                for (final Session S : CMLib.sessions().localOnlineIterable()) {
                    if ((S.mob() != null)
                        && (S.mob().location() != null)
                        && (CMSecurity.isAllowed(mob, S.mob().location(), CMSecurity.SecFlag.WIZEMOTE))
                        && (CMLib.english().containsString(S.mob().name(), who)
                        || CMLib.english().containsString(S.mob().location().getArea().name(), who))) {
                        S.stdPrintln("^w" + msg + "^?");
                        found = true;
                        break;
                    }
                }
                if (!found)
                    mob.tell(L("You can't find anyone or anywhere by that name."));
            }
        } else
            mob.tell(L("You must specify either all, or an area/mob name, and an message."));
        return false;
    }

    @Override
    public boolean canBeOrdered() {
        return true;
    }

    @Override
    public boolean securityCheck(MOB mob) {
        return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.WIZEMOTE);
    }

}
