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
package com.planet_ink.game.Abilities.Prayers;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.CoffeeTableRow;
import com.planet_ink.game.Items.interfaces.Wearable;
import com.planet_ink.game.Libraries.interfaces.ChannelsLibrary;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;


public class Prayer_Annul extends Prayer {
    private final static String localizedName = CMLib.lang().L("Annul");

    @Override
    public String ID() {
        return "Prayer_Annul";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_NEUTRALIZATION;
    }

    @Override
    public long flags() {
        return Ability.FLAG_NEUTRAL;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_OTHERS;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;
        if (!target.isMarriedToLiege()) {
            mob.tell(L("@x1 is not married!", target.name(mob)));
            return false;
        }
        if (target.fetchItem(null, Wearable.FILTER_WORNONLY, "wedding band") != null) {
            mob.tell(L("@x1 must remove the wedding band first.", target.name(mob)));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> annul(s) the marriage between <T-NAMESELF> and @x1.^?", target.getLiegeID()));
            if (mob.location().okMessage(mob, msg)) {
                if ((!target.isMonster()) && (target.soulMate() == null))
                    CMLib.coffeeTables().bump(target, CoffeeTableRow.STAT_DIVORCES);
                mob.location().send(mob, msg);
                final List<String> channels = CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DIVORCES);
                for (int i = 0; i < channels.size(); i++)
                    CMLib.commands().postChannel(channels.get(i), mob.clans(), L("@x1 and @x2 just had their marriage annulled.", target.name(), target.getLiegeID()), true);
                final MOB M = CMLib.players().getPlayer(target.getLiegeID());
                if (M != null)
                    M.setLiegeID("");
                target.setLiegeID("");
            }
        } else
            beneficialWordsFizzle(mob, target, L("<S-NAME> clear(s) <S-HIS-HER> throat."));

        return success;
    }
}