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
package com.syncleus.aethermud.game.Abilities.Prayers;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.Social;
import com.syncleus.aethermud.game.Items.interfaces.Weapon;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.*;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class Prayer_PietyCurse extends Prayer {
    private final static String localizedName = CMLib.lang().L("Piety Curse");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Piety Curse)");
    private static String[] UNPIOUS_SOCIALS =
        {"AARGH", "BITE", "BLEED", "BOAST", "BONK", "BOUNCE", "BRICK", "BURP", "CAMEL", "CHALLENGE", "CHARGE",
            "COLLAPSE", "COUGH", "CRITICIZE", "CUDDLE", "CURSE", "DANCE", "DISCODANCE", "EGRIN", "EMBRACE", "EPOKE",
            "EXPLODE", "FART", "FIST", "FLASH", "FLIRT", "FLUTTER", "FONDLE", "FRENCH", "FUME", "GOOSE", "GROPE", "HOWL",
            "HUSH", "LAP", "LBITE", "LICK", "LIVER", "LUST", "MAKE", "MASSAGE", "MATE", "MISCHIEVOUS", "MOO", "MOON", "MOSH",
            "NIBBLE", "NOOGIE", "NTWIST", "OGLE", "OOGLE", "PANT", "PECK", "PINCH", "POUNCEPUKE", "PUNCH", "PURR", "ROAR",
            "RUB", "ROLLOVER", "SCREAM", "SLAP", "SLOBBER", "SMOOCH", "SMURF", "SNARL", "SNORT", "SNUGGLE", "SPANK", "SPIT",
            "SPOON", "STRANGLE", "TACKLE", "THREATEN", "TONGUE", "TOUT", "WEDGIE", "VOODOO", "WHIP", "WIGGLE", "WIGGY",
            "WRESTLE", "ZERBERT"};
    private static Set<String> UNPIOS_SET = CMParms.toStringSet(Arrays.asList(UNPIOUS_SOCIALS), new TreeSet<String>());

    @Override
    public String ID() {
        return "Prayer_PietyCurse";
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
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_CURSING;
    }

    @Override
    public long flags() {
        return Ability.FLAG_HOLY;
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return Ability.CAN_MOBS;
    }

    @Override
    public void unInvoke() {
        if (!(affected instanceof MOB))
            return;
        // undo the affects of this spell
        final MOB mob = (MOB) affected;
        super.unInvoke();
        if ((canBeUninvoked() && (!mob.amDead()))) {
            mob.tell(L("Your piety curse has been lifted"));
            if (mob.isMonster() && (!mob.amDead())) {
                CMLib.commands().postStand(mob, true);
                CMLib.tracking().wanderAway(mob, false, true);
                if ((invoker != null) && (invoker != mob) && (invoker.location() == mob.location()))
                    CMLib.combat().postAttack(mob, invoker, mob.fetchWieldedItem());
            }
        }
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        final Physical affected = this.affected;
        if ((affected instanceof MOB) && (((MOB) affected).isMonster())) {
            final MOB mob = (MOB) affected;
            if (mob.isInCombat())
                CMLib.combat().postPanic(mob, null);
        }
        return true;
    }

    @Override
    public void executeMsg(Environmental myHost, CMMsg msg) {
        super.executeMsg(myHost, msg);
        if (msg.source() == affected) {
            int damageToDo = 0;
            final String filterPatternStart = CMProps.getFilterPattern().substring(0, 3);
            if ((msg.sourceMinor() == CMMsg.TYP_SPEAK)
                && (msg.sourceMessage() != null)) {
                final String sayMessage = CMStrings.getSayFromMessage(msg.sourceMessage());
                if (sayMessage != null) {
                    if (CMProps.isAnyINIFiltered(sayMessage))
                        damageToDo = (int) Math.round(1.0 + (5 + (invoker.phyStats().level() / 20))) + msg.source().phyStats().level();
                    else
                        for (String s : CMLib.english().parseWords(sayMessage)) {
                            s = s.toUpperCase().trim();
                            if (UNPIOS_SET.contains(s)
                                || s.startsWith(filterPatternStart)) {
                                damageToDo = (int) Math.round(1.0 + (5 + (invoker.phyStats().level() / 20))) + msg.source().phyStats().level();
                                break;
                            }
                        }
                }
            } else if (msg.target() instanceof MOB) {
                if ((msg.tool() instanceof Social)
                    && (UNPIOS_SET.contains(((Social) msg.tool()).baseName().toUpperCase().trim())))
                    damageToDo = (int) Math.round(1.0 + (5 + (invoker.phyStats().level() / 20))) + msg.source().phyStats().level();
                else if ((msg.targetMinor() == CMMsg.TYP_WEAPONATTACK)
                    && (msg.target() instanceof MOB))
                    damageToDo = CMLib.dice().roll(1, (int) Math.round(1.0 + (5 + (invoker.phyStats().level() / 20)) / ((MOB) msg.target()).phyStats().speed()), 0);
            }
            if (damageToDo > 0) {
                final MOB M = msg.source();
                final MOB invoker = (invoker() != null) ? invoker() : M;
                final int damage = damageToDo;
                CMLib.combat().postDamage(invoker, M, this, damage, CMMsg.MASK_MALICIOUS | CMMsg.MASK_ALWAYS | CMMsg.TYP_CAST_SPELL, Weapon.TYPE_STRIKING, L("The piety curse <DAMAGE> <T-NAME>!"));
                CMLib.combat().postRevengeAttack(M, invoker);
            }
        }
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);

        if (success && (!CMLib.flags().isGood(target))) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto) | CMMsg.MASK_MALICIOUS, auto ? L("<T-NAME> gain(s) an holy curse of piety!") : L("^S<S-NAME> judge(s) <T-NAMESELF>, laying a piety curse upon <T-HIM-HER>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (msg.value() <= 0) {
                    final Ability A = maliciousAffect(mob, target, asLevel, 0, -1);
                    if (A == null)
                        success = false;
                    target.recoverPhyStats();
                }
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> attempt(s) to curse <T-NAMESELF> piously, but nothing happens."));

        // return whether it worked
        return success;
    }
}
