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
package com.syncleus.aethermud.game.Abilities.Archon;

import com.syncleus.aethermud.game.Abilities.StdAbility;
import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.interfaces.Environmental;

import java.util.List;
import java.util.NoSuchElementException;


public class ArchonSkill extends StdAbility {
    private final static String localizedName = CMLib.lang().L("an Archon Skill");
    private final static String localizedStaticDisplay = CMLib.lang().L("(in the realms of greatest power)");

    @Override
    public String ID() {
        return "ArchonSkill";
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
    public boolean putInCommandlist() {
        return false;
    }

    @Override
    protected int canAffectCode() {
        return 0;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_ARCHON;
    }

    public MOB getTargetAnywhere(MOB mob, List<String> commands, Environmental givenTarget, boolean playerOnly) {
        return getTargetAnywhere(mob, commands, givenTarget, false, false, playerOnly);
    }

    public MOB getTargetAnywhere(MOB mob, List<String> commands, Environmental givenTarget, boolean quiet, boolean alreadyAffOk, boolean playerOnly) {
        MOB target = super.getTarget(mob, commands, givenTarget, true, alreadyAffOk);
        if (target != null)
            return target;

        String targetName = CMParms.combine(commands, 0);
        if ((givenTarget != null) && (givenTarget instanceof MOB))
            target = (MOB) givenTarget;
        else if ((targetName.length() == 0) && (mob.isInCombat()) && (castingQuality(mob, mob.getVictim()) == Ability.QUALITY_MALICIOUS))
            target = mob.getVictim();
        else if ((targetName.length() == 0) && (castingQuality(mob, mob) == Ability.QUALITY_BENEFICIAL_SELF))
            target = mob;
        else if ((targetName.length() == 0) && (abstractQuality() != Ability.QUALITY_MALICIOUS))
            target = mob;
        else if (targetName.equalsIgnoreCase("self") || targetName.equalsIgnoreCase("me"))
            target = mob;
        else if (targetName.length() > 0) {
            try {
                final List<MOB> targets = CMLib.map().findInhabitants(CMLib.map().rooms(), mob, targetName, 50);
                if (targets.size() > 0)
                    target = targets.get(CMLib.dice().roll(1, targets.size(), -1));
            } catch (final NoSuchElementException e) {
            }
        }

        if ((target == null) || ((playerOnly) && (target.isMonster()))) {
            if (CMLib.players().playerExists(targetName))
                target = CMLib.players().getLoadPlayer(targetName);
        }

        if ((target != null) && ((!playerOnly) || (!target.isMonster())))
            targetName = target.name();

        if (((target == null) || ((playerOnly) && (target.isMonster())))
            || ((givenTarget == null) && (!CMLib.flags().canBeSeenBy(target, mob)) && ((!CMLib.flags().canBeHeardMovingBy(target, mob)) || (!target.isInCombat())))) {
            if (!quiet) {
                if (targetName.trim().length() == 0)
                    mob.tell(L("You don't know of anyone called '@x1'.", targetName));
                else
                    mob.tell(L("You don't know of anyone called '@x1' here.", targetName));
            }
            return null;
        }

        if ((!alreadyAffOk) && (!isAutoInvoked()) && (target.fetchEffect(this.ID()) != null)) {
            if ((givenTarget == null) && (!quiet)) {
                if (target == mob)
                    mob.tell(L("You are already affected by @x1.", name()));
                else
                    mob.tell(target, null, null, L("<S-NAME> is already affected by @x1.", name()));
            }
            return null;
        }
        return target;
    }
}
