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
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.AetherTableRow;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.Libraries.interfaces.ChannelsLibrary;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Prayer_Marry extends Prayer {
    private final static String localizedName = CMLib.lang().L("Marry");

    @Override
    public String ID() {
        return "Prayer_Marry";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public long flags() {
        return Ability.FLAG_HOLY;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_OTHERS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_BLESSING;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (commands.size() < 2) {
            mob.tell(L("Whom to whom?"));
            return false;
        }
        final String name1 = commands.get(commands.size() - 1);
        final String name2 = CMParms.combine(commands, 0, commands.size() - 1);
        MOB husband = mob.location().fetchInhabitant(name1);
        if ((husband == null) || (!CMLib.flags().canBeSeenBy(mob, husband))) {
            mob.tell(L("You don't see @x1 here!", name1));
            return false;
        }
        MOB wife = mob.location().fetchInhabitant(name2);
        if ((wife == null) || (!CMLib.flags().canBeSeenBy(mob, wife))) {
            mob.tell(L("You don't see @x1 here!", name2));
            return false;
        }
        if (wife.charStats().getStat(CharStats.STAT_GENDER) == 'M') {
            final MOB M = wife;
            wife = husband;
            husband = M;
        }
        if (wife.isMarriedToLiege()) {
            mob.tell(L("@x1 is already married!!", wife.name()));
            return false;
        }
        if (husband.isMarriedToLiege()) {
            mob.tell(L("@x1 is already married!!", husband.name()));
            return false;
        }
        if (wife.getLiegeID().length() > 0) {
            mob.tell(L("@x1 is lieged to @x2, and cannot marry.", wife.name(), wife.getLiegeID()));
            return false;
        }
        if (husband.getLiegeID().length() > 0) {
            mob.tell(L("@x1 is lieged to @x2, and cannot marry.", husband.name(), husband.getLiegeID()));
            return false;
        }
        if ((wife.isMonster()) || (wife.playerStats() == null)) {
            mob.tell(L("@x1 must be a player to marry.", wife.name()));
            return false;
        }
        if ((husband.isMonster()) || (husband.playerStats() == null)) {
            mob.tell(L("@x1 must be a player to marry.", husband.name()));
            return false;
        }
        CMLib.aetherTables().bump(husband, AetherTableRow.STAT_BIRTHS);
        Item I = husband.fetchItem(null, Wearable.FILTER_WORNONLY, "wedding band");
        if (I == null) {
            mob.tell(L("@x1 isn't wearing a wedding band!", husband.name()));
            return false;
        }
        I = wife.fetchItem(null, Wearable.FILTER_WORNONLY, "wedding band");
        if (I == null) {
            mob.tell(L("@x1 isn't wearing a wedding band!", wife.name()));
            return false;
        }
        MOB witness = null;
        for (int i = 0; i < mob.location().numInhabitants(); i++) {
            final MOB M = mob.location().fetchInhabitant(i);
            if ((M != null)
                && (M != mob)
                && (M != husband)
                && (M != wife))
                witness = M;
        }
        if (witness == null) {
            mob.tell(L("You need a witness present."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, null, this, verbalCastCode(mob, null, auto), auto ? "" : L("^S<S-NAME> @x1 to bless the holy union between @x2 and @x3.^?", prayForWord(mob), husband.name(), wife.name()));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                husband.setLiegeID(wife.Name());
                wife.setLiegeID(husband.Name());
                CMLib.aetherTables().bump(husband, AetherTableRow.STAT_MARRIAGES);
                CMLib.commands().postSay(mob, husband, L("You may kiss your bride!"), false, false);
                final List<String> channels = CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.MARRIAGES);
                for (int i = 0; i < channels.size(); i++)
                    CMLib.commands().postChannel(channels.get(i), husband.clans(), L("@x1 and @x2 were just joined in holy matrimony!", husband.name(), wife.name()), true);
            }
        } else
            beneficialWordsFizzle(mob, null, L("<S-NAME> start(s) 'Dearly beloved', and then clear(s) <S-HIS-HER> throat."));

        return success;
    }
}
