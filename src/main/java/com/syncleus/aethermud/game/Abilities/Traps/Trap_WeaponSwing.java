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
package com.syncleus.aethermud.game.Abilities.Traps;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Abilities.interfaces.Trap;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;


public class Trap_WeaponSwing extends StdTrap {
    private final static String localizedName = CMLib.lang().L("weapon swing");

    @Override
    public String ID() {
        return "Trap_WeaponSwing";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_EXITS | Ability.CAN_ITEMS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    protected int trapLevel() {
        return 9;
    }

    @Override
    public String requiresToSet() {
        return "a melee weapon";
    }

    protected Item getPoison(MOB mob) {
        if (mob == null)
            return null;
        if (mob.location() == null)
            return null;
        for (int i = 0; i < mob.location().numItems(); i++) {
            final Item I = mob.location().getItem(i);
            if ((I != null)
                && (I instanceof Weapon)
                && (((Weapon) I).weaponClassification() != Weapon.CLASS_RANGED))
                return I;
        }
        return null;
    }

    @Override
    public List<Item> getTrapComponents() {
        final Vector<Item> V = new Vector<Item>();
        V.addElement(CMClass.getWeapon("Sword"));
        return V;
    }

    @Override
    public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm) {
        if (P == null)
            return null;
        final Item I = getPoison(mob);
        setMiscText("3/a club");
        if (I != null) {
            setMiscText("" + I.basePhyStats().damage() + "/" + I.name());
            I.destroy();
        }
        return super.setTrap(mob, P, trapBonus, qualifyingClassLevel, perm);
    }

    @Override
    public boolean canSetTrapOn(MOB mob, Physical P) {
        if (!super.canSetTrapOn(mob, P))
            return false;
        if (mob != null) {
            final Item I = getPoison(mob);
            if (I == null) {
                mob.tell(L("You'll need to set down a melee weapon first."));
                return false;
            }
        }
        return true;
    }

    @Override
    public void spring(MOB target) {
        if ((target != invoker())
            && (target.location() != null)) {
            final int x = text().indexOf('/');
            int dam = 3;
            String name = "a club";
            if (x >= 0) {
                dam = CMath.s_int(text().substring(0, x));
                name = text().substring(x + 1);
            }
            if ((!invoker().mayIFight(target))
                || (isLocalExempt(target))
                || (invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
                || (target == invoker())
                || (doesSaveVsTraps(target)))
                target.location().show(target, null, null, CMMsg.MASK_ALWAYS | CMMsg.MSG_NOISE, L("<S-NAME> avoid(s) setting off @x1 trap!", name));
            else if (target.location().show(target, target, this, CMMsg.MASK_ALWAYS | CMMsg.MSG_NOISE, L("<S-NAME> <S-IS-ARE> struck by @x1 trap!", name))) {
                super.spring(target);
                final int damage = CMLib.dice().roll(trapLevel() + abilityCode(), dam, 1);
                CMLib.combat().postDamage(invoker(), target, this, damage, CMMsg.NO_EFFECT, -1, null);
                if ((canBeUninvoked()) && (affected instanceof Item))
                    disable();
            }
        }
    }
}
