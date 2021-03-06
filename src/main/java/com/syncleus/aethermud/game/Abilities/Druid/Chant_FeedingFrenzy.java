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
import com.syncleus.aethermud.game.Behaviors.interfaces.Behavior;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Libraries.interfaces.TrackingLibrary;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.Enumeration;
import java.util.List;


public class Chant_FeedingFrenzy extends Chant {
    private final static String localizedName = CMLib.lang().L("Feeding Frenzy");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Feeding Frenzy)");
    final TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags()
        .plus(TrackingLibrary.TrackingFlag.AREAONLY)
        .plus(TrackingLibrary.TrackingFlag.UNDERWATERONLY);
    protected Behavior aggro = null;

    @Override
    public String ID() {
        return "Chant_FeedingFrenzy";
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
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int enchantQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
    }

    @Override
    public void unInvoke() {
        if (canBeUninvoked()) {
            if (affected instanceof MOB) {
                MOB mob = (MOB) affected;
                mob.tell(L("You are no longer in a frenzy."));
            }
        }
        super.unInvoke();
    }

    public void ensureFrenzy() {
        if (this.aggro == null) {
            this.aggro = CMClass.getBehavior("Aggressive");
        }
    }

    @Override
    public void executeMsg(Environmental myHost, CMMsg msg) {
        super.executeMsg(myHost, msg);
        ensureFrenzy();
        if (this.aggro != null)
            this.aggro.executeMsg(myHost, msg);
    }

    @Override
    public boolean okMessage(Environmental myHost, CMMsg msg) {
        if (!super.okMessage(myHost, msg))
            return false;
        ensureFrenzy();
        if ((this.aggro != null) && (!this.aggro.okMessage(myHost, msg)))
            return false;
        return true;

    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        ensureFrenzy();
        if (this.aggro != null)
            this.aggro.tick(ticking, tickID);
        return true;
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Room target = mob.location();
        if (target == null)
            return false;
        if ((mob.location().domainType() != Room.DOMAIN_INDOORS_UNDERWATER)
            && (mob.location().domainType() != Room.DOMAIN_OUTDOORS_UNDERWATER)) {
            mob.tell(L("You must be under a sea, lake or ocean for this chant to work."));
            return false;
        }
        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;
        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, null, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> chant(s) to the creatures of the water.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                int radius = 10 + super.getXMAXRANGELevel(mob) + (super.getXLEVELLevel(mob) / 2);
                List<Room> rooms = CMLib.tracking().getRadiantRooms(mob.location(), flags, radius);
                for (Room room : rooms) {
                    if ((room != null) && (room.numInhabitants() > 0)) {
                        for (Enumeration<MOB> m = room.inhabitants(); m.hasMoreElements(); ) {
                            final MOB M = m.nextElement();
                            if (M.isMonster()
                                && CMLib.flags().isAnimalIntelligence(M)
                                && CMLib.flags().canActAtAll(M)
                                && (M.fetchEffect(ID()) == null)
                                && (CMLib.flags().isMarine(M)))
                                maliciousAffect(mob, M, asLevel, 0, CMMsg.MASK_MALICIOUS | CMMsg.TYP_MIND);
                        }
                    }
                }
            }
        } else
            return maliciousFizzle(mob, null, L("<S-NAME> chant(s) to the water, but the magic fades."));
        // return whether it worked
        return success;
    }
}
