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
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;

import java.util.List;
import java.util.Vector;


public class Whale extends GiantFish {
    private final static String localizedStaticName = CMLib.lang().L("Whale");
    private final static String localizedStaticRacialCat = CMLib.lang().L("Sea Mammal");
    //  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
    private static final int[] parts = {0, 2, 0, 1, 0, 0, 0, 1, 0, 0, 0, 2, 1, 0, 1, 0};
    protected static Vector<RawMaterial> resources = new Vector<RawMaterial>();
    private final int[] agingChart = {0, 1, 3, 15, 35, 53, 70, 74, 78};

    @Override
    public String ID() {
        return "Whale";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public int shortestMale() {
        return 80;
    }

    @Override
    public int shortestFemale() {
        return 75;
    }

    @Override
    public int heightVariance() {
        return 20;
    }

    @Override
    public int lightestWeight() {
        return 21955;
    }

    @Override
    public int weightVariance() {
        return 2405;
    }

    @Override
    public long forbiddenWornBits() {
        return ~(Wearable.WORN_EYES);
    }

    @Override
    public String racialCategory() {
        return localizedStaticRacialCat;
    }

    @Override
    public int[] getBreathables() {
        return breatheAirWaterArray;
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
        //super.affectCharStats(affectedMOB, affectableStats);
        affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE, 1);
        affectableStats.setRacialStat(CharStats.STAT_STRENGTH, 22);
        affectableStats.setRacialStat(CharStats.STAT_DEXTERITY, 3);
    }

    @Override
    public String arriveStr() {
        return "swims in";
    }

    @Override
    public String leaveStr() {
        return "swims";
    }

    @Override
    public Weapon myNaturalWeapon() {
        if (naturalWeapon == null) {
            naturalWeapon = CMClass.getWeapon("StdWeapon");
            naturalWeapon.setName(L("a deadly maw"));
            naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
            naturalWeapon.setUsesRemaining(1000);
            naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
        }
        return naturalWeapon;
    }

    @Override
    public List<RawMaterial> myResources() {
        synchronized (resources) {
            if (resources.size() == 0) {
                for (int i = 0; i < 125; i++) {
                    resources.addElement(makeResource
                        (L("some @x1", name().toLowerCase()), RawMaterial.RESOURCE_FISH));
                }
                for (int i = 0; i < 115; i++) {
                    resources.addElement(makeResource
                        (L("a slippery @x1 hide", name().toLowerCase()), RawMaterial.RESOURCE_HIDE));
                }
                resources.addElement(makeResource
                    (L("some @x1 blood", name().toLowerCase()), RawMaterial.RESOURCE_BLOOD));
            }
        }
        return resources;
    }
}
