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
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Libraries.interfaces.TrackingLibrary;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;
import java.util.Vector;


public class Chant_LocatePlants extends Chant {
    private final static String localizedName = CMLib.lang().L("Locate Plants");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Locating Plants)");
    public int nextDirection = -2;
    protected List<Room> theTrail = null;

    @Override
    public String ID() {
        return "Chant_LocatePlants";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTCONTROL;
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
    public long flags() {
        return Ability.FLAG_TRACKING;
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if (tickID == Tickable.TICKID_MOB) {
            if (nextDirection == -999)
                return true;

            if ((theTrail == null)
                || (affected == null)
                || (!(affected instanceof MOB)))
                return false;

            final MOB mob = (MOB) affected;

            if (nextDirection == 999) {
                mob.tell(plantsHere(mob, mob.location()));
                nextDirection = -2;
                unInvoke();
            } else if (nextDirection == -1) {
                if (plantsHere(mob, mob.location()).length() == 0)
                    mob.tell(L("The plant life trail fizzles out here."));
                nextDirection = -999;
                unInvoke();
            } else if (nextDirection >= 0) {
                mob.tell(L("Your sense plant life @x1.", CMLib.directions().getDirectionName(nextDirection)));
                nextDirection = -2;
            }

        }
        return true;
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        super.executeMsg(myHost, msg);

        if (!(affected instanceof MOB))
            return;

        final MOB mob = (MOB) affected;
        if ((msg.amISource(mob))
            && (msg.amITarget(mob.location()))
            && (CMLib.flags().canBeSeenBy(mob.location(), mob))
            && (msg.targetMinor() == CMMsg.TYP_LOOK))
            nextDirection = CMLib.tracking().trackNextDirectionFromHere(theTrail, mob.location(), false);
    }

    public String plantsHere(MOB mob, Room R) {
        final StringBuffer msg = new StringBuffer("");
        if (R == null)
            return msg.toString();
        final Room room = R;
        if ((room.domainType() == Room.DOMAIN_OUTDOORS_WOODS)
            || (room.domainType() == Room.DOMAIN_OUTDOORS_PLAINS)
            || (room.domainType() == Room.DOMAIN_OUTDOORS_HILLS)
            || ((room.myResource() & RawMaterial.MATERIAL_MASK) == RawMaterial.MATERIAL_WOODEN)
            || ((room.myResource() & RawMaterial.MATERIAL_MASK) == RawMaterial.MATERIAL_VEGETATION)
            || (room.domainType() == Room.DOMAIN_OUTDOORS_JUNGLE)
            || (room.domainType() == Room.DOMAIN_OUTDOORS_SWAMP))
            msg.append(L("There seem to be a large number of plants all around you!\n\r"));
        return msg.toString();
    }

    @Override
    public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats) {
        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_NOT_TRACK);
        super.affectPhyStats(affectedEnv, affectableStats);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        MOB target = mob;
        if ((auto) && (givenTarget != null) && (givenTarget instanceof MOB))
            target = (MOB) givenTarget;

        if (target.fetchEffect(this.ID()) != null) {
            mob.tell(target, null, null, L("<S-NAME> <S-IS-ARE> already trying to find plant life."));
            return false;
        }
        final List<Ability> V = CMLib.flags().flaggedAffects(mob, Ability.FLAG_TRACKING);
        for (final Ability A : V) A.unInvoke();

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final String here = plantsHere(target, target.location());
        if (here.length() > 0) {
            target.tell(here);
            return true;
        }

        final boolean success = proficiencyCheck(mob, 0, auto);

        TrackingLibrary.TrackingFlags flags;
        flags = CMLib.tracking().newFlags()
            .plus(TrackingLibrary.TrackingFlag.NOAIR)
            .plus(TrackingLibrary.TrackingFlag.NOWATER);
        final Vector<Room> rooms = new Vector<Room>();
        int range = 50 + super.getXLEVELLevel(mob) + (2 * super.getXMAXRANGELevel(mob));
        final List<Room> checkSet = CMLib.tracking().getRadiantRooms(mob.location(), flags, range);
        for (final Room R : checkSet) {
            if (plantsHere(mob, R).length() > 0)
                rooms.addElement(R);
        }

        if (rooms.size() > 0) {
            //TrackingLibrary.TrackingFlags flags;
            flags = CMLib.tracking().newFlags()
                .plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
                .plus(TrackingLibrary.TrackingFlag.NOAIR);
            theTrail = CMLib.tracking().findTrailToAnyRoom(target.location(), rooms, flags, range);
        }

        if ((success) && (theTrail != null)) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? L("<T-NAME> begin(s) to sense plant life!") : L("^S<S-NAME> chant(s) for a route to plant life.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final Chant_LocatePlants newOne = (Chant_LocatePlants) this.copyOf();
                if (target.fetchEffect(newOne.ID()) == null)
                    target.addEffect(newOne);
                target.recoverPhyStats();
                newOne.nextDirection = CMLib.tracking().trackNextDirectionFromHere(newOne.theTrail, target.location(), false);
            }
        } else
            beneficialVisualFizzle(mob, null, L("<S-NAME> chant(s) to find plant life, but fail(s)."));

        return success;
    }
}
