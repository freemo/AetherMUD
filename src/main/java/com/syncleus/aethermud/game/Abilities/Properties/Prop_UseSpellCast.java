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
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.*;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Drink;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.PhysicalAgent;

import java.util.List;
import java.util.Vector;


public class Prop_UseSpellCast extends Prop_SpellAdder {
    @Override
    public String ID() {
        return "Prop_UseSpellCast";
    }

    @Override
    public String name() {
        return "Casting spells when used";
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_ITEMS;
    }

    public boolean addMeIfNeccessary(PhysicalAgent source, Physical target, int asLevel, short maxTicks) {
        final List<Ability> V = getMySpellsV();
        if ((target == null)
            || (V.size() == 0)
            || ((compiledMask != null) && (!CMLib.masking().maskCheck(compiledMask, target, true))))
            return false;

        final MOB qualMOB = getInvokerMOB(source, target);

        for (int v = 0; v < V.size(); v++) {
            Ability A = V.get(v);
            Ability EA = target.fetchEffect(A.ID());
            if ((EA == null) && (didHappen())) {
                final String t = A.text();
                A = (Ability) A.copyOf();
                Vector<String> V2 = new Vector<String>();
                if (t.length() > 0) {
                    final int x = t.indexOf('/');
                    if (x < 0) {
                        V2 = CMParms.parse(t);
                        A.setMiscText("");
                    } else {
                        V2 = CMParms.parse(t.substring(0, x));
                        A.setMiscText(t.substring(x + 1));
                    }
                }
                if ((target instanceof Item)
                    && ((!A.canTarget(target)) && (!A.canAffect(target))))
                    return false;
                A.invoke(qualMOB, V2, target, true, asLevel > 0 ? asLevel : ((affected != null) ? affected.phyStats().level() : 0));
                if ((maxTicks > 0) && (maxTicks < Short.MAX_VALUE)) {
                    EA = target.fetchEffect(A.ID());
                    if ((EA != null) && (CMath.s_int(EA.getStat("TICKDOWN")) > maxTicks))
                        EA.setStat("TICKDOWN", Short.toString(maxTicks));
                }
            }
        }
        return true;
    }

    @Override
    public String accountForYourself() {
        return spellAccountingsWithMask("Casts ", " when used.");
    }

    @Override
    public void affectPhyStats(Physical host, PhyStats affectableStats) {
    }

    @Override
    public int triggerMask() {
        if ((affected instanceof Armor) || (affected instanceof Weapon))
            return TriggeredAffect.TRIGGER_WEAR_WIELD;
        if ((affected instanceof Drink) || (affected instanceof Food))
            return TriggeredAffect.TRIGGER_USE;
        if (affected instanceof Container)
            return TriggeredAffect.TRIGGER_DROP_PUTIN;
        return TriggeredAffect.TRIGGER_WEAR_WIELD;
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        if (processing)
            return;
        processing = true;

        if (affected == null)
            return;
        final Item myItem = (Item) affected;
        if (myItem.owner() == null)
            return;
        if (!(myItem.owner() instanceof MOB))
            return;
        if (msg.amISource((MOB) myItem.owner()))
            switch (msg.sourceMinor()) {
                case CMMsg.TYP_FILL:
                    if ((myItem instanceof Drink)
                        && (msg.tool() != myItem)
                        && (msg.amITarget(myItem)))
                        addMeIfNeccessary(msg.source(), msg.source(), 0, maxTicks);
                    break;
                case CMMsg.TYP_WEAR:
                    if ((myItem instanceof Armor)
                        && (msg.amITarget(myItem)))
                        addMeIfNeccessary(msg.source(), msg.source(), 0, maxTicks);
                    break;
                case CMMsg.TYP_PUT:
                case CMMsg.TYP_INSTALL:
                    if ((myItem instanceof Container)
                        && (msg.amITarget(myItem)))
                        addMeIfNeccessary(msg.source(), msg.source(), 0, maxTicks);
                    break;
                case CMMsg.TYP_WIELD:
                case CMMsg.TYP_HOLD:
                    if ((!(myItem instanceof Drink))
                        && (!(myItem instanceof Armor))
                        && (!(myItem instanceof Container))
                        && (msg.amITarget(myItem)))
                        addMeIfNeccessary(msg.source(), msg.source(), 0, maxTicks);
                    break;
            }
        processing = false;
    }
}
