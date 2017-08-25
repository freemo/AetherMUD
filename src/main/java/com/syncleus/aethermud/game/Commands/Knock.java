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
package com.planet_ink.game.Commands;

import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Exits.interfaces.Exit;
import com.planet_ink.game.Items.interfaces.BoardableShip;
import com.planet_ink.game.Items.interfaces.Wearable;
import com.planet_ink.game.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMParms;
import com.planet_ink.game.core.CMProps;
import com.planet_ink.game.core.collections.XVector;
import com.planet_ink.game.core.interfaces.Environmental;

import java.util.List;
import java.util.Vector;


public class Knock extends StdCommand {
    private final String[] access = I(new String[]{"KNOCK"});

    public Knock() {
    }

    @Override
    public String[] getAccessWords() {
        return access;
    }

    @Override
    public boolean execute(MOB mob, List<String> commands, int metaFlags)
        throws java.io.IOException {
        Vector<String> origCmds = new XVector<String>(commands);
        if (commands.size() <= 1) {
            CMLib.commands().doCommandFail(mob, origCmds, L("Knock on what?"));
            return false;
        }
        final String knockWhat = CMParms.combine(commands, 1).toUpperCase();
        final int dir = CMLib.tracking().findExitDir(mob, mob.location(), knockWhat);
        if (dir < 0) {
            final Environmental getThis = mob.location().fetchFromMOBRoomItemExit(mob, null, knockWhat, Wearable.FILTER_UNWORNONLY);
            if (getThis == null) {
                CMLib.commands().doCommandFail(mob, origCmds, L("You don't see '@x1' here.", knockWhat.toLowerCase()));
                return false;
            }
            final CMMsg msg = CMClass.getMsg(mob, getThis, null, CMMsg.MSG_KNOCK, CMMsg.MSG_KNOCK, CMMsg.MSG_KNOCK, L("<S-NAME> knock(s) on <T-NAMESELF>.@x1", CMLib.protocol().msp("knock.wav", 50)));
            if (mob.location().okMessage(mob, msg))
                mob.location().send(mob, msg);

        } else {
            Exit E = mob.location().getExitInDir(dir);
            if (E == null) {
                CMLib.commands().doCommandFail(mob, origCmds, L("Knock on what?"));
                return false;
            }
            if (!E.hasADoor()) {
                CMLib.commands().doCommandFail(mob, origCmds, L("You can't knock on @x1!", E.name()));
                return false;
            }
            final CMMsg msg = CMClass.getMsg(mob, E, null, CMMsg.MSG_KNOCK, CMMsg.MSG_KNOCK, CMMsg.MSG_KNOCK, L("<S-NAME> knock(s) on <T-NAMESELF>.@x1", CMLib.protocol().msp("knock.wav", 50)));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                E = mob.location().getPairedExit(dir);
                final Room R = mob.location().getRoomInDir(dir);
                if ((R != null) && (E != null) && (E.hasADoor())
                    && (R.showOthers(mob, E, null, CMMsg.MSG_KNOCK, L("You hear a knock on <T-NAMESELF>.@x1", CMLib.protocol().msp("knock.wav", 50))))
                    && ((R.domainType() & Room.INDOORS) == Room.INDOORS)) {
                    final Vector<Room> V = new Vector<Room>();
                    V.add(mob.location());
                    TrackingLibrary.TrackingFlags flags;
                    flags = CMLib.tracking().newFlags()
                        .plus(TrackingLibrary.TrackingFlag.OPENONLY);
                    CMLib.tracking().getRadiantRooms(R, V, flags, null, 5, null);
                    V.removeElement(mob.location());
                    for (int v = 0; v < V.size(); v++) {
                        final Room R2 = V.get(v);
                        final int dir2 = CMLib.tracking().radiatesFromDir(R2, V);
                        if ((dir2 >= 0) && ((R2.domainType() & Room.INDOORS) == Room.INDOORS)) {
                            final Room R3 = R2.getRoomInDir(dir2);
                            if (((R3 != null) && (R3.domainType() & Room.INDOORS) == Room.INDOORS)) {
                                final boolean useShipDirs = (R2 instanceof BoardableShip) || (R2.getArea() instanceof BoardableShip);
                                final String inDirName = useShipDirs ? CMLib.directions().getShipInDirectionName(dir2) : CMLib.directions().getInDirectionName(dir2);
                                R2.showHappens(CMMsg.MASK_SOUND | CMMsg.TYP_KNOCK, L("You hear a knock @x1.@x2", inDirName, CMLib.protocol().msp("knock.wav", 50)));
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public double combatActionsCost(final MOB mob, final List<String> cmds) {
        return CMProps.getCommandCombatActionCost(ID());
    }

    @Override
    public double actionsCost(final MOB mob, final List<String> cmds) {
        return CMProps.getCommandActionCost(ID());
    }

    @Override
    public boolean canBeOrdered() {
        return true;
    }

}