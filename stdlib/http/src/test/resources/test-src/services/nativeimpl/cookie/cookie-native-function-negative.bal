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
import ballerina/file;
import ballerina/io;

string filePath = "src/test/resources/test-src/services/nativeimpl/cookie/cookie-test-data/";

function testAddCookieWithUnmatchedDomain() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "foo.example.com";
    http:Client cookieClientEndpoint = new("http://bar.example.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://bar.example.com", "/sample");
        if (result is error) {
            io:println(result);
        }
    }
    // Gets all the cookies.
    return cookieStore.getAllCookies();
}

function testAddCookieWithUnmatchedPath() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/mail/inbox";
    cookie1.domain = "example.com";
    http:Client cookieClientEndpoint = new("http://example.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://example.com", "/mail");
        if (result is error) {
            io:println(result);
        }
    }
    return cookieStore.getAllCookies();
}

function testAddSimilarCookie() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    cookie1.httpOnly = true;
    http:Cookie cookie2 = new("SID002", "6789mnmsddd34");
    cookie2.path = "/sample";
    cookie2.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
        if (result is error) {
            io:println(result);
        }
        result = cookieStore.addCookie(cookie2, cookieConfigVal, "google.com", "/sample");
        if (result is error) {
            io:println(result);
        }
    }
    return cookieStore.getAllCookies();
}

function testAddHttpOnlyCookie() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    cookie1.httpOnly = true;
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "google.com", "/sample");
        if (result is error) {
            io:println(result);
        }
    }
    return cookieStore.getAllCookies();
}

function testGetSecureCookieFromCookieStore() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    cookie1.secure = true;
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getCookies("http://google.com", "/sample");
}

function testGetHttpOnlyCookieFromCookieStore() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    cookie1.httpOnly = true;
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getCookies("google.com", "/sample");
}

function testGetCookieToUnmatchedDomain1() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getCookies("http://mail.google.com", "/sample");
}

function testGetCookieToUnmatchedDomain2() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "foo.google.com";
    http:Client cookieClientEndpoint = new("http://foo.google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://foo.google.com", "/sample");
    }
    return cookieStore.getCookies("http://google.com", "/sample");
}

function testGetCookieToUnmatchedPath1() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/mail/inbox";
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/mail/inbox");
    }
    return cookieStore.getCookies("http://google.com", "/mail");
}

function testGetCookieToUnmatchedPath2() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/mail");
    }
    return cookieStore.getCookies("http://google.com", "/sample");
}

function testRemoveCookieFromCookieStore() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample" );
    }
    var removeResult = cookieStore.removeCookie("SID003", "google.com", "/sample");
    if (removeResult is error) {
        io:println(removeResult);
    }
    return cookieStore.getAllCookies();
}

function testCheckMaxTotalCookieCount() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID001", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Cookie cookie2 = new("SID002", "jka6mnmsddd34");
    cookie2.path = "/sample";
    cookie2.domain = "google.com";
    http:Cookie cookie3 = new("SID003", "kafh34dmnmsddd34");
    cookie3.path = "/sample";
    cookie3.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true, maxTotalCookieCount:2 } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
        result = cookieStore.addCookie(cookie2, cookieConfigVal, "http://google.com", "/sample");
        result = cookieStore.addCookie(cookie3, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getAllCookies();
}

function testCheckMaxCookiesPerDomain() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID001", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Cookie cookie2 = new("SID002", "jka6mnmsddd34");
    cookie2.path = "/sample";
    cookie2.domain = "google.com";
    http:Cookie cookie3 = new("SID003", "kafh34dmnmsddd34");
    cookie3.path = "/sample";
    cookie3.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true, maxCookiesPerDomain:2 } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
        result = cookieStore.addCookie(cookie2, cookieConfigVal, "http://google.com", "/sample");
        result = cookieStore.addCookie(cookie3, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getAllCookies();
}

function testAddPersistentCookieWithoutPersistentStore() returns @tainted http:Cookie[] {
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    cookie1.expires = "2030-07-15 05:46:22";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    http:CookieStore? cookieStore = cookieClientEndpoint.getCookieStore();
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    http:Cookie[] cookies = [];
    if (cookieConfigVal is http:CookieConfig && cookieStore is http:CookieStore && cookie1.isValid() == true) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
        if (result is error) {
            io:println(result);
        }
        cookies = cookieStore.getAllCookies();
    }
    return cookies;
}

function testRemovePersistentCookieFromCookieStore_1() returns @tainted http:Cookie[] {
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    cookie1.expires = "2030-07-15 05:46:22";
    http:CsvPersistentCookieHandler myPersistentStore = new(filePath + "client-6.csv");
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true, persistentCookieHandler: myPersistentStore } } );
    http:CookieStore? cookieStore = cookieClientEndpoint.getCookieStore();
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    http:Cookie[] cookies = [];
    if (cookieConfigVal is http:CookieConfig && cookieStore is http:CookieStore && cookie1.isValid() == true) {
        var result = cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
        result = cookieStore.removeCookie("SID003", "google.com", "/sample");
        if (result is error) {
            io:println(result);
        }
        cookies = cookieStore.getAllCookies();
    }
    error? removeResults = file:remove(filePath, true);
    return cookies;
}

function testRemovePersistentCookieFromCookieStore_2() returns @tainted http:Cookie[] {
    http:CsvPersistentCookieHandler myPersistentStore = new(filePath + "client-7.csv");
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true, persistentCookieHandler: myPersistentStore } } );
    http:CookieStore? cookieStore = cookieClientEndpoint.getCookieStore();
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    http:Cookie[] cookies = [];
    if (cookieConfigVal is http:CookieConfig && cookieStore is http:CookieStore) {
        var result = cookieStore.removeCookie("SID003", "google.com", "/sample");
        if (result is error) {
            io:println(result);
        }
        cookies = cookieStore.getAllCookies();
    }
    return cookies;
}

function testRemoveAllCookiesFromCookieStore() returns @tainted http:Cookie[] {
    http:CsvPersistentCookieHandler myPersistentStore = new(filePath + "client-8.csv");
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true, persistentCookieHandler: myPersistentStore } } );
    http:CookieStore? cookieStore = cookieClientEndpoint.getCookieStore();
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    http:Cookie[] cookies = [];
    if (cookieConfigVal is http:CookieConfig && cookieStore is http:CookieStore) {
        var result = cookieStore.removeAllCookies();
        if (result is error) {
            io:println(result);
        }
        cookies = cookieStore.getAllCookies();
    }
    return cookies;
}
