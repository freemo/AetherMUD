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
import com.syncleus.aethermud.game.core.CMFile;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.Resources;

import java.util.List;


public class Rules extends StdCommand {
    private final String[] access = I(new String[]{"RULES"});

    public Rules() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        StringBuffer credits = new CMFile(Resources.buildResourcePath("text") + "rules.txt", null, CMFile.FLAG_LOGERRORS).text();
        try {
            credits = CMLib.webMacroFilter().virtualPageFilter(credits);
        } catch (final Exception e) {
        }
        if ((credits != null) && (mob.session() != null) && (credits.length() > 0))
            mob.session().colorOnlyPrintln(credits.toString());
        else
            mob.tell(L("This mud has no rules.  Welcome to chaos."));
        return false;
    }

    @Override
    public boolean canBeOrdered() {
        return true;
    }

}
