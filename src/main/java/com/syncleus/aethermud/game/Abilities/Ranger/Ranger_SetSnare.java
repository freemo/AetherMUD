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
package com.planet_ink.game.Abilities.Ranger;

import com.planet_ink.game.Abilities.StdAbility;
import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Abilities.interfaces.Trap;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;


public class Ranger_SetSnare extends StdAbility {
    private final static String localizedName = CMLib.lang().L("Set Snare");
    private static final String[] triggerStrings = I(new String[]{"SETSNARE"});

    @Override
    public String ID() {
        return "Ranger_SetSnare";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_ROOMS;
    }

    @Override
    protected int canTargetCode() {
        return Ability.CAN_ROOMS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_TRAPPING;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT | USAGE_MANA;
    }

    protected int maxLevel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            if (mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        Trap theTrap = (Trap) CMClass.getAbility("Trap_Snare");

        Room trapThis = null;
        if (givenTarget instanceof Room) {
            trapThis = (Room) givenTarget;
            if (!theTrap.canSetTrapOn(mob, trapThis))
                theTrap = null;
        } else {
            trapThis = mob.location();
            if (mob.isInCombat()) {
                mob.tell(L("You are too busy to be setting snares at the moment!"));
                return false;
            }
            final Trap theOldTrap = CMLib.utensils().fetchMyTrap(trapThis);
            if (!auto) {
                if ((theOldTrap != null)
                    && (theOldTrap.ID().equals(theTrap.ID()))
                    && (theOldTrap.invoker() == mob)) {
                    if (!theOldTrap.canReSetTrap(mob))
                        return false;
                    theTrap = theOldTrap;
                } else if (!theTrap.canSetTrapOn(mob, trapThis))
                    return false;

            }
        }

        if ((theTrap == null) || (trapThis == null)) {
            mob.tell(L("Something went wrong."));
            return false;
        }

        final Trap theOldTrap = CMLib.utensils().fetchMyTrap(trapThis);
        if ((theOldTrap != null)
            && (theOldTrap.ID().equals(theTrap.ID()))
            && (theOldTrap.invoker() != mob)) {
            mob.tell(L("A snare is already set here."));
            return false;
        }

        if (!CMLib.flags().isInWilderness(trapThis)) {
            mob.tell(L("You can't set a snare here."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, +((mob.phyStats().level() + (getXLEVELLevel(mob) * 2)
            - trapThis.phyStats().level()) * 3), auto);
        if (theOldTrap != null) {
            if (theOldTrap.disabled())
                success = false;
            else if (theOldTrap.sprung() && (theOldTrap.invoker() == mob)) {
                success = true;
                theTrap = theOldTrap;
            } else {
                theOldTrap.spring(mob);
                return false;
            }
        }

        final CMMsg msg = CMClass.getMsg(mob, trapThis, this, auto ? CMMsg.MSG_OK_ACTION : CMMsg.MSG_THIEF_ACT, CMMsg.MASK_ALWAYS | CMMsg.MSG_THIEF_ACT, CMMsg.MSG_OK_ACTION, (auto ? L("@x1 appears!", trapThis.name()) : L("<S-NAME> attempt(s) to set a snare on <T-NAMESELF>.")));
        if (mob.location().okMessage(mob, msg)) {
            mob.location().send(mob, msg);
            if (success) {
                if (theTrap.sprung())
                    mob.tell(L("You have reset the snare."));
                else
                    mob.tell(L("You have set the snare."));
                boolean permanent = false;
                if (CMLib.law().doesOwnThisLand(mob, trapThis))
                    permanent = true;
                if (theTrap.sprung())
                    theTrap.resetTrap(mob);
                else
                    theTrap.setTrap(mob, trapThis, getXLEVELLevel(mob), adjustedLevel(mob, asLevel), permanent);
                if (permanent)
                    CMLib.database().DBUpdateRoom(mob.location());
            } else {
                if (CMLib.dice().rollPercentage() > 50) {
                    final Trap T = theTrap.setTrap(mob, trapThis, getXLEVELLevel(mob), adjustedLevel(mob, asLevel), false);
                    mob.location().show(mob, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> trigger(s) the snare on accident!"));
                    T.spring(mob);
                } else {
                    mob.tell(L("You fail in your snare setting attempt."));
                }
            }
        }
        return success;
    }
}