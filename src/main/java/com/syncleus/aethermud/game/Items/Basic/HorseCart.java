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
package com.syncleus.aethermud.game.Items.Basic;

import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.core.interfaces.Rideable;


public class HorseCart extends StdRideable {
    public HorseCart() {
        super();
        setName("a horse cart");
        setDisplayText("a large horse cart is here");
        setDescription("Looks like quite a bit can ride in there!");
        capacity = 1000;
        baseGoldValue = 500;
        basePhyStats().setWeight(500);
        setMaterial(RawMaterial.RESOURCE_OAK);
        setRideBasis(Rideable.RIDEABLE_WAGON);
        setRiderCapacity(10);
        recoverPhyStats();
    }

    @Override
    public String ID() {
        return "HorseCart";
    }

}
