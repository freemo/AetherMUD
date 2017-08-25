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

import com.syncleus.aethermud.game.Commands.interfaces.Command;
import com.syncleus.aethermud.game.Common.interfaces.PlayerStats;
import com.syncleus.aethermud.game.Common.interfaces.Session;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.collections.XVector;

import java.util.List;
import java.util.Vector;


public class Reply extends StdCommand {
    private final String[] access = I(new String[]{"REPLY", "REP", "RE"});

    public Reply() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        if (mob == null)
            return false;
        if ((!mob.isMonster()) && mob.isAttributeSet(MOB.Attrib.QUIET)) {
            CMLib.commands().doCommandFail(mob, commands, L("You have QUIET mode on.  You must turn it off first."));
            return false;
        }
        Vector<String> origCmds = new XVector<String>(commands);
        final PlayerStats pstats = mob.playerStats();
        if (pstats == null)
            return false;
        if (pstats.getReplyToMOB() == null) {
            CMLib.commands().doCommandFail(mob, origCmds, L("No one has told you anything yet!"));
            return false;
        }
        if ((pstats.getReplyToMOB().Name().indexOf('@') < 0)
            && ((CMLib.players().getPlayer(pstats.getReplyToMOB().Name()) == null)
            || (pstats.getReplyToMOB().isMonster())
            || (!CMLib.flags().isInTheGame(pstats.getReplyToMOB(), true)))) {
            CMLib.commands().doCommandFail(mob, origCmds, L("@x1 is no longer logged in.", pstats.getReplyToMOB().Name()));
            return false;
        }
        if (CMParms.combine(commands, 1).length() == 0) {
            CMLib.commands().doCommandFail(mob, origCmds, L("Tell '@x1' what?", pstats.getReplyToMOB().Name()));
            return false;
        }
        final int replyType = pstats.getReplyType();

        switch (replyType) {
            case PlayerStats.REPLY_SAY:
                if ((pstats.getReplyToMOB().Name().indexOf('@') < 0)
                    && ((mob.location() == null) || (!mob.location().isInhabitant(pstats.getReplyToMOB())))) {
                    CMLib.commands().doCommandFail(mob, origCmds, L("@x1 is no longer in the room.", pstats.getReplyToMOB().Name()));
                    return false;
                }
                CMLib.commands().postSay(mob, pstats.getReplyToMOB(), CMParms.combine(commands, 1), false, false);
                break;
            case PlayerStats.REPLY_TELL: {
                final Session S = pstats.getReplyToMOB().session();
                if (pstats.getReplyToMOB().isAttributeSet(MOB.Attrib.QUIET)) {
                    CMLib.commands().doCommandFail(mob, origCmds, L("That person can not hear you."));
                    return false;
                }
                if (S != null)
                    S.snoopSuspension(1);
                CMLib.commands().postSay(mob, pstats.getReplyToMOB(), CMParms.combine(commands, 1), true, true);
                if (S != null)
                    S.snoopSuspension(-11);
                break;
            }
            case PlayerStats.REPLY_YELL: {
                final Command C = CMClass.getCommand("Say");
                if ((C != null) && (C.securityCheck(mob))) {
                    commands.set(0, "Yell");
                    C.execute(mob, commands, metaFlags);
                }
                break;
            }
        }
        if ((pstats.getReplyToMOB().session() != null)
            && (pstats.getReplyToMOB().session().isAfk()))
            mob.tell(pstats.getReplyToMOB().session().getAfkMessage());
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
