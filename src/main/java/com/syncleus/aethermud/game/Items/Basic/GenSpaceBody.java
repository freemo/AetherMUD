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
package com.syncleus.aethermud.game.Items.Basic;

import com.syncleus.aethermud.game.Libraries.interfaces.GenericBuilder;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.game.core.interfaces.Environmental;
import com.syncleus.aethermud.game.core.interfaces.SpaceObject;


public class GenSpaceBody extends StdSpaceBody {
    private final static String[] MYCODES = {"DIRECTION", "SPEED", "RADIUS", "COORDINATES"};
    private static String[] codes = null;
    protected String readableText = "";

    public GenSpaceBody() {
        super();
        setName("a generic space object");
        setDisplayText("a generic space object sits here.");
        setDescription("");
    }

    @Override
    public String ID() {
        return "GenSpaceBody";
    }

    @Override
    public boolean isGeneric() {
        return true;
    }

    @Override
    public String text() {
        return CMLib.aetherMaker().getPropertiesStr(this, false);
    }

    @Override
    public String readableText() {
        return readableText;
    }

    @Override
    public void setReadableText(String text) {
        readableText = text;
    }

    @Override
    public void setMiscText(String newText) {
        miscText = "";
        CMLib.aetherMaker().setPropertiesStr(this, newText, false);
        recoverPhyStats();
    }

    @Override
    public String getStat(String code) {
        if (CMLib.aetherMaker().getGenItemCodeNum(code) >= 0)
            return CMLib.aetherMaker().getGenItemStat(this, code);
        switch (getCodeNum(code)) {
            case 0:
                return "" + CMParms.toListString(direction());
            case 1:
                return "" + speed();
            case 2:
                return "" + radius();
            case 3:
                return "" + CMParms.toListString(coordinates());
            default:
                return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
        }
    }

    @Override
    public void setStat(String code, String val) {
        if (CMLib.aetherMaker().getGenItemCodeNum(code) >= 0)
            CMLib.aetherMaker().setGenItemStat(this, code, val);
        else
            switch (getCodeNum(code)) {
                case 0:
                    setDirection(CMParms.toDoubleArray(CMParms.parseCommas(val, true)));
                    break;
                case 1:
                    setSpeed(CMath.s_parseMathExpression(val));
                    break;
                case 2:
                    setRadius(CMath.s_parseLongExpression(val));
                    break;
                case 3:
                    setCoords(CMParms.toLongArray(CMParms.parseCommas(val, true)));
                    coordinates[0] = coordinates[0] % SpaceObject.Distance.GalaxyRadius.dm;
                    coordinates[1] = coordinates[1] % SpaceObject.Distance.GalaxyRadius.dm;
                    coordinates[2] = coordinates[2] % SpaceObject.Distance.GalaxyRadius.dm;
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
        final String[] MYCODES = CMProps.getStatCodesList(GenSpaceBody.MYCODES, this);
        final String[] superCodes = CMParms.toStringArray(GenericBuilder.GenItemCode.values());
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
        if (!(E instanceof GenSpaceBody))
            return false;
        final String[] theCodes = getStatCodes();
        for (int i = 0; i < theCodes.length; i++) {
            if (!E.getStat(theCodes[i]).equals(getStat(theCodes[i])))
                return false;
        }
        return true;
    }
}
