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
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.Races.interfaces.Race;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class Fighter_KnifeHand extends MonkSkill {
    private final static String localizedName = CMLib.lang().L("Knife Hand");
    protected Weapon naturalWeapon = null;

    @Override
    public String ID() {
        return "Fighter_KnifeHand";
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
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_PUNCHING;
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
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if ((tickID == Tickable.TICKID_MOB)
            && (affected instanceof MOB)) {
            final MOB mob = (MOB) affected;
            if ((mob.isInCombat())
                && (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
                && (mob.rangeToTarget() == 0)
                && (mob.charStats().getBodyPart(Race.BODY_HAND) > 1)
                && (!anyWeapons(mob))) {
                if (CMLib.dice().rollPercentage() > 95)
                    helpProficiency(mob, 0);
                if ((naturalWeapon == null)
                    || (naturalWeapon.amDestroyed())) {
                    naturalWeapon = CMClass.getWeapon("GenWeapon");
                    naturalWeapon.setName(L("a knife hand"));
                    naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
                    naturalWeapon.setUsesRemaining(1000);
                    naturalWeapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
                    naturalWeapon.basePhyStats().setDamage(7);
                    naturalWeapon.recoverPhyStats();
                }
                CMLib.combat().postAttack(mob, mob.getVictim(), naturalWeapon);
            }
        }
        return true;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!super.okMessage(myHost, msg))
            return false;

        if (!(affected instanceof MOB))
            return true;

        final MOB mob = (MOB) affected;
        if (msg.amISource(mob)
            && (msg.targetMinor() == CMMsg.TYP_DAMAGE)
            && (msg.tool() instanceof Weapon)
            && (msg.tool() == naturalWeapon))
            msg.setValue(msg.value() + naturalWeapon.basePhyStats().damage() + super.getXLEVELLevel(mob));
        return true;
    }
}
