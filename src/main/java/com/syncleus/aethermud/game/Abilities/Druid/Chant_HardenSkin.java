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
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Chant_HardenSkin extends Chant {
    private final static String localizedName = CMLib.lang().L("Harden Skin");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Harden Skin)");

    @Override
    public String ID() {
        return "Chant_HardenSkin";
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
        return Ability.ACODE_CHANT | Ability.DOMAIN_SHAPE_SHIFTING;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_SELF;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        affectableStats.setArmor(affectableStats.armor() - 10 - (2 * getXLEVELLevel(invoker())));
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        super.affectCharStats(affected, affectableStats);
        final int bonus = (2 * getXLEVELLevel(invoker()));
        affectableStats.setStat(CharStats.STAT_SAVE_COLD, affectableStats.getStat(CharStats.STAT_SAVE_COLD) + 10 + affected.phyStats().level() + bonus);
        affectableStats.setStat(CharStats.STAT_SAVE_FIRE, affectableStats.getStat(CharStats.STAT_SAVE_FIRE) + affected.phyStats().level() + bonus);
        affectableStats.setStat(CharStats.STAT_SAVE_WATER, affectableStats.getStat(CharStats.STAT_SAVE_WATER) + 25 + affected.phyStats().level() + bonus);
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        super.unInvoke();
        if (canBeUninvoked())
            if ((mob.location() != null) && (!mob.amDead()))
                mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> skin softens."));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        MOB target = mob;
        if ((auto) && (givenTarget != null) && (givenTarget instanceof MOB))
            target = (MOB) givenTarget;

        if (target.fetchEffect(this.ID()) != null) {
            mob.tell(target, null, null, L("<S-YOUPOSS> skin is already hard."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? L("<T-YOUPOSS> skin hardens!") : L("^S<S-NAME> chant(s) to <T-NAMESELF> and <T-HIS-HER> skin hardens!^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                success = beneficialAffect(mob, target, asLevel, 0) != null;
                target.location().recoverRoomStats();
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens"));

        // return whether it worked
        return success;
    }
}
