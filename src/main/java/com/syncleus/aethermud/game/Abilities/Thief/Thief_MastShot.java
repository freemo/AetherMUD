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
package com.syncleus.aethermud.game.Abilities.Thief;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Items.interfaces.*;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.PhysicalAgent;
import com.syncleus.aethermud.game.core.interfaces.Rideable;

import java.util.List;


public class Thief_MastShot extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Mast Shot");
    private static final String[] triggerStrings = I(new String[]{"MASTSHOT"});
    protected int code = 0;

    @Override
    public String ID() {
        return "Thief_MastShot";
    }

    @Override
    public String name() {
        return localizedName;
    }

    @Override
    public String displayText() {
        return "";
    }

    @Override
    protected int canAffectCode() {
        return 0;
    }

    @Override
    protected int canTargetCode() {
        return CAN_ITEMS;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public int usageType() {
        return USAGE_MOVEMENT;
    }

    @Override
    public int abilityCode() {
        return code;
    }

    @Override
    public void setAbilityCode(int newCode) {
        code = newCode;
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        if (affected instanceof BoardableShip) {
            affectableStats.setAbility(affectableStats.ability() - abilityCode());
        }
    }

    @Override
    public void setMiscText(String newMiscText) {
        super.setMiscText(newMiscText);
        if (CMath.isInteger(newMiscText))
            setAbilityCode(CMath.s_int(newMiscText));
    }

    @Override
    public boolean okMessage(Environmental myHost, CMMsg msg) {
        if (!super.okMessage(myHost, msg))
            return false;
        final Physical affected = this.affected;
        if (affected instanceof BoardableShip) {

        } else if (affected instanceof AmmunitionWeapon) {
            if ((msg.source().riding() instanceof SailingShip)
                && (msg.tool() == affected)
                && (msg.targetMinor() == CMMsg.TYP_DAMAGE)
                && (msg.target() instanceof Rideable)
                && (msg.target() instanceof BoardableShip)
                && (msg.target() instanceof Physical)
                ) {
                if (CMLib.dice().rollPercentage() < (10 + abilityCode())) {
                    final Room room = CMLib.map().roomLocation(msg.target());
                    final AmmunitionWeapon weapon = (AmmunitionWeapon) affected;
                    final String mastString = "^F^<FIGHT^>" +
                        L("<O-NAME> fired from <S-NAME> shreds <T-NAME>'s mast.")
                        + "^</FIGHT^>^?";
                    final CMMsg msg2 = CMClass.getMsg(msg.source(),
                        msg.target(),
                        weapon,
                        CMMsg.MSG_ATTACKMISS,
                        mastString);
                    CMLib.color().fixSourceFightColor(msg2);
                    if ((room != null)
                        && (room.okMessage(msg.source(), msg2))) {
                        room.send(msg.source(), msg2);
                        Ability oldA = ((Physical) msg.target()).fetchEffect(ID());
                        if (oldA != null) {
                            oldA.setAbilityCode(oldA.abilityCode() + 1);
                        } else {
                            final MOB mob = (invoker != null) ? invoker : msg.source();
                            Ability A = beneficialAffect(mob, (Physical) msg.target(), 0, 0);
                            if (A != null)
                                A.setAbilityCode(1);
                        }
                    }
                    if (this.canBeUninvoked())
                        unInvoke();
                    return false;
                } else {
                    CMLib.combat().postShipWeaponAttackResult(msg.source(), (SailingShip) msg.source().riding(), (Rideable) msg.target(), (Weapon) affected, false);
                    if (this.canBeUninvoked())
                        unInvoke();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        final Room R = mob.location();
        if (R == null)
            return false;
        if ((!(R.getArea() instanceof BoardableShip))
            || (!(((BoardableShip) R.getArea()).getShipItem() instanceof SailingShip))
            || ((R.domainType() & Room.INDOORS) != 0)) {
            mob.tell(L("You must be on the deck of a ship to fire a mast shot."));
            return false;
        }
        final BoardableShip myShip = (BoardableShip) R.getArea();
        final SailingShip myShipItem = (SailingShip) myShip.getShipItem();
        if ((myShipItem == null)
            || (!(myShipItem.owner() instanceof Room))
            || (!CMLib.flags().isWateryRoom((Room) myShipItem.owner()))) {
            mob.tell(L("Your ship must be at sea to fire a mast shot."));
            return false;
        }
        SailingShip enemyShip = null;
        PhysicalAgent combatant = myShipItem.getCombatant();
        if (combatant != null) {
            if (combatant instanceof SailingShip)
                enemyShip = (SailingShip) combatant;
        }

        if (enemyShip == null) {
            mob.tell(L("Your ship must be in combat with another big ship to fire a mast shot."));
            return false;
        }

        final Item enemyShipItem = enemyShip.getShipItem();

        if (commands.size() == 0) {
            mob.tell(L("You must specify a siege weapon to make the mast shot with."));
            return false;
        }

        final String weaponName = CMParms.combine(commands);

        if (!CMLib.flags().isStanding(mob) && (!auto)) {
            mob.tell(L("You need to stand up!"));
            return false;
        }

        final Item siegeItem = R.findItem(null, weaponName);
        if (((siegeItem) == null)
            || (!CMLib.flags().canBeSeenBy(siegeItem, mob))
            || (!CMLib.combat().isAShipSiegeWeapon(siegeItem))) {
            mob.tell(L("You don't see a siege weapon called '@x1' here.", weaponName));
            return false;
        }

        if (siegeItem.fetchEffect(ID()) != null) {
            mob.tell(L("That weapon is already aimed at the masts."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        boolean success = proficiencyCheck(mob, 0, auto);
        if (success) {
            final CMMsg msg = CMClass.getMsg(mob, siegeItem, this, auto ? CMMsg.MSG_OK_ACTION : CMMsg.MSG_THIEF_ACT, auto ? "" : L("<S-NAME> aim(s) <T-NAME> at @x1's masts!", enemyShipItem.Name()));
            if (R.okMessage(mob, msg)) {
                R.send(mob, msg);
                Ability A = beneficialAffect(mob, siegeItem, asLevel, 0);
                A.setAbilityCode(2 * super.getXLEVELLevel(mob));
            }
        } else
            super.beneficialVisualFizzle(mob, siegeItem, L("<S-NAME> tr(ys) to aim <T-NAME> at @x1's masts, but fail(s).", enemyShipItem.Name()));
        return success;
    }
}
