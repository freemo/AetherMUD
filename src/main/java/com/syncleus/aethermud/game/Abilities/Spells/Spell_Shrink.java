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
package com.planet_ink.game.Abilities.Spells;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.CharStats;
import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.Wearable;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;


public class Spell_Shrink extends Spell {

    private final static String localizedName = CMLib.lang().L("Shrink");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Shrunk)");

    @Override
    public String ID() {
        return "Spell_Shrink";
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
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    protected int canAffectCode() {
        return CAN_ITEMS | CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return CAN_MOBS | CAN_ITEMS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_TRANSMUTATION;
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        MOB recheckMOB = null;
        if (canBeUninvoked()) {
            if (affected instanceof MOB) {
                final MOB mob = (MOB) affected;
                if ((mob.location() != null) && (!mob.amDead()))
                    mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> return(s) to <S-HIS-HER> normal size."));
                recheckMOB = mob;
            } else if (affected instanceof Item) {
                final Item item = (Item) affected;
                if (item.owner() != null) {
                    if (item.owner() instanceof Room)
                        ((Room) item.owner()).showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 returns to its proper size.", item.name()));
                    else if (item.owner() instanceof MOB) {
                        ((MOB) item.owner()).tell(L("@x1 returns to its proper size.", item.name()));
                        recheckMOB = (MOB) item.owner();
                    }
                }
            }
        }
        super.unInvoke();
        if (recheckMOB != null)
            CMLib.utensils().confirmWearability(recheckMOB);
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        super.affectCharStats(affected, affectableStats);
        final int str = affectableStats.getStat(CharStats.STAT_STRENGTH);
        final int baseDex = affected.baseCharStats().getStat(CharStats.STAT_DEXTERITY);
        affectableStats.setStat(CharStats.STAT_STRENGTH, (str / 10) + 1);
        if (affectableStats.getStat(CharStats.STAT_DEXTERITY) <= baseDex + 5)
            affectableStats.setStat(CharStats.STAT_DEXTERITY, baseDex + 5);
    }

    @Override
    public void affectPhyStats(Physical host, PhyStats affectedStats) {
        super.affectPhyStats(host, affectedStats);
        int height = (int) Math.round(affectedStats.height() * 0.10);
        if (height == 0)
            height = 1;
        affectedStats.setHeight(height);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Physical target = getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_UNWORNONLY);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);
        if ((success) && ((target instanceof MOB) || (target instanceof Item))) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? L("<T-NAME> feel(s) somewhat smaller.") : L("^S<S-NAME> cast(s) a small spell on <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                boolean isJustUnInvoking = false;
                if (target instanceof Item) {
                    final Ability A = target.fetchEffect("Spell_Shrink");
                    if ((A != null) && (A.canBeUninvoked())) {
                        A.unInvoke();
                        isJustUnInvoking = true;
                    }
                } else if (target instanceof MOB) {
                    final Ability A = target.fetchEffect("Spell_Grow");
                    if ((A != null) && (A.canBeUninvoked())) {
                        A.unInvoke();
                        isJustUnInvoking = true;
                    }
                }

                if ((!isJustUnInvoking) && (msg.value() <= 0)) {
                    beneficialAffect(mob, target, asLevel, 0);
                    if (target instanceof MOB)
                        CMLib.utensils().confirmWearability((MOB) target);
                }
            }
        } else
            beneficialWordsFizzle(mob, target, L("<S-NAME> attempt(s) to cast a small spell, but fail(s)."));

        return success;
    }
}