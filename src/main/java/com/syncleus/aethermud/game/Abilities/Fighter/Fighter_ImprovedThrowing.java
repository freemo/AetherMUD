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
package com.syncleus.aethermud.game.Abilities.Fighter;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;


public class Fighter_ImprovedThrowing extends FighterSkill {
    private final static String localizedName = CMLib.lang().L("Improved Throwing");

    @Override
    public String ID() {
        return "Fighter_ImprovedThrowing";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return "";
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_SELF;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_WEAPON_USE;
    }

    @Override
    public boolean isAutoInvoked() {
        return true;
    }

    @Override
    public boolean canBeUninvoked() {
        return false;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if ((affected instanceof MOB)
            && (msg.amISource((MOB) affected))
            && (msg.targetMinor() == CMMsg.TYP_DAMAGE)
            && (msg.tool() instanceof Weapon)
            && (msg.value() > 0)
            && (((Weapon) msg.tool()).weaponClassification() == Weapon.CLASS_THROWN)) {
            if (CMLib.dice().rollPercentage() < 25)
                helpProficiency((MOB) affected, 0);
            msg.setValue(msg.value() + (int) Math.round(CMath.mul(msg.value(), CMath.div(proficiency(), 100.0 - (10.0 * getXLEVELLevel(invoker()))))));
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        if (affected instanceof MOB) {
            final Item myWeapon = ((MOB) affected).fetchWieldedItem();
            if ((myWeapon instanceof Weapon)
                && (((Weapon) myWeapon).weaponClassification() == Weapon.CLASS_THROWN))
                affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + (int) Math.round((15.0 + getXLEVELLevel(invoker())) * (CMath.div(proficiency(), 100.0))));
        }
    }

}
