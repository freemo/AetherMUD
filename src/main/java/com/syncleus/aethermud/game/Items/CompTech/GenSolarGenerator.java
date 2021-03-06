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
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.SpaceObject;

import java.util.List;


public class GenSolarGenerator extends GenFuellessGenerator {
    public GenSolarGenerator() {
        super();
        setName("a solar generator");
        setDisplayText("a solar generator sits here.");
        setDescription("");
    }

    @Override
    public String ID() {
        return "GenSolarGenerator";
    }

    @Override
    protected boolean canGenerateRightNow() {
        if (activated()) {
            final Area A = CMLib.map().areaLocation(this);
            if (A instanceof SpaceShip) {
                final Room dockRoom = ((SpaceShip) A).getIsDocked();
                if (dockRoom != null)
                    return (dockRoom.getArea() != null) && (dockRoom.getArea().getClimateObj().canSeeTheSun(dockRoom));
                final SpaceObject obj = ((SpaceShip) A).getShipSpaceObject();
                final List<SpaceObject> objs = CMLib.map().getSpaceObjectsWithin(obj, obj.radius(), SpaceObject.Distance.SolarSystemDiameter.dm);
                for (final SpaceObject o : objs) {
                    if ((o instanceof Physical)
                        && (!CMLib.flags().isLightSource((Physical) o)))
                        continue;
                    if (o.radius() >= (SpaceObject.Distance.StarDRadius.dm / 2)
                        && (o.getMass() >= (o.radius() * SpaceObject.MULTIPLIER_STAR_MASS)))
                        return true;
                }
            } else if ((A != null) && (A.getClimateObj().canSeeTheSun(CMLib.map().roomLocation(this))))
                return true;
        }
        return false;
    }

    @Override
    public boolean sameAs(Environmental E) {
        if (!(E instanceof GenSolarGenerator))
            return false;
        return super.sameAs(E);
    }
}
