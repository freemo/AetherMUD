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
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Prayer_FountainLife extends Prayer {
    private final static String localizedName = CMLib.lang().L("Fountain of Life");
    protected Room SpringLocation = null;
    protected Item littleSpring = null;

    @Override
    public String ID() {
        return "Prayer_FountainLife";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_CREATION;
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
    protected int canAffectCode() {
        return Ability.CAN_ITEMS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    protected int overrideMana() {
        return Ability.COST_ALL;
    }

    @Override
    public void unInvoke() {
        if (SpringLocation == null)
            return;
        if (littleSpring == null)
            return;
        if (canBeUninvoked())
            SpringLocation.showHappens(CMMsg.MSG_OK_VISUAL, L("The fountain of life dries up."));
        super.unInvoke();
        if (canBeUninvoked()) {
            final Item spring = littleSpring; // protects against uninvoke loops!
            littleSpring = null;
            spring.destroy();
            SpringLocation.recoverRoomStats();
            SpringLocation = null;
        }
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        // now see if it worked
        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, null, this, verbalCastCode(mob, null, auto), auto ? "" : L("^S<S-NAME> @x1 for the fountain of life.^?", prayWord(mob)));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final String itemID = "LifeFountain";

                final Item newItem = CMClass.getMiscMagic(itemID);

                if (newItem == null) {
                    mob.tell(L("There's no such thing as a '@x1'.\n\r", itemID));
                    return false;
                }

                mob.location().addItem(newItem);
                mob.location().showHappens(CMMsg.MSG_OK_ACTION, L("Suddenly, @x1 starts flowing here.", newItem.name()));
                SpringLocation = mob.location();
                littleSpring = newItem;
                beneficialAffect(mob, newItem, asLevel, 0);
                mob.location().recoverPhyStats();
            }
        } else
            return beneficialWordsFizzle(mob, null, L("<S-NAME> @x1 for a fountain of life, but there is no answer.", prayWord(mob)));

        // return whether it worked
        return success;
    }
}
