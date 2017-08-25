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
package com.planet_ink.game.Behaviors;

import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.interfaces.PhysicalAgent;


public class Fighterness extends CombatAbilities {
    @Override
    public String ID() {
        return "Fighterness";
    }

    @Override
    public String accountForYourself() {
        return "fighterliness";
    }

    @Override
    public void startBehavior(PhysicalAgent forMe) {
        super.startBehavior(forMe);
        if (!(forMe instanceof MOB))
            return;
        final MOB mob = (MOB) forMe;
        combatMode = COMBAT_RANDOM;
        makeClass(mob, getParmsMinusCombatMode(), "Fighter");
        newCharacter(mob);
        //%%%%%att,armor,damage,hp,mana,move
        if ((preCastSet == Integer.MAX_VALUE) || (preCastSet <= 0)) {
            setCombatStats(mob, 0, 15, 0, 15, -10, 10, true);
            setCharStats(mob);
        }
    }
}