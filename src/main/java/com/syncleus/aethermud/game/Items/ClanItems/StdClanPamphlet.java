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
package com.syncleus.aethermud.game.Items.ClanItems;

import com.syncleus.aethermud.game.Behaviors.interfaces.LegalBehavior;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.ClanItem;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class StdClanPamphlet extends StdClanItem {
    protected int tradeTime = -1;

    public StdClanPamphlet() {
        super();

        setName("a clan pamphlet");
        basePhyStats.setWeight(1);
        setDisplayText("a pamphlet belonging to a clan is here.");
        setDescription("");
        secretIdentity = "";
        baseGoldValue = 1;
        setClanItemType(ClanItem.ClanItemType.PROPAGANDA);
        material = RawMaterial.RESOURCE_PAPER;
        recoverPhyStats();
    }

    @Override
    public String ID() {
        return "StdClanPamphlet";
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if ((tickID == Tickable.TICKID_CLANITEM)
            && (owner() instanceof MOB)
            && (clanID().length() > 0)
            && (((MOB) owner()).isMonster())
            && (!CMLib.flags().isAnimalIntelligence((MOB) owner()))
            && (((MOB) owner()).getStartRoom() != null)
            && (((MOB) owner()).location() != null)
            && (((MOB) owner()).getStartRoom().getArea() == ((MOB) owner()).location().getArea())) {
            final Room R = ((MOB) owner()).location();
            if ((((MOB) owner()).getClanRole(clanID()) != null)
                || (((--tradeTime) <= 0))) {
                final LegalBehavior B = CMLib.law().getLegalBehavior(R);
                if (B != null) {
                    final String rulingClan = B.rulingOrganization();
                    if ((rulingClan != null) && (rulingClan.length() > 0)
                        && (!rulingClan.equals(clanID()))
                        && (((MOB) owner()).getClanRole(rulingClan) != null))
                        ((MOB) owner()).setClan(rulingClan, -1);
                    if (tradeTime <= 0) {
                        final MOB mob = (MOB) owner();
                        if ((rulingClan != null)
                            && (rulingClan.length() > 0)
                            && (!rulingClan.equals(clanID()))
                            && (mob.getClanRole(rulingClan) == null)
                            && (mob.getClanRole(clanID()) == null)
                            && (CMLib.flags().canSpeak(mob))
                            && (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
                            && (R != null)) {
                            final MOB M = R.fetchRandomInhabitant();
                            if ((M != null)
                                && (M != mob)
                                && (M.isMonster())
                                && (M.getClanRole(rulingClan) != null)
                                && (!CMLib.flags().isAnimalIntelligence(M))
                                && (CMLib.flags().canBeSeenBy(M, mob))
                                && (CMLib.flags().canBeHeardMovingBy(M, mob))) {
                                CMLib.commands().postSay(mob, M, L("Hey, take a look at this."), false, false);
                                final ClanItem I = (ClanItem) copyOf();
                                mob.addItem(I);
                                final CMMsg newMsg = CMClass.getMsg(mob, M, I, CMMsg.MSG_GIVE, L("<S-NAME> give(s) <O-NAME> to <T-NAMESELF>."));
                                if (mob.location().okMessage(mob, newMsg) && (!((Item) I).amDestroyed()))
                                    mob.location().send(mob, newMsg);
                                if (!M.isMine(I))
                                    ((Item) I).destroy();
                                else if (mob.isMine(I))
                                    ((Item) I).destroy();
                            }
                        }
                    }
                }
                if (tradeTime <= 0)
                    tradeTime = CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
            }
        }
        return true;
    }
}
