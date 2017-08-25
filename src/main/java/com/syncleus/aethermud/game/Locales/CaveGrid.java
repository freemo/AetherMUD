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
package com.planet_ink.game.Locales;

import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.core.interfaces.Places;

import java.util.List;


public class CaveGrid extends StdGrid {
    public CaveGrid() {
        super();
        basePhyStats().setDisposition(basePhyStats().disposition() | PhyStats.IS_DARK);
        recoverPhyStats();
        climask = Places.CLIMASK_NORMAL;
    }

    @Override
    public String ID() {
        return "CaveGrid";
    }

    @Override
    public int domainType() {
        return Room.DOMAIN_INDOORS_CAVE;
    }

    @Override
    public int maxRange() {
        return 5;
    }

    @Override
    public String getGridChildLocaleID() {
        return "CaveRoom";
    }

    @Override
    public List<Integer> resourceChoices() {
        return CaveRoom.roomResources;
    }
}