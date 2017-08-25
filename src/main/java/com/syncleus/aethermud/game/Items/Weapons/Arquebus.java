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


public class Arquebus extends StdWeapon {
    public Arquebus() {
        super();

        setName("an arquebus");
        setDisplayText("an arquebus is on the ground.");
        setDescription("It\\`s got a metal barrel and wooden stock.");
        basePhyStats().setAbility(0);
        basePhyStats().setLevel(0);
        basePhyStats.setWeight(15);

        basePhyStats().setAttackAdjustment(-1);
        basePhyStats().setDamage(10);

        setAmmunitionType("bullets");
        setAmmoCapacity(1);
        setAmmoRemaining(1);
        minRange = 0;
        maxRange = 5;
        baseGoldValue = 500;
        recoverPhyStats();
        wornLogicalAnd = true;
        material = RawMaterial.RESOURCE_IRON;
        properWornBitmap = Wearable.WORN_HELD | Wearable.WORN_WIELD;
        weaponClassification = Weapon.CLASS_RANGED;
        weaponDamageType = Weapon.TYPE_PIERCING;
    }

    @Override
    public String ID() {
        return "Arquebus";
    }

//	protected boolean isBackfire()
//	{
//
//	}

}
