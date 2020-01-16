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

// Tests for session cookies.
function testAddCookieToCookieStore1() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Client cookieclientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieclientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    // Gets all the cookies.
    return cookieStore.getAllCookies();
}

function testAddCookieToCookieStore2() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://mail.google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://mail.google.com", "/sample");
    }
    return cookieStore.getAllCookies();
}

function testAddCookieToCookieStore3() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getAllCookies();
}

function testAddCookieToCookieStore4() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://mail.google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getAllCookies();
}

function testAddCookieToCookieStore5() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.domain = "google.com";
    cookie1.path = "/mail";
    http:Client cookieClientEndpoint = new("http://mail.google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
       cookieStore.addCookie(cookie1, cookieConfigVal, "http://mail.google.com", "/mail/inbox");
    }
    return cookieStore.getAllCookies();
}

function testAddThirdPartyCookieToCookieStore() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.domain = "ad.doubleclick.net";
    cookie1.path = "/home";
    http:Client cookieClientEndpoint = new("http://mail.google.com", { cookieConfig: { enabled: true, blockThirdPartyCookies:false } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
       cookieStore.addCookie(cookie1, cookieConfigVal, "http://mail.google.com", "/mail/inbox");
    }
    return cookieStore.getAllCookies();
}

function testAddCookiesToCookieStore() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID001", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Cookie cookie2 = new("SID002", "239d4dmnmsddd34");
    cookie2.path = "/sample";
    cookie2.domain = "google.com";
    http:Cookie[] cookiesToadd =[cookie1, cookie2];
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookies(cookiesToadd, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getAllCookies();
}

function testAddSimilarCookieToCookieStore() returns @tainted http:Cookie[] {
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
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
        cookieStore.addCookie(cookie2, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getAllCookies();
}

function testAddCookiesConcurrentlyToCookieStore() returns @tainted http:Cookie[] {
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Cookie[] cookiesToadd = [cookie1];
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    http:CookieStore? cookieStore = cookieClientEndpoint.getCookieStore();
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    worker w1 {
        if (cookieConfigVal is http:CookieConfig && cookieStore is http:CookieStore) {
            cookieStore.addCookies(cookiesToadd, cookieConfigVal, "http://google.com", "/sample");
        }
    }
    worker w2 {
        if (cookieConfigVal is http:CookieConfig && cookieStore is http:CookieStore) {
            cookieStore.addCookies(cookiesToadd, cookieConfigVal, "http://google.com", "/sample");
        }
    }
    worker w3 {
        if (cookieConfigVal is http:CookieConfig && cookieStore is http:CookieStore) {
            cookieStore.addCookies(cookiesToadd, cookieConfigVal, "http://google.com", "/sample");
        }
    }
    worker w4 {
        if (cookieConfigVal is http:CookieConfig && cookieStore is http:CookieStore) {
            cookieStore.addCookies(cookiesToadd, cookieConfigVal, "http://google.com", "/sample");
        }
    }
    _ = wait {w1, w2, w3, w4};
    http:Cookie[] cookies = [];
    if (cookieStore is http:CookieStore) {
        cookies = cookieStore.getAllCookies();
    }
    return cookies;
}

function testGetCookiesFromCookieStore1() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    // Adds cookie.
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    // Gets the relevant cookie from the cookie store.
    return cookieStore.getCookies("http://google.com", "/sample");
}

function testGetCookiesFromCookieStore2() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getCookies("http://mail.google.com", "/sample");
}

function testGetCookiesFromCookieStore3() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getCookies("http://google.com", "/sample");
}

function testGetCookiesFromCookieStore4() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/mail";
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/mail");
    }
    return cookieStore.getCookies("http://google.com", "/mail/inbox");
}

function testGetCookiesFromCookieStore5() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getCookies("http://google.com", "/sample");
}

function testGetCookiesFromCookieStore6() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID001", "7Av239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    cookie1.secure = true;
    http:Cookie cookie2 = new("SID002", "239d4dmnmsddd34");
    cookie2.path = "/sample";
    cookie2.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
        cookieStore.addCookie(cookie2, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getCookies("http://google.com", "/sample");
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
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    return cookieStore.getCookies("https://google.com", "/sample");
}

function testRemoveCookieFromCookieStore() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookie(cookie1, cookieConfigVal, "http://google.com", "/sample");
    }
    boolean isRemoved = cookieStore.removeCookie("SID002", "google.com", "/sample");
    return cookieStore.getAllCookies();
}

function testClearAllCookiesInCookieStore() returns @tainted http:Cookie[] {
    http:CookieStore cookieStore = new;
    http:Cookie cookie1 = new("SID002", "239d4dmnmsddd34");
    cookie1.path = "/sample";
    cookie1.domain = "google.com";
    http:Cookie cookie2 = new("SID003", "239d4dmnmsddd34");
    cookie2.path = "/sample";
    cookie2.domain = "google.com";
    http:Cookie[] cookiesToadd =[cookie1, cookie2];
    http:Client cookieClientEndpoint = new("http://google.com", { cookieConfig: { enabled: true } } );
    var cookieConfigVal = cookieClientEndpoint.config.cookieConfig;
    if (cookieConfigVal is http:CookieConfig) {
        cookieStore.addCookies(cookiesToadd, cookieConfigVal, "http://google.com", "/sample");
    }
    cookieStore.clear();
    return cookieStore.getAllCookies();
}
