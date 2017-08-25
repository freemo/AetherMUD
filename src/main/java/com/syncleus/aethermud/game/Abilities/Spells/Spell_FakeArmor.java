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
package com.syncleus.aethermud.game.Abilities.Spells;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.Armor;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Spell_FakeArmor extends Spell {

    private final static String localizedName = CMLib.lang().L("Fake Armor");
    private static boolean notAgainThisRound = false;
    protected Item myItem = null;

    @Override
    public String ID() {
        return "Spell_FakeArmor";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    protected int canAffectCode() {
        return CAN_ITEMS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_ILLUSION;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (notAgainThisRound)
            notAgainThisRound = false;
        return super.tick(ticking, tickID);
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if ((affected != null)
            && (!notAgainThisRound)
            && (msg.target() instanceof MOB)
            && (msg.targetMinor() == CMMsg.TYP_DAMAGE)
            && (affected instanceof Item)
            && (msg.amITarget(((Item) affected).owner()))) {
            notAgainThisRound = true;
            msg.addTrailerMsg(CMClass.getMsg((MOB) msg.target(), null, CMMsg.MSG_OK_VISUAL, L("@x1 absorbs some of the damage done to <S-NAME>.", affected.name())));
            ((Item) affected).unWear();
            ((Item) affected).destroy();
        }
        return super.okMessage(myHost, msg);

    }

    @Override
    public void unInvoke() {
        if (myItem == null)
            return;
        super.unInvoke();
        if (canBeUninvoked()) {
            final Item item = myItem;
            myItem = null;
            item.destroy();
        }
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final String[] choices = {"plate", "chain", "leather", "studded"};
        final String[] choices2 = {"helmet", "shirt", "leggings", "sleeves", "boots"};
        int choice = -1;
        int choice2 = -1;
        if (commands.size() > 1) {
            for (int i = 0; i < choices.length; i++) {
                if (choices[i].equalsIgnoreCase(commands.get(0)))
                    choice = i;
            }
            for (int i = 0; i < choices2.length; i++) {
                if (choices2[i].equalsIgnoreCase(CMParms.combine(commands, 1)))
                    choice2 = i;
            }
        }
        if ((choice < 0) || (choice2 < 0)) {
            mob.tell(L("You must specify what kind of armor to create: plate, chain, studded, or leather.You must also specify a armor type: helmet, shirt, leggings, sleeves, or boots"));
            return false;
        }
        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, null, this, somanticCastCode(mob, null, auto), auto ? "" : L("^S<S-NAME> wave(s) <S-HIS-HER> arms around dramatically.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final Armor armor = (Armor) CMClass.getItem("GenArmor");
                armor.basePhyStats().setArmor(0);
                armor.basePhyStats().setDisposition(armor.basePhyStats().disposition() | PhyStats.IS_BONUS);
                armor.setMaterial(RawMaterial.RESOURCE_COTTON);
                String materialName = "cloth";
                switch (choice) {
                    case 0:
                        materialName = "platemail";
                        break;
                    case 1:
                        materialName = "chainmail";
                        break;
                    case 3:
                        materialName = "studded leather";
                        break;
                    case 2:
                        materialName = "leather";
                        break;
                }
                switch (choice2) {
                    case 0:
                        armor.setName(L("a @x1 helmet", materialName));
                        armor.setRawProperLocationBitmap(Wearable.WORN_HEAD);
                        break;
                    case 1:
                        armor.setName(L("a @x1 shirt", materialName));
                        armor.setRawProperLocationBitmap(Wearable.WORN_HEAD);
                        break;
                    case 2:
                        armor.setName(L("a pair of @x1 leggings", materialName));
                        armor.setRawProperLocationBitmap(Wearable.WORN_LEGS);
                        break;
                    case 3:
                        armor.setName(L("a pair of @x1 sleeves", materialName));
                        armor.setRawProperLocationBitmap(Wearable.WORN_ARMS);
                        break;
                    case 4:
                        armor.setName(L("a pair of @x1 boots", materialName));
                        armor.setRawProperLocationBitmap(Wearable.WORN_FEET);
                        break;
                }
                armor.setDisplayText(L("@x1 sits here", armor.name()));
                armor.setDescription(L("looks like your size!"));
                armor.basePhyStats().setWeight(0);
                armor.recoverPhyStats();
                armor.setBaseValue(0);
                mob.addItem(armor);
                mob.location().show(mob, null, armor, CMMsg.MSG_OK_ACTION, L("Suddenly, <S-NAME> own(s) <O-NAME>!"));
                myItem = armor;
                beneficialAffect(mob, armor, asLevel, 0);
            }
        } else
            beneficialVisualFizzle(mob, null, L("<S-NAME> dramatically wave(s) <S-HIS-HER> arms around, but fizzle(s) the spell."));

        // return whether it worked
        return success;
    }
}
