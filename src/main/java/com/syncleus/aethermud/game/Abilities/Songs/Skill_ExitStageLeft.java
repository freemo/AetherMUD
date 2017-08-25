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
package com.planet_ink.game.Abilities.Songs;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.Directions;
import com.planet_ink.game.core.interfaces.Physical;
import com.planet_ink.game.core.interfaces.Tickable;

import java.util.List;
import java.util.Set;


public class Skill_ExitStageLeft extends BardSkill {
    private final static String localizedName = CMLib.lang().L("Exit Stage Left");
    private static final String[] triggerStrings = I(new String[]{"EXITSTAGELEFT"});

    @Override
    public String ID() {
        return "Skill_ExitStageLeft";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    protected int canAffectCode() {
        return 0;
    }

    @Override
    protected int canTargetCode() {
        return CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_THEATRE;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT | USAGE_MANA;
    }

    @Override
    public long flags() {
        return Ability.FLAG_MOVING | Ability.FLAG_TRANSPORTING;
    }

    @Override
    public void unInvoke() {
        MOB M = null;
        if (affected instanceof MOB)
            M = (MOB) affected;
        final MOB invoker = invoker();
        super.unInvoke();
        if (M != null) {
            if (!M.isMonster())
                CMLib.commands().postStand(M, true);
            M.tell(L("You are no longer off stage."));
            if (M.isMonster())
                CMLib.tracking().wanderAway(M, false, true);
            if ((M.location() != null) && (invoker != null) && (M.location().isInhabitant(invoker))) {
                M.setVictim(invoker);
                CMLib.combat().postAttack(M, invoker(), M.fetchWieldedItem());
            }
        }
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (affected instanceof MOB) {
            final MOB mob = (MOB) affected;
            final Room R = mob.location();
            if (R == null) {
                unInvoke();
                return false;
            }

            if ((R.getRoomInDir(Directions.WEST) == null)
                || (R.getExitInDir(Directions.WEST) == null)) {
                unInvoke();
                return false;
            }

            if (!R.getExitInDir(Directions.WEST).isOpen()) {
                unInvoke();
                return false;
            }
            if (!mob.isMonster())
                CMLib.commands().postStand(mob, true);
            CMLib.tracking().walk(mob, Directions.WEST, mob.isInCombat(), false);
        }
        return super.tick(ticking, tickID);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Set<MOB> h = properTargets(mob, givenTarget, auto);
        if (h == null) {
            mob.tell(L("There doesn't appear to be anyone here worth exiting."));
            return false;
        }

        final Room R = mob.location();
        if (R == null)
            return false;

        if ((R.getRoomInDir(Directions.WEST) == null)
            || (R.getExitInDir(Directions.WEST) == null)) {
            mob.tell(L("There must be a west exit for this to work."));
            return false;
        }

        if (!R.getExitInDir(Directions.WEST).isOpen()) {
            mob.tell(L("The west exit must be open first."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, null, this, CMMsg.MASK_MAGIC | CMMsg.MSG_NOISYMOVEMENT | (auto ? CMMsg.MASK_ALWAYS : 0), L("<S-NAME> call(s) the curtain, motioning some of the cast to exit, stage left."));
            if (R.okMessage(mob, msg)) {
                R.send(mob, msg);
                for (MOB M : h) {
                    final CMMsg msg2 = CMClass.getMsg(mob, M, this, CMMsg.MASK_MAGIC | CMMsg.MSG_NOISYMOVEMENT | (auto ? CMMsg.MASK_ALWAYS : 0), null);
                    if (mob.location().okMessage(mob, msg2)) {
                        mob.location().send(mob, msg2);
                        if (msg2.value() <= 0) {
                            if (maliciousAffect(mob, M, asLevel, 5 + super.getXLEVELLevel(mob), CMMsg.TYP_MIND) != null)
                                CMLib.tracking().walk(M, Directions.WEST, M.isInCombat(), false);
                        }
                    }
                }
            }
        } else
            return beneficialVisualFizzle(mob, null, L("<S-NAME> call(s) the curtain, but nothing happens."));

        return success;
    }

}