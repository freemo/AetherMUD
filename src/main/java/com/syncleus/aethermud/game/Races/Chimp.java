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
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;

import java.util.List;
import java.util.Vector;


public class Chimp extends Monkey {
    private final static String localizedStaticName = CMLib.lang().L("Chimp");
    private final static String localizedStaticRacialCat = CMLib.lang().L("Primate");
    //  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
    private static final int[] parts = {0, 2, 2, 1, 1, 2, 2, 1, 2, 2, 1, 0, 1, 1, 1, 0};
    protected static Vector<RawMaterial> resources = new Vector<RawMaterial>();
    private final String[] racialAbilityNames = {"ChimpSpeak"};
    private final int[] racialAbilityLevels = {1};
    private final int[] racialAbilityProficiencies = {100};
    private final boolean[] racialAbilityQuals = {false};
    private final String[] racialAbilityParms = {""};

    @Override
    public String ID() {
        return "Chimp";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public int shortestMale() {
        return 36;
    }

    @Override
    public int shortestFemale() {
        return 34;
    }

    @Override
    public int heightVariance() {
        return 8;
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
    public String racialCategory() {
        return localizedStaticRacialCat;
    }

    @Override
    protected String[] racialAbilityNames() {
        return racialAbilityNames;
    }

    @Override
    protected int[] racialAbilityLevels() {
        return racialAbilityLevels;
    }

    @Override
    protected int[] racialAbilityProficiencies() {
        return racialAbilityProficiencies;
    }

    @Override
    protected boolean[] racialAbilityQuals() {
        return racialAbilityQuals;
    }

    @Override
    public String[] racialAbilityParms() {
        return racialAbilityParms;
    }

    @Override
    public int[] bodyMask() {
        return parts;
    }

    @Override
    public void affectCharStats(MOB affectedMOB, CharStats affectableStats) {
        super.affectCharStats(affectedMOB, affectableStats);
        affectableStats.setRacialStat(CharStats.STAT_STRENGTH, 15);
        affectableStats.setRacialStat(CharStats.STAT_DEXTERITY, 15);
        affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE, 1);
    }

    @Override
    public List<RawMaterial> myResources() {
        synchronized (resources) {
            if (resources.size() == 0) {
                resources.addElement(makeResource
                    (L("a @x1 hide", name().toLowerCase()), RawMaterial.RESOURCE_FUR));
                resources.addElement(makeResource
                    (L("some @x1 toes", name().toLowerCase()), RawMaterial.RESOURCE_HIDE));
                resources.addElement(makeResource
                    (L("a pound of @x1 flesh", name().toLowerCase()), RawMaterial.RESOURCE_MEAT));
                resources.addElement(makeResource
                    (L("some @x1 blood", name().toLowerCase()), RawMaterial.RESOURCE_BLOOD));
                resources.addElement(makeResource
                    (L("a pile of @x1 bones", name().toLowerCase()), RawMaterial.RESOURCE_BONE));
            }
        }
        return resources;
    }
}

