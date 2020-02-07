// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;

@http:ServiceConfig {
    basePath: "/cookie"
}
service cookie on new http:Listener(9253) {

   @http:ResourceConfig {
        methods: ["GET"],
        path: "/cookieBackend_1"
   }
    resource function addPersistentAndSessionCookies(http:Caller caller, http:Request req) {
        http:Cookie cookie1 = new("SID001", "239d4dmnmsddd34");
        cookie1.path = "/cookie/cookieBackend_1";
        cookie1.domain = "localhost:9253";
        cookie1.httpOnly = true;
        cookie1.secure = false;
        cookie1.expires = "2030-06-26 05:46:22";

        http:Cookie cookie2 = new("SID002", "178gd4dmnmsddd34");
        cookie2.path = "/cookie/cookieBackend_1";
        cookie2.domain = "localhost:9253";
        cookie2.httpOnly = true;
        cookie2.secure = false;
        cookie2.expires = "2030-07-15 05:46:22";

        http:Cookie cookie3 = new("SID003", "895gd4dmnmsddd34");
        cookie3.path = "/cookie/cookieBackend_1";
        cookie3.domain = "localhost:9253";
        cookie3.httpOnly = true;
        cookie3.secure = false;

        http:Response res = new;
        http:Cookie[] reqstCookies=req.getCookies();
        // Adds cookies if there are no cookies in the inbound request.
        if (reqstCookies.length() == 0) {
            res.addCookie(cookie1);
            res.addCookie(cookie3);
            var result = caller->respond(res);
        } else if (reqstCookies.length() == 2) {
            res.addCookie(cookie2);
            var result = caller->respond(res);
        } else {
            string cookieHeader = req.getHeader("Cookie");
            res.setPayload(<@untainted> cookieHeader);
            var result = caller->respond(res);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
          path: "/cookieBackend_2"
    }
    resource function addSimilarSessionCookie(http:Caller caller, http:Request req) {
        // Creates the cookies.
        http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
        cookie1.path = "/cookie";
        cookie1.domain = "localhost:9253";
        cookie1.httpOnly = true;

        http:Cookie cookie2 = new("SID002", "178gd4dmnmsddd34");
        cookie2.path = "/cookie";
        cookie2.domain = "localhost:9253";
        cookie2.httpOnly = false;

        http:Response res = new;
        http:Cookie[] reqstCookies=req.getCookies();
        // Adds cookies only if there are no cookies in the inbound request.
        if (reqstCookies.length() == 0) {
            res.addCookie(cookie1);
            res.addCookie(cookie2);
            var result = caller->respond(res);
        } else {
            string cookieHeader = req.getHeader("Cookie");
            res.setPayload(<@untainted> cookieHeader);
            var result = caller->respond(res);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
          path: "/cookieBackend_3"
    }
    resource function removeSessionCookie(http:Caller caller, http:Request req) {
        // Creates the cookies.
        http:Cookie cookie1 = new("SID001", "239d4dmnmsddd34");
        cookie1.path = "/cookie";
        cookie1.domain = "localhost:9253";
        cookie1.httpOnly = true;

        http:Cookie cookie2 = new("SID002", "178gd4dmnmsddd34");
        cookie2.path = "/cookie/cookieBackend_3";
        cookie2.domain = "localhost:9253";
        cookie2.httpOnly = true;
        cookie2.secure = false;

        http:Response res = new;
        http:Cookie[] reqstCookies=req.getCookies();
        // Adds cookies if there are no cookies in the inbound request.
        if (reqstCookies.length() == 0) {
            res.addCookie(cookie1);
            res.addCookie(cookie2);
            var result = caller->respond(res);
        } else if (reqstCookies.length() == 2) {
            res.removeCookiesFromRemoteStore(cookie1);
            var result = caller->respond(res);
        } else {
            string cookieHeader = req.getHeader("Cookie");
            res.setPayload(<@untainted> cookieHeader);
            var result = caller->respond(res);
        }
    }

   @http:ResourceConfig {
        methods: ["GET"],
        path: "/cookieBackend_4"
   }
    resource function sendSimilarPersistentCookies(http:Caller caller, http:Request req) {
        http:Cookie cookie1 = new("SID001", "239d4dmnmsddd34");
        cookie1.path = "/cookie/cookieBackend_4";
        cookie1.domain = "localhost:9253";
        cookie1.httpOnly = false;
        cookie1.secure = false;
        cookie1.expires = "2030-06-26 05:46:22";

        http:Cookie cookie3 = new("SID001", "895gd4dmnmsddd34");
        cookie3.path = "/cookie/cookieBackend_4";
        cookie3.domain = "localhost:9253";
        cookie3.httpOnly = true;
        cookie3.secure = false;
        cookie3.expires = "2030-06-26 05:46:22";
        http:Response res = new;

        http:Cookie[] reqstCookies=req.getCookies();
        // Adds cookies if there are no cookies in the inbound request.
        if (reqstCookies.length() == 0) {
            res.addCookie(cookie1);
            res.addCookie(cookie3);
            var result = caller->respond(res);
        } else {
            string cookieHeader = req.getHeader("Cookie");
            res.setPayload(<@untainted> cookieHeader);
            var result = caller->respond(res);
        }
    }

   @http:ResourceConfig {
        methods: ["GET"],
        path: "/cookieBackend_5"
   }
    resource function sendSimilarPersistentAndSessionCookies_1(http:Caller caller, http:Request req) {
        http:Cookie cookie2 = new("SID003", "895gd4dmnmsddd34");
        cookie2.path = "/cookie/cookieBackend_5";
        cookie2.domain = "localhost:9253";
        cookie2.httpOnly = true;
        cookie2.secure = false;

        http:Cookie cookie3 = new("SID003", "aeaa895gd4dmnmsddd34");
        cookie3.path = "/cookie/cookieBackend_5";
        cookie3.domain = "localhost:9253";
        cookie3.httpOnly = false;
        cookie3.secure = false;
        cookie3.expires = "2030-07-15 05:46:22";

        http:Response res = new;
        http:Cookie[] reqstCookies=req.getCookies();
        // Adds cookies if there are no cookies in the inbound request.
        if (reqstCookies.length() == 0) {
            res.addCookie(cookie2); // Adds a session cookie.
            res.addCookie(cookie3); // Adds a similar persistent cookie.
            var result = caller->respond(res);
        } else {
            string cookieHeader = req.getHeader("Cookie");
            res.setPayload(<@untainted> cookieHeader);
            var result = caller->respond(res);
        }
    }

   @http:ResourceConfig {
        methods: ["GET"],
        path: "/cookieBackend_6"
   }
    resource function sendSimilarPersistentAndSessionCookies_2(http:Caller caller, http:Request req) {
        http:Cookie cookie2 = new("SID003", "aeaa895gd4dmnmsddd34");
        cookie2.path = "/cookie/cookieBackend_6";
        cookie2.domain = "localhost:9253";
        cookie2.httpOnly = false;
        cookie2.secure = false;
        cookie2.expires = "2030-07-15 05:46:22";

        http:Cookie cookie3 = new("SID003", "895gd4dmnmsddd34");
        cookie3.path = "/cookie/cookieBackend_6";
        cookie3.domain = "localhost:9253";
        cookie3.httpOnly = true;
        cookie3.secure = false;

        http:Response res = new;
        http:Cookie[] reqstCookies=req.getCookies();
        // Adds cookies if there are no cookies in the inbound request.
        if (reqstCookies.length() == 0) {
            res.addCookie(cookie2); // Adds a persistent cookie.
            res.addCookie(cookie3); // Adds a similar session cookie.
            var result = caller->respond(res);
        } else {
            string cookieHeader = req.getHeader("Cookie");
            res.setPayload(<@untainted> cookieHeader);
            var result = caller->respond(res);
        }
    }

   @http:ResourceConfig {
        methods: ["GET"],
        path: "/cookieBackend_7"
   }
    resource function removePersistentCookieByServer(http:Caller caller, http:Request req) {
        // Creates the cookies.
        http:Cookie cookie1 = new("SID001", "239d4dmnmsddd34");
        cookie1.path = "/cookie/cookieBackend_7";
        cookie1.domain = "localhost:9253";
        cookie1.httpOnly = true;
        cookie1.expires = "2030-07-15 05:46:22";

        http:Cookie cookie2 = new("SID002", "178gd4dmnmsddd34");
        cookie2.path = "/cookie/cookieBackend_7";
        cookie2.domain = "localhost:9253";
        cookie2.httpOnly = true;
        cookie2.secure = false;

        http:Response res = new;
        http:Cookie[] reqstCookies=req.getCookies();
        // Adds cookies if there are no cookies in the inbound request.
        if (reqstCookies.length() == 0) {
            res.addCookie(cookie1);
            res.addCookie(cookie2);
            var result = caller->respond(res);
        } else if (reqstCookies.length() == 2) {
            res.removeCookiesFromRemoteStore(cookie1);
            var result = caller->respond(res);
        } else {
            string cookieHeader = req.getHeader("Cookie");
            res.setPayload(<@untainted> cookieHeader);
            var result = caller->respond(res);
        }
    }
}
