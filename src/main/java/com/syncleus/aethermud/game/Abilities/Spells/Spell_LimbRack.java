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
import com.planet_ink.game.Abilities.interfaces.LimbDamage;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Items.interfaces.Weapon;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.Races.interfaces.Race;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.interfaces.Physical;
import com.planet_ink.game.core.interfaces.Tickable;

import java.util.List;
import java.util.Vector;


public class Spell_LimbRack extends Spell {

    private final static String localizedName = CMLib.lang().L("Limb Rack");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Being pulled apart)");
    public List<String> limbsToRemove = new Vector<String>();

    @Override
    public String ID() {
        return "Spell_LimbRack";
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
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SPELL | Ability.DOMAIN_EVOCATION;
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        final MOB mob = (MOB) affected;
        final MOB invoker = (invoker() != null) ? invoker() : mob;
        if ((mob.location() != null)
            && (mob.charStats().getMyRace().bodyMask()[Race.BODY_ARM] >= 0)
            && (mob.charStats().getMyRace().bodyMask()[Race.BODY_LEG] >= 0)) {
            final String str = (text().equalsIgnoreCase("ARMSONLY")) ?
                L("<T-NAME> <T-IS-ARE> having <T-HIS-HER> arms pulled from <T-HIS-HER> body!") : L("<T-NAME> <T-IS-ARE> having <T-HIS-HER> arms and legs pulled from <T-HIS-HER> body!");
            CMLib.combat().postDamage(invoker, mob, this, mob.maxState().getHitPoints() / (10 - (getXLEVELLevel(invoker) / 2)), CMMsg.MASK_ALWAYS | CMMsg.TYP_JUSTICE, Weapon.TYPE_BURSTING, str);
        }

        return true;
    }

    @Override
    public void unInvoke() {
        if ((affected instanceof MOB)
            && (((MOB) affected).amDead())) {
            final MOB mob = (MOB) affected;
            if ((mob.location() != null)
                && (mob.charStats().getMyRace().bodyMask()[Race.BODY_ARM] > 0)
                && (mob.charStats().getMyRace().bodyMask()[Race.BODY_LEG] > 0)) {
                if (text().equalsIgnoreCase("ARMSONLY"))
                    mob.location().show(mob, null, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> has <S-HIS-HER> arms TORN OFF!"));
                else
                    mob.location().show(mob, null, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> has <S-HIS-HER> arms and legs TORN OFF!"));
                LimbDamage ampuA = (LimbDamage) mob.fetchEffect("Amputation");
                if (ampuA == null) {
                    ampuA = (LimbDamage) CMClass.getAbility("Amputation");
                    ampuA.setAffectedOne(mob);
                }
                boolean success = true;
                for (int i = 0; i < limbsToRemove.size(); i++)
                    success = success && (ampuA.damageLimb(limbsToRemove.get(i)) != null);
                if (success) {
                    if (mob.fetchEffect(ampuA.ID()) == null)
                        mob.addNonUninvokableEffect(ampuA);
                }
            }
            CMLib.utensils().confirmWearability(mob);
        }
        super.unInvoke();
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        LimbDamage A = (LimbDamage) target.fetchEffect("Amputation");
        if (A == null) {
            A = (LimbDamage) CMClass.getAbility("Amputation");
            A.setAffectedOne(target);
        }
        final List<String> remainingLimbList = A.unaffectedLimbSet();
        for (int i = remainingLimbList.size() - 1; i >= 0; i--) {
            final String gone = remainingLimbList.get(i);
            if ((!gone.toUpperCase().endsWith(" ARM"))
                && (!gone.toUpperCase().endsWith(" LEG")))
                remainingLimbList.remove(i);
        }
        if ((remainingLimbList.size() == 0)
            || ((target.charStats().getMyRace().bodyMask()[Race.BODY_ARM] <= 0)
            && (target.charStats().getMyRace().bodyMask()[Race.BODY_LEG] <= 0))) {
            if (!auto)
                mob.tell(L("There is nothing left on @x1 to rack off!", target.name(mob)));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        // now see if it worked
        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), L(auto ? "!" : "^S<S-NAME> invoke(s) a stretching spell upon <T-NAMESELF>"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (msg.value() <= 0) {
                    super.maliciousAffect(mob, target, asLevel, 12, -1);
                    final Ability A2 = target.fetchEffect(ID());
                    if (A2 != null) {
                        ((Spell_LimbRack) A2).limbsToRemove = new Vector<String>();
                        ((Spell_LimbRack) A2).limbsToRemove.addAll(remainingLimbList);
                    }
                }
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> incant(s) stretchingly at <T-NAMESELF>, but flub(s) the spell."));

        // return whether it worked
        return success;
    }
}