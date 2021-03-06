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
package com.syncleus.aethermud.game.Items.CompTech;

import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Items.interfaces.SpaceShip;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.SpaceObject;


public class GenBussardCollector extends GenFuellessGenerator {
    public GenBussardCollector() {
        super();
        setName("a bussard collector generator");
        setDisplayText("a bussard collector generator sits here.");
        setDescription("");
    }

    @Override
    public String ID() {
        return "GenBussardCollector";
    }

    @Override
    protected boolean canGenerateRightNow() {
        final Area A = CMLib.map().areaLocation(this);
        if (A instanceof SpaceShip) {
            return ((SpaceShip) A).speed() > SpaceObject.VELOCITY_SUBLIGHT;
        }
        return false;
    }

    @Override
    public boolean sameAs(Environmental E) {
        if (!(E instanceof GenBussardCollector))
            return false;
        return super.sameAs(E);
    }
}
