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
package com.syncleus.aethermud.game.Items.Armor;

import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;


public class LeatherBoots extends StdArmor {
    public LeatherBoots() {
        super();

        setName("a pair of leather boots");
        setDisplayText("a pair of leather boots sits here.");
        setDescription("They look like a rather nice pair of footwear.");
        properWornBitmap = Wearable.WORN_FEET;
        wornLogicalAnd = false;
        basePhyStats().setArmor(1);
        basePhyStats().setWeight(1);
        basePhyStats().setAbility(0);
        baseGoldValue = 5;
        recoverPhyStats();
        material = RawMaterial.RESOURCE_LEATHER;
    }

    @Override
    public String ID() {
        return "LeatherBoots";
    }

}
