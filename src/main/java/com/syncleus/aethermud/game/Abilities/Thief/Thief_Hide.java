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
import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Exits.interfaces.Exit;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Environmental;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;


public class Thief_Hide extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Hide");
    private static final String[] triggerStrings = I(new String[]{"HIDE"});
    public int code = 0;
    private int bonus = 0;

    @Override
    public String ID() {
        return "Thief_Hide";
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
        return Ability.QUALITY_OK_SELF;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_STEALTHY;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT | USAGE_MANA;
    }

    @Override
    public int abilityCode() {
        return code;
    }

    @Override
    public void setAbilityCode(int newCode) {
        code = newCode;
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        if (!(affected instanceof MOB))
            return;

        final MOB mob = (MOB) affected;

        if (msg.amISource(mob)) {
            if (((msg.sourceMinor() == CMMsg.TYP_ENTER)
                || (msg.sourceMinor() == CMMsg.TYP_LEAVE)
                || (msg.sourceMinor() == CMMsg.TYP_FLEE)
                || (msg.sourceMinor() == CMMsg.TYP_RECALL))
                && (!msg.sourceMajor(CMMsg.MASK_ALWAYS))
                && (msg.sourceMajor() > 0)) {
                unInvoke();
                mob.recoverPhyStats();
                mob.recoverCharStats();
            } else if ((abilityCode() == 0)
                && (!msg.sourceMajor(CMMsg.MASK_ALWAYS))
                && (msg.othersMinor() != CMMsg.TYP_LOOK)
                && (msg.othersMinor() != CMMsg.TYP_EXAMINE)
                && (msg.othersMajor() > 0)) {
                if (msg.othersMajor(CMMsg.MASK_SOUND)) {
                    unInvoke();
                    mob.recoverPhyStats();
                    mob.recoverCharStats();
                } else
                    switch (msg.othersMinor()) {
                        case CMMsg.TYP_SPEAK:
                        case CMMsg.TYP_CAST_SPELL: {
                            unInvoke();
                            mob.recoverPhyStats();
                            mob.recoverCharStats();
                        }
                        break;
                        case CMMsg.TYP_OPEN:
                        case CMMsg.TYP_CLOSE:
                        case CMMsg.TYP_LOCK:
                        case CMMsg.TYP_UNLOCK:
                        case CMMsg.TYP_PUSH:
                        case CMMsg.TYP_PULL:
                            if (((msg.target() instanceof Exit)
                                || ((msg.target() instanceof Item)
                                && (!msg.source().isMine(msg.target()))))) {
                                unInvoke();
                                mob.recoverPhyStats();
                                mob.recoverCharStats();
                            }
                            break;
                    }
            }
        }
        return;
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        super.affectCharStats(affected, affectableStats);
        affectableStats.setStat(CharStats.STAT_SAVE_DETECTION, proficiency() + bonus + affectableStats.getStat(CharStats.STAT_SAVE_DETECTION));
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_HIDDEN);
        if (CMLib.flags().isSneaking(affected))
            affectableStats.setDisposition(affectableStats.disposition() - PhyStats.IS_SNEAKING);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Room room = mob.location();
        if ((mob.fetchEffect(this.ID()) != null) || (room == null)) {
            mob.tell(L("You are already hiding."));
            return false;
        }

        if (mob.isInCombat()) {
            mob.tell(L("Not while in combat!"));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final MOB highestMOB = getHighestLevelMOB(mob, null);
        final int levelDiff = (mob.phyStats().level() + (2 * getXLEVELLevel(mob))) - getMOBLevel(highestMOB);

        final String str = L("You creep into a shadow and remain completely still.");

        boolean success = (highestMOB == null) || proficiencyCheck(mob, levelDiff * 10, auto);

        if (!success) {
            if (highestMOB != null)
                beneficialVisualFizzle(mob, highestMOB, L("<S-NAME> attempt(s) to hide from <T-NAMESELF> and fail(s)."));
            else
                beneficialVisualFizzle(mob, null, L("<S-NAME> attempt(s) to hide and fail(s)."));
        } else {
            final CMMsg msg = CMClass.getMsg(mob, null, this, auto ? CMMsg.MSG_OK_ACTION : (CMMsg.MSG_DELICATE_HANDS_ACT | CMMsg.MASK_MOVE), str, CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null);
            if (room.okMessage(mob, msg)) {
                room.send(mob, msg);
                invoker = mob;
                final Ability newOne = (Ability) this.copyOf();
                ((Thief_Hide) newOne).bonus = getXLEVELLevel(mob) * 2;
                if (mob.fetchEffect(newOne.ID()) == null)
                    mob.addEffect(newOne);
                mob.recoverPhyStats();
            } else
                success = false;
        }
        return success;
    }
}