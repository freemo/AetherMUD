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
package com.syncleus.aethermud.game.Abilities.Specializations;

import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;


public class Specialization_Ranged extends Specialization_Weapon {
    private final static String localizedName = CMLib.lang().L("Ranged Weapon Specialization");

    public Specialization_Ranged() {
        super();
        weaponClass = Weapon.CLASS_RANGED;
        secondWeaponClass = Weapon.CLASS_THROWN;
    }

    @Override
    public String ID() {
        return "Specialization_Ranged";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        if ((activated)
            && (CMLib.dice().rollPercentage() < 25)
            && (affected instanceof MOB)
            && (msg.amISource((MOB) affected))
            && (msg.sourceMinor() == CMMsg.TYP_THROW)
            && (msg.tool() instanceof Item)
            && (msg.target() instanceof MOB))
            helpProficiency((MOB) affected, 0);
        super.executeMsg(myHost, msg);
    }
}
