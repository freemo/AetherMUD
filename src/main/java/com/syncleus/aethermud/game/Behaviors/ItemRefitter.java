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
package com.planet_ink.game.Behaviors;

import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Items.interfaces.Armor;
import com.planet_ink.game.Items.interfaces.Coins;
import com.planet_ink.game.Items.interfaces.Item;
import com.planet_ink.game.MOBS.interfaces.MOB;
import com.planet_ink.game.core.*;
import com.planet_ink.game.core.interfaces.Environmental;


public class ItemRefitter extends StdBehavior {
    private CMath.CompiledFormula costFormula = null;

    @Override
    public String ID() {
        return "ItemRefitter";
    }

    @Override
    public String accountForYourself() {
        return "item refitting for a price";
    }

    protected double cost(Item item) {
        if (costFormula != null) {
            final double[] vars = {item.phyStats().level(), item.value(), item.usesRemaining(), CMLib.flags().isABonusItems(item) ? 1.0 : 0.0, item.basePhyStats().level(), item.baseGoldValue(), 0, 0, 0, 0, 0};
            return CMath.parseMathExpression(costFormula, vars, 0.0);
        } else {
            int cost = item.phyStats().level() * 100;
            if (CMLib.flags().isABonusItems(item))
                cost += (item.phyStats().level() * 100);
            return cost;
        }
    }

    @Override
    public void setParms(String parms) {
        super.setParms(parms);
        final String formulaString = CMParms.getParmStr(parms, "COST", "(@x1*100)+(@x4*@x1*100)");
        costFormula = null;
        if (formulaString.trim().length() > 0) {
            try {
                costFormula = CMath.compileMathExpression(formulaString);
            } catch (final Exception e) {
                Log.errOut(ID(), "Error compiling formula: " + formulaString);
            }
        }
    }

    @Override
    public boolean okMessage(Environmental affecting, CMMsg msg) {
        if (!super.okMessage(affecting, msg))
            return false;
        final MOB source = msg.source();
        if (!canFreelyBehaveNormal(affecting))
            return true;
        final MOB observer = (MOB) affecting;
        if ((source != observer)
            && (msg.amITarget(observer))
            && (msg.targetMinor() == CMMsg.TYP_GIVE)
            && (!CMSecurity.isAllowed(source, source.location(), CMSecurity.SecFlag.CMDROOMS))
            && (!(msg.tool() instanceof Coins))
            && (msg.tool() instanceof Item)) {
            final Item tool = (Item) msg.tool();
            final double cost = cost(tool);
            if (!(tool instanceof Armor)) {
                CMLib.commands().postSay(observer, source, L("I'm sorry, I can't refit that."), true, false);
                return false;
            }

            if (tool.basePhyStats().height() == 0) {
                CMLib.commands().postSay(observer, source, L("This already looks your size!"), true, false);
                return false;
            }
            if (CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(), observer) < (cost)) {
                final String costStr = CMLib.beanCounter().nameCurrencyShort(observer, cost);
                CMLib.commands().postSay(observer, source, L("You'll need @x1 for me to refit that.", costStr), true, false);
                return false;
            }
            return true;
        }
        return true;
    }

    @Override
    public void executeMsg(Environmental affecting, CMMsg msg) {
        super.executeMsg(affecting, msg);
        final MOB source = msg.source();
        if (!canFreelyBehaveNormal(affecting))
            return;
        final MOB observer = (MOB) affecting;

        if ((source != observer)
            && (msg.amITarget(observer))
            && (msg.targetMinor() == CMMsg.TYP_GIVE)
            && (!CMSecurity.isAllowed(source, source.location(), CMSecurity.SecFlag.CMDROOMS))
            && (!(msg.tool() instanceof Coins))
            && (msg.tool() instanceof Armor)) {
            final double cost = cost((Item) msg.tool());
            CMLib.beanCounter().subtractMoney(source, CMLib.beanCounter().getCurrency(observer), cost);
            final String costStr = CMLib.beanCounter().nameCurrencyLong(observer, cost);
            source.recoverPhyStats();
            ((Item) msg.tool()).basePhyStats().setHeight(0);
            ((Item) msg.tool()).recoverPhyStats();

            CMMsg newMsg = CMClass.getMsg(observer, source, msg.tool(), CMMsg.MSG_GIVE, L("<S-NAME> give(s) <O-NAME> to <T-NAMESELF> and charges <T-NAMESELF> @x1.", costStr));
            msg.addTrailerMsg(newMsg);
            newMsg = CMClass.getMsg(observer, source, null, CMMsg.MSG_SPEAK, L("^T<S-NAME> say(s) 'There she is, a perfect fit!  Thanks for your business' to <T-NAMESELF>.^?"));
            msg.addTrailerMsg(newMsg);
            newMsg = CMClass.getMsg(observer, msg.tool(), null, CMMsg.MSG_DROP, null);
            msg.addTrailerMsg(newMsg);
        }
    }
}