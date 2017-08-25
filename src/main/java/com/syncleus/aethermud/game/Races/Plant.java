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
package com.planet_ink.game.Races;

import com.planet_ink.game.Areas.interfaces.Area;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.CharState;
import com.planet_ink.game.Common.interfaces.CharStats;
import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Items.interfaces.RawMaterial;
import com.planet_ink.game.Items.interfaces.Weapon;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMath;
import com.planet_ink.game.core.interfaces.Environmental;
import com.planet_ink.game.core.interfaces.Physical;
import com.planet_ink.game.core.interfaces.Tickable;

import java.util.List;
import java.util.Vector;


public class Plant extends Vine {
    private final static String localizedStaticName = CMLib.lang().L("Plant");
    private final static String localizedStaticRacialCat = CMLib.lang().L("Vegetation");
    //  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
    private static final int[] parts = {0, 0, 0, 0, 0, 8, 8, 1, 0, 0, 0, 0, 0, 0, 0, 0};
    protected static Vector<RawMaterial> resources = new Vector<RawMaterial>();

    @Override
    public String ID() {
        return "Plant";
    }

    @Override
    public String name() {
        return localizedStaticName;
    }

    @Override
    public int shortestMale() {
        return 4;
    }

    @Override
    public int shortestFemale() {
        return 4;
    }

    @Override
    public int heightVariance() {
        return 5;
    }

    @Override
    public int lightestWeight() {
        return 1;
    }

    @Override
    public int weightVariance() {
        return 1;
    }

    @Override
    public long forbiddenWornBits() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String racialCategory() {
        return localizedStaticRacialCat;
    }

    @Override
    public int[] getBreathables() {
        return breatheAnythingArray;
    }

    @Override
    public int[] bodyMask() {
        return parts;
    }

    @Override
    public int availabilityCode() {
        return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_GOLEM);
        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_NOT_SPEAK | PhyStats.CAN_NOT_TASTE | PhyStats.CAN_NOT_MOVE);
    }

    @Override
    public void affectCharState(MOB affectedMOB, CharState affectableState) {
        affectableState.setHunger((Integer.MAX_VALUE / 2) + 10);
        affectedMOB.curState().setHunger(affectableState.getHunger());
    }

    @Override
    public void affectCharStats(MOB affectedMOB, CharStats affectableStats) {
        super.affectCharStats(affectedMOB, affectableStats);
        affectableStats.setStat(CharStats.STAT_GENDER, 'N');
        affectableStats.setStat(CharStats.STAT_SAVE_POISON, affectableStats.getStat(CharStats.STAT_SAVE_POISON) + 100);
        affectableStats.setStat(CharStats.STAT_SAVE_MIND, affectableStats.getStat(CharStats.STAT_SAVE_MIND) + 100);
        affectableStats.setStat(CharStats.STAT_SAVE_GAS, affectableStats.getStat(CharStats.STAT_SAVE_GAS) + 100);
        affectableStats.setStat(CharStats.STAT_SAVE_PARALYSIS, affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS) + 100);
        affectableStats.setStat(CharStats.STAT_SAVE_UNDEAD, affectableStats.getStat(CharStats.STAT_SAVE_UNDEAD) + 100);
        affectableStats.setStat(CharStats.STAT_SAVE_DISEASE, affectableStats.getStat(CharStats.STAT_SAVE_DISEASE) + 100);
    }

    @Override
    public String arriveStr() {
        return "shuffles in";
    }

    @Override
    public String leaveStr() {
        return "shuffles";
    }

    @Override
    public Weapon myNaturalWeapon() {
        if (naturalWeapon == null) {
            naturalWeapon = CMClass.getWeapon("StdWeapon");
            naturalWeapon.setName(L("a nasty vine"));
            naturalWeapon.setRanges(0, 3);
            naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
            naturalWeapon.setUsesRemaining(1000);
            naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
        }
        return naturalWeapon;
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if ((myHost != null)
            && (myHost instanceof MOB)
            && (msg.amISource((MOB) myHost))) {
            if (((msg.targetMinor() == CMMsg.TYP_LEAVE)
                || (msg.sourceMinor() == CMMsg.TYP_ADVANCE)
                || (msg.sourceMinor() == CMMsg.TYP_RETREAT))) {
                msg.source().tell(L("You can't really go anywhere -- you are rooted!"));
                return false;
            }
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        if ((tickID == Tickable.TICKID_MOB) && (ticking instanceof MOB))
            CMLib.combat().recoverTick((MOB) ticking);
        return true;
    }

    @Override
    public String healthText(MOB viewer, MOB mob) {
        final double pct = (CMath.div(mob.curState().getHitPoints(), mob.maxState().getHitPoints()));

        if (pct < .10)
            return L("^r@x1^r is near destruction!^N", mob.name(viewer));
        else if (pct < .20)
            return L("^r@x1^r is massively shredded and damaged.^N", mob.name(viewer));
        else if (pct < .30)
            return L("^r@x1^r is extremely shredded and damaged.^N", mob.name(viewer));
        else if (pct < .40)
            return L("^y@x1^y is very shredded and damaged.^N", mob.name(viewer));
        else if (pct < .50)
            return L("^y@x1^y is shredded and damaged.^N", mob.name(viewer));
        else if (pct < .60)
            return L("^p@x1^p is shredded and slightly damaged.^N", mob.name(viewer));
        else if (pct < .70)
            return L("^p@x1^p has lost numerous leaves.^N", mob.name(viewer));
        else if (pct < .80)
            return L("^g@x1^g has lost some leaves.^N", mob.name(viewer));
        else if (pct < .90)
            return L("^g@x1^g has lost a few leaves.^N", mob.name(viewer));
        else if (pct < .99)
            return L("^g@x1^g is no longer in perfect condition.^N", mob.name(viewer));
        else
            return L("^c@x1^c is in perfect condition.^N", mob.name(viewer));
    }

    @Override
    public List<RawMaterial> myResources() {
        synchronized (resources) {
            if (resources.size() == 0) {
                resources.addElement(makeResource
                    (L("a stem"), RawMaterial.RESOURCE_VINE));
            }
        }
        return resources;
    }
}