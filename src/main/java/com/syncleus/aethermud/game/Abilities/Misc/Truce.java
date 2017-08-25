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
package com.syncleus.aethermud.game.Abilities.Misc;

import com.syncleus.aethermud.game.Abilities.StdAbility;
import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Abilities.interfaces.DiseaseAffect;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMStrings;
import com.syncleus.aethermud.game.core.collections.CMList;
import com.syncleus.aethermud.game.core.collections.Pair;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.Iterator;
import java.util.List;


public class Truce extends StdAbility {
    private final static String localizedName = CMLib.lang().L("Truce");
    private static final String[] triggerStrings = I(new String[]{"DECLARETRUCE"});
    public boolean truceWithAnyone = false;
    public CMList<Pair<String, Long>> truces = new CMList<Pair<String, Long>>();

    @Override
    public String ID() {
        return "Truce";
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
        return CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public boolean putInCommandlist() {
        return false;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL;
    }

    @Override
    public void setMiscText(String newMiscText) {
        super.setMiscText(CMStrings.capitalizeAndLower(newMiscText));
        truceWithAnyone = ((newMiscText == null) || (newMiscText.trim().length() == 0));
    }

    public Pair<String, Long> getMyPair(final String name) {
        final long now = System.currentTimeMillis();
        for (final Iterator<Pair<String, Long>> i = truces.iterator(); i.hasNext(); ) {
            final Pair<String, Long> p = i.next();
            if (p != null) {
                if ((now - p.second.longValue()) > 30000)
                    i.remove();
                else if (p.first.equals(name))
                    return p;
            }
        }
        return null;
    }

    public boolean isTruceWith(final String name) {
        if (truceWithAnyone)
            return getMyPair(name) != null;
        else
            return (text().equals(name));
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        if (affected instanceof MOB) {
            final MOB mob = (MOB) affected;
            if (truceWithAnyone
                && (msg.sourceMinor() == CMMsg.TYP_DEATH)
                && (msg.tool() == mob)) {
                final Pair<String, Long> p = getMyPair(msg.source().Name());
                if (p != null)
                    p.second = Long.valueOf(System.currentTimeMillis());
                else
                    truces.add(new Pair<String, Long>(msg.source().Name(), Long.valueOf(System.currentTimeMillis())));
            }
        }
        super.executeMsg(myHost, msg);
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!(affected instanceof MOB))
            return true;

        final MOB mob = (MOB) affected;
        if ((msg.targetMajor(CMMsg.MASK_MALICIOUS))
            && (((msg.source() == mob) && (msg.target() != null) && (isTruceWith(msg.target().Name())))
            || ((msg.target() == mob) && (isTruceWith(msg.source().Name()))))
            && ((!(msg.tool() instanceof DiseaseAffect)) || (((DiseaseAffect) msg.tool()).isMalicious()))
            && (!msg.sourceMajor(CMMsg.MASK_ALWAYS))) {
            msg.source().tell(msg.source(), msg.target(), null, L("You have made peace with <T-NAMESELF>."));
            msg.source().makePeace(true);
            if (msg.target() instanceof MOB)
                ((MOB) msg.target()).makePeace(true);
            return false;
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> make(s) a truce with <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (msg.value() <= 0) {
                    success = beneficialAffect(mob, target, asLevel, auto ? 3 : 0) != null;
                    final Ability A = target.fetchEffect(ID());
                    if (A != null)
                        A.setMiscText(target.Name());
                }
                target.makePeace(true);
                if (mob.getVictim() == target)
                    mob.makePeace(true);
            }
        } else
            return maliciousFizzle(mob, target, auto ? "" : L("^S<S-NAME> tr(ys) to make <T-NAMESELF> fall asleep, but fails.^?"));

        // return whether it worked
        return success;
    }
}
