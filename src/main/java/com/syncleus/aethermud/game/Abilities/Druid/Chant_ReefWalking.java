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
package com.syncleus.aethermud.game.Abilities.Druid;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Chant_ReefWalking extends Chant_PlantPass {
    private final static String localizedName = CMLib.lang().L("Reef Walking");

    @Override
    public String ID() {
        return "Chant_ReefWalking";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTGROWTH;
    }

    @Override
    protected int canAffectCode() {
        return CAN_ITEMS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    protected String getPlantsWord() {
        return "growths";
    }

    @Override
    protected boolean isAcceptableTargetRoom(MOB mob, Room newRoom) {
        if (!CMLib.flags().isWateryRoom(newRoom)) {
            mob.tell(L("You mau only travel to another a watery area with this chant."));
            return false;
        }
        return true;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (mob == null)
            return false;
        final Room R = mob.location();
        if (R == null)
            return false;
        if (!CMLib.flags().isWateryRoom(R)) {
            mob.tell(L("You must be in a watery area to do reef walking."));
            return false;
        }
        return super.invoke(mob, commands, givenTarget, auto, asLevel);
    }
}
