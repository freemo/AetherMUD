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
package com.syncleus.aethermud.game.Abilities.Spells;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.PlayerStats;
import com.syncleus.aethermud.game.Common.interfaces.TimeClock;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.Races.interfaces.Race;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Spell_Youth extends Spell {

    private final static String localizedName = CMLib.lang().L("Youth");

    @Override
    public String ID() {
        return "Spell_Youth";
    }

    @Override
    public String name() {
        return localizedName;
    }

    public int overridemana() {
        return Ability.COST_ALL;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_TRANSMUTATION;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_OTHERS;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);

        final CMMsg msg = CMClass.getMsg(mob, target, this, somanticCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> wave(s) <S-HIS-HER> arms around <T-NAMESELF>, drawing forth <T-HIS-HER> youthful self.^?"));
        if (success) {
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if ((target.baseCharStats().getStat(CharStats.STAT_AGE) <= 0)
                    || (target.baseCharStats().ageCategory() <= Race.AGE_YOUNGADULT)) {
                    mob.tell(mob, target, null, L("The magic appears to have had no effect upon <T-NAME>."));
                    success = false;
                } else {
                    final int[] chart = target.baseCharStats().getMyRace().getAgingChart();
                    mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> grow(s) younger!"));
                    final int cat = target.baseCharStats().ageCategory();
                    int age = target.baseCharStats().getStat(CharStats.STAT_AGE);
                    if (cat >= Race.AGE_ANCIENT) {
                        final int diff = chart[Race.AGE_ANCIENT] - chart[Race.AGE_VENERABLE];
                        age = age - chart[Race.AGE_ANCIENT];
                        final int num = (diff > 0) ? (int) Math.abs(Math.floor(CMath.div(age, diff))) : 0;
                        if (num <= 0)
                            age = (int) Math.round(CMath.div(chart[cat] + chart[cat - 1], 2.0));
                        else
                            age = target.baseCharStats().getStat(CharStats.STAT_AGE) - diff;
                    } else
                        age = (int) Math.round(CMath.div(chart[cat] + chart[cat - 1], 2.0));
                    if (target.playerStats() != null) {
                        final TimeClock C = CMLib.time().localClock(target.getStartRoom());
                        target.playerStats().getBirthday()[PlayerStats.BIRTHDEX_YEAR] = C.getYear() - age;
                        final int day = C.getDayOfMonth();
                        final int month = C.getMonth();
                        final int bday = mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_MONTH];
                        final int bmonth = mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_DAY];
                        if ((month < bmonth) || ((month == bmonth) && (day < bday)))
                            age--;
                        target.baseCharStats().setStat(CharStats.STAT_AGE, age);
                    } else
                        target.baseCharStats().setStat(CharStats.STAT_AGE, age);
                    target.recoverCharStats();
                    target.recoverPhyStats();
                }
            }
        } else
            beneficialVisualFizzle(mob, target, L("<S-NAME> wave(s) <S-HIS-HER> arms around <T-NAMESELF>, but the spell fizzles."));

        // return whether it worked
        return success;
    }

}
