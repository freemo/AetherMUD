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
package com.syncleus.aethermud.web.servlets;

import com.syncleus.aethermud.web.http.HTTPMethod;
import com.syncleus.aethermud.web.http.HTTPStatus;
import com.syncleus.aethermud.web.http.MIMEType;
import com.syncleus.aethermud.web.interfaces.SimpleServlet;
import com.syncleus.aethermud.web.interfaces.SimpleServletRequest;
import com.syncleus.aethermud.web.interfaces.SimpleServletResponse;
import com.syncleus.aethermud.web.interfaces.SimpleServletSession;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


/**
 * Returns information about your servlet session in a page
 * @author Bo Zimmerman
 *
 */
public class SessionInfoServlet implements SimpleServlet {

    @Override
    public void doGet(SimpleServletRequest request, SimpleServletResponse response) {
        try {
            response.setMimeType(MIMEType.All.html.getType());
            final SimpleServletSession session = request.getSession();
            response.getOutputStream().write("<html><body>".getBytes());
            response.getOutputStream().write(("<h1>Hello Session#" + session.getSessionId() + "</h1>").getBytes());
            final String lastTouch = DateFormat.getDateTimeInstance().format(new Date(session.getSessionLastTouchTime()));
            response.getOutputStream().write(("Last request was at: " + lastTouch + "<br>").getBytes());
            final String firstTouch = DateFormat.getDateTimeInstance().format(session.getSessionStart());
            response.getOutputStream().write(("First request was at: " + firstTouch + "<br>").getBytes());
            if (session.getUser().length() == 0)
                session.setUser("BOB the " + this.hashCode());
            response.getOutputStream().write(("Your user name is: " + session.getUser() + "<br>").getBytes());
            response.getOutputStream().write("</body></html>".getBytes());
        } catch (final IOException e) {
            response.setStatusCode(500);
        }
    }

    @Override
    public void doPost(SimpleServletRequest request, SimpleServletResponse response) {
        response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED.getStatusCode());
    }

    @Override
    public void init() {
    }

    @Override
    public void service(HTTPMethod method, SimpleServletRequest request, SimpleServletResponse response) {
        if (method != HTTPMethod.GET)
            response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED.getStatusCode());
    }

}
