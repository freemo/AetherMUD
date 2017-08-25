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
package com.planet_ink.game.WebMacros;

import com.planet_ink.game.Abilities.interfaces.Ability;
import com.planet_ink.game.Races.interfaces.Race;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.game.core.CMProps;
import com.planet_ink.game.core.CMath;
import com.planet_ink.web.interfaces.HTTPRequest;
import com.planet_ink.web.interfaces.HTTPResponse;


public class AbilityRaceNext extends StdWebMacro {
    @Override
    public String name() {
        return "AbilityRaceNext";
    }

    @Override
    public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) {
        if (!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
            return CMProps.getVar(CMProps.Str.MUDSTATUS);

        final java.util.Map<String, String> parms = parseParms(parm);
        final String last = httpReq.getUrlParameter("ABILITY");
        if (parms.containsKey("RESET")) {
            if (last != null)
                httpReq.removeUrlParameter("ABILITY");
            return "";
        }
        final String ableType = httpReq.getUrlParameter("ABILITYTYPE");
        if ((ableType != null) && (ableType.length() > 0))
            parms.put(ableType, ableType);
        final String domainType = httpReq.getUrlParameter("DOMAIN");
        if ((domainType != null) && (domainType.length() > 0))
            parms.put("DOMAIN", domainType);

        String lastID = "";
        final String raceID = httpReq.getUrlParameter("RACE");
        Race R = null;
        if ((raceID != null) && (raceID.length() > 0))
            R = CMClass.getRace(raceID);
        if (R == null) {
            if (parms.containsKey("EMPTYOK"))
                return "<!--EMPTY-->";
            return " @break@";
        }

        for (final Ability A : R.racialAbilities(null)) {
            boolean okToShow = true;
            final int level = CMLib.ableMapper().getQualifyingLevel(R.ID(), false, A.ID());
            if (level < 0)
                okToShow = false;
            else {
                final String levelName = httpReq.getUrlParameter("LEVEL");
                if ((levelName != null) && (levelName.length() > 0) && (CMath.s_int(levelName) != level))
                    okToShow = false;
            }
            if (parms.containsKey("NOT"))
                okToShow = !okToShow;
            if (okToShow) {
                if ((last == null) || ((last.length() > 0) && (last.equals(lastID)) && (!A.ID().equals(lastID)))) {
                    httpReq.addFakeUrlParameter("ABILITY", A.ID());
                    return "";
                }
                lastID = A.ID();
            }
        }
        httpReq.addFakeUrlParameter("ABILITY", "");
        if (parms.containsKey("EMPTYOK"))
            return "<!--EMPTY-->";
        return " @break@";
    }
}
