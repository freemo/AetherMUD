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
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Drink;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Thief_HoldYourLiquor extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Hold Your Liquor");
    protected volatile int checkAgain = Integer.MAX_VALUE / 2;

    @Override
    public String ID() {
        return "Thief_HoldYourLiquor";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return "";
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_SELF;
    }

    @Override
    public boolean isAutoInvoked() {
        return true;
    }

    @Override
    public boolean canBeUninvoked() {
        return false;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_FITNESS;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!(affected instanceof MOB))
            return super.okMessage(myHost, msg);

        final MOB mob = (MOB) affected;
        if ((msg.amISource(mob))
            && (msg.targetMinor() == CMMsg.TYP_DRINK)
            && (msg.target() instanceof Drink)
            && (msg.target() instanceof Item)
            && (CMLib.flags().isAlcoholic((Item) msg.target()))) {
            if (proficiencyCheck(mob, 0, false))
                checkAgain = 2;
            super.helpProficiency(mob, 0);
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if (((--checkAgain) <= 0) && (ticking instanceof Physical)) {
            checkAgain = Integer.MAX_VALUE / 2;
            List<Ability> aList = CMLib.flags().flaggedAffects((Physical) ticking, Ability.FLAG_INTOXICATING);
            for (Ability A : aList) {
                //int code=A.abilityCode();
                A.setAbilityCode(0);
            }
            if (aList.size() > 0) {
                ((Physical) ticking).recoverPhyStats();
                if (ticking instanceof MOB) {
                    ((MOB) ticking).recoverCharStats();
                    ((MOB) ticking).recoverMaxState();
                }
            }
        }
        return true;
    }
}
