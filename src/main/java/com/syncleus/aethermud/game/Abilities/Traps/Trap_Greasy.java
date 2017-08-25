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
package com.planet_ink.game.Abilities.Traps;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Abilities.interfaces.Trap;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.RawMaterial;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMStrings;
import com.planet_ink.game.core.interfaces.Drink;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;


public class Trap_Greasy extends StdTrap {
    private final static String localizedName = CMLib.lang().L("greasy");
    int times = 20;

    @Override
    public String ID() {
        return "Trap_Greasy";
    }

    @Override
    public String name() {
        return localizedName;
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
    protected int trapLevel() {
        return 10;
    }

    @Override
    public String requiresToSet() {
        return "a container of lamp oil";
    }

    protected Item getPoison(MOB mob) {
        if (mob == null)
            return null;
        if (mob.location() == null)
            return null;
        for (int i = 0; i < mob.location().numItems(); i++) {
            final Item I = mob.location().getItem(i);
            if ((I != null)
                && (I instanceof Drink)
                && (((Drink) I).containsDrink())
                && (((Drink) I).liquidType() == RawMaterial.RESOURCE_LAMPOIL))
                return I;
        }
        return null;
    }

    @Override
    public List<Item> getTrapComponents() {
        final List<Item> V = new Vector<Item>();
        V.add(CMClass.getBasicItem("OilFlask"));
        return V;
    }

    @Override
    public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm) {
        if (P == null)
            return null;
        final Item I = getPoison(mob);
        if ((I != null) && (I instanceof Drink)) {
            ((Drink) I).setLiquidHeld(0);
            I.destroy();
        }
        return super.setTrap(mob, P, trapBonus, qualifyingClassLevel, perm);
    }

    @Override
    public boolean canSetTrapOn(MOB mob, Physical P) {
        if (!super.canSetTrapOn(mob, P))
            return false;
        final Item I = getPoison(mob);
        if ((I == null)
            && (mob != null)) {
            mob.tell(L("You'll need to set down a container of lamp oil first."));
            return false;
        }
        return true;
    }

    @Override
    public void spring(MOB target) {
        if ((target != invoker())
            && (target.location() != null)) {
            if ((doesSaveVsTraps(target))
                || (invoker().getGroupMembers(new HashSet<MOB>()).contains(target)))
                target.location().show(target, null, null, CMMsg.MASK_ALWAYS | CMMsg.MSG_NOISE, L("<S-NAME> avoid(s) setting off a trap!"));
            else if (target.location().show(target, target, this, CMMsg.MASK_ALWAYS | CMMsg.MSG_NOISE, L("@x1 is covered in grease!", CMStrings.capitalizeAndLower(affected.name())))) {
                super.spring(target);
                target.location().show(target, affected, null, CMMsg.MSG_DROP, L("<S-NAME> drop(s) the greasy <T-NAME>!"));
                if (((--times) <= 0) && (canBeUninvoked()) && (affected instanceof Item))
                    disable();
                else
                    sprung = false;
            }
        }
    }
}