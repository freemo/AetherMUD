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
package com.syncleus.aethermud.game.Abilities.Prayers;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Items.interfaces.DeadBody;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Prayer_TrueResurrection extends Prayer_Resurrect {
    private final static String localizedName = CMLib.lang().L("True Resurrection");

    @Override
    public String ID() {
        return "Prayer_TrueResurrection";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_DEATHLORE;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public long flags() {
        return Ability.FLAG_HOLY;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    public Item findCorpseRoom(List<Item> candidates) {
        for (int m = 0; m < candidates.size(); m++) {
            final Item item = candidates.get(m);
            if ((item instanceof DeadBody) && (((DeadBody) item).isPlayerCorpse())) {
                Room newRoom = CMLib.map().roomLocation(item);
                if (newRoom != null)
                    return item;
            }
        }
        return null;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (givenTarget == null) {
            if (commands.size() < 1) {
                mob.tell(L("You must specify the name of a corpse within range of this magic."));
                return false;
            }
            final String corpseName = CMParms.combine(commands, 0).trim().toUpperCase();

            List<Item> candidates = CMLib.map().findRoomItems(mob.location().getArea().getProperMap(), mob, corpseName, false, 5);
            Item corpseItem = this.findCorpseRoom(candidates);
            Room newRoom = null;
            if (corpseItem != null)
                newRoom = CMLib.map().roomLocation(corpseItem);
            if (newRoom == null) {
                candidates = CMLib.map().findRoomItems(CMLib.map().rooms(), mob, corpseName, false, 5);
                corpseItem = this.findCorpseRoom(candidates);
                if (corpseItem != null)
                    newRoom = CMLib.map().roomLocation(corpseItem);
            }
            candidates.clear();
            if (newRoom == null) {
                mob.tell(L("You can't seem to fixate on a corpse called '@x1', perhaps it has decayed?", corpseName));
                return false;
            }
            givenTarget = corpseItem;
        }
        return super.invoke(mob, commands, givenTarget, auto, asLevel);
    }
}
