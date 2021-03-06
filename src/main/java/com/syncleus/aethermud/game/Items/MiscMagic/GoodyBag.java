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
package com.syncleus.aethermud.game.Items.MiscMagic;

import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.ArchonOnly;
import com.syncleus.aethermud.game.Items.interfaces.Coins;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Pill;
import com.syncleus.aethermud.game.Libraries.interfaces.MoneyLibrary;
import com.syncleus.aethermud.game.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;

import java.util.List;


public class GoodyBag extends BagOfEndlessness implements ArchonOnly {
    boolean alreadyFilled = false;

    public GoodyBag() {
        super();
        setName("a goody bag");
        setDisplayText("a small bag is sitting here.");
        setDescription("A nice little bag to put your things in.");
        secretIdentity = "The Archon's Goody Bag";
        recoverPhyStats();
    }

    @Override
    public String ID() {
        return "GoodyBag";
    }

    private void putInBag(Item I) {
        I.setContainer(this);
        if (owner() instanceof Room)
            ((Room) owner()).addItem(I);
        else if (owner() instanceof MOB)
            ((MOB) owner()).addItem(I);
        I.recoverPhyStats();
    }

    public void addMoney(double value) {
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        if ((!alreadyFilled) && (owner() != null)) {
            alreadyFilled = true;
            if (!hasContent()) {
                final List<String> V = CMLib.moneyCounter().getAllCurrencies();
                for (int v = 0; v < V.size(); v++) {
                    final String currency = V.get(v);
                    final MoneyLibrary.MoneyDenomination[] DV = CMLib.moneyCounter().getCurrencySet(currency);
                    for (final MoneyDenomination element : DV) {
                        final Coins C = CMLib.moneyCounter().makeBestCurrency(currency, element.value(), owner(), this);
                        if (C != null)
                            C.setNumberOfCoins(100);
                    }
                }
                Item I = CMClass.getItem("GenSuperPill");
                I.setName(L("a training pill"));
                I.setDisplayText(L("A small round pill has been left here."));
                ((Pill) I).setSpellList("train+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a practice pill"));
                I.setDisplayText(L("A tiny little pill has been left here."));
                ((Pill) I).setSpellList("prac+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a quest point pill"));
                I.setDisplayText(L("A questy little pill has been left here."));
                ((Pill) I).setSpellList("ques+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a 100 exp pill"));
                I.setDisplayText(L("An important little pill has been left here."));
                ((Pill) I).setSpellList("expe+100");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a 500 exp pill"));
                I.setDisplayText(L("An important little pill has been left here."));
                ((Pill) I).setSpellList("expe+500");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a 1000 exp pill"));
                I.setDisplayText(L("An important little pill has been left here."));
                ((Pill) I).setSpellList("expe+1000");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a 2000 exp pill"));
                I.setDisplayText(L("An important little pill has been left here."));
                ((Pill) I).setSpellList("expe+2000");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a 5000 exp pill"));
                I.setDisplayText(L("An important little pill has been left here."));
                ((Pill) I).setSpellList("expe+5000");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a strength pill"));
                I.setDisplayText(L("An strong little pill has been left here."));
                ((Pill) I).setSpellList("str+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("an intelligence pill"));
                I.setDisplayText(L("An smart little pill has been left here."));
                ((Pill) I).setSpellList("int+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a wisdom pill"));
                I.setDisplayText(L("A wise little pill has been left here."));
                ((Pill) I).setSpellList("wis+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a dexterity pill"));
                I.setDisplayText(L("A quick little pill has been left here."));
                ((Pill) I).setSpellList("dex+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a constitution pill"));
                I.setDisplayText(L("A nutricious little pill has been left here."));
                ((Pill) I).setSpellList("con+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a charisma pill"));
                I.setDisplayText(L("A pretty little pill has been left here."));
                ((Pill) I).setSpellList("cha+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a level pill"));
                I.setDisplayText(L("A pretty little pill has been left here."));
                ((Pill) I).setSpellList("level+1");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a 5-level pill"));
                I.setDisplayText(L("A pretty little pill has been left here."));
                ((Pill) I).setSpellList("level+5");
                putInBag(I);
                I = CMClass.getItem("GenSuperPill");
                I.setName(L("a 10-level pill"));
                I.setDisplayText(L("A pretty little pill has been left here."));
                ((Pill) I).setSpellList("level+10");
                putInBag(I);
            }
        }
        super.executeMsg(myHost, msg);
    }
}
