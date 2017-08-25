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

import com.planet_ink.game.Races.interfaces.Race;
import com.planet_ink.game.core.CMClass;
import com.planet_ink.web.interfaces.HTTPRequest;
import com.planet_ink.web.interfaces.HTTPResponse;


public class RaceName extends StdWebMacro {
    @Override
    public String name() {
        return "RaceName";
    }

    @Override
    public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) {
        final String last = httpReq.getUrlParameter("RACE");
        if (last == null)
            return " @break@";
        if (last.length() > 0) {
            final Race R = CMClass.getRace(last);
            if (R != null)
                return clearWebMacros(R.name());
        }
        return "";
    }
}
