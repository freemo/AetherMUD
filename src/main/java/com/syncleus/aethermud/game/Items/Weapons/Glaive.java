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
package com.syncleus.aethermud.game.Items.Weapons;

import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;


public class Glaive extends StdWeapon {
    public Glaive() {
        super();

        setName("a heavy glaive");
        setDisplayText("a glaive leans against the wall.");
        setDescription("A long blade on a pole.");
        basePhyStats().setAbility(0);
        basePhyStats().setLevel(0);
        basePhyStats.setWeight(8);
        basePhyStats().setAttackAdjustment(0);
        basePhyStats().setDamage(6);
        weaponDamageType = TYPE_SLASHING;
        baseGoldValue = 6;
        recoverPhyStats();
        material = RawMaterial.RESOURCE_STEEL;
        wornLogicalAnd = true;
        properWornBitmap = Wearable.WORN_HELD | Wearable.WORN_WIELD;
        weaponClassification = Weapon.CLASS_POLEARM;
    }

    @Override
    public String ID() {
        return "Glaive";
    }

}
