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
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Prayer_Fertilize extends Prayer {
    private final static String localizedName = CMLib.lang().L("Fertilize");

    @Override
    public String ID() {
        return "Prayer_Fertilize";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_ROOMS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_CREATION;
    }

    @Override
    public long flags() {
        return Ability.FLAG_NEUTRAL;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if ((affected != null) && (affected instanceof Room)) {
            final Room R = (Room) affected;
            if ((R.myResource() & RawMaterial.MATERIAL_MASK) == RawMaterial.MATERIAL_VEGETATION) {
                for (int m = 0; m < R.numInhabitants(); m++) {
                    final MOB M = R.fetchInhabitant(m);
                    if (M != null) {
                        Ability A = M.fetchEffect("Farming");
                        if (A == null)
                            A = M.fetchEffect("Foraging");
                        if (A == null)
                            A = M.fetchEffect("MasterFarming");
                        if (A == null)
                            A = M.fetchEffect("MasterForaging");
                        if (A != null)
                            A.setAbilityCode(1);
                    }
                }
            }
        }
        return super.tick(ticking, tickID);

    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {

        final int type = mob.location().domainType();
        if (((type & Room.INDOORS) > 0)
            || (type == Room.DOMAIN_OUTDOORS_AIR)
            || (type == Room.DOMAIN_OUTDOORS_CITY)
            || (type == Room.DOMAIN_OUTDOORS_SPACEPORT)
            || (type == Room.DOMAIN_OUTDOORS_UNDERWATER)
            || (type == Room.DOMAIN_OUTDOORS_WATERSURFACE)) {
            mob.tell(L("That magic won't work here."));
            return false;
        }
        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, mob.location(), this, verbalCastCode(mob, mob.location(), auto), auto ? "" : L("^S<S-NAME> @x1 to make the land fruitful.^?", prayForWord(mob)));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                beneficialAffect(mob,
                    mob.location(),
                    asLevel,
                    CMLib.ableMapper().qualifyingClassLevel(mob, this) *
                        (int) ((CMProps.getMillisPerMudHour() *
                            (mob.location().getArea().getTimeObj().getHoursInDay())) /
                            CMProps.getTickMillis()));
            }

        } else
            beneficialWordsFizzle(mob, null, L("<S-NAME> @x1 to make the land fruitful, but nothing happens.", prayForWord(mob)));

        // return whether it worked
        return success;
    }
}
