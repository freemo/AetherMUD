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
package com.syncleus.aethermud.game.Behaviors;

import com.syncleus.aethermud.game.Behaviors.interfaces.Behavior;
import com.syncleus.aethermud.game.Exits.interfaces.Exit;
import com.syncleus.aethermud.game.Libraries.interfaces.MaskingLibrary;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMSecurity;
import com.syncleus.aethermud.game.core.Directions;
import com.syncleus.aethermud.game.core.interfaces.Rideable;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class VeryAggressive extends Aggressive {
    public VeryAggressive() {
        super();

        tickWait = 0;
        tickDown = 0;
    }

    @Override
    public String ID() {
        return "VeryAggressive";
    }

    @Override
    public long flags() {
        return Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING;
    }

    @Override
    public String accountForYourself() {
        if (getParms().trim().length() > 0)
            return "surprising aggression against " + CMLib.masking().maskDesc(getParms(), true).toLowerCase();
        else
            return "surprising aggressiveness";
    }

    @Override
    public void setParms(String newParms) {
        super.setParms(newParms);
        tickWait = CMParms.getParmInt(newParms, "delay", 0);
        tickDown = tickWait;
    }

    @Override
    public boolean grantsAggressivenessTo(MOB M) {
        return CMLib.masking().maskCheck(getParms(), M, false);
    }

    public void tickVeryAggressively(Tickable ticking, int tickID, boolean wander, boolean mobKiller, boolean misBehave,
                                     boolean levelCheck, MaskingLibrary.CompiledZMask mask, String attackMsg) {
        if (tickID != Tickable.TICKID_MOB)
            return;
        if (!canFreelyBehaveNormal(ticking))
            return;
        final MOB mob = (MOB) ticking;
        if (CMLib.flags().isATrackingMonster(mob))
            return;

        // ridden things dont wander!
        if (ticking instanceof Rideable) {
            if (((Rideable) ticking).numRiders() > 0)
                return;
        }

        if (((mob.amFollowing() != null) && (mob.location() == mob.amFollowing().location()))
            || (!CMLib.flags().canTaste(mob)))
            return;

        // let's not do this 100%
        if (CMLib.dice().rollPercentage() > 15)
            return;

        final Room thisRoom = mob.location();
        for (int m = 0; m < thisRoom.numInhabitants(); m++) {
            final MOB inhab = thisRoom.fetchInhabitant(m);
            if ((inhab != null)
                && (CMSecurity.isAllowed(inhab, thisRoom, CMSecurity.SecFlag.ORDER))
                && (CMSecurity.isAllowed(inhab, thisRoom, CMSecurity.SecFlag.CMDROOMS)))
                return;
        }

        int dirCode = -1;
        for (int d = Directions.NUM_DIRECTIONS() - 1; d >= 0; d--) {
            final Room room = thisRoom.getRoomInDir(d);
            final Exit exit = thisRoom.getExitInDir(d);
            if ((room != null)
                && (exit != null)
                && (wander || room.getArea().Name().equals(thisRoom.getArea().Name()))) {
                if (exit.isOpen()) {
                    for (int i = 0; i < room.numInhabitants(); i++) {
                        final MOB inhab = room.fetchInhabitant(i);
                        if ((inhab != null)
                            && ((!inhab.isMonster()) || (mobKiller))
                            && (CMLib.flags().canSenseEnteringLeaving(inhab, mob))
                            && ((!levelCheck) || (mob.phyStats().level() < (inhab.phyStats().level() + 5)))
                            && (CMLib.masking().maskCheck(mask, inhab, false))
                            && (((mask != null) && (mask.entries().length > 0))
                            || ((inhab.phyStats().level() < (mob.phyStats().level() + 8))
                            && (inhab.phyStats().level() > (mob.phyStats().level() - 8))))) {
                            dirCode = d;
                            break;
                        }
                    }
                }
            }
            if (dirCode >= 0)
                break;
        }
        if ((dirCode >= 0)
            && (!CMSecurity.isDisabled(CMSecurity.DisFlag.MOBILITY))) {
            CMLib.tracking().walk(mob, dirCode, false, false);
            pickAFight(mob, mask, mobKiller, misBehave, levelCheck, attackMsg);
        }
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        super.tick(ticking, tickID);
        if ((--tickDown) < 0) {
            tickDown = tickWait;
            tickVeryAggressively(ticking, tickID,
                wander,
                mobkill,
                misbehave,
                this.levelcheck,
                this.mask,
                attackMessage);
        }
        return true;
    }
}
