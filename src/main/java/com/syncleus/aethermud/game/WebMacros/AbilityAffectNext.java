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
import com.planet_ink.game.core.CMClass;
import com.planet_ink.game.core.CMLib;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.interfaces.HTTPResponse;

import java.util.Enumeration;


public class AbilityAffectNext extends StdWebMacro {
    @Override
    public String name() {
        return "AbilityAffectNext";
    }

    @Override
    public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) {
        final java.util.Map<String, String> parms = parseParms(parm);
        final String last = httpReq.getUrlParameter("ABILITY");
        if (parms.containsKey("RESET")) {
            if (last != null)
                httpReq.removeUrlParameter("ABILITY");
            return "";
        }
        String lastID = "";
        final String ableType = httpReq.getUrlParameter("ABILITYTYPE");
        if ((ableType != null) && (ableType.length() > 0))
            parms.put(ableType, ableType);
        for (final Enumeration<Ability> a = CMClass.abilities(); a.hasMoreElements(); ) {
            final Ability A = a.nextElement();
            boolean okToShow = true;
            final int classType = A.classificationCode() & Ability.ALL_ACODES;
            if (CMLib.ableMapper().getQualifyingLevel("Archon", true, A.ID()) >= 0)
                continue;
            boolean containsOne = false;
            for (final String element : Ability.ACODE_DESCS) {
                if (parms.containsKey(element)) {
                    containsOne = true;
                    break;
                }
            }
            if (containsOne && (!parms.containsKey(Ability.ACODE_DESCS[classType])))
                okToShow = false;
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