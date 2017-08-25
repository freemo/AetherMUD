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
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.Armor;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;
import java.util.Vector;

public class Hobbit extends StdRace {
    private final static String localizedStaticName = CMLib.lang().L("Hobbit");
    private final static String localizedStaticRacialCat = CMLib.lang().L("Halfling");
    //  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
    private static final int[] parts = {0, 2, 2, 1, 1, 2, 2, 1, 2, 2, 1, 0, 1, 1, 0, 0};
    protected static Vector<RawMaterial> resources = new Vector<RawMaterial>();
    private final String[] culturalAbilityNames = {"Fighter_RapidShot", "Cooking", "Farming"};
    private final int[] culturalAbilityProficiencies = {25, 75, 25};
    private final int[] agingChart = {0, 1, 4, 20, 50, 75, 100, 110, 120};

    @Override
    public String ID() {
        return "Hobbit";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public int availabilityCode() {
        return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
    }

    @Override
    public int shortestMale() {
        return 40;
    }

    @Override
    public int shortestFemale() {
        return 36;
    }

    @Override
    public int heightVariance() {
        return 6;
    }

    @Override
    public int lightestWeight() {
        return 80;
    }

    @Override
    public int weightVariance() {
        return 50;
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
    public int[] getAgingChart() {
        return agingChart;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_SEE_INFRARED);
    }

    @Override
    public void affectCharStats(MOB affectedMOB, CharStats affectableStats) {
        super.affectCharStats(affectedMOB, affectableStats);
        affectableStats.setStat(CharStats.STAT_DEXTERITY, affectableStats.getStat(CharStats.STAT_DEXTERITY) + 1);
        affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ, affectableStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ) + 1);
        affectableStats.setStat(CharStats.STAT_STRENGTH, affectableStats.getStat(CharStats.STAT_STRENGTH) - 1);
        affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ, affectableStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ) - 1);
        affectableStats.setStat(CharStats.STAT_SAVE_PARALYSIS, affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS) + 10);
        affectableStats.setStat(CharStats.STAT_SAVE_DETECTION, affectableStats.getStat(CharStats.STAT_SAVE_DETECTION) + 10);
    }

    @Override
    public List<Item> outfit(MOB myChar) {
        if (outfitChoices == null) {
            // Have to, since it requires use of special constructor
            final Armor s1 = CMClass.getArmor("GenShirt");
            if (s1 == null)
                return new Vector<Item>();
            outfitChoices = new Vector<Item>();
            s1.setName(L("a small tunic"));
            s1.setDisplayText(L("a small tunic is folded neatly here."));
            s1.setDescription(L("It is a small but nicely made button-up tunic."));
            s1.text();
            outfitChoices.add(s1);
            final Armor p1 = CMClass.getArmor("GenPants");
            p1.setName(L("some small pants"));
            p1.setDisplayText(L("some small pants lie here."));
            p1.setDescription(L("They appear to be for a dimunitive person, and extend barely past the knee at that."));
            p1.text();
            outfitChoices.add(p1);
            final Armor s3 = CMClass.getArmor("GenBelt");
            outfitChoices.add(s3);
        }
        return outfitChoices;
    }

    @Override
    public Weapon myNaturalWeapon() {
        return funHumanoidWeapon();
    }

    @Override
    public String healthText(MOB viewer, MOB mob) {
        final double pct = (CMath.div(mob.curState().getHitPoints(), mob.maxState().getHitPoints()));

        if (pct < .10)
            return L("^r@x1^r has very little life left.^N", mob.name(viewer));
        else if (pct < .20)
            return L("^r@x1^r is covered in small streams of blood.^N", mob.name(viewer));
        else if (pct < .30)
            return L("^r@x1^r is bleeding badly from lots of small wounds.^N", mob.name(viewer));
        else if (pct < .40)
            return L("^y@x1^y has numerous bloody wounds and small gashes.^N", mob.name(viewer));
        else if (pct < .50)
            return L("^y@x1^y has some bloody wounds and small gashes.^N", mob.name(viewer));
        else if (pct < .60)
            return L("^p@x1^p has a few small bloody wounds.^N", mob.name(viewer));
        else if (pct < .70)
            return L("^p@x1^p is cut and bruised in small places.^N", mob.name(viewer));
        else if (pct < .80)
            return L("^g@x1^g has some small cuts and bruises.^N", mob.name(viewer));
        else if (pct < .90)
            return L("^g@x1^g has a few bruises and small scratches.^N", mob.name(viewer));
        else if (pct < .99)
            return L("^g@x1^g has a few small bruises.^N", mob.name(viewer));
        else
            return L("^c@x1^c is in perfect health.^N", mob.name(viewer));
    }

    @Override
    public List<RawMaterial> myResources() {
        synchronized (resources) {
            if (resources.size() == 0) {
                resources.addElement(makeResource
                    (L("a pair of hairy @x1 feet", name().toLowerCase()), RawMaterial.RESOURCE_MEAT));
                resources.addElement(makeResource
                    (L("some @x1 blood", name().toLowerCase()), RawMaterial.RESOURCE_BLOOD));
                resources.addElement(makeResource
                    (L("a pile of @x1 bones", name().toLowerCase()), RawMaterial.RESOURCE_BONE));
            }
        }
        return resources;
    }
}
