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
package com.syncleus.aethermud.game.Items.BasicTech;

import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Electronics;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMStrings;


public class GenEnergyShield extends GenPersonalShield {
    public GenEnergyShield() {
        super();
        setName("an energy shield generator");
        setDisplayText("an energy shield generator sits here.");
        setDescription("The energy shield generator is worn about the body and activated to use. It protects against standard blaster and energy fire. ");
    }

    @Override
    public String ID() {
        return "GenEnergyShield";
    }

    @Override
    protected String fieldOnStr(MOB viewerM) {
        return L((owner() instanceof MOB) ?
            "An energy field surrounds <O-NAME>." :
            "An energy field surrounds <T-NAME>.");
    }

    @Override
    protected String fieldDeadStr(MOB viewerM) {
        return L((owner() instanceof MOB) ?
            "The energy field around <O-NAME> flickers and dies out." :
            "The energy field around <T-NAME> flickers and dies out.");
    }

    @Override
    protected boolean doShield(MOB mob, CMMsg msg, double successFactor) {
        if (mob.location() != null) {
            if (msg.tool() instanceof Weapon) {
                final String s = "^F" + ((Weapon) msg.tool()).hitString(0) + "^N";
                if (s.indexOf("<DAMAGE> <T-HIM-HER>") > 0)
                    mob.location().show(msg.source(), msg.target(), msg.tool(), CMMsg.MSG_OK_VISUAL, CMStrings.replaceAll(s, "<DAMAGE>", L("it is absorbed by the shield around")));
                else if (s.indexOf("<DAMAGES> <T-HIM-HER>") > 0)
                    mob.location().show(msg.source(), msg.target(), msg.tool(), CMMsg.MSG_OK_VISUAL, CMStrings.replaceAll(s, "<DAMAGES>", L("is absorbed by the shield around")));
                else
                    mob.location().show(mob, msg.source(), msg.tool(), CMMsg.MSG_OK_VISUAL, L("The field around <S-NAME> absorbs the <O-NAMENOART> damage."));
            } else
                mob.location().show(mob, msg.source(), msg.tool(), CMMsg.MSG_OK_VISUAL, L("The field around <S-NAME> absorbs the <O-NAMENOART> damage."));
        }
        return false;
    }

    @Override
    protected boolean doesShield(MOB mob, CMMsg msg, double successFactor) {
        if (!activated())
            return false;
        if ((msg.tool() instanceof Electronics)
            && (msg.tool() instanceof Weapon)
            && (Math.random() >= successFactor)
            && (((Weapon) msg.tool()).weaponDamageType() == Weapon.TYPE_SHOOT)) {
            return true;
        }
        return false;
    }
}
