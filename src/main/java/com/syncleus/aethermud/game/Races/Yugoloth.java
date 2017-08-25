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
package com.syncleus.aethermud.game.Races;

import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;


public class Yugoloth extends Demon {
    private final static String localizedStaticName = CMLib.lang().L("Yugoloth");
    private final static String localizedStaticRacialCat = CMLib.lang().L("Demon");
    // 									   an ey ea he ne ar ha to le fo no gi mo wa ta wi
    private static final int[] parts = {0, 2, 2, 1, 1, 2, 2, 1, 2, 2, 1, 0, 1, 1, 1, 0};
    private final String[] culturalAbilityNames = {"Undercommon"};
    private final int[] culturalAbilityProficiencies = {25};

    @Override
    public String ID() {
        return "Yugoloth";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public int shortestMale() {
        return 60;
    }

    @Override
    public int shortestFemale() {
        return 60;
    }

    @Override
    public int heightVariance() {
        return 12;
    }

    @Override
    public int lightestWeight() {
        return 120;
    }

    @Override
    public int weightVariance() {
        return 20;
    }

    @Override
    public long forbiddenWornBits() {
        return 0;
    }

    @Override
    public String racialCategory() {
        return localizedStaticRacialCat;
    }

    @Override
    public String[] culturalAbilityNames() {
        return culturalAbilityNames;
    }

    @Override
    public int[] culturalAbilityProficiencies() {
        return culturalAbilityProficiencies;
    }

    @Override
    public int[] bodyMask() {
        return parts;
    }

    @Override
    public int availabilityCode() {
        return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_SEE_INFRARED);
    }
}
