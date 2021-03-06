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
package com.syncleus.aethermud.game.Abilities.Properties;

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Common.interfaces.CharStats;
import com.syncleus.aethermud.game.Common.interfaces.PhyStats;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMSecurity;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;

import java.util.List;


public class Prop_WizInvis extends Property {
    protected boolean disabled = false;
    protected boolean unInvokable = false;
    protected int abilityCode = PhyStats.IS_NOT_SEEN | PhyStats.IS_CLOAKED;

    @Override
    public String ID() {
        return "Prop_WizInvis";
    }

    @Override
    public long flags() {
        return Ability.FLAG_ADJUSTER;
    }

    @Override
    public String displayText() {
        if (CMath.bset(abilityCode(), PhyStats.IS_CLOAKED | PhyStats.IS_NOT_SEEN))
            return "(Wizard Invisibility)";
        else if (CMath.bset(abilityCode(), PhyStats.IS_NOT_SEEN))
            return "(WizUndetectable)";
        else if (CMath.bset(abilityCode(), PhyStats.IS_CLOAKED))
            return "(Cloaked)";
        else
            return "";
    }

    @Override
    public String name() {
        return "Wizard Invisibility";
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_MOBS;
    }

    @Override
    public int abilityCode() {
        return abilityCode;
    }

    @Override
    public void setAbilityCode(int newCode) {
        abilityCode = newCode;
    }

    @Override
    public String accountForYourself() {
        return "Wizard Invisibile";
    }

    @Override
    public boolean canBeUninvoked() {
        return true;
    }

    public boolean isAnAutoEffect() {
        return false;
    }

    @Override
    public void setMiscText(String newMiscText) {
        super.setMiscText(newMiscText);
        List<String> ps = CMParms.parse(newMiscText.toUpperCase());
        unInvokable = ps.contains("UNINVOKABLE");
    }

    @Override
    public void affectPhyStats(Physical affected, PhyStats affectableStats) {
        super.affectPhyStats(affected, affectableStats);
        // when this spell is on a MOBs Affected list,
        // it should consistantly put the mob into
        // a sleeping state, so that nothing they do
        // can get them out of it.
        affectableStats.setDisposition(affectableStats.disposition() | abilityCode);
        if (CMath.bset(abilityCode(), PhyStats.IS_NOT_SEEN)) {
            affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_INVISIBLE);
            affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_HIDDEN);
            affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_SNEAKING);
            affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_FLYING);
            affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_CLIMBING);
            affectableStats.setDisposition(affectableStats.disposition() | PhyStats.IS_SWIMMING);
        }
        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_SEE_HIDDEN);
        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_SEE_SNEAKERS);
        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_SEE_DARK);
        affectableStats.setSensesMask(affectableStats.sensesMask() | PhyStats.CAN_SEE_INVISIBLE);
    }

    @Override
    public void affectCharStats(MOB affected, CharStats affectableStats) {
        super.affectCharStats(affected, affectableStats);
        affected.curState().setHunger(affected.maxState().maxHunger(affected.baseWeight()));
        affected.curState().setThirst(affected.maxState().maxThirst(affected.baseWeight()));
    }

    @Override
    public void unInvoke() {
        if (!(affected instanceof MOB))
            return;
        final MOB mob = (MOB) affected;

        if (affected == null)
            return;
        final Physical being = affected;

        if (this.canBeUninvoked()) {
            being.delEffect(this);
            if (being instanceof Room)
                ((Room) being).recoverRoomStats();
            else if (being instanceof MOB) {
                if (((MOB) being).location() != null)
                    ((MOB) being).location().recoverRoomStats();
                else {
                    being.recoverPhyStats();
                    ((MOB) being).recoverCharStats();
                    ((MOB) being).recoverMaxState();
                }
            } else
                being.recoverPhyStats();
            mob.tell(L("You begin to fade back into view."));
        }
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if ((CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS)
            && (msg.amITarget(affected))
            && (affected != null)
            && (!disabled))) {
            if (msg.source() != msg.target()) {
                msg.source().tell(L("Ah, leave @x1 alone.", affected.name()));
                if (affected instanceof MOB)
                    ((MOB) affected).makePeace(true);
            }
            return false;
        } else if (affected instanceof MOB) {
            if ((CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS)) && (msg.amISource((MOB) affected)))
                disabled = true;
            else if (msg.amISource((MOB) affected)) {
                final Room R = msg.source().location();
                if (R != null) {
                    if ((msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS))
                        && (!CMSecurity.isAllowed(msg.source(), R, CMSecurity.SecFlag.SYSMSGS)))
                        msg.source().setAttribute(MOB.Attrib.SYSOPMSGS, false);
                    else if (unInvokable
                        && (!CMSecurity.isAllowed(msg.source(), R, CMSecurity.SecFlag.WIZINV)))
                        unInvoke();
                }

            }
        }
        return super.okMessage(myHost, msg);
    }
}
