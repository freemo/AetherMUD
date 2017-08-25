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
package com.planet_ink.game.Abilities.Thief;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.CharStats;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.*;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;


public class Thief_CarefulStep extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Careful Step");
    private static final String[] triggerStrings = I(new String[]{"CARESTEP", "CAREFULSTEP"});

    @Override
    public String ID() {
        return "Thief_CarefulStep";
    }

    @Override
    public String name() {
        return localizedName;
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
    public double castingTime(final MOB mob, final List<String> cmds) {
        return CMProps.getSkillActionCost(ID(), CMath.div(CMProps.getIntVar(CMProps.Int.DEFABLETIME), 50.0));
    }

    @Override
    public double combatCastingTime(final MOB mob, final List<String> cmds) {
        return CMProps.getSkillCombatActionCost(ID(), CMath.div(CMProps.getIntVar(CMProps.Int.DEFCOMABLETIME), 50.0));
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_ACROBATIC;
    }

    @Override
    public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining) {
        if (secondsElapsed == 0) {
            String dir = CMParms.combine(commands, 0);
            if (commands.size() > 0)
                dir = commands.get(commands.size() - 1);
            final int dirCode = CMLib.directions().getGoodDirectionCode(dir);
            if (dirCode < 0) {
                mob.tell(L("Step where?"));
                return false;
            }
            if (mob.isInCombat()) {
                mob.tell(L("Not while you are fighting!"));
                return false;
            }

            if ((mob.location().getRoomInDir(dirCode) == null) || (mob.location().getExitInDir(dirCode) == null)) {
                mob.tell(L("Step where?"));
                return false;
            }
            final CMMsg msg = CMClass.getMsg(mob, null, this, auto ? CMMsg.MSG_OK_VISUAL : CMMsg.MSG_DELICATE_HANDS_ACT, L("<S-NAME> start(s) walking carefully @x1.", CMLib.directions().getDirectionName(dirCode)));
            if (mob.location().okMessage(mob, msg))
                mob.location().send(mob, msg);
            else
                return false;
        }
        return true;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        String dir = CMParms.combine(commands, 0);
        if (commands.size() > 0)
            dir = commands.get(commands.size() - 1);
        final int dirCode = CMLib.directions().getGoodDirectionCode(dir);
        if (!preInvoke(mob, commands, givenTarget, auto, asLevel, 0, 0.0))
            return false;

        final MOB highestMOB = getHighestLevelMOB(mob, null);
        int levelDiff = mob.phyStats().level() + (2 * getXLEVELLevel(mob)) - getMOBLevel(highestMOB);

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = false;
        final CMMsg msg = CMClass.getMsg(mob, null, this, auto ? CMMsg.MSG_OK_VISUAL : CMMsg.MSG_DELICATE_HANDS_ACT, L("<S-NAME> walk(s) carefully @x1.", CMLib.directions().getDirectionName(dirCode)));
        if (mob.location().okMessage(mob, msg)) {
            mob.location().send(mob, msg);
            if (levelDiff < 0)
                levelDiff = levelDiff * 8;
            else
                levelDiff = levelDiff * 10;
            success = proficiencyCheck(mob, levelDiff, auto);
            final int oldDex = mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY);
            if (success)
                mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY, oldDex + 100);
            mob.recoverCharStats();
            CMLib.tracking().walk(mob, dirCode, false, false);
            if (oldDex != mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY))
                mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY, oldDex);
            mob.recoverCharStats();
        }
        return success;
    }

}