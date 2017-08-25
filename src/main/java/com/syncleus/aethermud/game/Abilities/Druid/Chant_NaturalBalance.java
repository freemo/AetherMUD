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
import com.syncleus.aethermud.game.Common.interfaces.Faction;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Chant_NaturalBalance extends Chant {
    private final static String localizedName = CMLib.lang().L("Natural Balance");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Communing with the Natural Balance)");
    long lastTime = 0;

    @Override
    public String ID() {
        return "Chant_NaturalBalance";
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
        return Ability.QUALITY_OK_SELF;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_ENDURING;
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public void unInvoke() {
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        super.unInvoke();
        if (canBeUninvoked()) {
            if (!mob.amDead()) {
                if (mob.location() != null)
                    mob.location().show(mob, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> end(s) <S-HIS-HER> natural communion."));
                else
                    mob.tell(L("Your communion with natural balance ends."));
            }
        }
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        super.executeMsg(myHost, msg);
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        if ((msg.amISource(mob))
            && (msg.tool() != this)
            && (!CMath.bset(msg.sourceMajor(), CMMsg.MASK_CHANNEL))
            && ((CMath.bset(msg.sourceMajor(), CMMsg.MASK_MOVE)) || (CMath.bset(msg.sourceMajor(), CMMsg.MASK_HANDS)) || (CMath.bset(msg.sourceMajor(), CMMsg.MASK_MOUTH))))
            unInvoke();
        return;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!(affected instanceof MOB))
            return super.okMessage(myHost, msg);
        final MOB mob = (MOB) affected;

        if ((msg.amISource(mob)
            && (!msg.sourceMajor(CMMsg.MASK_ALWAYS))
            && (!CMath.bset(msg.sourceMajor(), CMMsg.MASK_CHANNEL))
            && (msg.sourceMajor() > 0))) {
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!(affected instanceof MOB))
            return super.tick(ticking, tickID);

        final MOB mob = (MOB) affected;

        if (tickID != Tickable.TICKID_MOB)
            return true;
        if (!mob.isInCombat()) {
            if ((System.currentTimeMillis() - lastTime) < 60000)
                return true;
            if (!proficiencyCheck(null, 0, false))
                return true;
            lastTime = System.currentTimeMillis();
            final Room room = mob.location();
            final int myAlignment = mob.fetchFaction(CMLib.factions().AlignID());
            final int total = CMLib.factions().getTotal(CMLib.factions().AlignID());
            final int oneHalfPct = (int) Math.round(CMath.mul(total, .01)) / 2;
            if (CMLib.factions().getAlignPurity(myAlignment, Faction.Align.INDIFF) < 99) {
                if (CMLib.factions().getAlignPurity(myAlignment, Faction.Align.EVIL) < CMLib.factions().getAlignPurity(myAlignment, Faction.Align.GOOD))
                    CMLib.factions().postFactionChange(mob, this, CMLib.factions().AlignID(), oneHalfPct);
                else
                    CMLib.factions().postFactionChange(mob, this, CMLib.factions().AlignID(), -oneHalfPct);
                switch (CMLib.dice().roll(1, 10, 0)) {
                    case 0:
                        room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> empathize(s) with the plants."));
                        break;
                    case 1:
                        room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> learn(s) from the birds."));
                        break;
                    case 2:
                        room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> watch(es) the insects."));
                        break;
                    case 3:
                        room.show(mob, null, this, CMMsg.MSG_HANDS | CMMsg.MASK_ALWAYS, L("<S-NAME> hug(s) the ground."));
                        break;
                    case 4:
                        room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> inhale(s) the fresh air."));
                        break;
                    case 5:
                        room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> watch(es) the plants grow."));
                        break;
                    case 6:
                        room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> become(s) one with life."));
                        break;
                    case 7:
                        room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> seek(s) the inner beauty of the natural order."));
                        break;
                    case 8:
                        room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> expunge(s) <S-HIS-HER> unnatural thoughts."));
                        break;
                    case 9:
                        room.show(mob, null, this, CMMsg.MSG_CONTEMPLATE, L("<S-NAME> find(s) clarity in the natural world."));
                        break;
                }
            }
        } else {
            unInvoke();
            return false;
        }
        return super.tick(ticking, tickID);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (mob.isInCombat()) {
            mob.tell(L("You can't commune while in combat!"));
            return false;
        }
        if ((mob.location().domainType() & Room.INDOORS) > 0) {
            mob.tell(L("You must be outdoors for this chant to work."));
            return false;
        }
        if ((mob.location().domainType() == Room.DOMAIN_OUTDOORS_CITY)
            || (mob.location().domainType() == Room.DOMAIN_OUTDOORS_SPACEPORT)) {
            mob.tell(L("This magic will not work here."));
            return false;
        }

        // now see if it worked
        final boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, null, this, somanticCastCode(mob, null, auto), L("^S<S-NAME> begin(s) to commune with the natural balance...^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                beneficialAffect(mob, mob, asLevel, Ability.TICKS_FOREVER);
                helpProficiency(mob, 0);
            }
        } else
            return beneficialVisualFizzle(mob, null, L("<S-NAME> chant(s) to commune with nature, but lose(s) concentration."));

        // return whether it worked
        return success;
    }
}
