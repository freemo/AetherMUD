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
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Scroll;
import com.syncleus.aethermud.game.Libraries.interfaces.MoneyLibrary;
import com.syncleus.aethermud.game.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.interfaces.LandTitle;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Thief_Forgery extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Forgery");
    private static final String[] triggerStrings = I(new String[]{"FORGERY"});

    @Override
    public String ID() {
        return "Thief_Forgery";
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
        return Ability.CAN_MOBS;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_CALLIGRAPHY;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (commands.size() < 2) {
            mob.tell(L("What would you like to forge, and onto what?"));
            return false;
        }
        final Item target = mob.findItem(null, commands.get(commands.size() - 1));
        if ((target == null) || (!CMLib.flags().canBeSeenBy(target, mob))) {
            mob.tell(L("You don't see '@x1' here.", (commands.get(commands.size() - 1))));
            return false;
        }
        commands.remove(commands.size() - 1);

        if ((!target.isGeneric())
            || ((!(target instanceof Scroll)) && (!target.isReadable()))) {
            mob.tell(L("You can't forge anything on that."));
            return false;
        }

        String forgeWhat = CMParms.combine(commands, 0);
        if (forgeWhat.length() == 0) {
            mob.tell(L("Forge what onto '@x1'?  Try a spell name, a room ID, or a bank note name.", target.name(mob)));
            return false;
        }

        String newName = "";
        String newDisplay = "";
        String newDescription = "";
        String newSecretIdentity = "";
        final Room room = CMLib.map().getRoom(forgeWhat);
        if (room != null) {
            final Item I = CMClass.getItem("StdTitle");
            ((LandTitle) I).setLandPropertyID(CMLib.map().getExtendedRoomID(room));
            newName = I.name();
            newDescription = I.description();
            newDisplay = I.displayText();
            newSecretIdentity = I.secretIdentity();
        }
        if (newName.length() == 0) {
            final Ability A = CMClass.findAbility(forgeWhat);
            if ((A != null) && ((A.classificationCode() & Ability.ALL_ACODES) != Ability.ACODE_SPELL)) {
                mob.tell(L("You can't forge '@x1'.", A.name()));
                return false;
            } else if (A != null) {
                if (!(target instanceof Scroll)) {
                    mob.tell(L("You can only forge a spell onto real scrollpaper."));
                    return false;
                } else if (((Scroll) target).getSpells().size() > 0) {
                    mob.tell(L("That already has real spells on it!"));
                    return false;
                } else {
                    newName = target.name();
                    newDisplay = target.displayText();
                    newDescription = target.description();
                    newSecretIdentity = "a scroll of " + A.name() + " Charges: 10\n";
                }
            }
        }
        if (newName.length() == 0) {
            final MoneyLibrary.MoneyDenomination[] DV = CMLib.moneyCounter().getCurrencySet(CMLib.moneyCounter().getCurrency(mob));
            for (final MoneyDenomination element : DV) {
                final Item note = CMLib.moneyCounter().makeBestCurrency(CMLib.moneyCounter().getCurrency(mob), element.value());
                if ((note != null) && (CMLib.english().containsString(note.name(), forgeWhat))) {
                    newName = note.name();
                    newDisplay = note.displayText();
                    newDescription = note.description();
                    newSecretIdentity = note.rawSecretIdentity();
                    break;
                }
            }
        }
        if (newName.length() == 0) {
            mob.tell(L("You don't know how to forge a '@x1'.  Try a spell name, a room ID, or a bank note name.", forgeWhat));
            return false;
        }
        forgeWhat = newName;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        int levelDiff = (mob.phyStats().level() + (2 * getXLEVELLevel(mob))) - target.phyStats().level();
        if (levelDiff > 0)
            levelDiff = 0;
        levelDiff *= 5;
        final boolean success = proficiencyCheck(mob, levelDiff, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, CMMsg.MSG_THIEF_ACT, L("<S-NAME> forge(s) @x1 on <T-NAMESELF>.", forgeWhat));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                target.setName(newName);
                target.setDescription(newDescription);
                target.setDisplayText(newDisplay);
                target.setSecretIdentity(newSecretIdentity);
            }
        } else
            beneficialVisualFizzle(mob, target, L("<S-NAME> attempt(s) to forge @x1, but fail(s).", forgeWhat));
        return success;
    }
}
