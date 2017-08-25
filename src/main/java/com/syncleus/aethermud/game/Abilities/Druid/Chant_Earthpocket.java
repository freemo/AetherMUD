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
package com.planet_ink.game.Abilities.Druid;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Common.interfaces.PhyStats;
import com.planet_ink.game.Items.interfaces.Container;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.Items.interfaces.RawMaterial;
import com.planet_ink.game.Locales.interfaces.Room;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMProps;
import com.planet_ink.game.core.CMath;
import com.planet_ink.game.core.interfaces.Environmental;
import com.planet_ink.game.core.interfaces.Physical;
import com.planet_ink.game.core.interfaces.Tickable;

import java.util.List;


public class Chant_Earthpocket extends Chant {
    private final static String localizedName = CMLib.lang().L("Earthpocket");
    private Container pocket = null;

    @Override
    public String ID() {
        return "Chant_Earthpocket";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return L("(Earthpocket: " + (super.tickDown / CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)) + ")");
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_OK_SELF;
    }

    @Override
    protected int canAffectCode() {
        return CAN_MOBS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_ROCKCONTROL;
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;
        super.unInvoke();

        if ((pocket != null) && (!pocket.amDestroyed())) {
            mob.tell(L("Your earthpocket fades away, dumping its contents into your inventory!"));
            final List<Item> V = pocket.getDeepContents();
            for (int v = 0; v < V.size(); v++) {
                V.get(v).setContainer(null);
                mob.moveItemTo(V.get(v));
            }
            pocket.destroy();
            pocket = null;
        }
    }

    @Override
    public boolean okMessage(Environmental host, CMMsg msg) {
        if ((msg.source() != affected)
            && ((msg.target() == pocket) || (msg.tool() == pocket))
            && (CMath.bset(msg.sourceMajor(), CMMsg.MASK_HANDS)
            || CMath.bset(msg.sourceMajor(), CMMsg.MASK_MOVE)
            || CMath.bset(msg.sourceMajor(), CMMsg.MASK_DELICATE)
            || CMath.bset(msg.sourceMajor(), CMMsg.MASK_MOUTH))) {
            msg.source().tell(L("The dark pocket draws away from you, preventing your action."));
            return false;
        }
        return true;
    }

    @Override
    public void executeMsg(Environmental host, CMMsg msg) {
        super.executeMsg(host, msg);
        movePocket();
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (!super.tick(ticking, tickID))
            return false;
        movePocket();
        return true;
    }

    public void movePocket() {
        if ((affected instanceof MOB) && (pocket != null)) {
            if (pocket.owner() instanceof MOB)
                pocket.removeFromOwnerContainer();
            else if (pocket.owner() instanceof Room) {
                final Room mobR = ((MOB) affected).location();
                if (mobR != null) {
                    if ((mobR.domainType() == Room.DOMAIN_INDOORS_CAVE)
                        || ((mobR.getAtmosphere() & RawMaterial.MATERIAL_ROCK) != 0)) {
                        if (CMath.bset(pocket.basePhyStats().disposition(), PhyStats.IS_NOT_SEEN)) {
                            pocket.basePhyStats().setDisposition(pocket.basePhyStats().disposition() - PhyStats.IS_NOT_SEEN);
                            pocket.recoverPhyStats();
                        }
                        if (!mobR.isContent(pocket)) {
                            final Room R = CMLib.map().roomLocation(pocket);
                            mobR.moveItemTo(pocket);
                            if (mobR.isContent(pocket)) {
                                if ((R != mobR) && (R.isContent(pocket)))
                                    R.delItem(pocket);
                            }
                        }
                    } else if (!CMath.bset(pocket.basePhyStats().disposition(), PhyStats.IS_NOT_SEEN)) {
                        pocket.basePhyStats().setDisposition(pocket.basePhyStats().disposition() | PhyStats.IS_NOT_SEEN);
                        pocket.recoverPhyStats();
                    }
                } else if (!CMath.bset(pocket.basePhyStats().disposition(), PhyStats.IS_NOT_SEEN)) {
                    pocket.basePhyStats().setDisposition(pocket.basePhyStats().disposition() | PhyStats.IS_NOT_SEEN);
                    pocket.recoverPhyStats();
                }
            }
        }
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        MOB target = mob;
        if ((auto) && (givenTarget != null) && (givenTarget instanceof MOB))
            target = (MOB) givenTarget;

        if ((mob.location().domainType() != Room.DOMAIN_INDOORS_CAVE)
            && ((mob.location().getAtmosphere() & RawMaterial.MATERIAL_ROCK) == 0)) {
            mob.tell(L("The earthpocket can only be summoned or seen in a cave."));
            return false;
        }

        if (target.fetchEffect(ID()) != null) {
            mob.tell(target, null, null, L("<S-NAME> <S-IS-ARE> already connected with an earthpocket."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> chant(s) for a connection with a mystical dimension!^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                pocket = (Container) CMClass.getItem("GenContainer");
                pocket.setCapacity(Integer.MAX_VALUE);
                pocket.basePhyStats().setSensesMask(PhyStats.SENSE_ITEMNOTGET);
                pocket.basePhyStats().setWeight(0);
                pocket.setMaterial(RawMaterial.RESOURCE_NOTHING);
                pocket.setName(L("an earthpocket"));
                pocket.setDisplayText(L("an empty pitch-black pocket is in the wall here."));
                pocket.setDescription(L("It looks like an endless black hole in the wall.  Very mystical."));
                pocket.recoverPhyStats();
                target.location().addItem(pocket);
                beneficialAffect(mob, target, asLevel, CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH));
                target.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("A dark pocket of energy appears in a nearby wall."));
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> chant(s), but nothing more happens."));

        // return whether it worked
        return success;
    }
}