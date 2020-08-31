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

import ballerina/log;
import ballerina/time;

# Represents the cookie store.
#
# + allSessionCookies - Array to store all the session cookies
# + persistentCookieHandler - Persistent cookie handler to manage persistent cookies
public class CookieStore {

    Cookie[] allSessionCookies = [];
    PersistentCookieHandler? persistentCookieHandler;

    public function init(PersistentCookieHandler? persistentCookieHandler = ()) {
        self.persistentCookieHandler = persistentCookieHandler;
    }

    # Adds a cookie to the cookie store according to the rules in [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.3).
    #
    # + cookie - Cookie to be added
    # + cookieConfig - Configurations associated with the cookies
    # + url - Target service URL
    # + requestPath - Resource path
    # + return - An `http:CookieHandlingError` if there is any error occurred when adding a cookie or else `()`
    public function addCookie(Cookie cookie, CookieConfig cookieConfig, string url, string requestPath) returns CookieHandlingError? {
        if (self.getAllCookies().length() == cookieConfig.maxTotalCookieCount) {
            return CookieHandlingError("Number of total cookies in the cookie store can not exceed the maximum amount");
        }
        string domain = getDomain(url);
        if (self.getCookiesByDomain(domain).length() == cookieConfig.maxCookiesPerDomain) {
            return CookieHandlingError("Number of total cookies for the domain: " + domain + " in the cookie store can not exceed the maximum amount per domain");
        }
        string path  = requestPath;
        int? index = requestPath.indexOf("?");
        if (index is int) {
            path = requestPath.substring(0, index);
        }
        lock {
            Cookie? identicalCookie = getIdenticalCookie(cookie, self);
            if (!isDomainMatched(cookie, domain, cookieConfig)) {
                return;
            }
            if (!isPathMatched(cookie, path, cookieConfig)) {
                return;
            }
            if (!isExpiresAttributeValid(cookie)) {
                return;
            }
            if (!((url.startsWith(HTTP) && cookie.httpOnly) || cookie.httpOnly == false)) {
                return;
            }
            if (cookie.isPersistent()) {
                var persistentCookieHandler = self.persistentCookieHandler;
                if (persistentCookieHandler is PersistentCookieHandler) {
                    var result = addPersistentCookie(identicalCookie, cookie, url, persistentCookieHandler, self);
                    if (result is error) {
                        return CookieHandlingError("Error in adding persistent cookies", result);
                    }
                } else if (isFirstRequest(self.allSessionCookies, domain)) {
                    log:printWarn("Client is not configured to use persistent cookies. Hence, persistent cookies from " + domain + " will be discarded.");
                }
            } else {
                var result = addSessionCookie(identicalCookie, cookie, url, self);
                if (result is error) {
                    return CookieHandlingError("Error in adding session cookie", result);
                }
            }
        }
    }

    # Adds an array of cookies.
    #
    # + cookiesInResponse - Cookies to be added
    # + cookieConfig - Configurations associated with the cookies
    # + url - Target service URL
    # + requestPath - Resource path
    public function addCookies(Cookie[] cookiesInResponse, CookieConfig cookieConfig, string url, string requestPath) {
        foreach var cookie in cookiesInResponse {
            var result = self.addCookie(cookie, cookieConfig, url, requestPath);
            if (result is error) {
                log:printError("Error in adding cookies to cookie store: ", result);
            }
        }
    }

    # Gets the relevant cookies for the given URL and the path according to the rules in [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.4).
    #
    # + url - URL of the request URI
    # + requestPath - Path of the request URI
    # + return - Array of the matched cookies stored in the cookie store
    public function getCookies(string url, string requestPath) returns Cookie[] {
        Cookie[] cookiesToReturn = [];
        string domain = getDomain(url);
        string path  = requestPath;
        int? index = requestPath.indexOf("?");
        if (index is int) {
            path = requestPath.substring(0,index);
        }
        Cookie[] allCookies = self.getAllCookies();
        lock {
            foreach var cookie in allCookies {
                if (isExpired(cookie)) {
                    continue;
                }
                if (!((url.startsWith(HTTPS) && cookie.secure) || cookie.secure == false)) {
                    continue;
                }
                if (!((url.startsWith(HTTP) && cookie.httpOnly) || cookie.httpOnly == false)) {
                    continue;
                }
                if (cookie.hostOnly == true) {
                    if (cookie.domain == domain && checkPath(path, cookie)) {
                        cookiesToReturn.push(cookie);
                    }
                } else {
                    var cookieDomain = cookie.domain;
                    if (((cookieDomain is string && domain.endsWith("." + cookieDomain)) || cookie.domain == domain ) && checkPath(path, cookie)) {
                        cookiesToReturn.push(cookie);
                    }
                }
            }
            return cookiesToReturn;
        }
    }

    # Gets all the cookies in the cookie store.
    #
    # + return - Array of all the cookie objects
    public function getAllCookies() returns Cookie[] {
        var persistentCookieHandler = self.persistentCookieHandler;
        Cookie[] allCookies = [];
        foreach var cookie in self.allSessionCookies {
            allCookies.push(cookie);
        }
        if (persistentCookieHandler is PersistentCookieHandler) {
            var result = persistentCookieHandler.getAllCookies();
            if (result is error) {
                log:printError("Error in getting persistent cookies: ", result);
            } else {
                foreach var cookie in result {
                    allCookies.push(cookie);
                }
            }
        }
        return allCookies;
    }

    # Gets all the cookies, which have the given name as the name of the cookie.
    #
    # + cookieName - Name of the cookie
    # + return - Array of all the matched cookie objects
    public function getCookiesByName(string cookieName) returns Cookie[] {
        Cookie[] cookiesToReturn = [];
        Cookie[] allCookies = self.getAllCookies();
        foreach var cookie in allCookies {
            if (cookie.name == cookieName) {
                cookiesToReturn.push(cookie);
            }
        }
        return cookiesToReturn;
    }

    # Gets all the cookies, which have the given name as the domain of the cookie.
    #
    # + domain - Name of the domain
    # + return - Array of all the matched cookie objects
    public function getCookiesByDomain(string domain) returns Cookie[] {
        Cookie[] cookiesToReturn = [];
        Cookie[] allCookies = self.getAllCookies();
        foreach var cookie in allCookies {
            if (cookie.domain == domain) {
                cookiesToReturn.push(cookie);
            }
        }
        return cookiesToReturn;
    }

    # Removes a specific cookie.
    #
    # + name - Name of the cookie to be removed
    # + domain - Domain of the cookie to be removed
    # + path - Path of the cookie to be removed
    # + return - An `http:CookieHandlingError` if there is any error occurred during the removal of the cookie or else `()`
    public function removeCookie(string name, string domain, string path) returns CookieHandlingError? {
        lock {
            // Removes the session cookie if it is in the session cookies array, which is matched with the given name, domain, and path.
            int k = 0;
            while (k < self.allSessionCookies.length()) {
                if (name == self.allSessionCookies[k].name && domain == self.allSessionCookies[k].domain && path ==  self.allSessionCookies[k].path) {
                    int j = k;
                    while (j < self.allSessionCookies.length() - 1) {
                        self.allSessionCookies[j] = self.allSessionCookies[j + 1];
                        j = j + 1;
                    }
                    _ = self.allSessionCookies.pop();
                    return;
                }
                k = k + 1;
            }
            // Removes the persistent cookie if it is in the persistent cookie store, which is matched with the given name, domain, and path.
            var persistentCookieHandler = self.persistentCookieHandler;
            if (persistentCookieHandler is PersistentCookieHandler) {
                return persistentCookieHandler.removeCookie(name, domain, path);
            }
            return CookieHandlingError("Error in removing cookie: No such cookie to remove");
        }
    }

    # Removes cookies, which match with the given domain.
    #
    # + domain - Domain of the cookie to be removed
    # + return - An `http:CookieHandlingError` if there is any error occurred during the removal of cookies by domain or else `()`
    public function removeCookiesByDomain(string domain) returns CookieHandlingError? {
        Cookie[] allCookies = self.getAllCookies();
        lock {
            foreach var cookie in allCookies {
                if (cookie.domain != domain ) {
                    continue;
                }
                var cookieName = cookie.name;
                var cookiePath = cookie.path;
                if (cookieName is string && cookiePath is string) {
                    var result = self.removeCookie(cookieName, domain, cookiePath);
                    if (result is error) {
                        return CookieHandlingError("Error in removing cookies", result);
                    }
                }
            }
        }
    }

    # Removes all expired cookies.
    #
    # + return - An `http:CookieHandlingError` if there is any error occurred during the removal of expired cookies or else `()`
    public function removeExpiredCookies() returns CookieHandlingError? {
        var persistentCookieHandler = self.persistentCookieHandler;
        if (persistentCookieHandler is PersistentCookieHandler) {
            var result = persistentCookieHandler.getAllCookies();
            if (result is error) {
                return CookieHandlingError("Error in removing expired cookies", result);
            } else {
                lock {
                    foreach var cookie in result {
                        if (!isExpired(cookie)) {
                            continue;
                        }
                        var cookieName = cookie.name;
                        var cookieDomain = cookie.domain;
                        var cookiePath = cookie.path;
                        if (cookieName is string && cookieDomain is string && cookiePath is string) {
                            var removeResult = persistentCookieHandler.removeCookie(cookieName, cookieDomain, cookiePath);
                            if (removeResult is error) {
                                return CookieHandlingError("Error in removing expired cookies", removeResult);
                            }
                        }
                    }
                }
            }
        } else {
            return CookieHandlingError("No persistent cookie store to remove expired cookies");
        }
    }

    # Removes all the cookies.
    #
    # + return - An `http:CookieHandlingError` if there is any error occurred during the removal of all the cookies or else `()`
    public function removeAllCookies() returns CookieHandlingError? {
        var persistentCookieHandler = self.persistentCookieHandler;
        lock {
            self.allSessionCookies = [];
            if (persistentCookieHandler is PersistentCookieHandler) {
                return persistentCookieHandler.removeAllCookies();
            }
        }
    }
}

const string HTTP = "http";
const string HTTPS = "https";
const string URL_TYPE_1 = "https://www.";
const string URL_TYPE_2 = "http://www.";
const string URL_TYPE_3 = "http://";
const string URL_TYPE_4 = "https://";

// Extracts domain name from the request URL.
function getDomain(string url) returns string {
    string domain = url;
    if (url.startsWith(URL_TYPE_1)) {
        domain = url.substring(URL_TYPE_1.length(), url.length());
    } else if (url.startsWith(URL_TYPE_2)) {
        domain = url.substring(URL_TYPE_2.length(), url.length());
    } else if (url.startsWith(URL_TYPE_3)) {
        domain = url.substring(URL_TYPE_3.length(), url.length());
    } else if (url.startsWith(URL_TYPE_4)) {
        domain = url.substring(URL_TYPE_4.length(), url.length());
    }
    return domain;
}

# Gets the identical cookie for a given cookie if one exists.
# Identical cookie is the cookie, which has the same name, domain and path as the given cookie.
#
# + cookieToCompare - Cookie to be compared
# + cookieStore - Cookie store of the client
# + return - Identical cookie if one exists, else `()`
function getIdenticalCookie(Cookie cookieToCompare, CookieStore cookieStore) returns Cookie? {
    Cookie[] allCookies = cookieStore.getAllCookies();
    int k = 0 ;
    while (k < allCookies.length()) {
        if (cookieToCompare.name == allCookies[k].name && cookieToCompare.domain == allCookies[k].domain
            && cookieToCompare.path ==  allCookies[k].path) {
            return allCookies[k];
        }
        k = k + 1;
    }
}

// Returns true if the cookie domain matches with the request domain according to [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.1.3).
function isDomainMatched(Cookie cookie, string domain, CookieConfig cookieConfig) returns boolean {
    if (cookie.domain == ()) {
        cookie.domain = domain;
        cookie.hostOnly = true;
        return true;
    }
    cookie.hostOnly = false;
    if (!cookieConfig.blockThirdPartyCookies) {
        return true;
    }
    var cookieDomain = cookie.domain;
    if (cookieDomain == domain || (cookieDomain is string && domain.endsWith("." + cookieDomain))) {
        return true;
    }
    return false;
}

// Returns true if the cookie path matches the request path according to [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.1.4).
function isPathMatched(Cookie cookie, string path, CookieConfig cookieConfig) returns boolean {
    if (cookie.path == ()) {
        cookie.path = path;
        return true;
    }
    if (!cookieConfig.blockThirdPartyCookies) {
        return true;
    }
    if (checkPath(path, cookie)) {
        return true;
    }
    return false;
}

function checkPath(string path, Cookie cookie) returns boolean {
    if (cookie.path == path) {
        return true;
    }
    var cookiePath = cookie.path;
    if (cookiePath is string && path.startsWith(cookiePath) && cookiePath.endsWith("/")) {
        return true;
    }
    if (cookiePath is string && path.startsWith(cookiePath) && path[cookiePath.length()] == "/" ) {
        return true;
    }
    return false;
}

// Returns true if the cookie expires attribute value is valid according to [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.1.1).
function isExpiresAttributeValid(Cookie cookie) returns boolean {
    var expiryTime = cookie.expires;
    if (expiryTime is ()) {
         return true;
    } else {
        time:Time|error t1 = time:parse(expiryTime.substring(0, expiryTime.length() - 4), "E, dd MMM yyyy HH:mm:ss");
        if (t1 is time:Time) {
            int year = time:getYear(t1);
            if (year <= 69 && year >= 0) {
                time:Time tmAdd = time:addDuration(t1, 2000, 0, 0, 0, 0, 0, 0);
                string|error timeString = time:format(tmAdd, "E, dd MMM yyyy HH:mm:ss");
                if (timeString is string) {
                    cookie.expires = timeString + " GMT";
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }
}

// Checks whether the user has requested a particular domain or a sub-domain of it previously or not.
function isFirstRequest(Cookie[] allSessionCookies, string domain) returns boolean {
    foreach var cookie in allSessionCookies {
       var cookieDomain = cookie.domain;
       if (((cookieDomain is string && (domain.endsWith("." + cookieDomain) || cookieDomain.endsWith("." + domain))) || cookie.domain == domain )) {
           return false;
       }
    }
    return true;
}

// Adds a persistent cookie to the cookie store according to the rules in [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.3 , https://tools.ietf.org/html/rfc6265#section-4.1.2).
function addPersistentCookie(Cookie? identicalCookie, Cookie cookie, string url, PersistentCookieHandler persistentCookieHandler, CookieStore cookieStore) returns error? {
    if (identicalCookie is Cookie) {
        var identicalCookieName = identicalCookie.name;
        var identicalCookieDomain = identicalCookie.domain;
        var identicalCookiePath = identicalCookie.path;
        if (isExpired(cookie) && identicalCookieName is string && identicalCookieDomain is string && identicalCookiePath is string) {
            return cookieStore.removeCookie(identicalCookieName, identicalCookieDomain, identicalCookiePath);
        } else {
            // Removes the old cookie and adds the new persistent cookie.
            if (((identicalCookie.httpOnly && url.startsWith(HTTP)) || identicalCookie.httpOnly == false)
                && identicalCookieName is string && identicalCookieDomain is string && identicalCookiePath is string) {
                var removeResult = cookieStore.removeCookie(identicalCookieName, identicalCookieDomain, identicalCookiePath);
                if (removeResult is error) {
                    return removeResult;
                }
                cookie.createdTime = identicalCookie.createdTime;
                cookie.lastAccessedTime = time:currentTime();
                return persistentCookieHandler.storeCookie(cookie);
            }
        }
    } else {
        // If cookie is not expired, adds that cookie.
        if (!isExpired(cookie)) {
            cookie.createdTime = time:currentTime();
            cookie.lastAccessedTime = time:currentTime();
            return persistentCookieHandler.storeCookie(cookie);
        }
    }
}

// Returns true if the cookie is expired according to the rules in [RFC-6265](https://tools.ietf.org/html/rfc6265#section-4.1.2.2).
function isExpired(Cookie cookie) returns boolean {
    if (cookie.maxAge > 0) {
        time:Time expTime = time:addDuration(cookie.createdTime, 0, 0, 0, 0, 0, cookie.maxAge, 0);
        time:Time curTime = time:currentTime();
        return (expTime.time < curTime.time);
    }
    var expiryTime = cookie.expires;
    if (expiryTime is string) {
        time:Time|error cookieExpires = time:parse(expiryTime.substring(0, expiryTime.length() - 4), "E, dd MMM yyyy HH:mm:ss");
        time:Time curTime = time:currentTime();
        if ((cookieExpires is time:Time) && cookieExpires.time < curTime.time) {
            return true;
        }
        return false;
    }
    return false;
}

// Adds a session cookie to the cookie store according to the rules in [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.3 , https://tools.ietf.org/html/rfc6265#section-4.1.2).
function addSessionCookie(Cookie? identicalCookie, Cookie cookie, string url, CookieStore cookieStore) returns error? {
    if (identicalCookie is Cookie) {
        var identicalCookieName = identicalCookie.name;
        var identicalCookieDomain = identicalCookie.domain;
        var identicalCookiePath = identicalCookie.path;
        // Removes the old cookie and adds the new session cookie.
        if (((identicalCookie.httpOnly && url.startsWith(HTTP)) || identicalCookie.httpOnly == false)
            && identicalCookieName is string && identicalCookieDomain is string && identicalCookiePath is string) {
            var removeResult = cookieStore.removeCookie(identicalCookieName, identicalCookieDomain, identicalCookiePath);
            if (removeResult is error) {
                return removeResult;
            }
            cookie.createdTime = identicalCookie.createdTime;
            cookie.lastAccessedTime = time:currentTime();
            cookieStore.allSessionCookies.push(cookie);
        }
    } else {
        // Adds the session cookie.
        cookie.createdTime = time:currentTime();
        cookie.lastAccessedTime = time:currentTime();
        cookieStore.allSessionCookies.push(cookie);
    }
}
