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

import com.syncleus.aethermud.game.Abilities.interfaces.Ability;
import com.syncleus.aethermud.game.CharClasses.interfaces.CharClass;
import com.syncleus.aethermud.game.core.CMClass;
import com.syncleus.aethermud.game.core.CMLib;
import com.syncleus.aethermud.game.core.CMProps;
import com.syncleus.aethermud.web.interfaces.HTTPRequest;
import com.syncleus.aethermud.web.interfaces.HTTPResponse;

import java.util.Enumeration;
import java.util.Vector;


public class CrossClassAbilities extends StdWebMacro {
    @Override
    public String name() {
        return "CrossClassAbilities";
    }

    @Override
    public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) {
        final Vector<StringBuffer> rowsFavoring = new Vector<StringBuffer>();
        final Vector<StringBuffer> allOtherRows = new Vector<StringBuffer>();
        final String sort = httpReq.getUrlParameter("SORTBY");
        int sortByClassNum = -1;
        if ((sort != null) && (sort.length() > 0)) {
            int cnum = 0;
            for (final Enumeration<CharClass> c = CMClass.charClasses(); c.hasMoreElements(); ) {
                final CharClass C = c.nextElement();
                if ((C.ID().equals(sort)) && (CMProps.isTheme(C.availabilityCode())))
                    sortByClassNum = cnum;
                cnum++;
            }
        }
        for (final Enumeration<Ability> a = CMClass.abilities(); a.hasMoreElements(); ) {
            final Ability A = a.nextElement();
            final StringBuffer buf = new StringBuffer("");
            int numFound = 0;
            for (final Enumeration<CharClass> c = CMClass.charClasses(); c.hasMoreElements(); ) {
                final CharClass C = c.nextElement();
                if (CMProps.isTheme(C.availabilityCode())
                    && (CMLib.ableMapper().getQualifyingLevel(C.ID(), true, A.ID()) >= 0)) {
                    if ((++numFound) > 0)
                        break;
                }
            }
            if (numFound > 0) {
                buf.append("<TR><TD><B>" + A.name() + "</B></TD>");
                int cnum = 0;
                for (final Enumeration<CharClass> c = CMClass.charClasses(); c.hasMoreElements(); ) {
                    final CharClass C = c.nextElement();
                    if (CMProps.isTheme(C.availabilityCode())) {
                        final int qual = CMLib.ableMapper().getQualifyingLevel(C.ID(), true, A.ID());
                        if ((qual >= 0) && (!CMLib.ableMapper().getSecretSkill(C.ID(), false, A.ID()))) {
                            buf.append("<TD>" + qual + "</TD>");
                            if ((cnum == sortByClassNum) && (!rowsFavoring.contains(buf)))
                                rowsFavoring.addElement(buf);
                        } else
                            buf.append("<TD><BR></TD>");
                    }
                    cnum++;
                }
                if (!rowsFavoring.contains(buf))
                    allOtherRows.addElement(buf);
                buf.append("</TR>");
            }
        }
        final StringBuffer buf = new StringBuffer("");
        for (int i = 0; i < rowsFavoring.size(); i++)
            buf.append(rowsFavoring.elementAt(i));
        for (int i = 0; i < allOtherRows.size(); i++)
            buf.append(allOtherRows.elementAt(i));
        return clearWebMacros(buf);
    }

}
