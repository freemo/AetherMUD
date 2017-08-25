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
package com.planet_ink.game.Abilities.Paladin;

import com.planet_ink.game.Abilities.StdAbility;
import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Items.interfaces.Weapon;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Environmental;
import com.planet_ink.game.core.interfaces.Physical;
import com.planet_ink.game.core.interfaces.Tickable;

import java.util.List;


public class Paladin_Defend extends StdAbility {
    private final static String localizedName = CMLib.lang().L("All Defence");
    private static final String[] triggerStrings = I(new String[]{"DEFENCE"});
    public boolean fullRound = false;

    @Override
    public String ID() {
        return "Paladin_Defend";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_SELF;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_EVASIVE;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if ((affected == null) || (!(affected instanceof MOB)) || (invoker == null))
            return true;

        final MOB mob = (MOB) affected;
        if (invoker.location() != mob.location())
            unInvoke();
        else {
            // preventing distracting player from doin anything else
            if (msg.amISource(invoker)
                && (msg.targetMinor() == CMMsg.TYP_WEAPONATTACK)) {
                invoker.location().show((MOB) affected, msg.target(), CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> defend(s) <S-HIM-HERSELF> against <T-NAME>."));
                return false;
            }
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        super.executeMsg(myHost, msg);
        if ((affected == null) || (!(affected instanceof MOB)) || (invoker == null))
            return;
        if ((msg.amITarget(affected))
            && (msg.targetMinor() == CMMsg.TYP_DAMAGE)
            && (msg.tool() instanceof Weapon))
            fullRound = false;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        affectableStats.setArmor(affectableStats.armor() - 20 - (4 * getXLEVELLevel(invoker())));
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if (tickID == Tickable.TICKID_MOB) {
            if (fullRound) {
                final MOB mob = (MOB) affected;
                if (!mob.isInCombat())
                    unInvoke();
                if (mob.location() != null) {
                    if (mob.location().show(mob, null, this, CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> successful defence <S-HAS-HAVE> allowed <S-HIM-HER> to disengage."))) {
                        final MOB victim = mob.getVictim();
                        if ((victim != null) && (victim.getVictim() == mob))
                            victim.makePeace(true);
                        mob.makePeace(true);
                        unInvoke();
                    }
                }
            }
            fullRound = true;
        }
        return true;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (!CMLib.flags().isAliveAwakeMobile(mob, false))
            return false;

        final Ability A = mob.fetchEffect(ID());
        if (A != null) {
            A.unInvoke();
            mob.tell(L("You end your all-out defensive posture."));
            return true;
        }
        if (!mob.isInCombat()) {
            mob.tell(L("You must be in combat to defend!"));
            return false;
        }

        if ((!auto) && (!(CMLib.flags().isGood(mob)))) {
            mob.tell(L("You don't feel worthy of a good defence."));
            return false;
        }
        if (!super.invoke(mob, commands, mob, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, null, this, CMMsg.MSG_CAST_SOMANTIC_SPELL, L("^S<S-NAME> assume(s) an all-out defensive posture.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                fullRound = false;
                beneficialAffect(mob, mob, asLevel, Ability.TICKS_FOREVER);
            }
        } else
            return beneficialVisualFizzle(mob, null, L("<S-NAME> attempt(s) to assume an all-out defensive posture, but fail(s)."));

        // return whether it worked
        return success;
    }
}