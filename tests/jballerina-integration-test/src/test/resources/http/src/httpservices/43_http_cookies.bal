// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
        path: "/cookieBackend"
   }
    resource function addSessionCookies(http:Caller caller, http:Request req) {
        // Creates cookies.
        http:Cookie cookie1 = new;
        cookie1.name = "SID001";
        cookie1.value = "239d4dmnmsddd34";
        cookie1.path = "/cookie";
        cookie1.domain = "localhost:9253";
        cookie1.httpOnly = true;
        cookie1.secure = false;

        http:Cookie cookie2 = new;
        cookie2.name = "SID002";
        cookie2.value = "178gd4dmnmsddd34";
        cookie2.path = "/cookie/cookieBackend";
        cookie2.domain = "localhost:9253";
        cookie2.httpOnly = true;
        cookie2.secure = false;

        http:Cookie cookie3 = new;
        cookie3.name = "SID003";
        cookie3.value = "895gd4dmnmsddd34";
        cookie3.path = "/cookie/cookieBackend";
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
        // Creates cookies.
        http:Cookie cookie1 = new;
        cookie1.name = "SID002";
        cookie1.value = "239d4dmnmsddd34";
        cookie1.path = "/cookie";
        cookie1.domain = "localhost:9253";
        cookie1.httpOnly = true;

        http:Cookie cookie2 = new;
        cookie2.name = "SID002";
        cookie2.value = "178gd4dmnmsddd34";
        cookie2.path = "/cookie";
        cookie2.domain = "localhost:9253";
        cookie2.httpOnly = false;

        http:Response res = new;
        http:Cookie[] reqstCookies=req.getCookies();
        // Adds cookies if there are no cookies in the inbound request, not adding otherwise.
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
        // Creates cookies.
        http:Cookie cookie1 = new;
        cookie1.name = "SID001";
        cookie1.value = "239d4dmnmsddd34";
        cookie1.path = "/cookie";
        cookie1.domain = "localhost:9253";
        cookie1.httpOnly = true;

        http:Cookie cookie2 = new;
        cookie2.name = "SID002";
        cookie2.value = "178gd4dmnmsddd34";
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
}
