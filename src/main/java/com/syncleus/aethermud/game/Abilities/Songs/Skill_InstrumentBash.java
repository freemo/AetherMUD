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
package com.planet_ink.game.Abilities.Songs;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.MusicalInstrument.InstrumentType;
import com.planet_ink.game.Items.interfaces.Weapon;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;


public class Skill_InstrumentBash extends BardSkill {
    private final static String localizedName = CMLib.lang().L("Instrument Bash");
    private static final String[] triggerStrings = I(new String[]{"INSTRUMENTBASH", "IBASH"});

    @Override
    public String ID() {
        return "Skill_InstrumentBash";
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
        return CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_DIRTYFIGHTING;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT;
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            final Item instrument = Play.getInstrument(mob, InstrumentType.OTHER_INSTRUMENT_TYPE, true);
            if (instrument == null)
                return Ability.QUALITY_INDIFFERENT;
            if ((CMLib.flags().isSitting(target) || CMLib.flags().isSleeping(target)))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        final Item instrument = Play.getInstrument(mob, InstrumentType.OTHER_INSTRUMENT_TYPE, true);
        if (instrument == null)
            return false;

        if ((CMLib.flags().isSitting(target) || CMLib.flags().isSleeping(target))) {
            mob.tell(L("@x1 must stand up first!", target.name(mob)));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        String str = null;
        if (success) {
            str = auto ? L("<T-NAME> is bashed!") : L("^F^<FIGHT^><S-NAME> bash(es) <T-NAMESELF> with @x1!^</FIGHT^>^?", instrument.name());
            final CMMsg msg = CMClass.getMsg(mob, target, this, CMMsg.MSK_MALICIOUS_MOVE | CMMsg.TYP_JUSTICE | (auto ? CMMsg.MASK_ALWAYS : 0), str);
            CMLib.color().fixSourceFightColor(msg);
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final Weapon w = CMClass.getWeapon("ShieldWeapon");
                if (w != null) {
                    w.setName(instrument.name());
                    w.setDisplayText(instrument.displayText());
                    w.setDescription(instrument.description());
                    w.basePhyStats().setDamage(instrument.phyStats().level() + 5 + (2 * getXLEVELLevel(mob)));
                    CMLib.combat().postAttack(mob, target, w);
                }
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> attempt(s) to bash <T-NAMESELF> with @x1, but end(s) up looking silly.", instrument.name()));

        return success;
    }

}