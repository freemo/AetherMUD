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
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;
import java.util.Vector;


public class Thief_Surrender extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Surrender");
    private static final String[] triggerStrings = I(new String[]{"SURRENDER"});

    @Override
    public String ID() {
        return "Thief_Surrender";
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
        return Ability.QUALITY_OK_SELF;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Vector<MOB> theList = new Vector<MOB>();
        int gold = 0;
        for (int i = 0; i < mob.location().numInhabitants(); i++) {
            final MOB vic = mob.location().fetchInhabitant(i);
            if ((vic != null) && (vic != mob) && (vic.isInCombat()) && (vic.getVictim() == mob)) {
                gold += (vic.phyStats().level() * 100) - (2 * getXLEVELLevel(mob));
                theList.addElement(vic);
            }
        }
        final double goldRequired = gold;
        if ((!mob.isInCombat()) || (theList.size() == 0)) {
            mob.tell(L("There's no one to surrender to!"));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);

        final String localCurrency = CMLib.moneyCounter().getCurrency(mob.getVictim());
        final String costWords = CMLib.moneyCounter().nameCurrencyShort(localCurrency, goldRequired);
        if (success && CMLib.moneyCounter().getTotalAbsoluteValue(mob, localCurrency) >= goldRequired) {
            final StringBuffer enemiesList = new StringBuffer("");
            for (int v = 0; v < theList.size(); v++) {
                final MOB vic = theList.elementAt(v);
                if (v == 0)
                    enemiesList.append(vic.name());
                else if (v == theList.size() - 1)
                    enemiesList.append(", and " + vic.name());
                else
                    enemiesList.append(", " + vic.name());
            }
            final CMMsg msg = CMClass.getMsg(mob, null, this, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> surrender(s) to @x1, paying @x2.", enemiesList.toString(), costWords));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                CMLib.moneyCounter().subtractMoney(mob, localCurrency, goldRequired);
                mob.recoverPhyStats();
                mob.makePeace(true);
                for (int v = 0; v < theList.size(); v++) {
                    final MOB vic = theList.elementAt(v);
                    CMLib.moneyCounter().addMoney(vic, localCurrency, CMath.div(goldRequired, theList.size()));
                    vic.recoverPhyStats();
                    vic.makePeace(true);
                }
            } else
                success = false;
        } else
            beneficialVisualFizzle(mob, null, L("<S-NAME> attempt(s) to surrender and fail(s)."));
        return success;
    }
}
