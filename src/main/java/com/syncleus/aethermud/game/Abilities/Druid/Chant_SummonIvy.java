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
package com.syncleus.aethermud.game.Abilities.Druid;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;


public class Chant_SummonIvy extends Chant_SummonPlants {
    private final static String localizedName = CMLib.lang().L("Summon Ivy");

    @Override
    public String ID() {
        return "Chant_SummonIvy";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTGROWTH;
    }

    @Override
    protected int canAffectCode() {
        return CAN_ITEMS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    public Item buildIvy(MOB mob, Room room) {
        final Item newItem = CMClass.getItem("GenItem");
        newItem.setMaterial(RawMaterial.RESOURCE_GREENS);
        switch (CMLib.dice().roll(1, 5, 0)) {
            case 1:
            case 4:
                newItem.setName(L("poison ivy"));
                newItem.setDisplayText(L("a lovely trifoliate is growing here."));
                newItem.setDescription("");
                break;
            case 2:
                newItem.setName(L("poison sumac"));
                newItem.setDisplayText(L("a small pinnately leafletted tree grows here"));
                newItem.setDescription("");
                break;
            case 3:
            case 5:
                newItem.setName(L("poison oak"));
                newItem.setDisplayText(L("a lovely wrinkled plant grows here"));
                newItem.setDescription("");
                break;
        }
        final Chant_SummonIvy newChant = new Chant_SummonIvy();
        newItem.basePhyStats().setLevel(10 + newChant.getX1Level(mob));
        newItem.basePhyStats().setWeight(1);
        newItem.setSecretIdentity(mob.Name());
        newItem.setMiscText(newItem.text());
        newItem.addNonUninvokableEffect(CMClass.getAbility("Disease_PoisonIvy"));
        Druid_MyPlants.addNewPlant(mob, newItem);
        room.addItem(newItem);
        newItem.setExpirationDate(0);
        room.showHappens(CMMsg.MSG_OK_ACTION, CMLib.lang().L("Suddenly, @x1 sprout(s) up here.", newItem.name()));
        newChant.plantsLocationR = room;
        newChant.littlePlantsI = newItem;
        if (CMLib.law().doesOwnThisLand(mob, room)) {
            newChant.setInvoker(mob);
            newChant.setMiscText(mob.Name());
            newItem.addNonUninvokableEffect(newChant);
        } else
            newChant.beneficialAffect(mob, newItem, 0, (newChant.adjustedLevel(mob, 0) * 240) + 450);
        room.recoverPhyStats();
        return newItem;
    }

    @Override
    protected Item buildMyPlant(MOB mob, Room room) {
        return buildIvy(mob, room);
    }
}
