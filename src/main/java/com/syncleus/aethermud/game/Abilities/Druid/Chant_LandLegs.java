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
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.Races.interfaces.Race;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.Arrays;
import java.util.List;


public class Chant_LandLegs extends Chant {
    private final static String localizedName = CMLib.lang().L("Land Legs");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Land Legs)");
    protected String arriveStr = L("arrives");
    protected String leaveStr = L("leaves");
    protected int[] lastSet = null;
    protected int[] newSet = null;

    @Override
    public String ID() {
        return "Chant_LandLegs";
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
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_SELF;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_SHAPE_SHIFTING;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        super.affectCharStats(affected, affectableStats);
        if (affectableStats.getBodyPart(Race.BODY_LEG) == 0) {
            affectableStats.alterBodypart(Race.BODY_LEG, 2);
            affectableStats.alterBodypart(Race.BODY_FOOT, 2);
        }
        if (affectableStats.getBodyPart(Race.BODY_TAIL) > 0)
            affectableStats.alterBodypart(Race.BODY_TAIL, -affectableStats.getBodyPart(Race.BODY_TAIL));
        affectableStats.setArriveLeaveStr(arriveStr, leaveStr);
        final int[] breatheables = affectableStats.getBreathables();
        if (breatheables.length == 0)
            return;
        if ((lastSet != breatheables) || (newSet == null)) {
            int remove = 0;
            if (CMParms.contains(breatheables, RawMaterial.RESOURCE_SALTWATER))
                remove++;
            if (CMParms.contains(breatheables, RawMaterial.RESOURCE_FRESHWATER))
                remove++;
            boolean addAir = !CMParms.contains(breatheables, RawMaterial.RESOURCE_AIR);
            if (remove > 0) {
                newSet = Arrays.copyOf(breatheables, breatheables.length - remove + (addAir ? 1 : 0));
                for (int i = 0, ni = 0; i < breatheables.length; i++) {
                    if ((breatheables[i] != RawMaterial.RESOURCE_SALTWATER)
                        && (breatheables[i] != RawMaterial.RESOURCE_FRESHWATER))
                        newSet[ni++] = breatheables[i];
                }
                if (addAir)
                    newSet[newSet.length - 1] = RawMaterial.RESOURCE_AIR;
                Arrays.sort(newSet);
            } else
                newSet = breatheables;
            lastSet = breatheables;
        }
        affectableStats.setBreathables(newSet);
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        super.unInvoke();
        if (canBeUninvoked()) {
            if ((mob.location() != null) && (!mob.amDead()))
                mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> land legs disappear."));
            CMLib.utensils().confirmWearability(mob);
        }
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        MOB target = mob;
        if ((auto) && (givenTarget != null) && (givenTarget instanceof MOB))
            target = (MOB) givenTarget;

        if ((target.fetchEffect(this.ID()) != null)
            || (target.charStats().getBodyPart(Race.BODY_LEG) > 0)) {
            mob.tell(target, null, null, L("<S-NAME> already <S-HAS-HAVE> land legs."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, null, this, verbalCastCode(mob, null, auto), auto ? L("<T-NAME> grow(s) a pair of land legs!") : L("^S<S-NAME> chant(s) to <S-NAMESELF> and grow(s) a pair of legs!^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                success = beneficialAffect(mob, target, asLevel, 0) != null;
                target.location().recoverRoomStats();
                CMLib.utensils().confirmWearability(target);
            }
        } else
            return beneficialWordsFizzle(mob, null, L("<S-NAME> chant(s) to <S-NAMESELF>, but nothing happens"));

        // return whether it worked
        return success;
    }
}
