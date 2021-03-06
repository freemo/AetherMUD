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

import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.web.interfaces.HTTPRequest;
import com.syncleus.aethermud.web.interfaces.HTTPResponse;


public class MudInfo extends StdWebMacro {
    @Override
    public String name() {
        return "MudInfo";
    }

    @Override
    public boolean isAdminMacro() {
        return false;
    }

    @Override
    public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) {
        final java.util.Map<String, String> parms = parseParms(parm);
        if (parms.containsKey("DOMAIN"))
            return CMProps.getVar(CMProps.Str.MUDDOMAIN);
        if (parms.containsKey("EMAILOK"))
            return "" + (CMProps.getVar(CMProps.Str.MAILBOX).length() > 0);
        if (parms.containsKey("MAILBOX"))
            return CMProps.getVar(CMProps.Str.MAILBOX);
        if (parms.containsKey("NAME"))
            return CMProps.getVar(CMProps.Str.MUDNAME);
        if (parms.containsKey("CHARSET"))
            return CMProps.getVar(CMProps.Str.CHARSETOUTPUT);
        if (parms.containsKey("PORT")) {
            String ports = CMProps.getVar(CMProps.Str.MUDPORTS);
            if (ports == null)
                return "Booting";
            ports = ports.trim();
            final int x = ports.indexOf(' ');
            if (x < 0)
                return clearWebMacros(ports);
            return clearWebMacros(ports.substring(0, x));
        }
        return "";
    }
}
