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
package com.syncleus.aethermud.game.Abilities.Properties;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Abilities.interfaces.TriggeredAffect;
import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.BoardableShip;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class Prop_FightSpellCast extends Prop_SpellAdder {
    @Override
    public String ID() {
        return "Prop_FightSpellCast";
    }

    @Override
    public String name() {
        return "Casting spells when properly used during combat";
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_ITEMS;
    }

    @Override
    public String accountForYourself() {
        return spellAccountingsWithMask("Casts ", " during combat.");
    }

    @Override
    public long flags() {
        return Ability.FLAG_CASTER;
    }

    @Override
    public int triggerMask() {
        return TriggeredAffect.TRIGGER_HITTING_WITH;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        if (processing)
            return;

        if (!(affected instanceof Item))
            return;
        processing = true;

        final Item myItem = (Item) affected;

        if ((myItem != null)
            && (msg.targetMinor() == CMMsg.TYP_DAMAGE)
            && ((msg.value()) > 0)) {
            if (CMLib.combat().isAShipSiegeWeapon(myItem)
                && (msg.target() instanceof MOB))
                addMeIfNeccessary(msg.source(), (MOB) msg.target(), true, 0, maxTicks);
            else if ((!myItem.amWearingAt(Wearable.IN_INVENTORY))
                && (myItem.owner() instanceof MOB)
                && (msg.target() instanceof MOB)) {
                final MOB mob = (MOB) myItem.owner();
                if ((mob.isInCombat())
                    && (mob.location() != null)
                    && (!mob.amDead())) {
                    if ((myItem instanceof Weapon)
                        && (msg.tool() == myItem)
                        && (myItem.amWearingAt(Wearable.WORN_WIELD))
                        && (msg.amISource(mob)))
                        addMeIfNeccessary(msg.source(), (MOB) msg.target(), true, 0, maxTicks);
                    else if ((msg.amITarget(mob))
                        && (!myItem.amWearingAt(Wearable.WORN_WIELD))
                        && (!(myItem instanceof Weapon)))
                        addMeIfNeccessary(mob, mob, true, 0, maxTicks);
                }
            } else if (CMLib.combat().isAShipSiegeWeapon(myItem)
                && (msg.target() instanceof Item)) {
                Item I = (Item) msg.target();
                if (I instanceof BoardableShip) {
                    Area A = ((BoardableShip) I).getShipArea();
                    if (A != null) {
                        List<Physical> stuff = new ArrayList<Physical>();
                        for (Enumeration<Room> r = A.getProperMap(); r.hasMoreElements(); ) {
                            Room R = r.nextElement();
                            if ((R != null) && ((R.domainType() & Room.INDOORS) == 0)) {
                                Item I2 = R.getRandomItem();
                                if (I2 != null)
                                    stuff.add(I2);
                                MOB M = R.fetchRandomInhabitant();
                                if (M != null)
                                    stuff.add(M);
                            }
                        }
                        if (stuff.size() > 0) {
                            Physical P = stuff.get(CMLib.dice().roll(1, stuff.size(), -1));
                            if (P != null)
                                addMeIfNeccessary(msg.source(), P, true, 0, maxTicks);
                        }
                    }
                } else
                    addMeIfNeccessary(msg.source(), I, true, 0, maxTicks);
            }
        }
        processing = false;
    }
}
