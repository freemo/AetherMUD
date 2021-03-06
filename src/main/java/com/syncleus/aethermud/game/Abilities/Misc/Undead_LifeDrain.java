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
package com.syncleus.aethermud.game.Abilities.Misc;

import com.syncleus.aethermud.game.Abilities.StdAbility;
import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Undead_LifeDrain extends StdAbility {
    private final static String localizedName = CMLib.lang().L("Drain Life");
    private static final String[] triggerStrings = I(new String[]{"DRAINLIFE"});

    @Override
    public String ID() {
        return "Undead_LifeDrain";
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
        return 0;
    }

    @Override
    protected int canTargetCode() {
        return CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public boolean putInCommandlist() {
        return false;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_DEATHLORE;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);

        if (target == null)
            return false;
        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            int much = mob.phyStats().level();
            final CMMsg msg = CMClass.getMsg(mob, target, this, CMMsg.MSK_MALICIOUS_MOVE | CMMsg.TYP_UNDEAD | (auto ? CMMsg.MASK_ALWAYS : 0), auto ? "" : L("^S<S-NAME> clutch(es) <T-NAMESELF>, and drain(s) <T-HIS-HER> life!^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                mob.curState().adjMana(-much, mob.maxState());
                if (msg.value() > 0)
                    much = (int) Math.round(CMath.div(much, 2.0));
                CMLib.combat().postDamage(mob, target, this, much, CMMsg.MASK_ALWAYS | CMMsg.TYP_UNDEAD, Weapon.TYPE_GASSING, L("The drain <DAMAGE> <T-NAME>!"));
            }
        } else
            maliciousFizzle(mob, target, L("<S-NAME> attempt(s) to drain life from <T-NAMESELF>, but fail(s)."));

        // return whether it worked
        return success;
    }
}
