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
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;


public class Trap_DeepPit extends Trap_RoomPit {
    private final static String localizedName = CMLib.lang().L("deep pit");

    @Override
    public String ID() {
        return "Trap_DeepPit";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_ROOMS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    protected int trapLevel() {
        return 14;
    }

    @Override
    public String requiresToSet() {
        return "";
    }

    @Override
    public void finishSpringing(MOB target) {
        if ((!invoker().mayIFight(target)) || (target.phyStats().weight() < 5))
            target.location().show(target, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> float(s) gently into the pit!"));
        else {
            target.location().show(target, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> hit(s) the pit floor with a THUMP!"));
            int damage = CMLib.dice().roll(trapLevel() + abilityCode(), 15, 1);
            final int maxDamage = (int) Math.round(CMath.mul(target.baseState().getHitPoints(), .95));
            if (damage >= maxDamage)
                damage = maxDamage;
            CMLib.combat().postDamage(invoker(), target, this, damage, CMMsg.MASK_MALICIOUS | CMMsg.MASK_ALWAYS | CMMsg.TYP_JUSTICE, -1, null);
        }
        CMLib.commands().postLook(target, true);
    }
}
