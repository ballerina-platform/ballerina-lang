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

import ballerina/time;

# Represents the cookie store.
#
# + allSessionCookies - Array to store all the session cookies
public type CookieStore object {

    Cookie[] allSessionCookies = [];

    # Adds a cookie to the cookie store according to the rules in [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.3).
    #
    # + cookie - Cookie to be added
    # + cookieConfig - Configurations associated with the cookies
    # + url - Target service URL
    # + requestPath - Resource path
    public function addCookie(Cookie cookie, CookieConfig cookieConfig, string url, string requestPath) {
        string domain = getDomain(url);
        string path  = requestPath;
        int? index = requestPath.indexOf("?");
        if (index is int) {
            path = requestPath.substring(0,index);
        }
        lock {
            Cookie? identicalCookie = getIdenticalCookie(cookie, self.allSessionCookies);
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
                if (!cookieConfig.enablePersistence) {
                    return;
                }
                addPersistentCookie(identicalCookie, cookie, url, self);
            } else {
                addSessionCookie(identicalCookie, cookie, url, self);
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
            self.addCookie(cookie, cookieConfig, url, requestPath);
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
        lock {
            // Gets the session cookies.
            foreach var cookie in self.allSessionCookies {
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
                    var temp = cookie.domain;
                    if (((temp is string && domain.endsWith("." + temp)) || cookie.domain == domain ) && checkPath(path, cookie)) {
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
        return self.allSessionCookies;
    }

    # Removes a specific cookie.
    #
    # + name - Name of the cookie to be removed
    # + domain - Domain of the cookie to be removed
    # + path - Path of the cookie to be removed
    # + return - Return true if the relevant cookie is removed, false otherwise
    public function removeCookie(string name, string domain, string path) returns boolean {
         lock {
             // Removes the session cookie in the cookie store, which is matched with the given name, domain and path.
             int k = 0;
             while (k < self.allSessionCookies.length()) {
                 if (name == self.allSessionCookies[k].name && domain == self.allSessionCookies[k].domain && path ==  self.allSessionCookies[k].path) {
                     int j = k;
                     while (j < self.allSessionCookies.length()-1) {
                         self.allSessionCookies[j] = self.allSessionCookies[j + 1];
                         j = j + 1;
                     }
                     _ = self.allSessionCookies.pop();
                     return true;
                 }
                 k = k + 1;
             }
             return false;
         }
    }

    # Removes all the cookies.
    public function clear() {
        lock {
            self.allSessionCookies = [];
        }
    }
};

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
# + allSessionCookies - Array which stores all the session cookies
# + return - Identical cookie if one exists, else `()`
function getIdenticalCookie(Cookie cookieToCompare, Cookie[] allSessionCookies) returns Cookie? {
    // Searches for the session cookies.
    int k = 0 ;
    while (k < allSessionCookies.length()) {
        if (cookieToCompare.name == allSessionCookies[k].name && cookieToCompare.domain == allSessionCookies[k].domain  && cookieToCompare.path ==  allSessionCookies[k].path) {
            return allSessionCookies[k];
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
    var temp = cookie.domain;
    if (cookie.domain == domain || (temp is string && domain.endsWith("." + temp))) {
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
    var temp = cookie.path;
    if (temp is string && path.startsWith(temp) && temp.endsWith("/")) {
        return true;
    }
    if (temp is string && path.startsWith(temp) && path[temp.length()] == "/" ) {
        return true;
    }
    return false;
}

// Returns true if the cookie expires attribute value is valid according to [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.1.1).
function isExpiresAttributeValid(Cookie cookie) returns boolean {
    var temp = cookie.expires;
    if (temp is ()) {
         return true;
    } else {
        time:Time|error t1 = time:parse(temp.substring(0, temp.length() - 4), "E, dd MMM yyyy HH:mm:ss");
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

// Adds a persistent cookie to the cookie store according to the rules in [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.3 , https://tools.ietf.org/html/rfc6265#section-4.1.2).
function addPersistentCookie(Cookie? identicalCookie, Cookie cookie, string url, CookieStore cookieStore) {
    if (identicalCookie is Cookie) {
        var temp1 = identicalCookie.name;
        var temp2 = identicalCookie.domain;
        var temp3 = identicalCookie.path;
        if (isExpired(cookie) && temp1 is string && temp2 is string && temp3 is string) {
            _ = cookieStore.removeCookie(temp1, temp2, temp3);
        } else {
            // Removes the old cookie and adds the new persistent cookie.
            if (((identicalCookie.httpOnly && url.startsWith(HTTP)) || identicalCookie.httpOnly == false) && temp1 is string && temp2 is string && temp3 is string) {
                _ = cookieStore.removeCookie(temp1, temp2, temp3);
                cookie.creationTime = identicalCookie.creationTime;
                cookie.lastAccessedTime = time:currentTime();
                // TODO:insert into the database.
            }
        }
    } else {
        // If cookie is not expired adds that cookie.
        if (!isExpired(cookie)) {
            cookie.creationTime = time:currentTime();
            cookie.lastAccessedTime = time:currentTime();
            // TODO:insert into the database.
        }
    }
}

// Returns true if the cookie is expired according to the rules in [RFC-6265](https://tools.ietf.org/html/rfc6265#section-4.1.2.2).
function isExpired(Cookie cookie) returns boolean {
    if (cookie.maxAge > 0) {
        time:Time exptime = time:addDuration(cookie.creationTime, 0, 0, 0, 0, 0, cookie.maxAge, 0);
        time:Time curTime = time:currentTime();
        if (exptime.time < curTime.time) {
            return true;
        }
        return false;
    }
    var temp = cookie.expires;
    if (temp is string) {
        time:Time|error cookieExpires = time:parse(temp.substring(0, temp.length() - 4), "E, dd MMM yyyy HH:mm:ss");
        time:Time curTime = time:currentTime();
        if ((cookieExpires is time:Time) && cookieExpires.time < curTime.time) {
            return true;
        }
        return false;
    }
    return false;
}

// Adds a session cookie to the cookie store according to the rules in [RFC-6265](https://tools.ietf.org/html/rfc6265#section-5.3 , https://tools.ietf.org/html/rfc6265#section-4.1.2).
function addSessionCookie(Cookie? identicalCookie, Cookie cookie, string url, CookieStore cookieStore) {
    if (identicalCookie is Cookie) {
        var temp1 = identicalCookie.name;
        var temp2 = identicalCookie.domain;
        var temp3 = identicalCookie.path;
        // Removes the old cookie and adds the new session cookie.
        if (((identicalCookie.httpOnly && url.startsWith(HTTP)) || identicalCookie.httpOnly == false) && temp1 is string && temp2 is string && temp3 is string) {
            _ = cookieStore.removeCookie(temp1, temp2, temp3);
            cookie.creationTime = identicalCookie.creationTime;
            cookie.lastAccessedTime = time:currentTime();
            cookieStore.allSessionCookies.push(cookie);
       }
    } else {
        // Adds the session cookie.
        cookie.creationTime = time:currentTime();
        cookie.lastAccessedTime = time:currentTime();
        cookieStore.allSessionCookies.push(cookie);
    }
}
