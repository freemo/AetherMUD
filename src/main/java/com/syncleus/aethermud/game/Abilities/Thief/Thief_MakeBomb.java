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
import com.syncleus.aethermud.game.Abilities.interfaces.Trap;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMStrings;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


public class Thief_MakeBomb extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Make Bombs");
    private static final String[] triggerStrings = I(new String[]{"BOMB"});

    @Override
    public String ID() {
        return "Thief_MakeBomb";
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
        return Ability.CAN_ITEMS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_TRAPPING;
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
    public int usageType() {
        return USAGE_MOVEMENT | USAGE_MANA;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        Trap theTrap = null;
        final Vector<Trap> traps = new Vector<Trap>();
        final int qualifyingClassLevel = CMLib.ableMapper().qualifyingClassLevel(mob, this) + (getXLEVELLevel(mob));
        for (final Enumeration<Ability> a = CMClass.abilities(); a.hasMoreElements(); ) {
            final Ability A = a.nextElement();
            if ((A instanceof Trap)
                && (((Trap) A).isABomb())
                && (((Trap) A).maySetTrap(mob, qualifyingClassLevel)))
                traps.addElement((Trap) A);
        }
        final int colWidth = CMLib.lister().fixColWidth(15, mob.session());
        Physical trapThis = givenTarget;
        if (trapThis != null)
            theTrap = traps.elementAt(CMLib.dice().roll(1, traps.size(), -1));
        else if (CMParms.combine(commands, 0).equalsIgnoreCase("list")) {
            final StringBuffer buf = new StringBuffer(L("@x1 Requires\n\r", CMStrings.padRight(L("Bomb Name"), colWidth)));
            for (int r = 0; r < traps.size(); r++) {
                final Trap T = traps.elementAt(r);
                buf.append(CMStrings.padRight(T.name(), colWidth) + " ");
                buf.append(T.requiresToSet() + "\n\r");
            }
            if (mob.session() != null)
                mob.session().safeRawPrintln(buf.toString());
            return true;
        } else {
            if (commands.size() < 2) {
                mob.tell(L("Make a bomb from what, with what kind of bomb? Use bomb list for a list."));
                return false;
            }
            final String name = commands.get(commands.size() - 1);
            commands.remove(commands.size() - 1);
            for (int r = 0; r < traps.size(); r++) {
                final Trap T = traps.elementAt(r);
                if (CMLib.english().containsString(T.name(), name))
                    theTrap = T;
            }
            if (theTrap == null) {
                mob.tell(L("'@x1' is not a valid bomb name.  Try BOMB LIST.", name));
                return false;
            }

            trapThis = this.getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_UNWORNONLY);
            if (trapThis == null)
                return false;
            if ((!auto) && (!theTrap.canSetTrapOn(mob, trapThis)))
                return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, +((mob.phyStats().level() + (getXLEVELLevel(mob) * 3)
            - trapThis.phyStats().level()) * 3), auto);
        final Trap theOldTrap = CMLib.utensils().fetchMyTrap(trapThis);
        if (theOldTrap != null) {
            if (theOldTrap.disabled())
                success = false;
            else {
                theOldTrap.spring(mob);
                return false;
            }
        }

        final CMMsg msg = CMClass.getMsg(mob, trapThis, this, auto ? CMMsg.MSG_OK_ACTION : CMMsg.MSG_THIEF_ACT, CMMsg.MASK_ALWAYS | CMMsg.MSG_THIEF_ACT, CMMsg.MSG_OK_ACTION, (auto ? L("@x1 begins to glow!", trapThis.name()) : L("<S-NAME> attempt(s) to make a bomb out of <T-NAMESELF>.")));
        if (mob.location().okMessage(mob, msg)) {
            mob.location().send(mob, msg);
            if (success) {
                mob.tell(L("You have completed your task."));
                theTrap.setTrap(mob, trapThis, getXLEVELLevel(mob), adjustedLevel(mob, asLevel), false);
            } else {
                if (CMLib.dice().rollPercentage() > 50) {
                    final Trap T = theTrap.setTrap(mob, trapThis, getXLEVELLevel(mob), adjustedLevel(mob, asLevel), false);
                    mob.location().show(mob, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> set(s) the bomb off on accident!"));
                    T.spring(mob);
                } else {
                    mob.tell(L("You fail in your attempt."));
                }
            }
        }
        return success;
    }
}
