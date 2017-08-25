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
package com.planet_ink.game.Abilities.Ranger;

import com.planet_ink.game.Abilities.StdAbility;
import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMParms;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


public class Ranger_Sneak extends StdAbility {
    private final static String localizedName = CMLib.lang().L("Woodland Sneak");
    private static final String[] triggerStrings = I(new String[]{"WSNEAK"});

    @Override
    public String ID() {
        return "Ranger_Sneak";
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
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_STEALTHY;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT;
    }

    public int getMOBLevel(MOB meMOB) {
        if (meMOB == null)
            return 0;
        return meMOB.phyStats().level();
    }

    public MOB getHighestLevelMOB(MOB meMOB, Vector<MOB> not) {
        if (meMOB == null)
            return null;
        final Room R = meMOB.location();
        if (R == null)
            return null;
        int highestLevel = 0;
        MOB highestMOB = null;
        final Set<MOB> H = meMOB.getGroupMembers(new HashSet<MOB>());
        if (not != null)
            H.addAll(not);
        for (int i = 0; i < R.numInhabitants(); i++) {
            final MOB M = R.fetchInhabitant(i);
            if ((M != null)
                && (M != meMOB)
                && (!H.contains(M))
                && (highestLevel < M.phyStats().level())) {
                highestLevel = M.phyStats().level();
                highestMOB = M;
            }
        }
        return highestMOB;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        String dir = CMParms.combine(commands, 0);
        if (commands.size() > 0)
            dir = commands.get(commands.size() - 1);
        final int dirCode = CMLib.directions().getGoodDirectionCode(dir);
        if (dirCode < 0) {
            mob.tell(L("Sneak where?"));
            return false;
        }

        if ((!CMLib.flags().isInWilderness(mob)) && (!auto)) {
            mob.tell(L("You must be in the wilderness to do this."));
            return false;
        }
        if (((mob.location().domainType() == Room.DOMAIN_OUTDOORS_CITY)
            || (mob.location().domainType() == Room.DOMAIN_OUTDOORS_SPACEPORT))
            && (!auto)) {
            mob.tell(L("You don't know how to sneak around a place like this."));
            return false;
        }

        if ((mob.location().getRoomInDir(dirCode) == null) || (mob.location().getExitInDir(dirCode) == null)) {
            mob.tell(L("Sneak where?"));
            return false;
        }

        final MOB highestMOB = getHighestLevelMOB(mob, null);
        int levelDiff = (mob.phyStats().level() + (2 * getXLEVELLevel(mob))) - getMOBLevel(highestMOB);

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = false;
        final CMMsg msg = CMClass.getMsg(mob, null, this, auto ? CMMsg.MSG_OK_VISUAL : CMMsg.MSG_DELICATE_HANDS_ACT, L("You quietly sneak @x1.", CMLib.directions().getDirectionName(dirCode)), CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null);
        if (mob.location().okMessage(mob, msg)) {
            mob.location().send(mob, msg);
            if (levelDiff < 0)
                levelDiff = levelDiff * 10;
            else
                levelDiff = levelDiff * 5;
            success = proficiencyCheck(mob, levelDiff, auto);

            if (success) {
                mob.basePhyStats().setDisposition(mob.basePhyStats().disposition() | PhyStats.IS_SNEAKING);
                mob.recoverPhyStats();
            }
            CMLib.tracking().walk(mob, dirCode, false, false);
            if (success) {

                final int disposition = mob.basePhyStats().disposition();
                if ((disposition & PhyStats.IS_SNEAKING) > 0) {
                    mob.basePhyStats().setDisposition(disposition - PhyStats.IS_SNEAKING);
                    mob.recoverPhyStats();
                }
            }
        }
        return success;
    }

}