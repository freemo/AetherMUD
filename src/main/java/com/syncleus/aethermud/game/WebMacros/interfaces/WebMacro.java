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
package com.syncleus.aethermud.game.WebMacros.interfaces;

import com.syncleus.aethermud.game.core.exceptions.HTTPServerException;
import com.syncleus.aethermud.game.core.interfaces.CMObject;
import com.syncleus.aethermud.web.interfaces.HTTPRequest;
import com.syncleus.aethermud.web.interfaces.HTTPResponse;

/*
   Copyright 2002-2017 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/**
 * Web Macros are special commands which can be inserted into aethermud web page
 * (cmvp) files, and can have those command strings substituted with calculated
 * results. They can include parameters, and can access the other URL
 * parameters.
 */
public interface WebMacro extends CMObject {
    /**
     * The public name of this macro
     *
     * @return The public name of this macro
     */
    @Override
    public String name();

    /**
     * Whether the runMacro or runBinaryMacro executor should be called.
     *
     * @see WebMacro#runBinaryMacro(HTTPRequest, String, HTTPResponse)
     * @see WebMacro#runMacro(HTTPRequest, String, HTTPResponse)
     * @return whether the runBinaryMacro executor should be called instead of
     *         runMacro
     */
    public boolean preferBinary();

    /**
     * Whether this macro is restricted to the admin web server.
     *
     * @return true if the macro is restricted to the admin web server
     */
    public boolean isAdminMacro();

    /**
     * Whether this macro substitutes as an aspect of the web path instead of a
     * standard web macro. If true is returned, URLs such as:
     * http://mydomain.com/mymacroname?firstparm=value&amp;secondparm=value
     * might succeeed
     *
     * @return whether this is a wierd URL macro
     */
    public boolean isAWebPath();

    /**
     * This method is executed only if this macro returns true for
     * preferBinary(). It will execute the macro and return its results as a
     * binary byte array.
     *
     * The response object is used to set cookies and headers only.  Any
     * response body is in the return object.  Since 99% of macros are only
     * filling in an existing page, nothing will be done with that anyway,
     * and is only important when isAWebPath return true.
     *
     * @see WebMacro#preferBinary()
     * @see com.syncleus.aethermud.web.interfaces.HTTPRequest
     * @param httpReq the external requests object
     * @param parm any parameter strings given to the macro
     * @param httpResp the response, with headers
     * @return the binary stream result of running this macro
     * @throws HTTPServerException a http error to pass to the user
     */
    public byte[] runBinaryMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) throws HTTPServerException;

    /**
     * This method is executed only if this macro returns false for
     * preferBinary(). It will execute the macro and return its results as a
     * string, which is then substituted for the macro reference in the web page
     * where the macro was found.
     *
     * The response object is used to set cookies and headers only.  Any
     * response body is in the return object.  Since 99% of macros are only
     * filling in an existing page, nothing will be done with that anyway,
     * and is only important when isAWebPath return true.
     *
     * @see WebMacro#preferBinary()
     * @see com.syncleus.aethermud.web.interfaces.HTTPRequest
     * @param httpReq the external requests object
     * @param parm any parameter strings given to the macro
     * @param httpResp the response, with headers
     * @return the string result of running this macro
     * @throws HTTPServerException a http error to pass to the user
     */
    public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp) throws HTTPServerException;
}
