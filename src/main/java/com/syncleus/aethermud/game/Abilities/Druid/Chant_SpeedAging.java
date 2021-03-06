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
package com.syncleus.aethermud.game.Abilities.Druid;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.PlayerStats;
import com.syncleus.aethermud.game.Common.interfaces.TimeClock;
import com.syncleus.aethermud.game.Items.interfaces.*;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.Races.interfaces.Race;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Chant_SpeedAging extends Chant {
    private final static String localizedName = CMLib.lang().L("Speed Aging");

    @Override
    public String ID() {
        return "Chant_SpeedAging";
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
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_BREEDING;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    protected int overrideMana() {
        return Ability.COST_ALL;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Physical target = getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_ANY, true);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        int type = verbalCastCode(mob, target, auto);
        if ((target instanceof MOB)
            && (CMath.bset(type, CMMsg.MASK_MALICIOUS))
            && (((MOB) target).charStats().getStat(CharStats.STAT_AGE) > 0)) {
            final MOB mobt = (MOB) target;
            if (mobt.charStats().ageCategory() <= Race.AGE_CHILD)
                type = CMath.unsetb(type, CMMsg.MASK_MALICIOUS);
            else if ((mobt.getLiegeID().equals(mob.Name())) || (mobt.amFollowing() == mob))
                type = CMath.unsetb(type, CMMsg.MASK_MALICIOUS);
            else if ((mobt.charStats().ageCategory() <= Race.AGE_MATURE)
                && (mobt.getLiegeID().length() > 0))
                type = CMath.unsetb(type, CMMsg.MASK_MALICIOUS);
        }

        if ((target instanceof Item)
            || ((target instanceof MOB)
            && (((MOB) target).isMonster())
            && (CMLib.flags().isAnimalIntelligence((MOB) target))
            && (CMLib.law().doesHavePriviledgesHere(mob, mob.location())))) {
            type = CMath.unsetb(type, CMMsg.MASK_MALICIOUS);
        }

        boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, type, auto ? "" : L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final Ability A = target.fetchEffect("Age");
                if ((!(target instanceof MOB))
                    && (!(target instanceof CagedAnimal))
                    && (A == null)) {
                    if (target instanceof Food) {
                        mob.tell(L("@x1 rots away!", target.name(mob)));
                        ((Item) target).destroy();
                    } else if (target instanceof Item) {
                        switch (((Item) target).material() & RawMaterial.MATERIAL_MASK) {
                            case RawMaterial.MATERIAL_CLOTH:
                            case RawMaterial.MATERIAL_FLESH:
                            case RawMaterial.MATERIAL_LEATHER:
                            case RawMaterial.MATERIAL_PAPER:
                            case RawMaterial.MATERIAL_VEGETATION:
                            case RawMaterial.MATERIAL_WOODEN: {
                                mob.location().showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 rots away!", target.name()));
                                if (target instanceof Container)
                                    ((Container) target).emptyPlease(false);
                                ((Item) target).destroy();
                                break;
                            }
                            default:
                                mob.location().showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 ages, but nothing happens to it.", target.name()));
                                break;
                        }
                    } else
                        mob.location().showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 ages, but nothing happens to it.", target.name()));
                    success = false;
                } else if ((target instanceof MOB)
                    && ((A == null) || (A.displayText().length() == 0))) {
                    final MOB M = (MOB) target;
                    mob.location().show(M, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> age(s) a bit."));
                    if (M.baseCharStats().getStat(CharStats.STAT_AGE) <= 0)
                        M.setAgeMinutes(M.getAgeMinutes() + (M.getAgeMinutes() / 10));
                    else if ((M.playerStats() != null) && (M.playerStats().getBirthday() != null)) {
                        final TimeClock C = CMLib.time().localClock(M.getStartRoom());
                        final double aging = CMath.mul(M.baseCharStats().getStat(CharStats.STAT_AGE), .10);
                        int years = (int) Math.round(Math.floor(aging));
                        final int monthsInYear = C.getMonthsInYear();
                        int months = (int) Math.round(CMath.mul(aging - Math.floor(aging), monthsInYear));
                        if ((years <= 0) && (months == 0))
                            months++;
                        M.playerStats().getBirthday()[PlayerStats.BIRTHDEX_YEAR] -= years;
                        M.playerStats().getBirthday()[PlayerStats.BIRTHDEX_MONTH] -= months;
                        if (M.playerStats().getBirthday()[PlayerStats.BIRTHDEX_MONTH] < 1) {
                            M.playerStats().getBirthday()[PlayerStats.BIRTHDEX_YEAR]--;
                            years++;
                            M.playerStats().getBirthday()[PlayerStats.BIRTHDEX_MONTH] = monthsInYear + M.playerStats().getBirthday()[PlayerStats.BIRTHDEX_MONTH];
                        }
                        M.baseCharStats().setStat(CharStats.STAT_AGE, M.baseCharStats().getStat(CharStats.STAT_AGE) + years);
                    }
                    M.recoverPhyStats();
                    M.recoverCharStats();
                } else if (A != null) {
                    final long start = CMath.s_long(A.text());
                    long age = System.currentTimeMillis() - start;
                    final long millisPerMudday = CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY) * CMProps.getTickMillis();
                    if (age < millisPerMudday)
                        age = millisPerMudday;
                    final long millisPerMonth = CMLib.time().globalClock().getDaysInMonth() * millisPerMudday;
                    final long millisPerYear = CMLib.time().globalClock().getMonthsInYear() * millisPerMonth;
                    long ageBy = age / 10;
                    if (ageBy < millisPerMonth)
                        ageBy = millisPerMonth + 1;
                    else if (ageBy < millisPerYear)
                        ageBy = millisPerYear + 1;
                    A.setMiscText("" + (start - ageBy));
                    if (target instanceof MOB)
                        mob.location().show((MOB) target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> age(s) a bit."));
                    else
                        mob.location().showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 ages a bit.", target.name()));
                    target.recoverPhyStats();
                } else
                    return beneficialWordsFizzle(mob, target, L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));
            }
        } else if (CMath.bset(type, CMMsg.MASK_MALICIOUS))
            return maliciousFizzle(mob, target, L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));
        else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));

        // return whether it worked
        return success;
    }
}
