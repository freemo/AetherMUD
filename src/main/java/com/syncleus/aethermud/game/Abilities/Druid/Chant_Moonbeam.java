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
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Chant_Moonbeam extends Chant {
    private final static String localizedName = CMLib.lang().L("Moonbeam");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Moonbeam)");

    @Override
    public String ID() {
        return "Chant_Moonbeam";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return localizedStaticDisplay;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_MOONSUMMONING;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_SELF;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        if (!(affected instanceof Room))
            affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_LIGHTSOURCE);
        if (CMLib.flags().isInDark(affected))
            affectableStats.setDisposition(affectableStats.disposition() - PhyStats.IS_DARK);
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        final Room room = ((MOB) affected).location();
        if (canBeUninvoked())
            room.show(mob, null, CMMsg.MSG_OK_VISUAL, L("The moonbeam shining down from above <S-NAME> dims."));
        super.unInvoke();
        room.recoverRoomStats();
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            if (!CMLib.flags().canBeSeenBy(mob.location(), mob))
                return super.castingQuality(mob, target, Ability.QUALITY_BENEFICIAL_SELF);
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        MOB target = mob;
        if ((auto) && (givenTarget != null) && (givenTarget instanceof MOB))
            target = (MOB) givenTarget;

        if (target.fetchEffect(this.ID()) != null) {
            target.tell(L("The moonbeam is already with you."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (!success) {
            return beneficialWordsFizzle(mob, mob.location(), L("<S-NAME> chant(s) for a moonbeam, but fail(s)."));
        }

        final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? L("A moonbeam begin(s) to follow <T-NAME> around!") : L("^S<S-NAME> chant(s), causing a moonbeam to follow <S-HIM-HER> around!^?"));
        if (mob.location().okMessage(mob, msg)) {
            mob.location().send(mob, msg);
            beneficialAffect(mob, target, asLevel, 0);
            target.location().recoverRoomStats(); // attempt to handle followers
        }

        return success;
    }
}
