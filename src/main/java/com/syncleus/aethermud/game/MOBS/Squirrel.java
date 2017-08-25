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
package com.planet_ink.game.MOBS;

import com.planet_ink.game.Common.interfaces.CharStats;
import com.planet_ink.game.Common.interfaces.Faction;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMProps;

import java.util.Random;


public class Squirrel extends StdMOB {
    public Squirrel() {
        super();
        final Random randomizer = new Random(System.currentTimeMillis());

        username = "a squirrel";
        setDescription("It\\`s small, cute, and quick with a big expressive tail.");
        setDisplayText("A squirrel darts around.");
        CMLib.factions().setAlignment(this, Faction.Align.NEUTRAL);
        setMoney(0);
        basePhyStats.setWeight(4450 + Math.abs(randomizer.nextInt() % 5));
        setWimpHitPoint(2);

        basePhyStats().setDamage(2);

        baseCharStats().setStat(CharStats.STAT_INTELLIGENCE, 1);
        baseCharStats().setMyRace(CMClass.getRace("Squirrel"));
        baseCharStats().getMyRace().startRacing(this, false);

        basePhyStats().setAbility(0);
        basePhyStats().setLevel(1);
        basePhyStats().setArmor(90);

        baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(), CMProps.getMobHPBase(), basePhyStats().level()));

        recoverMaxState();
        resetToMaxState();
        recoverPhyStats();
        recoverCharStats();
    }

    @Override
    public String ID() {
        return "Squirrel";
    }

}