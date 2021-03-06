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

import com.syncleus.aethermud.game.Areas.interfaces.Area;
import com.syncleus.aethermud.web.interfaces.HTTPRequest;
import com.syncleus.aethermud.web.interfaces.HTTPResponse;

import java.util.Enumeration;


@SuppressWarnings("rawtypes")
public class RoomNext extends StdWebMacro {
    @Override
    public String name() {
        return "RoomNext";
    }

    @Override
    public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) {
        final java.util.Map<String, String> parms = parseParms(parm);
        final String area = httpReq.getUrlParameter("AREA");
        if (area == null)
            return " @break@";
        final Area A = MUDGrinder.getAreaObject(area);
        if (A == null)
            return " @break@";
        final String last = httpReq.getUrlParameter("ROOM");
        if (parms.containsKey("RESET")) {
            if (last != null)
                httpReq.removeUrlParameter("ROOM");
            return "";
        }
        String lastID = "";

        for (final Enumeration d = A.getProperRoomnumbers().getRoomIDs(); d.hasMoreElements(); ) {
            final String roomid = (String) d.nextElement();
            if ((last == null) || ((last.length() > 0) && (last.equals(lastID)) && (!roomid.equals(lastID)))) {
                httpReq.addFakeUrlParameter("ROOM", roomid);
                return "";
            }
            lastID = roomid;
        }
        httpReq.addFakeUrlParameter("ROOM", "");
        if (parms.containsKey("EMPTYOK"))
            return "<!--EMPTY-->";
        return " @break@";
    }
}
