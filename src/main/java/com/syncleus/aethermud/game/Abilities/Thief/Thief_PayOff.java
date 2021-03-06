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
package com.syncleus.aethermud.game.Abilities.Thief;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Behaviors.interfaces.LegalBehavior;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.Law;
import com.syncleus.aethermud.game.Common.interfaces.LegalWarrant;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Thief_PayOff extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Pay Off");
    private static final String[] triggerStrings = I(new String[]{"PAYOFF"});

    @Override
    public String ID() {
        return "Thief_PayOff";
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
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public boolean disregardsArmorCheck(MOB mob) {
        return true;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
    }

    protected void clearWarrants(final MOB officer, final MOB invoker, final Room R, final Area A, final LegalBehavior B) {
        final List<LegalWarrant> warrants = B.getWarrantsOf(A, invoker);
        if ((warrants != null) && (warrants.size() > 0)) {
            boolean didSomething = false;
            for (LegalWarrant W : warrants) {
                if ((W.arrestingOfficer() == officer)
                    && (System.currentTimeMillis() > W.getIgnoreUntilTime())) {
                    if (W.state() != Law.STATE_SEEKING)
                        didSomething = true;
                    B.release(A, W);
                }
            }
            if (didSomething && (officer.location() == invoker.location())) {
                R.show(officer, invoker, CMMsg.MSG_HANDS, L("<S-NAME> wink(s) at <T-NAME>."));
            }
        }
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        final MOB invoker = this.invoker;
        if (affected instanceof MOB) {
            final MOB officer = (MOB) affected;
            final Room R = officer.location();
            if (R != null) {
                final LegalBehavior B = CMLib.law().getLegalBehavior(R);
                final Area A = (B == null) ? null : CMLib.law().getLegalObject(R);
                if ((A != null) && (B != null)) {
                    clearWarrants(officer, invoker, R, A, B);
                }
            }
        }
        return true;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (commands.size() < 1) {
            mob.tell(L("Pay Off Whom?"));
            return false;
        }
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if ((target.charStats().getStat(CharStats.STAT_INTELLIGENCE) < 3)
            || (!target.isMonster())) {
            mob.tell(L("You can't pay off @x1.", target.name(mob)));
            return false;
        }

        final LegalBehavior B = CMLib.law().getLegalBehavior(mob.location());
        final Area A = (B == null) ? null : CMLib.law().getLegalObject(mob.location());
        if ((A == null) || (B == null)) {
            mob.tell(L("There's no point in paying off @x1.", target.name(mob)));
            return false;
        }

        boolean isJudge = B.isJudge(A, target);
        if ((!isJudge) && (!B.isAnyOfficer(A, target))) {
            mob.tell(L("Paying off @x1 won't help you.", target.name(mob)));
            return false;
        }

        final List<LegalWarrant> warrants = B.getWarrantsOf(A, mob);
        if ((warrants == null) || (warrants.size() == 0)) {
            mob.tell(L("Pay off @x1? Why? You aren't in any trouble.", target.name(mob)));
            return false;
        }

        if (target.fetchEffect(ID()) != null) {
            mob.tell(L("@x1 is already paid off.", target.name(mob)));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        double amountRequired = CMLib.moneyCounter().getTotalAbsoluteNativeValue(target)
            + ((double) ((100l - ((mob.charStats().getStat(CharStats.STAT_CHARISMA) + (2l * getXLEVELLevel(mob))) * 2))) * target.phyStats().level());
        if (isJudge)
            amountRequired *= 2;

        final String currency = CMLib.moneyCounter().getCurrency(target);
        boolean success = proficiencyCheck(mob, 0, auto);

        if ((!success) || (CMLib.moneyCounter().getTotalAbsoluteValue(mob, currency) < amountRequired)) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, CMMsg.MSG_SPEAK, L("^T<S-NAME> attempt(s) to pay off <T-NAMESELF> to '@x1', but no deal is reached.^?", CMParms.combine(commands, 0)));
            if (mob.location().okMessage(mob, msg))
                mob.location().send(mob, msg);
            if (CMLib.moneyCounter().getTotalAbsoluteValue(mob, currency) < amountRequired) {
                final String costWords = CMLib.moneyCounter().nameCurrencyShort(currency, amountRequired);
                mob.tell(L("@x1 requires @x2 to do this.", target.charStats().HeShe(), costWords));
            }
            success = false;
        } else {
            final String costWords = CMLib.moneyCounter().nameCurrencyShort(target, amountRequired);
            final CMMsg msg = CMClass.getMsg(mob, target, this, CMMsg.MSG_THIEF_ACT, L("^T<S-NAME> pay(s) off <T-NAMESELF>.^?", CMParms.combine(commands, 0), costWords));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                CMLib.moneyCounter().subtractMoney(mob, currency, amountRequired);
                CMLib.moneyCounter().addMoney(mob, currency, amountRequired);
                if (isJudge) {
                    for (LegalWarrant W : warrants) {
                        if (W.punishment() > 0)
                            W.setPunishment(W.punishment() - 1);
                    }
                } else {
                    clearWarrants(target, mob, mob.location(), A, B);
                    super.beneficialAffect(mob, target, asLevel, 0);
                }
            }
            target.recoverPhyStats();
        }
        return success;
    }
}
