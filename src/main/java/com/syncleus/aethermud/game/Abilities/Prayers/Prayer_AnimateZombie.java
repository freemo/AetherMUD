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
import com.syncleus.aethermud.game.Behaviors.interfaces.Behavior;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.Faction;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.DeadBody;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.Libraries.interfaces.ExpertiseLibrary;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Prayer_AnimateZombie extends Prayer {
    private final static String localizedName = CMLib.lang().L("Animate Zombie");
    private final static String localizedDiplayText = CMLib.lang().L("Newly animate dead");

    @Override
    public String ID() {
        return "Prayer_AnimateZombie";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_PRAYER | Ability.DOMAIN_DEATHLORE;
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
    public long flags() {
        return Ability.FLAG_UNHOLY;
    }

    @Override
    protected int canTargetCode() {
        return CAN_ITEMS;
    }

    @Override
    public String displayText() {
        return localizedDiplayText;
    }

    @Override
    public void unInvoke() {
        final Physical P = affected;
        super.unInvoke();
        if ((P instanceof MOB) && (this.canBeUninvoked) && (this.unInvoked)) {
            if ((!P.amDestroyed()) && (((MOB) P).amFollowing() == null)) {
                final Room R = CMLib.map().roomLocation(P);
                if (!CMLib.law().doesHavePriviledgesHere(invoker(), R)) {
                    if ((R != null) && (!((MOB) P).amDead()))
                        R.showHappens(CMMsg.MSG_OK_ACTION, P, L("<S-NAME> wander(s) off."));
                    P.destroy();
                }
            }
        }
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        int tickSet = super.tickDown;
        if (!super.tick(ticking, tickID))
            return false;
        if (ticking instanceof MOB) {
            final MOB mob = (MOB) ticking;
            if (mob.amFollowing() != null)
                super.tickDown = tickSet;
        }
        return true;
    }

    public int getUndeadLevel(final MOB mob, double baseLvl, double corpseLevel) {
        final ExpertiseLibrary exLib = CMLib.expertises();
        final double deathLoreExpertiseLevel = super.getXLEVELLevel(mob);
        final double appropriateLoreExpertiseLevel = super.getX1Level(mob);
        final double charLevel = mob.phyStats().level();
        final double maxDeathLoreExpertiseLevel = exLib.getHighestListableStageBySkill(mob, ID(), ExpertiseLibrary.Flag.LEVEL);
        final double maxApproLoreExpertiseLevel = exLib.getHighestListableStageBySkill(mob, ID(), ExpertiseLibrary.Flag.X1);
        double lvl = (charLevel * appropriateLoreExpertiseLevel / maxApproLoreExpertiseLevel)
            - (baseLvl + 4 + (2 * maxDeathLoreExpertiseLevel));
        if (lvl < 0.0)
            lvl = 0.0;
        lvl += baseLvl + (2 * deathLoreExpertiseLevel);
        if (lvl > corpseLevel)
            lvl = corpseLevel;
        return (int) Math.round(lvl);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Physical target = getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_UNWORNONLY);
        if (target == null)
            return false;

        if (target == mob) {
            mob.tell(L("@x1 doesn't look dead yet.", target.name(mob)));
            return false;
        }
        if (!(target instanceof DeadBody)) {
            mob.tell(L("You can't animate that."));
            return false;
        }

        final DeadBody body = (DeadBody) target;
        if (body.isPlayerCorpse() || (body.getMobName().length() == 0)
            || ((body.charStats() != null) && (body.charStats().getMyRace() != null) && (body.charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead")))) {
            mob.tell(L("You can't animate that."));
            return false;
        }
        String race = "a";
        if ((body.charStats() != null) && (body.charStats().getMyRace() != null))
            race = CMLib.english().startWithAorAn(body.charStats().getMyRace().name()).toLowerCase();
        String description = body.getMobDescription();
        if (description.trim().length() == 0)
            description = "It looks dead.";
        else
            description += "\n\rIt also looks dead.";

        if (body.basePhyStats().level() < 3) {
            mob.tell(L("This creature is too weak to create a zombie from."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob, target, auto), auto ? "" : L("^S<S-NAME> @x1 to animate <T-NAMESELF> as a zombie.^?", prayForWord(mob)));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                final MOB newMOB = CMClass.getMOB("GenUndead");
                newMOB.setName(L("@x1 zombie", race));
                newMOB.setDescription(description);
                newMOB.setDisplayText(L("@x1 zombie is here", race));
                newMOB.basePhyStats().setLevel(getUndeadLevel(mob, 2, body.phyStats().level()));
                newMOB.baseCharStats().setStat(CharStats.STAT_GENDER, body.charStats().getStat(CharStats.STAT_GENDER));
                newMOB.baseCharStats().setMyRace(CMClass.getRace("Undead"));
                newMOB.baseCharStats().setBodyPartsFromStringAfterRace(body.charStats().getBodyPartsAsString());
                final Ability P = CMClass.getAbility("Prop_StatTrainer");
                if (P != null) {
                    P.setMiscText("NOTEACH STR=20 INT=10 WIS=10 CON=10 DEX=3 CHA=2");
                    newMOB.addNonUninvokableEffect(P);
                }
                newMOB.basePhyStats().setSensesMask(PhyStats.CAN_SEE_DARK);
                newMOB.recoverCharStats();
                newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
                newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
                CMLib.factions().setAlignment(newMOB, Faction.Align.EVIL);
                newMOB.baseState().setHitPoints(25 * newMOB.basePhyStats().level());
                newMOB.baseState().setMovement(30);
                newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
                newMOB.baseState().setMana(0);
                newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
                newMOB.addNonUninvokableEffect(CMClass.getAbility("Spell_CauseStink"));
                final Behavior B = CMClass.getBehavior("Aggressive");
                if (B != null) {
                    B.setParms("+NAMES \"-" + mob.Name() + "\" -LEVEL +>" + newMOB.basePhyStats().level());
                    newMOB.addBehavior(B);
                }
                newMOB.recoverCharStats();
                newMOB.recoverPhyStats();
                newMOB.recoverMaxState();
                newMOB.resetToMaxState();
                newMOB.text();
                newMOB.bringToLife(mob.location(), true);
                CMLib.moneyCounter().clearZeroMoney(newMOB, null);
                newMOB.setMoneyVariation(0);
                //newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
                int it = 0;
                while (it < newMOB.location().numItems()) {
                    final Item item = newMOB.location().getItem(it);
                    if ((item != null) && (item.container() == body)) {
                        final CMMsg msg2 = CMClass.getMsg(newMOB, body, item, CMMsg.MSG_GET, null);
                        newMOB.location().send(newMOB, msg2);
                        final CMMsg msg4 = CMClass.getMsg(newMOB, item, null, CMMsg.MSG_GET, null);
                        newMOB.location().send(newMOB, msg4);
                        final CMMsg msg3 = CMClass.getMsg(newMOB, item, null, CMMsg.MSG_WEAR, null);
                        newMOB.location().send(newMOB, msg3);
                        if (!newMOB.isMine(item))
                            it++;
                        else
                            it = 0;
                    } else
                        it++;
                }
                body.destroy();
                newMOB.setStartRoom(null);
                beneficialAffect(mob, newMOB, 0, 0);
                mob.location().show(newMOB, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> begin(s) to rise!"));
                mob.location().recoverRoomStats();
            }
        } else
            return beneficialWordsFizzle(mob, target, L("<S-NAME> @x1 to animate <T-NAMESELF>, but fail(s) miserably.", prayForWord(mob)));

        // return whether it worked
        return success;
    }
}
