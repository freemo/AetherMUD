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
import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.game.Common.interfaces.CMMsg;
import com.syncleus.aethermud.game.Items.interfaces.Item;
import com.syncleus.aethermud.game.Items.interfaces.Wearable;
import com.syncleus.aethermud.game.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.syncleus.aethermud.game.Locales.interfaces.Room;
import com.syncleus.aethermud.game.MOBS.interfaces.MOB;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMStrings;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.CMath.CompiledFormula;
import com.syncleus.aethermud.game.core.interfaces.Environmental;


public class Prop_ModExperience extends Property {
    protected String operationFormula = "";
    protected boolean selfXP = false;
    protected CompiledFormula operation = null;
    protected CompiledZMask mask = null;

    @Override
    public String ID() {
        return "Prop_ModExperience";
    }

    @Override
    public String name() {
        return "Modifying Experience Gained";
    }

    @Override
    protected int canAffectCode() {
        return Ability.CAN_MOBS | Ability.CAN_ITEMS | Ability.CAN_AREAS | Ability.CAN_ROOMS;
    }

    @Override
    public String accountForYourself() {
        return "Modifies experience gained: " + operationFormula;
    }

    public int translateAmount(int amount, String val) {
        if (amount < 0)
            amount = -amount;
        if (val.endsWith("%"))
            return (int) Math.round(CMath.mul(amount, CMath.div(CMath.s_int(val.substring(0, val.length() - 1)), 100)));
        return CMath.s_int(val);
    }

    public String translateNumber(String val) {
        if (val.endsWith("%"))
            return "( @x1 * (" + val.substring(0, val.length() - 1) + " / 100) )";
        return Integer.toString(CMath.s_int(val));
    }

    @Override
    public void setMiscText(String newText) {
        super.setMiscText(newText);
        operation = null;
        mask = null;
        selfXP = false;
        String s = newText.trim();
        int x = s.indexOf(';');
        if (x >= 0) {
            mask = CMLib.masking().getPreCompiledMask(s.substring(x + 1).trim());
            s = s.substring(0, x).trim();
        }
        x = s.indexOf("SELF");
        if (x >= 0) {
            selfXP = true;
            s = s.substring(0, x) + s.substring(x + 4);
        }
        operationFormula = "Amount " + s;
        if (s.startsWith("="))
            operation = CMath.compileMathExpression(translateNumber(s.substring(1)).trim());
        else if (s.startsWith("+"))
            operation = CMath.compileMathExpression("@x1 + " + translateNumber(s.substring(1)).trim());
        else if (s.startsWith("-"))
            operation = CMath.compileMathExpression("@x1 - " + translateNumber(s.substring(1)).trim());
        else if (s.startsWith("*"))
            operation = CMath.compileMathExpression("@x1 * " + translateNumber(s.substring(1)).trim());
        else if (s.startsWith("/"))
            operation = CMath.compileMathExpression("@x1 / " + translateNumber(s.substring(1)).trim());
        else if (s.startsWith("(") && (s.endsWith(")"))) {
            operationFormula = "Amount =" + s;
            operation = CMath.compileMathExpression(s);
        } else
            operation = CMath.compileMathExpression(translateNumber(s.trim()));
        operationFormula = CMStrings.replaceAll(operationFormula, "@x1", "Amount");
    }

    @Override
    public boolean okMessage(final Environmental myHost, final CMMsg msg) {
        if (operation == null)
            setMiscText(text());
        if ((msg.sourceMinor() == CMMsg.TYP_EXPCHANGE)
            && (operation != null)
            && ((((msg.target() == affected) || (selfXP && (msg.source() == affected)))
            && (affected instanceof MOB))
            || ((affected instanceof Item)
            && (msg.source() == ((Item) affected).owner())
            && (!((Item) affected).amWearingAt(Wearable.IN_INVENTORY)))
            || (affected instanceof Room)
            || (affected instanceof Area))) {
            if (mask != null) {
                if (affected instanceof Item) {
                    if ((msg.target() == null) || (!(msg.target() instanceof MOB)) || (!CMLib.masking().maskCheck(mask, msg.target(), true)))
                        return super.okMessage(myHost, msg);
                } else if (!CMLib.masking().maskCheck(mask, msg.source(), true))
                    return super.okMessage(myHost, msg);
            }
            msg.setValue((int) Math.round(CMath.parseMathExpression(operation, new double[]{msg.value()}, 0.0)));
        }
        return super.okMessage(myHost, msg);
    }
}
