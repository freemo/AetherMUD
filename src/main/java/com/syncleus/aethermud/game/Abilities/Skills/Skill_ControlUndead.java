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
package com.syncleus.aethermud.game.Abilities.Skills;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Skill_ControlUndead extends StdSkill {
    private final static String localizedName = CMLib.lang().L("Control Undead");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Controlled)");
    private static final String[] triggerStrings = I(new String[]{"CONTROL"});
    protected MOB charmer = null;

    @Override
    public String ID() {
        return "Skill_ControlUndead";
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
    protected int canAffectCode() {
        return 0;
    }

    @Override
    protected int canTargetCode() {
        return CAN_MOBS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_SKILL | Ability.DOMAIN_DEATHLORE;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_MALICIOUS;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    protected MOB getCharmer() {
        if (charmer != null)
            return charmer;
        if ((invoker != null) && (invoker != affected))
            charmer = invoker;
        else if ((text().length() > 0) && (affected instanceof MOB)) {
            final Room R = ((MOB) affected).location();
            if (R != null)
                charmer = R.fetchInhabitant(text());
        }
        if (charmer == null)
            return invoker;
        return charmer;
    }

    @Override
    public void executeMsg(final Environmental myHost, final CMMsg msg) {
        super.executeMsg(myHost, msg);
        if ((affected != null)
            && (affected instanceof MOB)
            && (!((MOB) affected).isMonster())
            && (msg.amISource((MOB) affected) || msg.amISource(((MOB) affected).amFollowing()) || (msg.source() == invoker()))
            && (msg.sourceMinor() == CMMsg.TYP_QUIT)) {
            unInvoke();
            if (msg.source().playerStats() != null)
                msg.source().playerStats().setLastUpdated(0);
        }
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (!(affected instanceof MOB))
            return true;

        final MOB mob = (MOB) affected;

        // when this spell is on a MOBs Affected list,
        // it should consistantly prevent the mob
        // from trying to do ANYTHING except sleep
        if ((msg.amITarget(mob))
            && (CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
            && (msg.amISource(getCharmer()) || ((mob.amFollowing() != null) && (msg.source() == mob.amFollowing()))))
            unInvoke();
        else if ((msg.amISource(mob))
            && (CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
            && (msg.amITarget(getCharmer()) || ((mob.amFollowing() != null) && (msg.target() == mob.amFollowing())))) {
            if ((!invoker().isInCombat()) && (msg.source().getVictim() != invoker())) {
                msg.source().tell(L("You're too submissive towards @x1", invoker().name()));
                if (invoker().getVictim() == msg.source()) {
                    invoker().makePeace(true);
                    invoker().setVictim(null);
                }
            } else
                msg.source().tell(L("You're too submissive towards @x1", invoker().name()));
            return false;
        } else if (!mob.isMonster()) {
            if ((msg.amISource(mob))
                && (!mob.isMonster())
                && (msg.target() instanceof Room)
                && ((msg.targetMinor() == CMMsg.TYP_LEAVE) || (msg.sourceMinor() == CMMsg.TYP_RECALL))
                && (mob.amFollowing() != null)
                && (((Room) msg.target()).isInhabitant(mob.amFollowing()))) {
                mob.tell(L("You don't want to leave your master."));
                return false;
            } else if ((msg.amISource(mob))
                && (mob.amFollowing() != null)
                && (msg.sourceMinor() == CMMsg.TYP_NOFOLLOW)) {
                msg.source().tell(L("You're too submissive towards @x1", invoker().name()));
                return false;
            }
        }

        return super.okMessage(myHost, msg);
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if ((affecting() == null) || (!(affecting() instanceof MOB)))
            return false;
        final MOB mob = (MOB) affecting();

        if (mob.isMonster())
            return super.tick(ticking, tickID);

        if ((getCharmer() != null)
            && (!CMLib.flags().isInTheGame(getCharmer(), false)))
            unInvoke();
        else if ((affected == mob)
            && (mob != getCharmer())
            && ((mob.amFollowing() == null) || (mob.amFollowing() != getCharmer()))
            && (mob.location() != null)
            && (mob.location().isInhabitant(getCharmer())))
            CMLib.commands().postFollow(mob, getCharmer(), true);
        return super.tick(ticking, tickID);
    }

    @Override
    public void unInvoke() {
        // undo the affects of this spell
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        super.unInvoke();

        if ((canBeUninvoked() && (!mob.amDead()))) {
            mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> free-will returns."));
            if (mob.amFollowing() != null)
                CMLib.commands().postFollow(mob, null, false);
            CMLib.commands().postStand(mob, true);
            if (mob.isMonster()) {
                if ((CMLib.dice().rollPercentage() > 50)
                    || ((mob.getStartRoom() != null)
                    && (mob.getStartRoom().getArea() != mob.location().getArea())
                    && (CMLib.flags().canMove(mob))
                    && (CMLib.flags().isAggressiveTo(mob, null) || (invoker == null) || (!mob.location().isInhabitant(invoker)))))
                    CMLib.tracking().wanderAway(mob, true, true);
                else if ((invoker != null) && (invoker != mob))
                    mob.setVictim(invoker);
            }
        }
    }

    @Override
    public int castingQuality(MOB mob, Physical target) {
        if ((mob != null) && (target != null)) {
            if (!(target instanceof MOB))
                return Ability.QUALITY_INDIFFERENT;
            final MOB targetM = (MOB) target;
            if ((targetM.baseCharStats().getMyRace() == null)
                || (!targetM.baseCharStats().getMyRace().racialCategory().equals("Undead")))
                return Ability.QUALITY_INDIFFERENT;
            if (CMLib.flags().isGood(mob))
                return Ability.QUALITY_INDIFFERENT;
            if ((mob.isMonster()) && (((MOB) target).isMonster()))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob, target);
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final MOB target = this.getTarget(mob, commands, givenTarget);
        if (target == null)
            return false;

        if ((target.baseCharStats().getMyRace() == null)
            || (!target.baseCharStats().getMyRace().racialCategory().equals("Undead"))) {
            mob.tell(auto ? L("Only the undead can be controlled.") : L("You can only control the undead."));
            return false;
        }

        if (CMLib.flags().isGood(mob)) {
            mob.tell(L("Only the wicked may control the undead."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        int levelAdj = 0;
        final Room R = mob.location();
        if ((R != null) && (R.getArea() != null)) {
            String value = R.getArea().getBlurbFlag(ID());
            if ((value != null) && (value.length() > 0)) {
                for (String s : CMParms.parse(value)) {
                    if (s.startsWith("+") && (CMath.isNumber(value.substring(1))))
                        levelAdj = CMath.s_int(value.substring(1));
                    else if (CMath.isNumber(s))
                        levelAdj = CMath.s_int(value.trim());
                }
            }
        }

        final int adjustedCasterLevel = mob.phyStats().level() + levelAdj + (2 * getXLEVELLevel(mob));
        boolean success = proficiencyCheck(mob, (adjustedCasterLevel - target.phyStats().level()) * 30, auto);

        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, target, this, CMMsg.MSG_CAST_ATTACK_SOMANTIC_SPELL | (auto ? CMMsg.MASK_ALWAYS : 0), auto ? L("<T-NAME> seem(s) controlled.") : L("^S<S-NAME> control(s) <T-NAMESELF>.^?"));
            if (mob.location().okMessage(mob, msg)) {
                mob.location().send(mob, msg);
                if (msg.value() <= 0) {
                    success = (((adjustedCasterLevel - target.phyStats().level()) > 1) || (target.phyStats().level() == 1));
                    if (success) {
                        if (!target.isMonster())
                            success = maliciousAffect(mob, target, asLevel, 0, CMMsg.MSK_CAST_VERBAL | CMMsg.TYP_MIND | CMMsg.MASK_ALWAYS) != null;
                        if (success) {
                            mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> <S-IS-ARE> now controlled."));
                            target.makePeace(true);
                            CMLib.commands().postFollow(target, mob, false);
                            CMLib.combat().makePeaceInGroup(mob);
                            invoker = mob;
                            if (target.amFollowing() != mob)
                                mob.tell(L("@x1 seems unwilling to obey you.", target.name(mob)));
                            else if (!target.isMonster())
                                beneficialAffect(mob, target, asLevel, 0);
                        }
                    }
                    if (!success) {
                        mob.location().show(target, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> seem(s) submissive!"));
                        target.makePeace(true);
                        beneficialAffect(mob, target, asLevel, 5);
                    }
                }
            }
        } else
            return maliciousFizzle(mob, target, L("<S-NAME> attempt(s) to control <T-NAMESELF>, but fail(s)."));

        // return whether it worked
        return success;
    }
}
