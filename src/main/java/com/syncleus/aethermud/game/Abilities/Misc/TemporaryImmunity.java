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
package com.planet_ink.game.Abilities.Misc;

import com.planet_ink.game.Abilities.StdAbility;
import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMParms;
import com.planet_ink.game.core.CMath;
import com.planet_ink.game.core.collections.Pair;
import com.planet_ink.game.core.collections.PairVector;
import com.planet_ink.game.core.interfaces.Environmental;
import com.planet_ink.game.core.interfaces.Tickable;

import java.util.List;


public class TemporaryImmunity extends StdAbility {
    public final static long IMMUNITY_TIME = 36000000;
    private final static String localizedName = CMLib.lang().L("Temporary Immunity");
    protected final PairVector<String, Long> set = new PairVector<String, Long>();
    protected volatile int tickUp = 10;

    public TemporaryImmunity() {
        super();

        tickUp = 10;
    }

    @Override
    public String ID() {
        return "TemporaryImmunity";
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
        return Ability.ACODE_PROPERTY;
    }

    @Override
    public boolean canBeUninvoked() {
        return true;
    }

    @Override
    public boolean isAutoInvoked() {
        return true;
    }

    @Override
    public void unInvoke() {
        super.unInvoke();
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if ((affected instanceof MOB)
            && (tickID == Tickable.TICKID_MOB)
            && ((--tickUp) == 0)) {
            tickUp = 10;
            makeLongLasting();
            for (int s = set.size() - 1; s >= 0; s--) {
                final Long L = set.elementAt(s).second;
                if ((System.currentTimeMillis() - L.longValue()) > IMMUNITY_TIME)
                    set.removeElementAt(s);
            }

            if (set.size() == 0) {
                unInvoke();
                return false;
            }
        }
        return super.tick(ticking, tickID);
    }

    @Override
    public String text() {
        if (set.size() == 0)
            return "";
        final StringBuffer str = new StringBuffer("");
        for (int s = 0; s < set.size(); s++)
            str.append(set.elementAt(s).first + "/" + set.elementAt(s).second.longValue() + ";");
        return str.toString();
    }

    @Override
    public void setMiscText(String str) {
        if (str.startsWith("+")) {
            str = str.substring(1);
            final int x = set.indexOfFirst(str);
            if (x >= 0)
                set.setElementAt(new Pair<String, Long>(str, Long.valueOf(System.currentTimeMillis())), x);
            else
                set.addElement(str, Long.valueOf(System.currentTimeMillis()));
        } else {
            set.clear();
            final List<String> V = CMParms.parseSemicolons(str, true);
            for (int v = 0; v < V.size(); v++) {
                final String s = V.get(v);
                final int x = s.indexOf('/');
                if (x > 0)
                    set.addElement(s.substring(0, x), Long.valueOf(CMath.s_long(s.substring(x + 1))));
            }
        }
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if ((msg.amITarget(affected))
            && (msg.tool() instanceof Ability)
            && (set.containsFirst(msg.tool().ID()))
            && (affected instanceof MOB)) {
            final MOB mob = (MOB) affected;
            if (!mob.amDead()) {
                if (msg.source() != msg.target())
                    mob.location().show(mob, msg.source(), CMMsg.MSG_OK_VISUAL, L("<S-NAME> seem(s) immune to @x1.", msg.tool().name()));
                return false;
            }
        }
        return true;
    }
}