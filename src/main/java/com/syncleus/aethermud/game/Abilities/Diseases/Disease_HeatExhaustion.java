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
package com.syncleus.aethermud.game.Abilities.Diseases;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.Climate;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class Disease_HeatExhaustion extends Disease {
    private final static String localizedName = CMLib.lang().L("Heat Exhaustion");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Heat Exhaustion)");
    protected Room theRoom = null;
    protected int changeDown = 30;

    @Override
    public String ID() {
        return "Disease_HeatExhaustion";
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
        return CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public boolean putInCommandlist() {
        return false;
    }

    @Override
    public int difficultyLevel() {
        return 1;
    }

    @Override
    protected int DISEASE_TICKS() {
        return 300;
    }

    @Override
    protected int DISEASE_DELAY() {
        return 3;
    }

    @Override
    protected String DISEASE_DONE() {
        return L("Your head stops spinning.");
    }

    @Override
    protected String DISEASE_START() {
        return L("^G<S-NAME> <S-IS-ARE> overcome by the heat.^?");
    }

    @Override
    protected String DISEASE_AFFECT() {
        return "";
    }

    @Override
    public int abilityCode() {
        return 0;
    }

    public Room room(Room R) {
        if ((theRoom == null)
            && (R != null)
            && (!R.getArea().isProperlyEmpty()))
            theRoom = R.getArea().getRandomProperRoom();
        theRoom = CMLib.map().getRoom(theRoom);
        if (R == theRoom)
            theRoom = null;
        return theRoom;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if ((affected != null)
            && (affected == msg.source())
            && (msg.amITarget(msg.source().location()))
            && ((msg.targetMinor() == CMMsg.TYP_LOOK) || (msg.targetMinor() == CMMsg.TYP_EXAMINE))) {
            final Room R = room(msg.source().location());
            if ((R == null) || (R == msg.source().location()))
                return true;
            final CMMsg msg2 = CMClass.getMsg(msg.source(), R, msg.tool(),
                msg.sourceCode(), msg.sourceMessage(),
                msg.targetCode(), msg.targetMessage(),
                msg.othersCode(), msg.othersMessage());
            if (R.okMessage(msg.source(), msg2)) {
                R.executeMsg(msg.source(), msg2);
                return false;
            }
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if ((affected instanceof MOB)
            && (canBeUninvoked())) {
            final MOB M = ((MOB) affected);
            if (M.location() != null) {
                final Area A = M.location().getArea();
                if (CMLib.flags().isUnderWateryRoom(M.location())) {
                    unInvoke();
                    return false;
                }
                Climate C = null;
                if (A != null)
                    C = A.getClimateObj();
                if (C != null) {
                    switch (C.weatherType(M.location())) {
                        case Climate.WEATHER_BLIZZARD:
                        case Climate.WEATHER_HAIL:
                        case Climate.WEATHER_RAIN:
                        case Climate.WEATHER_SNOW:
                        case Climate.WEATHER_THUNDERSTORM:
                        case Climate.WEATHER_WINTER_COLD: {
                            unInvoke();
                            return false;
                        }
                        default:
                            break;
                    }
                }
            }

        }
        if ((--changeDown) <= 0) {
            changeDown = 30;
            theRoom = null;
        }
        return true;
    }
}
