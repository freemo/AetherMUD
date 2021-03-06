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
package com.syncleus.aethermud.game.Abilities.SuperPowers;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Exits.interfaces.Exit;
import com.syncleus.aethermud.game.Items.interfaces.Container;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.Directions;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.ShopKeeper;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.Iterator;
import java.util.List;


public class Power_TrapSense extends SuperPower {
    private final static String localizedName = CMLib.lang().L("Trap Sense");
    Room lastRoom = null;

    @Override
    public String ID() {
        return "Power_TrapSense";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return "";
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
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public boolean isAutoInvoked() {
        return true;
    }

    @Override
    public boolean canBeUninvoked() {
        return false;
    }

    public String trapCheck(Physical P) {
        if (P != null)
            if (CMLib.utensils().fetchMyTrap(P) != null)
                return P.name() + " is trapped.\n\r";
        return "";
    }

    public String trapHere(MOB mob, Physical P) {
        final StringBuffer msg = new StringBuffer("");
        if (P == null)
            return msg.toString();
        if ((P instanceof Room) && (CMLib.flags().canBeSeenBy(P, mob))) {
            msg.append(trapCheck(P));
            final Room R = (Room) P;
            for (int d = Directions.NUM_DIRECTIONS() - 1; d >= 0; d--) {
                if (R.getExitInDir(d) == P) {
                    final Exit E2 = R.getReverseExit(d);
                    msg.append(trapHere(mob, P));
                    msg.append(trapHere(mob, E2));
                    break;
                }
            }
            for (int i = 0; i < R.numItems(); i++) {
                final Item I = R.getItem(i);
                if ((I != null) && (I.container() == null))
                    msg.append(trapHere(mob, I));
            }
            for (int m = 0; m < R.numInhabitants(); m++) {
                final MOB M = R.fetchInhabitant(m);
                if ((M != null) && (M != mob))
                    msg.append(trapHere(mob, M));
            }
        } else if ((P instanceof Container) && (CMLib.flags().canBeSeenBy(P, mob))) {
            final Container C = (Container) P;
            final List<Item> V = C.getDeepContents();
            for (int v = 0; v < V.size(); v++)
                if (trapCheck(V.get(v)).length() > 0)
                    msg.append(L("@x1 contains something trapped.\n", C.name()));
        } else if ((P instanceof Item) && (CMLib.flags().canBeSeenBy(P, mob)))
            msg.append(trapCheck(P));
        else if ((P instanceof Exit) && (CMLib.flags().canBeSeenBy(P, mob)))
            msg.append(trapCheck(P));
        else if ((P instanceof MOB) && (CMLib.flags().canBeSeenBy(P, mob))) {
            for (int i = 0; i < ((MOB) P).numItems(); i++) {
                final Item I = ((MOB) P).getItem(i);
                if (trapCheck(I).length() > 0)
                    return P.name() + " is carrying something trapped.\n";
            }
            final ShopKeeper SK = CMLib.aetherShops().getShopKeeper(P);
            if (SK != null) {
                for (final Iterator<Environmental> i = SK.getShop().getStoreInventory(); i.hasNext(); ) {
                    final Environmental E2 = i.next();
                    if (E2 instanceof Item)
                        if (trapCheck((Item) E2).length() > 0)
                            return P.name() + " has something trapped in stock.\n";
                }
            }
        }
        return msg.toString();
    }

    public void messageTo(MOB mob) {
        final String here = trapHere(mob, mob.location());
        if (here.length() > 0) {
            mob.tell(here);
            if (CMLib.dice().rollPercentage() < 5)
                super.helpProficiency(mob, 0);
        } else {
            String last = "";
            String dirs = "";
            for (int d = Directions.NUM_DIRECTIONS() - 1; d >= 0; d--) {
                final Room R = mob.location().getRoomInDir(d);
                final Exit E = mob.location().getExitInDir(d);
                if ((R != null) && (E != null) && (trapHere(mob, R).length() > 0)) {
                    if (last.length() > 0)
                        dirs += ", " + last;
                    last = CMLib.directions().getFromCompassDirectionName(d);
                }
            }
            if ((dirs.length() == 0) && (last.length() > 0))
                mob.tell(L("You sense a trap to @x1.", last));
            else if ((dirs.length() > 2) && (last.length() > 0))
                mob.tell(L("You sense a trap to @x1, and @x2.", dirs.substring(2), last));
        }
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if ((tickID == Tickable.TICKID_MOB)
            && (affected instanceof MOB)
            && (((MOB) affected).location() != null)
            && ((lastRoom == null) || (((MOB) affected).location() != lastRoom))) {
            lastRoom = ((MOB) affected).location();
            messageTo((MOB) affected);
        }
        return true;
    }
}
