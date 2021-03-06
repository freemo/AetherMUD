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
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.*;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Rideable;

import java.util.List;


public class Skill_Swim extends StdSkill {
    private final static String localizedName = CMLib.lang().L("Swim");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Swimming)");
    private static final String[] triggerStrings = I(new String[]{"SWIM"});

    @Override
    public String ID() {
        return "Skill_Swim";
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
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
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
        return Ability.ACODE_SKILL | Ability.DOMAIN_FITNESS;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT;
    }

    @Override
    public double castingTime(final MOB mob, final List<String> cmds) {
        return CMProps.getSkillActionCost(ID(), CMath.greater(CMath.div(CMProps.getIntVar(CMProps.Int.DEFABLETIME), 50.0), 1.0));
    }

    @Override
    public double combatCastingTime(final MOB mob, final List<String> cmds) {
        return CMProps.getSkillCombatActionCost(ID(), CMath.greater(CMath.div(CMProps.getIntVar(CMProps.Int.DEFCOMABLETIME), 50.0), 1.0));
    }

    public boolean placeToSwim(Room r2) {
        if ((r2 == null)
            || (!CMLib.flags().isWateryRoom(r2)))
            return false;
        return true;
    }

    public boolean placeToSwim(Environmental E) {
        return placeToSwim(CMLib.map().roomLocation(E));
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_SWIMMING);
    }

    @Override
    public int[] usageCost(MOB mob, boolean ignoreClassOverride) {
        int[] cost = super.usageCost(mob, ignoreClassOverride);
        if ((mob != null) && (mob.isRacialAbility(ID())))
            return new int[cost.length];
        return cost;
    }

    @Override
    public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining) {
        if (secondsElapsed == 0) {
            final int dirCode = CMLib.directions().getDirectionCode(CMParms.combine(commands, 0));
            if (dirCode < 0) {
                mob.tell(L("Swim where?"));
                return false;
            }
            final Room r = mob.location().getRoomInDir(dirCode);
            if (CMLib.flags().isFloatingFreely(mob)) {
                // swimming in no grav is OK
            } else if (!placeToSwim(mob.location())) {
                if (!placeToSwim(r)) {
                    mob.tell(L("There is no water to swim on that way."));
                    return false;
                }
            } else if ((r != null)
                && (r.domainType() == Room.DOMAIN_OUTDOORS_AIR)
                && (r.domainType() == Room.DOMAIN_INDOORS_AIR)) {
                mob.tell(L("There is no water to swim on that way."));
                return false;
            }

            if ((mob.riding() != null)
                && (mob.riding().rideBasis() != Rideable.RIDEABLE_WATER)
                && (mob.riding().rideBasis() != Rideable.RIDEABLE_AIR)) {
                mob.tell(L("You need to get off @x1 first!", mob.riding().name()));
                return false;
            }
            final CMMsg msg = CMClass.getMsg(mob, null, this, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> start(s) swimming @x1.", CMLib.directions().getDirectionName(dirCode)));
            final Room R = mob.location();
            if ((R != null) && (R.okMessage(mob, msg)))
                R.send(mob, msg);
            else
                return false;
        }
        return true;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final int dirCode = CMLib.directions().getDirectionCode(CMParms.combine(commands, 0));
        if (!preInvoke(mob, commands, givenTarget, auto, asLevel, 0, 0.0))
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);
        final CMMsg msg = CMClass.getMsg(mob, null, this, CMMsg.MSG_NOISYMOVEMENT, null);
        final Room R = mob.location();
        if ((R != null)
            && (R.okMessage(mob, msg))) {
            R.send(mob, msg);
            success = proficiencyCheck(mob, 0, auto);
            if (!success)
                R.show(mob, null, this, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> struggle(s) against the water, making no progress."));
            else {
                if (mob.fetchEffect(ID()) == null)
                    mob.addEffect(this);
                mob.recoverPhyStats();

                CMLib.tracking().walk(mob, dirCode, false, false);
            }
            mob.delEffect(this);
            mob.recoverPhyStats();
            if (mob.location() != R)
                mob.location().show(mob, null, this, CMMsg.MSG_NOISYMOVEMENT, null);
        }
        return success;
    }
}
