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
package com.syncleus.aethermud.web.http;


/**
 * Public enum type for all acceptable request types.
 * Most of these types are arbitrary, as only GET and HEAD
 * are directly supported by the web file server, and servlets
 * can do what they want.  In other words, expand this list at will.
 * @author Bo Zimmerman
 */
public enum HTTPMethod {
    GET, HEAD, POST, PUT, DELETE, OPTIONS;

    public static String getAllowedList() {
        return "GET, HEAD, POST, PUT, DELETE, OPTIONS";
    }
}
