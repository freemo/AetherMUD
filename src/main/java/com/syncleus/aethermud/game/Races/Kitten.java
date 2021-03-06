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

import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.Races.interfaces.Race;
import com.syncleus.aethermud.game.core.CMLib;

import java.util.List;
import java.util.Vector;

public class Kitten extends Cat {
    private final static String localizedStaticName = CMLib.lang().L("Kitten");
    private final static String localizedStaticRacialCat = CMLib.lang().L("Feline");
    //  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
    private static final int[] parts = {0, 2, 2, 1, 1, 0, 0, 1, 4, 4, 1, 0, 1, 1, 1, 0};
    protected static Vector<RawMaterial> resources = new Vector<RawMaterial>();

    @Override
    public String ID() {
        return "Kitten";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public int shortestMale() {
        return 4;
    }

    @Override
    public int shortestFemale() {
        return 4;
    }

    @Override
    public int heightVariance() {
        return 3;
    }

    @Override
    public int lightestWeight() {
        return 7;
    }

    @Override
    public int weightVariance() {
        return 10;
    }

    @Override
    public long forbiddenWornBits() {
        return ~(Wearable.WORN_HEAD | Wearable.WORN_FEET | Wearable.WORN_EARS | Wearable.WORN_EYES);
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
    public List<RawMaterial> myResources() {
        synchronized (resources) {
            if (resources.size() == 0) {
                resources.addElement(makeResource
                    (L("a @x1 hide", name().toLowerCase()), RawMaterial.RESOURCE_FUR));
                resources.addElement(makeResource
                    (L("some @x1 blood", name().toLowerCase()), RawMaterial.RESOURCE_BLOOD));
            }
        }
        return resources;
    }

    @Override
    public String makeMobName(char gender, int age) {
        switch (age) {
            case Race.AGE_INFANT:
            case Race.AGE_TODDLER:
            case Race.AGE_CHILD:
                switch (gender) {
                    case 'M':
                    case 'm':
                        return "boy kitten";
                    case 'F':
                    case 'f':
                        return "girl kitten";
                    default:
                        return "kitten";
                }
            case Race.AGE_YOUNGADULT:
            case Race.AGE_MATURE:
            case Race.AGE_MIDDLEAGED:
            default:
                switch (gender) {
                    case 'M':
                    case 'm':
                        return "male cat";
                    case 'F':
                    case 'f':
                        return "female cat";
                    default:
                        return "cat";
                }
            case Race.AGE_OLD:
            case Race.AGE_VENERABLE:
            case Race.AGE_ANCIENT:
                switch (gender) {
                    case 'M':
                    case 'm':
                        return "old male cat";
                    case 'F':
                    case 'f':
                        return "old female cat";
                    default:
                        return "old cat";
                }
        }
    }
}
