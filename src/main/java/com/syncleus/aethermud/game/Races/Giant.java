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
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;

import java.util.List;
import java.util.Vector;


public class Giant extends StdRace {
    private final static String localizedStaticName = CMLib.lang().L("Giant");
    private final static String localizedStaticRacialCat = CMLib.lang().L("Giant-kin");
    //  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
    private static final int[] parts = {0, 2, 2, 1, 1, 2, 2, 1, 2, 2, 1, 0, 1, 1, 0, 0};
    protected static Vector<RawMaterial> resources = new Vector<RawMaterial>();
    private final int[] agingChart = {0, 1, 5, 40, 125, 188, 250, 270, 290};
    private final String[] culturalAbilityNames = {"Gigantic", "Skill_BoulderThrowing"};
    private final int[] culturalAbilityProficiencies = {100, 75};
    private final int[] culturalAbilityLevels = {0, 15};
    private final boolean[] culturalAbilityGains = {true, true};

    @Override
    public String ID() {
        return "Giant";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public int shortestMale() {
        return 84;
    }

    @Override
    public int shortestFemale() {
        return 80;
    }

    @Override
    public int heightVariance() {
        return 24;
    }

    @Override
    public int lightestWeight() {
        return 300;
    }

    @Override
    public int weightVariance() {
        return 200;
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
    public int[] bodyMask() {
        return parts;
    }

    @Override
    public int[] getAgingChart() {
        return agingChart;
    }

    @Override
    public int availabilityCode() {
        return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
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
    protected int[] culturalAbilityLevels() {
        return culturalAbilityLevels;
    }

    @Override
    protected boolean[] culturalAbilityAutoGains() {
        return culturalAbilityGains;
    }

    @Override
    public void affectCharStats(MOB affectedMOB, CharStats affectableStats) {
        super.affectCharStats(affectedMOB, affectableStats);
        affectableStats.setRacialStat(CharStats.STAT_STRENGTH, 18);
        affectableStats.setRacialStat(CharStats.STAT_DEXTERITY, 7);
        affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE, 7);
    }

    @Override
    public String arriveStr() {
        return "thunders in";
    }

    @Override
    public String leaveStr() {
        return "storms";
    }

    @Override
    public Weapon myNaturalWeapon() {
        if (naturalWeapon == null) {
            naturalWeapon = CMClass.getWeapon("StdWeapon");
            naturalWeapon.setName(L("a pair of gigantic fists"));
            naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
            naturalWeapon.setUsesRemaining(1000);
            naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
        }
        return naturalWeapon;
    }

    @Override
    public String healthText(MOB viewer, MOB mob) {
        final double pct = (CMath.div(mob.curState().getHitPoints(), mob.maxState().getHitPoints()));

        if (pct < .10)
            return L("^r@x1^r is almost fallen!^N", mob.name(viewer));
        else if (pct < .20)
            return L("^r@x1^r is covered in blood.^N", mob.name(viewer));
        else if (pct < .30)
            return L("^r@x1^r is bleeding badly from lots of large wounds.^N", mob.name(viewer));
        else if (pct < .40)
            return L("^y@x1^y has enormous bloody wounds and gashes.^N", mob.name(viewer));
        else if (pct < .50)
            return L("^y@x1^y has some huge wounds and gashes.^N", mob.name(viewer));
        else if (pct < .60)
            return L("^p@x1^p has a few huge bloody wounds.^N", mob.name(viewer));
        else if (pct < .70)
            return L("^p@x1^p has huge cuts and is heavily bruised.^N", mob.name(viewer));
        else if (pct < .80)
            return L("^g@x1^g has some large cuts and huge bruises.^N", mob.name(viewer));
        else if (pct < .90)
            return L("^g@x1^g has large bruises and scratches.^N", mob.name(viewer));
        else if (pct < .99)
            return L("^g@x1^g has a few small(?) bruises.^N", mob.name(viewer));
        else
            return L("^c@x1^c is in towering health^N", mob.name(viewer));
    }

    @Override
    public List<RawMaterial> myResources() {
        synchronized (resources) {
            if (resources.size() == 0) {
                resources.addElement(makeResource
                    (L("some @x1 hairs", name().toLowerCase()), RawMaterial.RESOURCE_FUR));
                resources.addElement(makeResource
                    (L("a strip of @x1 hide", name().toLowerCase()), RawMaterial.RESOURCE_HIDE));
                resources.addElement(makeResource
                    (L("some @x1 blood", name().toLowerCase()), RawMaterial.RESOURCE_BLOOD));
            }
        }
        return resources;
    }
}
