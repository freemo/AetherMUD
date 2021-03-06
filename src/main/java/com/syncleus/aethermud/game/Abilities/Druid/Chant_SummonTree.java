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
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;
import java.util.Vector;


public class Chant_SummonTree extends Chant_SummonPlants {
    private final static String localizedName = CMLib.lang().L("Summon Tree");
    protected int material = 0;
    protected int oldMaterial = -1;

    @Override
    public String ID() {
        return "Chant_SummonTree";
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
        return Ability.CAN_ITEMS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    protected Item buildMyPlant(MOB mob, Room room) {
        final int code = material & RawMaterial.RESOURCE_MASK;
        final Item newItem = CMClass.getBasicItem("GenItem");
        final String name = CMLib.english().startWithAorAn(RawMaterial.CODES.NAME(code).toLowerCase() + " tree");
        newItem.setName(name);
        newItem.setDisplayText(L("@x1 grows here.", newItem.name()));
        newItem.setDescription("");
        newItem.basePhyStats().setWeight(10000);
        CMLib.flags().setGettable(newItem, false);
        newItem.setMaterial(material);
        newItem.setSecretIdentity(mob.Name());
        newItem.setMiscText(newItem.text());
        Druid_MyPlants.addNewPlant(mob, newItem);
        room.addItem(newItem);
        final Chant_SummonTree newChant = new Chant_SummonTree();
        newItem.basePhyStats().setLevel(10 + newChant.getX1Level(mob));
        newItem.setExpirationDate(0);
        room.showHappens(CMMsg.MSG_OK_ACTION, L("a tall, healthy @x1 tree sprouts up.", RawMaterial.CODES.NAME(code).toLowerCase()));
        room.recoverPhyStats();
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
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if ((plantsLocationR == null) || (littlePlantsI == null))
            return false;
        if (plantsLocationR.myResource() != littlePlantsI.material()) {
            oldMaterial = plantsLocationR.myResource();
            plantsLocationR.setResource(littlePlantsI.material());
        }
        for (int i = 0; i < plantsLocationR.numInhabitants(); i++) {
            final MOB M = plantsLocationR.fetchInhabitant(i);
            if (M.fetchEffect("Chopping") != null) {
                unInvoke();
                break;
            }
        }
        return true;
    }

    @Override
    public void unInvoke() {
        if ((canBeUninvoked()) && (plantsLocationR != null) && (oldMaterial >= 0))
            plantsLocationR.setResource(oldMaterial);
        super.unInvoke();
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {

        material = RawMaterial.RESOURCE_OAK;
        if ((mob.location().myResource() & RawMaterial.MATERIAL_MASK) == RawMaterial.MATERIAL_WOODEN)
            material = mob.location().myResource();
        else {
            final List<Integer> V = mob.location().resourceChoices();
            final Vector<Integer> V2 = new Vector<Integer>();
            if (V != null)
                for (int v = 0; v < V.size(); v++) {
                    if (((V.get(v).intValue() & RawMaterial.MATERIAL_MASK) == RawMaterial.MATERIAL_WOODEN)
                        && ((V.get(v).intValue()) != RawMaterial.RESOURCE_WOOD))
                        V2.addElement(V.get(v));
                }
            if (V2.size() > 0)
                material = V2.elementAt(CMLib.dice().roll(1, V2.size(), -1)).intValue();
        }

        return super.invoke(mob, commands, givenTarget, auto, asLevel);
    }
}
