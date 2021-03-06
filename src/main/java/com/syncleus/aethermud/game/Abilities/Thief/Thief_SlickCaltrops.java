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
package com.syncleus.aethermud.game.Abilities.Thief;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;

import java.util.HashSet;


public class Thief_SlickCaltrops extends Thief_Caltrops {
    private final static String localizedName = CMLib.lang().L("Slick Caltrops");
    private static final String[] triggerStrings = I(new String[]{"SLICKCALTROPS"});

    @Override
    public String ID() {
        return "Thief_SlickCaltrops";
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
    public String caltropTypeName() {
        return CMLib.lang().L("slick ");
    }

    @Override
    public void spring(MOB mob) {
        final MOB invoker = (invoker() != null) ? invoker() : CMLib.map().deity();
        if ((!invoker.mayIFight(mob))
            || (invoker.getGroupMembers(new HashSet<MOB>()).contains(mob))
            || (CMLib.dice().rollPercentage() < mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
            mob.location().show(mob, affected, this, CMMsg.MSG_OK_ACTION, L("<S-NAME> avoid(s) some @x1caltrops on the floor.", caltropTypeName()));
        else

        {
            final Ability A = CMClass.getAbility("Slip");
            if ((A != null) && (A.castingQuality(invoker, mob) == Ability.QUALITY_MALICIOUS)) {
                mob.location().show(invoker, mob, this, CMMsg.MSG_OK_ACTION, L("The @x1caltrops on the ground cause <T-NAME> to slip!", caltropTypeName()));
                if (A.invoke(invoker, mob, true, adjustedLevel(invoker(), 0))) {
                    if (CMLib.dice().rollPercentage() < mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
                        CMLib.combat().postDamage(invoker, mob, null, CMLib.dice().roll(5, 6, 6 * adjustedLevel(invoker(), 0)),
                            CMMsg.MASK_MALICIOUS | CMMsg.TYP_JUSTICE, Weapon.TYPE_PIERCING, L("The @x1caltrops on the ground <DAMAGE> <T-NAME>.", caltropTypeName()));
                }
            }
        }
        // does not set sprung flag -- as this trap never goes out of use
    }
}
