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
package com.syncleus.aethermud.game.Abilities.Common;

import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class MasterTailoring extends Tailoring {
    private final static String localizedName = CMLib.lang().L("Master Tailoring");
    private static final String[] triggerStrings = I(new String[]{"MASTERKNIT", "MKNIT", "MTAILOR", "MTAILORING", "MASTERTAILOR", "MASTERTAILORING"});

    @Override
    public String ID() {
        return "MasterTailoring";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public String parametersFile() {
        return "mastertailor.txt";
    }

    @Override
    protected boolean masterCraftCheck(final Item I) {
        if (I.name().toUpperCase().startsWith("DESIGNER") || (I.name().toUpperCase().indexOf(" DESIGNER ") > 0))
            return true;
        if (I.basePhyStats().level() < 31)
            return false;
        return true;
    }

    @Override
    protected boolean autoGenInvoke(final MOB mob, List<String> commands, Physical givenTarget, final boolean auto,
                                    final int asLevel, int autoGenerate, boolean forceLevels, List<Item> crafted) {
        if (super.checkStop(mob, commands))
            return true;

        if (super.checkInfo(mob, commands))
            return true;

        randomRecipeFix(mob, addRecipes(mob, loadRecipes()), commands, autoGenerate);
        if (commands.size() == 0) {
            commonTell(mob, L("Knit what? Enter \"mknit list\" for a list, \"mknit info <item>\", \"mknit refit <item>\" to resize,"
                + " \"mknit learn <item>\", \"mknit scan\", \"mknit mend <item>\", or \"mknit stop\" to cancel."));
            return false;
        }
        return super.autoGenInvoke(mob, commands, givenTarget, auto, asLevel, autoGenerate, forceLevels, crafted);
    }
}

