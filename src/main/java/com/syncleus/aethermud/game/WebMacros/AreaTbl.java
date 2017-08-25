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
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.game.core.CMath;
import com.syncleus.aethermud.web.interfaces.HTTPRequest;
import com.syncleus.aethermud.web.interfaces.HTTPResponse;

import java.util.Enumeration;
import java.util.Vector;


public class AreaTbl extends StdWebMacro {
    protected static final int AT_MAX_COL = 3;

    @Override
    public String name() {
        return "AreaTbl";
    }

    @Override
    public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) {
        // have to check, otherwise we'll be stuffing a blank string into resources
        if (!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)) {
            return "<TR><TD colspan=\"" + AT_MAX_COL + "\" class=\"cmAreaTblEntry\"><I>Game is not running - unable to get area list!</I></TD></TR>";
        }

        final Vector<String> areasVec = new Vector<String>();

        for (final Enumeration<Area> a = CMLib.map().areas(); a.hasMoreElements(); ) {
            final Area A = a.nextElement();
            if ((!CMLib.flags().isHidden(A)) && (!CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD)))
                areasVec.addElement(A.name());
        }
        final StringBuffer msg = new StringBuffer("\n\r");
        int col = 0;
        int percent = 100 / AT_MAX_COL;
        for (int i = 0; i < areasVec.size(); i++) {
            if (col == 0) {
                msg.append("<tr>");
                // the bottom elements can be full width if there's
                //  not enough to fill one row
                // ie.   -X- -X- -X-
                //  	 -X- -X- -X-
                //  	 -----X-----
                //  	 -----X-----
                if (i > areasVec.size() - AT_MAX_COL)
                    percent = 100;
            }

            msg.append("<td");

            if (percent == 100)
                msg.append(" colspan=\"" + AT_MAX_COL + "\"");    //last element is width of remainder
            else
                msg.append(" width=\"" + percent + "%\"");

            msg.append(L(" class=\"cmAreaTblEntry\">"));
            msg.append(areasVec.elementAt(i));
            msg.append("</td>");
            // finish the row
            if ((percent == 100) || (++col) > (AT_MAX_COL - 1)) {
                msg.append("</tr>\n\r");
                col = 0;
            }
        }
        if (!msg.toString().endsWith("</tr>\n\r"))
            msg.append("</tr>\n\r");
        return clearWebMacros(msg);
    }

}
