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

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Behaviors.interfaces.Behavior;
import com.syncleus.aethermud.game.Libraries.interfaces.TrackingLibrary;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.Log;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.Vector;

/**
 * Title: False Realities Flavored AetherMUD
 * Description: The False Realities Version of AetherMUD
 * Copyright: Copyright (c) 2003 Jeremy Vyska
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Company: http://www.falserealities.com
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class MOBHunter extends ActiveTicker {
    protected boolean debug = false;
    int radius = 20;

    public MOBHunter() {
        super();
        minTicks = 600;
        maxTicks = 1200;
        chance = 100;
        radius = 15;
        tickReset();
    }

    @Override
    public String ID() {
        return "MOBHunter";
    }

    @Override
    protected int canImproveCode() {
        return Behavior.CAN_MOBS;
    }

    @Override
    public long flags() {
        return Behavior.FLAG_MOBILITY | Behavior.FLAG_POTENTIALLYAGGRESSIVE;
    }

    @Override
    public String accountForYourself() {
        if (getParms().length() > 0)
            return "hunters of  " + CMLib.masking().maskDesc(getParms());
        else
            return "creature hunting";
    }

    protected boolean isHunting(MOB mob) {
        final Ability A = mob.fetchEffect("Thief_Assasinate");
        if (A != null)
            return true;
        return false;
    }

    @Override
    public void setParms(String newParms) {
        super.setParms(newParms);
        radius = CMParms.getParmInt(newParms, "radius", radius);
    }

    protected MOB findPrey(MOB mob) {
        MOB prey = null;
        final Vector<Room> rooms = new Vector<Room>();
        TrackingLibrary.TrackingFlags flags;
        flags = CMLib.tracking().newFlags()
            .plus(TrackingLibrary.TrackingFlag.OPENONLY)
            .plus(TrackingLibrary.TrackingFlag.AREAONLY)
            .plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
            .plus(TrackingLibrary.TrackingFlag.NOAIR)
            .plus(TrackingLibrary.TrackingFlag.NOWATER);
        CMLib.tracking().getRadiantRooms(mob.location(), rooms, flags, null, radius, null);
        for (int r = 0; r < rooms.size(); r++) {
            final Room R = rooms.elementAt(r);
            for (int i = 0; i < R.numInhabitants(); i++) {
                final MOB M = R.fetchInhabitant(i);
                if (CMLib.masking().maskCheck(getParms(), M, false)) {
                    prey = M;
                    break;
                }
            }
        }
        return prey;
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        super.tick(ticking, tickID);
        if ((canAct(ticking, tickID)) && (ticking instanceof MOB)) {
            final MOB mob = (MOB) ticking;
            if (debug)
                Log.sysOut("ZAPHUNT", "Tick starting");
            if (!isHunting(mob)) {
                if (debug)
                    Log.sysOut("ZAPHUNT", "'" + mob.Name() + "' not hunting.");
                final MOB prey = findPrey(mob);
                if (prey != null) {
                    if (debug)
                        Log.sysOut("ZAPHUNT", "'" + mob.Name() + "' found prey: '" + prey.Name() + "'");
                    final Ability A = CMClass.getAbility("Thief_Assassinate");
                    A.setProficiency(100);
                    mob.curState().setMana(mob.maxState().getMana());
                    mob.curState().setMovement(mob.maxState().getMovement());
                    A.invoke(mob, new Vector<String>(), prey, false, 0);
                }
            }
        }
        return true;
    }
}
