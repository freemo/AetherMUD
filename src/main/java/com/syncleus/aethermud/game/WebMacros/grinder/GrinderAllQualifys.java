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
package com.syncleus.aethermud.game.WebMacros.grinder;

import com.syncleus.aethermud.game.Libraries.interfaces.AbilityMapper;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMParms;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.web.interfaces.HTTPRequest;

import java.util.Map;


public class GrinderAllQualifys {
    public String name() {
        return "GrinderAllQualifys";
    }

    public String editAllQualify(HTTPRequest httpReq, java.util.Map<String, String> parms) {
        final String last = httpReq.getUrlParameter("ALLQUALID");
        if ((last == null) || (last.length() == 0))
            return " @break@";
        String which = httpReq.getUrlParameter("ALLQUALWHICH");
        if (parms.containsKey("WHICH"))
            which = parms.get("WHICH");
        if ((which == null) || (which.length() == 0))
            return " @break@";
        final Map<String, Map<String, AbilityMapper.AbilityMapping>> allQualMap = CMLib.ableMapper().getAllQualifiesMap(httpReq.getRequestObjects());
        final Map<String, AbilityMapper.AbilityMapping> map = allQualMap.get(which.toUpperCase().trim());
        if (map == null)
            return " @break@";

        AbilityMapper.AbilityMapping newMap = map.get(last.toUpperCase().trim());
        if (newMap == null) {
            newMap = CMLib.ableMapper().newAbilityMapping().ID(last.toUpperCase().trim());
            newMap.abilityID(last);
            newMap.allQualifyFlag(true);
        }
        String s;
        s = httpReq.getUrlParameter("LEVEL");
        if (s != null)
            newMap.qualLevel(CMath.s_int(s));
        s = httpReq.getUrlParameter("PROF");
        if (s != null)
            newMap.defaultProficiency(CMath.s_int(s));
        s = httpReq.getUrlParameter("MASK");
        if (s != null)
            newMap.extraMask(s);
        s = httpReq.getUrlParameter("AUTOGAIN");
        if (s != null)
            newMap.autoGain(s.equalsIgnoreCase("on"));
        final StringBuilder preReqs = new StringBuilder("");
        int curChkNum = 1;
        while (httpReq.isUrlParameter("REQABLE" + curChkNum)) {
            final String curVal = httpReq.getUrlParameter("REQABLE" + curChkNum);
            if (curVal.equals("DEL") || curVal.equals("DELETE") || curVal.trim().length() == 0) {
                // do nothing
            } else {
                final String curLvl = httpReq.getUrlParameter("REQLEVEL" + curChkNum);
                preReqs.append(curVal);
                if ((curLvl != null) && (curLvl.trim().length() > 0) && (CMath.s_int(curLvl.trim()) > 0))
                    preReqs.append("(").append(curLvl).append(")");
                preReqs.append(" ");
            }
            curChkNum++;
        }
        newMap = CMLib.ableMapper().makeAbilityMapping(newMap.abilityID(), newMap.qualLevel(), newMap.abilityID(), newMap.defaultProficiency(), 100, "", newMap.autoGain(), false,
            true, CMParms.parseSpaces(preReqs.toString().trim(), true), newMap.extraMask(), null);
        map.put(last.toUpperCase().trim(), newMap);
        CMLib.ableMapper().saveAllQualifysFile(allQualMap);
        return "";
    }
}
