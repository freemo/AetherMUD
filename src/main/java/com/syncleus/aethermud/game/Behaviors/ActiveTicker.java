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

import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Behaviors.interfaces.Behavior;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class ActiveTicker extends StdBehavior {
    protected int minTicks = 10;
    protected int maxTicks = 30;
    protected int chance = 100;
    //protected short speed=1;
    protected int tickDown = (int) Math.round(Math.random() * (maxTicks - minTicks)) + minTicks;

    @Override
    public String ID() {
        return "ActiveTicker";
    }

    @Override
    protected int canImproveCode() {
        return Behavior.CAN_ITEMS | Behavior.CAN_MOBS | Behavior.CAN_ROOMS | Behavior.CAN_EXITS | Behavior.CAN_AREAS;
    }

    protected void tickReset() {
        tickDown = (int) Math.round(Math.random() * (maxTicks - minTicks)) + minTicks;
    }

    @Override
    public void setParms(String newParms) {
        parms = newParms;
        minTicks = CMParms.getParmInt(parms, "min", minTicks);
        maxTicks = CMParms.getParmInt(parms, "max", maxTicks);
        chance = CMParms.getParmInt(parms, "chance", chance);
        tickReset();
    }

    public String rebuildParms() {
        final StringBuffer rebuilt = new StringBuffer("");
        rebuilt.append(" min=" + minTicks);
        rebuilt.append(" max=" + maxTicks);
        rebuilt.append(" chance=" + chance);
        return rebuilt.toString();
    }

    public String getParmsNoTicks() {
        String parms = getParms();
        char c = ';';
        int x = parms.indexOf(c);
        if (x < 0) {
            c = '/';
            x = parms.indexOf(c);
        }
        if (x > 0) {
            if ((x + 1) > parms.length())
                return "";
            parms = parms.substring(x + 1);
        } else {
            return "";
        }
        return parms;
    }

    protected boolean canAct(Tickable ticking, int tickID) {
        switch (tickID) {
            case Tickable.TICKID_AREA: {
                if (!(ticking instanceof Area))
                    break;
            }
            //$FALL-THROUGH$
            case Tickable.TICKID_MOB:
            case Tickable.TICKID_ITEM_BEHAVIOR:
            case Tickable.TICKID_ROOM_BEHAVIOR: {
                if ((--tickDown) < 1) {
                    tickReset();
                    if ((ticking instanceof MOB) && (!canActAtAll(ticking)))
                        return false;
                    final int a = CMLib.dice().rollPercentage();
                    if (a > chance)
                        return false;
                    return true;
                }
                break;
            }
            default:
                break;
        }
        return false;
    }
}
