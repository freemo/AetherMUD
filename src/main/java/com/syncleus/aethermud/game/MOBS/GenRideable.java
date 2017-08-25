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
package com.syncleus.aethermud.game.MOBS;

import com.syncleus.aethermud.game.Libraries.interfaces.GenericBuilder;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.Rideable;


public class GenRideable extends StdRideable {
    private final static String[] MYCODES = {"RIDEBASIS", "MOBSHELD"};
    private static String[] codes = null;

    public GenRideable() {
        super();
        username = "a generic horse";
        setDescription("");
        setDisplayText("A generic horse stands here.");
        basePhyStats().setAbility(CMProps.getMobHPBase()); // his only off-default

        recoverMaxState();
        resetToMaxState();
        recoverPhyStats();
        recoverCharStats();
    }

    @Override
    public String ID() {
        return "GenRideable";
    }

    @Override
    public boolean isGeneric() {
        return true;
    }

    @Override
    public String text() {
        if (CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS))
            miscText = CMLib.encoder().compressString(CMLib.coffeeMaker().getPropertiesStr(this, false));
        else
            miscText = CMLib.coffeeMaker().getPropertiesStr(this, false);
        return super.text();
    }

    @Override
    public void setMiscText(String newText) {
        super.setMiscText(newText);
        CMLib.coffeeMaker().resetGenMOB(this, newText);
    }

    @Override
    public String getStat(String code) {
        if (CMLib.coffeeMaker().getGenMobCodeNum(code) >= 0)
            return CMLib.coffeeMaker().getGenMobStat(this, code);
        switch (getCodeNum(code)) {
            case 0:
                return "" + rideBasis();
            case 1:
                return "" + riderCapacity();
            default:
                return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
        }
    }

    @Override
    public void setStat(String code, String val) {
        if (CMLib.coffeeMaker().getGenMobCodeNum(code) >= 0)
            CMLib.coffeeMaker().setGenMobStat(this, code, val);
        else
            switch (getCodeNum(code)) {
                case 0:
                    setRideBasis(CMath.s_parseListIntExpression(Rideable.RIDEABLE_DESCS, val));
                    break;
                case 1:
                    setRiderCapacity(CMath.s_parseIntExpression(val));
                    break;
                default:
                    CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
                    break;
            }
    }

    @Override
    protected int getCodeNum(String code) {
        for (int i = 0; i < MYCODES.length; i++) {
            if (code.equalsIgnoreCase(MYCODES[i]))
                return i;
        }
        return -1;
    }

    @Override
    public String[] getStatCodes() {
        if (codes != null)
            return codes;
        final String[] MYCODES = CMProps.getStatCodesList(GenRideable.MYCODES, this);
        final String[] superCodes = CMParms.toStringArray(GenericBuilder.GenMOBCode.values());
        codes = new String[superCodes.length + MYCODES.length];
        int i = 0;
        for (; i < superCodes.length; i++)
            codes[i] = superCodes[i];
        for (int x = 0; x < MYCODES.length; i++, x++)
            codes[i] = MYCODES[x];
        return codes;
    }

    @Override
    public boolean sameAs(Environmental E) {
        if (!(E instanceof GenRideable))
            return false;
        final String[] codes = getStatCodes();
        for (int i = 0; i < codes.length; i++) {
            if (!E.getStat(codes[i]).equals(getStat(codes[i])))
                return false;
        }
        return true;
    }
}
