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
package com.planet_ink.game.Items.Weapons;

import com.planet_ink.game.Items.interfaces.RawMaterial;
import com.planet_ink.game.Items.interfaces.Weapon;


public class Sickle extends StdWeapon {
    public Sickle() {
        super();

        setName("a sickle");
        setDisplayText("a sickle lies on the ground.");
        setDescription("A long and very curvy blade attached to a wooden handle.");
        basePhyStats().setAbility(0);
        basePhyStats().setLevel(0);
        basePhyStats.setWeight(3);
        basePhyStats().setAttackAdjustment(0);
        basePhyStats().setDamage(5);
        baseGoldValue = 1;
        recoverPhyStats();
        material = RawMaterial.RESOURCE_OAK;
        weaponDamageType = TYPE_PIERCING;
        weaponClassification = Weapon.CLASS_EDGED;
    }

    @Override
    public String ID() {
        return "Sickle";
    }

}