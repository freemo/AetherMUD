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
package com.syncleus.aethermud.game.Abilities.Thief;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Coins;
import com.syncleus.aethermud.game.Items.interfaces.Container;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.collections.Triad;
import com.syncleus.aethermud.game.core.interfaces.ItemCollection;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class Thief_PiecesOfEight extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Pieces of Eight");
    private static final String[] triggerStrings = I(new String[]{"PIECESOFEIGHT"});

    @Override
    public String ID() {
        return "Thief_PiecesOfEight";
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
        return 0;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_DECEPTIVE;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT | USAGE_MANA;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Room R = mob.location();
        if (R == null)
            return false;

        if (commands.size() == 0) {
            mob.tell(L("Fetch how much from your buried treasure?"));
            return false;
        }

        final String moneyCmd = CMParms.combine(commands);
        final Triad<String, Double, Long> triad = CMLib.english().parseMoneyStringSDL(mob, moneyCmd, null);
        final double totalAmount = CMath.mul(triad.second.doubleValue(), triad.third.doubleValue());

        if (totalAmount == 0) {
            mob.tell(L("'@x1' is invalid. Fetch how much from your buried treasure?", moneyCmd));
            return false;
        }

        if (mob.playerStats() == null)
            return false;

        final ItemCollection coll = mob.playerStats().getExtItems();
        if (coll == null) {
            mob.tell(L("You haven't buried any treasure."));
            return false;
        }

        List<Coins> coins = new LinkedList<Coins>();
        double totalCoinValue = 0.0;
        HashSet<Double> otherDenoms = new HashSet<Double>();
        for (Enumeration<Item> i = coll.items(); i.hasMoreElements(); ) {
            final Item I = i.nextElement();
            if (I instanceof Container) {
                final Ability A = I.fetchEffect("Thief_BuriedTreasure");
                if ((A != null)
                    && (mob.Name().equals(I.fetchEffect(A.text())))) {
                    List<Item> contents = ((Container) I).getContents();
                    for (Item I2 : contents) {
                        if (I2 instanceof Coins) {
                            Coins C = (Coins) I2;
                            if (C.getCurrency().equals(triad.first)) {
                                if (C.getDenomination() == triad.second.doubleValue()) {
                                    coins.add(C);
                                    totalCoinValue += C.getTotalValue();
                                } else {
                                    otherDenoms.add(Double.valueOf(C.getDenomination()));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (coins.size() == 0) {
            mob.tell(L("You don't have any buried @x1.", CMLib.moneyCounter().getDenominationName(triad.first, triad.second.doubleValue())));
            return false;
        }

        if (totalAmount > totalCoinValue) {
            mob.tell(L("You don't have enough buried @x1.", CMLib.moneyCounter().getDenominationName(triad.first, triad.second.doubleValue())));
            return false;
        }

        totalCoinValue = 0;
        for (Coins C : coins) {
            if ((totalCoinValue + C.getTotalValue()) < totalAmount) {
                totalCoinValue += C.getTotalValue();
                C.destroy();
                coll.delItem(C);
            } else {
                double remainder = (totalCoinValue + C.getTotalValue()) - totalAmount;
                C.setNumberOfCoins((int) Math.round(remainder / C.getDenomination()));
            }
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final String moneyStr = triad.third + " " + CMLib.moneyCounter().getDenominationName(triad.first, triad.second.doubleValue());
            final CMMsg msg = CMClass.getMsg(mob, null, this, CMMsg.MASK_MAGIC | CMMsg.MSG_THIEF_ACT, L("<S-NAME> pull(s) @x1 out of <S-HIS-HER> hard earned booty.", moneyStr));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                Coins C = CMLib.moneyCounter().makeCurrency(triad.first, triad.second.doubleValue(), triad.third.intValue());
                mob.addItem(C);
                C.putCoinsBack();
            }
        } else
            mob.tell(L("You think about it, but can't remember where you buried your treasure."));
        return success;
    }
}
