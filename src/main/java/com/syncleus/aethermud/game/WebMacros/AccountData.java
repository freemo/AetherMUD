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
package com.syncleus.aethermud.game.WebMacros;

import com.syncleus.aethermud.game.Common.interfaces.PlayerAccount;
import com.syncleus.aethermud.game.Common.interfaces.Tattoo;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMStrings;
import com.syncleus.aethermud.web.interfaces.HTTPRequest;
import com.syncleus.aethermud.web.interfaces.HTTPResponse;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


public class AccountData extends StdWebMacro {
    @Override
    public String name() {
        return "AccountData";
    }

    @Override
    public boolean isAdminMacro() {
        return true;
    }

    @Override
    public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) {
        final java.util.Map<String, String> parms = parseParms(parm);
        final String last = httpReq.getUrlParameter("ACCOUNT");
        if (last == null)
            return "";
        if (last.length() > 0) {
            final PlayerAccount A = CMLib.players().getLoadAccount(last);
            if (A == null)
                return "";
            if (parms.containsKey("NAME") || parms.containsKey("ACCOUNT"))
                return clearWebMacros(A.getAccountName());
            if (parms.containsKey("CLASS"))
                return clearWebMacros(A.ID());
            if (parms.containsKey("LASTIP"))
                return "" + A.getLastIP();
            if (parms.containsKey("LASTDATETIME"))
                return "" + CMLib.time().date2String(A.getLastDateTime());
            if (parms.containsKey("EMAIL"))
                return "" + A.getEmail();
            if (parms.containsKey("BONUSLANGS"))
                return "" + A.getBonusLanguageLimits();
            if (parms.containsKey("BONUSCHARLIMIT"))
                return "" + A.getBonusCharsLimit();
            if (parms.containsKey("BONUSCHARONLINE"))
                return "" + A.getBonusCharsOnlineLimit();
            if (parms.containsKey("BONUSALLCOMMONSKILLS"))
                return "" + A.getBonusCommonSkillLimits();
            if (parms.containsKey("BONUSCRAFTINGSKILLS"))
                return "" + A.getBonusCraftingSkillLimits();
            if (parms.containsKey("BONUSNONCRAFTINGSKILLS"))
                return "" + A.getBonusNonCraftingSkillLimits();
            if (parms.containsKey("NOTES"))
                return "" + A.getNotes();
            if (parms.containsKey("TATTOOS")) {
                StringBuilder str = new StringBuilder("");
                for (final Enumeration<Tattoo> e = A.tattoos(); e.hasMoreElements(); )
                    str.append(e.nextElement().toString() + ", ");
                return str.toString();
            }
            if (parms.containsKey("ACCTEXPIRATION")) {
                if (A.isSet(PlayerAccount.AccountFlag.NOEXPIRE))
                    return "Never";
                return "" + CMLib.time().date2String(A.getAccountExpiration());
            }
            for (final PlayerAccount.AccountFlag flag : PlayerAccount.AccountFlag.values()) {
                if (parms.containsKey("IS" + flag.name()))
                    return "" + A.isSet(flag);
            }
            if (parms.containsKey("FLAGS")) {
                final String old = httpReq.getUrlParameter("FLAGS");
                List<String> set = null;
                if (old == null) {
                    final String matList = A.getStat("FLAG");
                    set = CMParms.parseCommas(matList, true);
                } else {
                    String id = "";
                    set = new Vector<String>();
                    for (int i = 0; httpReq.isUrlParameter("FLAG" + id); id = "" + (++i))
                        set.add(httpReq.getUrlParameter("FLAG" + id));
                }
                final StringBuffer str = new StringBuffer("");
                for (final PlayerAccount.AccountFlag element : PlayerAccount.AccountFlag.values()) {
                    str.append("<OPTION VALUE=\"" + element + "\"");
                    if (set.contains(element))
                        str.append(" SELECTED");
                    str.append(">" + CMStrings.capitalizeAndLower(element.name()));
                }
                str.append(", ");
            }
            if (parms.containsKey("IGNORE"))
                return "" + CMParms.toListString(A.getIgnored());
        }
        return "";
    }
}
