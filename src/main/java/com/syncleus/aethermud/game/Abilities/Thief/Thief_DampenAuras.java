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
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Physical;
import com.syncleus.aethermud.game.core.interfaces.Tickable;

import java.util.List;


public class Thief_DampenAuras extends ThiefSkill {
    private final static String localizedName = CMLib.lang().L("Dampen Auras");
    private final static String localizedStaticDisplay = CMLib.lang().L("(Dampened Auras)");
    private static final String[] triggerStrings = I(new String[]{"DAMPENAURAS"});

    @Override
    public String ID() {
        return "Thief_DampenAuras";
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
        return CAN_MOBS;
    }

    @Override
    protected int canTargetCode() {
        return 0;
    }

    @Override
    public int abstractQuality() {
        return Ability.QUALITY_INDIFFERENT;
    }

    @Override
    public int classificationCode() {
        return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_DECEPTIVE;
    }

    @Override
    public String[] triggerStrings() {
        return triggerStrings;
    }

    @Override
    public boolean tick(Tickable ticking, int tickID) {
        if (unInvoked)
            return false;
        return super.tick(ticking, tickID);
    }

    @Override
    public void affectPhyStats(Physical host, PhyStats stats) {
        super.affectPhyStats(host, stats);
        if (unInvoked)
            host.delEffect(this);
        else
            stats.addAmbiance("-MOST");
    }

    @Override
    public void executeMsg(Environmental host, CMMsg msg) {
        super.executeMsg(host, msg);
        if (super.canBeUninvoked()) {
            if ((affected != null)
                && (affected instanceof MOB)
                && (msg.amISource((MOB) affected))
                && (msg.sourceMinor() == CMMsg.TYP_QUIT))
                unInvoke();
            else if (msg.sourceMinor() == CMMsg.TYP_SHUTDOWN)
                unInvoke();
        }
    }

    @Override
    public void unInvoke() {
        final Environmental E = affected;
        super.unInvoke();
        if ((E instanceof MOB) && (!((MOB) E).amDead()))
            ((MOB) E).tell(L("You noticed the aura dampening is wearing away on @x1.", E.name()));
    }

    @Override
    public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel) {
        MOB target = mob;
        if ((auto) && (givenTarget != null) && (givenTarget instanceof MOB))
            target = (MOB) givenTarget;

        if (target.fetchEffect(this.ID()) != null) {
            mob.tell(target, null, null, L("<S-NAME> can't dampen <S-YOUPOSS> auras again so soon."));
            return false;
        }

        if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
            return false;

        final boolean success = proficiencyCheck(mob, 0, auto);

        final CMMsg msg = CMClass.getMsg(mob, target, this, auto ? CMMsg.MASK_ALWAYS : CMMsg.MSG_DELICATE_SMALL_HANDS_ACT, CMMsg.MSG_OK_VISUAL, CMMsg.MSG_OK_VISUAL, auto ? "" : L("<T-NAME> dampen(s) <T-HIS-HER> auras."));
        if (!success)
            return beneficialVisualFizzle(mob, null, auto ? "" : L("<S-NAME> attempt(s) to dampen <S-HIS-HER> auras, but fail(s)."));
        else if (mob.location().okMessage(mob, msg)) {
            mob.location().send(mob, msg);
            beneficialAffect(mob, target, asLevel, 0);
            final Ability A = target.fetchEffect(ID());
            if (A != null) {
                A.tick(target, Tickable.TICKID_MOB);
                Item I = null;
                final Physical affecting = A.affecting();
                final StringBuffer items = new StringBuffer("");
                for (int i = 0; i < target.numItems(); i++) {
                    I = target.getItem(i);
                    if ((I != null) && (I.container() == null)) {
                        I.addEffect(A);
                        A.setAffectedOne(affecting);
                        items.append(", " + I.name());
                    }
                }
                if (items.length() > 2)
                    target.tell(L("You've dampened the auras on the following items: @x1", items.substring(2)));
                target.location().recoverRoomStats();
            }
        }
        return success;
    }
}
