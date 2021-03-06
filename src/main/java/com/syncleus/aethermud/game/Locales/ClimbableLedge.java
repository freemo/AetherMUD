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
package com.syncleus.aethermud.game.Locales;

import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.Directions;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Rideable;


public class ClimbableLedge extends ClimbableSurface {
    @Override
    public String ID() {
        return "ClimbableLedge";
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (CMLib.flags().isSleeping(this))
            return super.okMessage(myHost, msg);

        if ((msg.targetMinor() == CMMsg.TYP_ENTER)
            && (msg.amITarget(this))) {
            final Rideable ladder = CMLib.tracking().findALadder(msg.source(), this);
            if (ladder != null) {
                msg.source().setRiding(ladder);
                msg.source().recoverPhyStats();
            }
            if ((getRoomInDir(Directions.DOWN) != msg.source().location()))
                return true;
        }
        return super.okMessage(myHost, msg);
    }

}
