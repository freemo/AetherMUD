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
package com.syncleus.aethermud.game.Abilities.Skills;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMStrings;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.MUDCmdProcessor;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Skill_Haggle extends StdSkill {
    private final static String localizedName = CMLib.lang().L("Haggle");
    private static final String[] triggerStrings = I(new String[]{"HAGGLE"});

    @Override
    public String ID() {
        return "Skill_Haggle";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_INFLUENTIAL;
    }

    @Override
    public void affectCharStats(MOB affectedMOB, CharStats affectableStats) {
        super.affectCharStats(affectedMOB, affectableStats);
        affectableStats.setStat(CharStats.STAT_CHARISMA, affectableStats.getStat(CharStats.STAT_CHARISMA) + 10 + getXLEVELLevel(invoker()));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        String cmd = "";
        if (commands.size() > 0)
            cmd = (commands.get(0)).toUpperCase();

        if ((commands.size() < 2) || ((!cmd.equals("BUY") && (!cmd.equals("SELL"))))) {
            mob.tell(L("You must specify BUY, SELL, an item, and possibly a ShopKeeper (unless it is implied)."));
            return false;
        }

        final Environmental shopkeeper = CMLib.english().parseShopkeeper(mob, commands, CMStrings.capitalizeAndLower(cmd) + " what to whom?");
        if (shopkeeper == null)
            return false;
        if (commands.size() == 0) {
            mob.tell(L("@x1 what?", CMStrings.capitalizeAndLower(cmd)));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, shopkeeper, this, CMMsg.MSG_SPEAK, auto ? "" : L("<S-NAME> haggle(s) with <T-NAMESELF>."));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                invoker = mob;
                mob.addEffect(this);
                mob.recoverCharStats();
                commands.add(0, CMStrings.capitalizeAndLower(cmd));
                mob.doCommand(commands, MUDCmdProcessor.METAFLAG_FORCED);
                commands.add(shopkeeper.name());
                mob.delEffect(this);
                mob.recoverCharStats();
            }
        } else
            beneficialWordsFizzle(mob, shopkeeper, L("<S-NAME> haggle(s) with <T-NAMESELF>, but <S-IS-ARE> unconvincing."));

        // return whether it worked
        return success;
    }
}
