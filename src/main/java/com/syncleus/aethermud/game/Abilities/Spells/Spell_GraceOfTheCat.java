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
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;

import java.util.List;


public class Spell_GraceOfTheCat extends Spell {

    private final static String localizedName = CMLib.lang().L("Grace Of The Cat");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Grace Of The Cat)");
    int increase = -1;

    @Override
    public String ID() {
        return "Spell_GraceOfTheCat";
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
        return Ability.QUALITY_BENEFICIAL_OTHERS;
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_TRANSMUTATION;
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        super.affectCharStats(affected, affectableStats);
        if (increase <= 0) {
            increase = 1;
            if (affectableStats.getCurrentClass().baseClass().equals("Thief"))
                increase = 2;
            if (affectableStats.getCurrentClass().baseClass().equals("Bard"))
                increase = 2;
            if (affectableStats.getCurrentClass().baseClass().equals("Fighter"))
                increase = 3;
            if (affectableStats.getCurrentClass().baseClass().equals("Mage"))
                increase = 2;
            if (affectableStats.getCurrentClass().baseClass().equals("Cleric"))
                increase = 1;
            if (affectableStats.getCurrentClass().baseClass().equals("Druid"))
                increase = 1;
            increase += (getXLEVELLevel(invoker()) + 2) / 3;
        }
        affectableStats.setStat(CharStats.STAT_DEXTERITY, affectableStats.getStat(CharStats.STAT_DEXTERITY) + increase);
    }

    @Override
    public void unInvoke() {
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();
        if (canBeUninvoked())
            mob.tell(L("You begin to feel more like your regular clumsy self."));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        // now see if it worked
        final boolean success = proficiencyCheck(mob, 0, auto);
        Room R = target.location();
        if ((success) && (R != null)) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, somanticCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> speak(s) and gesture(s) to <T-NAMESELF>.^?"));
            if (R.okMessage(mob, msg)) {
                R.send(mob, msg);
                R.show(target, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> move(s) more gracefully!"));
                beneficialAffect(mob, target, asLevel, 0);
            }
        } else
            return beneficialVisualFizzle(mob, target, L("<S-NAME> speak(s) gracefully to <T-NAMESELF>, but nothing more happens."));

        // return whether it worked
        return success;
    }
}