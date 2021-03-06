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
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.Races.interfaces.Race;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Prayer_GodLight extends Prayer {
    private final static String localizedName = CMLib.lang().L("Godlight");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Godlight)");

    @Override
    public String ID() {
        return "Prayer_GodLight";
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
    public long flags() {
        return Ability.FLAG_HOLY;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_CREATION;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        if (affected == null)
            return;
        affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_LIGHTSOURCE);
        affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_GLOWING);
        if (CMath.bset(affectableStats.disposition(), PhyStats.IS_DARK))
            affectableStats.setDisposition(affectableStats.disposition() - PhyStats.IS_DARK);
        if (!(affected instanceof MOB))
            return;
        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_NOT_SEE);
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();

        if (canBeUninvoked())
            mob.tell(L("Your vision returns."));
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            if (mob.isInCombat()) {
                if (CMLib.flags().isInDark(mob.location()))
                    return Ability.QUALITY_INDIFFERENT;
                if (target instanceof MOB) {
                    if (((MOB) target).charStats().getBodyPart(Race.BODY_EYE) == 0)
                        return Ability.QUALITY_INDIFFERENT;
                    if (!CMLib.flags().canSee((MOB) target))
                        return Ability.QUALITY_INDIFFERENT;
                }
            }
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {

        Physical target = null;
        if ((!auto)
            && ((commands.size() == 0) || ((commands.get(0)).equalsIgnoreCase("ROOM")))
            && (!mob.isInCombat()))
            target = mob.location();
        else {
            if ((commands.size() == 0) && (mob.isInCombat()))
                target = mob.getVictim();
            if (target == null)
                target = getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_UNWORNONLY);
        }
        if (target == null)
            return false;
        if ((target instanceof Room) && (target.fetchEffect(ID()) != null)) {
            mob.tell(L("This place already has the god light."));
            return false;
        }

        if ((target instanceof MOB)
            && (((MOB) target).charStats().getBodyPart(Race.BODY_EYE) == 0)) {
            mob.tell(L("@x1 has no eyes, and would not be affected.", target.name(mob)));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            if (target instanceof Room)
                mob.phyStats().setSensesMask(mob.phyStats().sensesMask() | PhyStats.CAN_SEE_DARK);
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto) | CMMsg.MASK_MALICIOUS, auto ? "" : ((target instanceof MOB) ? "^S<S-NAME> point(s) to <T-NAMESELF> and " + prayWord(mob) + ". A beam of bright sunlight flashes into <T-HIS-HER> eyes!^?" : "^S<S-NAME> point(s) at <T-NAMESELF> and " + prayWord(mob) + ".^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.recoverPhyStats();
                mob.location().send(mob, msg);
                if (msg.value() <= 0) {
                    if (target instanceof MOB)
                        mob.location().show((MOB) target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> go(es) blind!"));
                    maliciousAffect(mob, target, asLevel, 0, -1);
                    mob.location().recoverRoomStats();
                    mob.location().recoverRoomStats();
                }
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> point(s) at <T-NAMESELF> and @x1, but nothing happens.", prayWord(mob)));

        // return whether it worked
        return success;
    }
}
