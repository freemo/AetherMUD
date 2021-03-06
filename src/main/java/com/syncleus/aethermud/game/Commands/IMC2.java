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

import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMSecurity;
import com.syncleus.aethermud.game.core.Log;

import java.util.List;


public class IMC2 extends StdCommand {
    private final String[] access = I(new String[]{"IMC2"});

    public IMC2() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    public void IMC2Error(MOB mob) {
        if (CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.IMC2))
            mob.tell(L("Try IMC2 LIST, IMC2 INFO [MUD], IMC2 LOCATE, IMC2 RESTART, or IMC2 CHANNELS."));
        else
            mob.tell(L("Try IMC2 LIST, IMC2 INFO [MUD], IMC2 LOCATE"));
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        if (!(CMLib.intermud().imc2online())) {
            mob.tell(L("IMC2 is unavailable."));
            return false;
        }
        commands.remove(0);
        if (commands.size() < 1) {
            IMC2Error(mob);
            return false;
        }
        final String str = commands.get(0);
        if (!(CMLib.intermud().imc2online()))
            mob.tell(L("IMC2 is unavailable."));
        else if (str.equalsIgnoreCase("list"))
            CMLib.intermud().giveIMC2MudList(mob);
        else if (str.equalsIgnoreCase("locate"))
            CMLib.intermud().i3locate(mob, CMParms.combine(commands, 1));
        else if (str.equalsIgnoreCase("channels") && CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.IMC2))
            CMLib.intermud().giveIMC2ChannelsList(mob);
        else if (str.equalsIgnoreCase("info"))
            CMLib.intermud().imc2mudInfo(mob, CMParms.combine(commands, 1));
        else if (str.equalsIgnoreCase("restart") && CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.IMC2)) {
            try {
                mob.tell(CMLib.hosts().get(0).executeCommand("START IMC2"));
            } catch (final Exception e) {
                Log.errOut("IMC2Cmd", e);
            }
        } else
            IMC2Error(mob);

        return false;
    }

    @Override
    public boolean canBeOrdered() {
        return true;
    }

}
