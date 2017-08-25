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

import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;

import java.util.List;
import java.util.Vector;


public class Harpy extends GreatBird {
    private final static String localizedStaticName = CMLib.lang().L("Harpy");
    private final static String localizedStaticRacialCat = CMLib.lang().L("Avian");
    //  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
    private static final int[] parts = {0, 2, 2, 1, 1, 0, 0, 1, 2, 2, 1, 0, 1, 1, 1, 2};
    protected static Vector<RawMaterial> resources = new Vector<RawMaterial>();
    private final int[] agingChart = {0, 1, 4, 20, 50, 75, 100, 110, 120};

    @Override
    public String ID() {
        return "Harpy";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public int shortestMale() {
        return 59;
    }

    @Override
    public int shortestFemale() {
        return 59;
    }

    @Override
    public int heightVariance() {
        return 12;
    }

    @Override
    public int lightestWeight() {
        return 160;
    }

    @Override
    public int weightVariance() {
        return 80;
    }

    @Override
    public long forbiddenWornBits() {
        return Wearable.WORN_HELD | Wearable.WORN_WIELD | Wearable.WORN_FEET;
    }

    @Override
    public String racialCategory() {
        return localizedStaticRacialCat;
    }

    @Override
    public int[] bodyMask() {
        return parts;
    }

    @Override
    public int[] getAgingChart() {
        return agingChart;
    }

    @Override
    public void affectCharStats(MOB affectedMOB, CharStats affectableStats) {
        affectableStats.setStat(CharStats.STAT_GENDER, 'F');
    }

    @Override
    public List<RawMaterial> myResources() {
        synchronized (resources) {
            if (resources.size() == 0) {
                resources.addElement(makeResource
                    (L("some greasy @x1 claws", name().toLowerCase()), RawMaterial.RESOURCE_BONE));
                for (int i = 0; i < 2; i++) {
                    resources.addElement(makeResource
                        (L("some dirty @x1 feathers", name().toLowerCase()), RawMaterial.RESOURCE_FEATHERS));
                }
                resources.addElement(makeResource
                    (L("some @x1 meat", name().toLowerCase()), RawMaterial.RESOURCE_POULTRY));
                resources.addElement(makeResource
                    (L("some @x1 blood", name().toLowerCase()), RawMaterial.RESOURCE_BLOOD));
                resources.addElement(makeResource
                    (L("a pile of @x1 bones", name().toLowerCase()), RawMaterial.RESOURCE_BONE));
            }
        }
        return resources;
    }
}
