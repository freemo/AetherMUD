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
package com.syncleus.aethermud.game.Abilities.Languages;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Abilities.interfaces.Language;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.Enumeration;
import java.util.List;


public class Common extends StdLanguage {
    private final static String localizedName = CMLib.lang().L("Common");

    public Common() {
        super();
        proficiency = 100;
    }

    @Override
    public String ID() {
        return "Common";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public boolean isAutoInvoked() {
        return false;
    }

    @Override
    public boolean canBeUninvoked() {
        return canBeUninvoked;
    }

    @Override
    public int proficiency() {
        return 100;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        boolean anythingDone = false;
        for (final Enumeration<Ability> a = mob.effects(); a.hasMoreElements(); ) {
            final Ability A = a.nextElement();
            if ((A != null) && (A instanceof Language)) {
                if (((Language) A).beingSpoken(ID())) {
                    anythingDone = true;
                    ((Language) A).setBeingSpoken(ID(), false);
                }
            }
        }
        isAnAutoEffect = false;
        if (!auto) {
            String msg = null;
            if (!anythingDone)
                msg = "already speaking " + name() + ".";
            else
                msg = "now speaking " + name() + ".";
            mob.tell(L("You are @x1", msg));
            if ((mob.isMonster()) && (mob.amFollowing() != null))
                CMLib.commands().postSay(mob, L("I am @x1", msg));
        }
        return true;
    }
}
