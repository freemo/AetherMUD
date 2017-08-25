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
package com.planet_ink.game.Items.CompTech;

import com.planet_ink.game.Common.interfaces.CMMsg;
import com.planet_ink.game.Items.interfaces.TechComponent;
import com.planet_ink.game.Libraries.interfaces.GenericBuilder;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMParms;
import com.planet_ink.game.core.CMProps;
import com.planet_ink.game.core.CMath;
import com.planet_ink.game.core.interfaces.Environmental;

import java.util.List;


public class GenShipWeapon extends StdShipWeapon {
    private final static String[] MYCODES = {"POWERCAP", "ACTIVATED", "POWERREM", "MANUFACTURER", "INSTFACT",
        "SWARNUMPORTS", "SWARPORTS", "SWARMTYPES", "RECHRATE"};
    private static String[] codes = null;

    public GenShipWeapon() {
        super();
        setName("a generic ship weapon");
        setDisplayText("a generic ship weapon.");
        setDescription("");
    }

    @Override
    public String ID() {
        return "GenShipWeapon";
    }

    @Override
    public boolean isGeneric() {
        return true;
    }

    @Override
    public String text() {
        return CMLib.coffeeMaker().getPropertiesStr(this, false);
    }

    @Override
    public void setMiscText(String newText) {
        miscText = "";
        CMLib.coffeeMaker().setPropertiesStr(this, newText, false);
        recoverPhyStats();
    }

    @Override
    public String getStat(String code) {
        if (CMLib.coffeeMaker().getGenItemCodeNum(code) >= 0)
            return CMLib.coffeeMaker().getGenItemStat(this, code);
        switch (getCodeNum(code)) {
            case 0:
                return "" + powerCapacity();
            case 1:
                return "" + activated();
            case 2:
                return "" + powerRemaining();
            case 3:
                return "" + getManufacturerName();
            case 4:
                return "" + getInstalledFactor();
            case 5:
                return "" + getPermittedNumDirections();
            case 6:
                return CMParms.toListString(getPermittedDirections());
            case 7: {
                final StringBuilder str = new StringBuilder("");
                for (int i = 0; i < this.getDamageMsgTypes().length; i++) {
                    if (i > 0)
                        str.append(", ");
                    str.append(CMMsg.TYPE_DESCS[getDamageMsgTypes()[i]]);
                }
                return str.toString();
            }
            case 8:
                return "" + getRechargeRate();
            default:
                return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
        }
    }

    @Override
    public void setStat(String code, String val) {
        if (CMLib.coffeeMaker().getGenItemCodeNum(code) >= 0)
            CMLib.coffeeMaker().setGenItemStat(this, code, val);
        else
            switch (getCodeNum(code)) {
                case 0:
                    setPowerCapacity(CMath.s_parseLongExpression(val));
                    break;
                case 1:
                    activate(CMath.s_bool(val));
                    break;
                case 2:
                    setPowerRemaining(CMath.s_parseLongExpression(val));
                    break;
                case 3:
                    setManufacturerName(val);
                    break;
                case 4:
                    setInstalledFactor((float) CMath.s_parseMathExpression(val));
                    break;
                case 5:
                    setPermittedNumDirections(CMath.s_int(val));
                    break;
                case 6:
                    this.setPermittedDirections(CMParms.parseEnumList(TechComponent.ShipDir.class, val, ',').toArray(new TechComponent.ShipDir[0]));
                    break;
                case 7: {
                    final List<String> types = CMParms.parseCommas(val.toUpperCase(), true);
                    final int[] newTypes = new int[types.size()];
                    for (int x = 0; x < types.size(); x++) {
                        final int typCode = CMParms.indexOf(CMMsg.TYPE_DESCS, types.get(x).trim());
                        if (typCode > 0)
                            newTypes[x] = typCode;
                    }
                    super.setDamageMsgTypes(newTypes);
                    break;
                }
                case 8:
                    setRechargeRate((float) CMath.s_parseMathExpression(val));
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
        final String[] MYCODES = CMProps.getStatCodesList(GenShipWeapon.MYCODES, this);
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
        if (!(E instanceof GenShipWeapon))
            return false;
        final String[] theCodes = getStatCodes();
        for (int i = 0; i < theCodes.length; i++) {
            if (!E.getStat(theCodes[i]).equals(getStat(theCodes[i])))
                return false;
        }
        return true;
    }
}