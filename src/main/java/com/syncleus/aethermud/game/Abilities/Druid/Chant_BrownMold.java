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
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.Faction;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.RawMaterial;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Chant_BrownMold extends Chant {
    private final static String localizedName = CMLib.lang().L("Brown Mold");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Brown Mold)");

    @Override
    public String ID() {
        return "Chant_BrownMold";
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
    public int classificationCode() {
        return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTGROWTH;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_BENEFICIAL_SELF;
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
    public boolean tick(Tickable ticking, int tickID) {
        if (tickID == Tickable.TICKID_MOB) {
            if ((affected != null)
                && (affected instanceof MOB)
                && (invoker != null)) {
                final MOB mob = (MOB) affected;
                if (((mob.amFollowing() == null)
                    || (mob.amDead())
                    || (!mob.isInCombat())
                    || ((invoker != null) && (mob.location() != invoker.location()))))
                    unInvoke();
            }
        }
        return super.tick(ticking, tickID);
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if ((affected != null)
            && (affected instanceof MOB)
            && (msg.amISource((MOB) affected))) {
            if (msg.sourceMinor() == CMMsg.TYP_DEATH) {
                unInvoke();
                return false;
            }
        }
        return super.okMessage(myHost, msg);
    }

    @Override
    public void unInvoke() {
        final MOB mob = (MOB) affected;
        super.unInvoke();
        if ((canBeUninvoked()) && (mob != null)) {
            if (mob.location() != null)
                mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> wither(s) away."));
            if (mob.amDead())
                mob.setLocation(null);
            mob.destroy();
        }
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        super.executeMsg(myHost, msg);
        if ((affected != null)
            && (affected instanceof MOB)
            && (msg.amISource((MOB) affected) || msg.amISource(((MOB) affected).amFollowing()) || (msg.source() == invoker()))
            && (msg.sourceMinor() == CMMsg.TYP_QUIT)) {
            unInvoke();
            if (msg.source().playerStats() != null)
                msg.source().playerStats().setLastUpdated(0);
        }
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if (mob != null) {
            if (!mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        if (!mob.isInCombat()) {
            mob.tell(L("Only the anger of combat can summon the brown mold."));
            return false;
        }
        final int material = RawMaterial.RESOURCE_HEMP;

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            invoker = mob;
            final CMMsg msg = CMClass.getMsg(mob, null, this, verbalCastCode(mob, null, auto), auto ? "" : L("^S<S-NAME> chant(s) and summon(s) a brown mold!^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final MOB target = determineMonster(mob, material);
                beneficialAffect(mob, target, asLevel, 0);
            }
        } else
            return beneficialWordsFizzle(mob, null, L("<S-NAME> chant(s), but nothing happens."));

        // return whether it worked
        return success;
    }

    public MOB determineMonster(MOB caster, int material) {
        final MOB victim = caster.getVictim();
        final MOB newMOB = CMClass.getMOB("GenMOB");
        final int level = 20;
        newMOB.basePhyStats().setLevel(level);
        newMOB.basePhyStats().setAbility(25);
        newMOB.baseCharStats().setMyRace(CMClass.getRace("Mold"));
        final String name = "a brown mold";
        newMOB.setName(name);
        newMOB.setDisplayText(L("@x1 looks scary!", name));
        newMOB.setDescription("");
        CMLib.factions().setAlignment(newMOB, Faction.Align.NEUTRAL);
        final Ability A = CMClass.getAbility("Fighter_Rescue");
        A.setProficiency(100);
        newMOB.addAbility(A);
        newMOB.setVictim(victim);
        newMOB.basePhyStats().setAbility(newMOB.basePhyStats().ability() * 2);
        newMOB.basePhyStats().setSensesMask(newMOB.basePhyStats().sensesMask() | PhyStats.CAN_SEE_DARK);
        newMOB.setLocation(caster.location());
        newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
        newMOB.basePhyStats().setDamage(25);
        newMOB.basePhyStats().setAttackAdjustment(60);
        newMOB.basePhyStats().setArmor(-super.getX1Level(caster));
        newMOB.baseCharStats().setStat(CharStats.STAT_GENDER, 'N');
        newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
        newMOB.setMiscText(newMOB.text());
        newMOB.recoverCharStats();
        newMOB.recoverPhyStats();
        newMOB.recoverMaxState();
        newMOB.resetToMaxState();
        newMOB.bringToLife(caster.location(), true);
        CMLib.moneyCounter().clearZeroMoney(newMOB, null);
        newMOB.setMoneyVariation(0);
        newMOB.setStartRoom(null); // keep before postFollow for Conquest
        CMLib.commands().postFollow(newMOB, caster, true);
        if (newMOB.amFollowing() != caster)
            caster.tell(L("@x1 seems unwilling to follow you.", newMOB.name()));
        else {
            if (newMOB.getVictim() != victim)
                newMOB.setVictim(victim);
            newMOB.location().showOthers(newMOB, victim, CMMsg.MSG_OK_ACTION, L("<S-NAME> start(s) attacking <T-NAMESELF>!"));
        }
        return (newMOB);
    }
}
