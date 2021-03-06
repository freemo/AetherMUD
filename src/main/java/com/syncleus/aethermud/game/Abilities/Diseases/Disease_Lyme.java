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
package com.syncleus.aethermud.game.Abilities.Diseases;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Abilities.interfaces.DiseaseAffect;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.CMSecurity;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Tickable;


public class Disease_Lyme extends Disease {
    private final static String localizedName = CMLib.lang().L("Lyme Disease");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Lyme Disease)");
    int days = 0;

    @Override
    public String ID() {
        return "Disease_Lyme";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return localizedStaticDisplay;
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
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
    protected int DISEASE_TICKS() {
        return 9 * CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
    }

    @Override
    protected int DISEASE_DELAY() {
        return CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
    }

    @Override
    protected String DISEASE_DONE() {
        return L("Your lyme disease goes away.");
    }

    @Override
    protected String DISEASE_START() {
        return L("^G<S-NAME> get(s) lyme disease!^?");
    }

    @Override
    protected String DISEASE_AFFECT() {
        return "";
    }

    @Override
    public int spreadBitmap() {
        return DiseaseAffect.SPREAD_CONSUMPTION | DiseaseAffect.SPREAD_DAMAGE;
    }

    @Override
    public int difficultyLevel() {
        return 5;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!(affected instanceof MOB))
            return super.okMessage(myHost, msg);

        final MOB mob = (MOB) affected;

        // when this spell is on a MOBs Affected list,
        // it should consistantly prevent the mob
        // from trying to do ANYTHING except sleep
        if ((msg.amISource(mob))
            && (days > 0)
            && (msg.tool() instanceof Ability)
            && (mob.fetchAbility(msg.tool().ID()) == msg.tool())
            && (CMLib.dice().rollPercentage() > (mob.charStats().getSave(CharStats.STAT_SAVE_MIND) + 25))) {
            mob.tell(L("Your headaches make you forget @x1!", msg.tool().name()));
            return false;
        }

        return super.okMessage(myHost, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if (affected == null)
            return false;
        if (!(affected instanceof MOB))
            return true;

        final MOB mob = (MOB) affected;
        if ((!mob.amDead()) && (getTickDownRemaining() == 1)) {
            MOB diseaser = invoker;
            if (diseaser == null)
                diseaser = mob;
            Ability A = null;
            if (CMLib.dice().rollPercentage() > 50)
                A = CMClass.getAbility("Disease_Fever");
            else if (CMLib.dice().rollPercentage() > 50)
                A = CMClass.getAbility("Disease_Amnesia");
            else if (CMLib.dice().rollPercentage() > 50)
                A = CMClass.getAbility("Disease_Arthritis");
            else
                A = CMClass.getAbility("Disease_Fever");
            if (A != null) {
                A.invoke(diseaser, mob, true, 0);
                A = mob.fetchEffect(A.ID());
                if ((A != null) && (!CMSecurity.isAbilityDisabled(A.ID())))
                    A.makeLongLasting();
            }
        } else if ((!mob.amDead()) && ((--diseaseTick) <= 0)) {
            days++;
            diseaseTick = DISEASE_DELAY();
            if (CMLib.dice().rollPercentage() < mob.charStats().getSave(CharStats.STAT_SAVE_DISEASE)) {
                unInvoke();
                return false;
            }
            return true;
        }
        return true;
    }
}
