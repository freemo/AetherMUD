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
package com.syncleus.aethermud.game.Behaviors;

import com.syncleus.aethermud.game.Abilities.interfaces.DiseaseAffect;
import com.syncleus.aethermud.game.Behaviors.interfaces.Behavior;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;


public class Guard extends StdBehavior {
    @Override
    public String ID() {
        return "Guard";
    }

    @Override
    public long flags() {
        return Behavior.FLAG_POTENTIALLYAGGRESSIVE;
    }

    @Override
    public String accountForYourself() {
        return "protective of particular friends";
    }

    @Override
    public void executeMsg(Environmental affecting, CMMsg msg) {
        super.executeMsg(affecting, msg);
        if ((msg.target() == null) || (!(msg.target() instanceof MOB)))
            return;
        final MOB source = msg.source();
        final MOB observer = (MOB) affecting;
        final MOB target = (MOB) msg.target();

        if ((source != observer)
            && (source != target)
            && (target != null)
            && (CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
            && ((getParms().trim().length() == 0) || (getParms().equalsIgnoreCase(target.Name())))
            && (!observer.isInCombat())
            && (CMLib.flags().canBeSeenBy(source, observer))
            && (CMLib.flags().canBeSeenBy(target, observer))
            && ((!(msg.tool() instanceof DiseaseAffect)) || (((DiseaseAffect) msg.tool()).isMalicious()))
            && (!BrotherHelper.isBrother(source, observer, false)))
            Aggressive.startFight(observer, source, true, false, null);
    }
}
