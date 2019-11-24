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

    # Adds a cookie to the cookie store according to the rules in rfc-6265. - https://tools.ietf.org/html/rfc6265#section-5.3
    #
    # + cookie - Cookie to be added
    # + cookieConfig - Configurations associated with cookies
    # + url - Target service url
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
            if (!((url.startsWith("http") && cookie.httpOnly) || cookie.httpOnly == false)) {
                return;
            }
            if (cookie.isPersistent()) {
                if (!cookieConfig.enablePersistent) {
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
    # + cookieConfig - Configurations associated with cookies
    # + url - Target service url
    # + requestPath - Resource path
    public function addCookies(Cookie[] cookiesInResponse, CookieConfig cookieConfig, string url, string requestPath) {
        foreach var cookie in cookiesInResponse {
            self.addCookie(cookie, cookieConfig, url, requestPath);
        }
    }

    # Gets the relevant cookies for the given url and path according to the rules in rfc-6265. - https://tools.ietf.org/html/rfc6265#section-5.4
    #
    # + url - URL of the request URI
    # + requestPath - Path of the request URI
    # + return - Array of matched cookies stored in the cookie store
    public function getCookies(string url, string requestPath) returns Cookie[] {
        Cookie[] cookiesToReturn = [];
        string domain = getDomain(url);
        string path  = requestPath;
        int? index = requestPath.indexOf("?");
        if (index is int) {
            path = requestPath.substring(0,index);
        }
        lock {
            // Gets session cookies.
            foreach var cookie in self.allSessionCookies {
                if (!((url.startsWith("https") && cookie.secure) || cookie.secure == false)) {
                    return cookiesToReturn;
                }
                if (!((url.startsWith("http") && cookie.httpOnly) || cookie.httpOnly == false)) {
                    return cookiesToReturn;
                }
                if (cookie.hostOnly == true) {
                    if (cookie.domain == domain && checkPath(path, cookie)) {
                        cookiesToReturn[cookiesToReturn.length()] = cookie;
                    }
                } else {
                    if ((domain.endsWith("." + cookie.domain) || cookie.domain == domain ) && checkPath(path, cookie)) {
                        cookiesToReturn[cookiesToReturn.length()] = cookie;
                    }
                }
            }
            // TODO:Get persistent cookies from database.
            return cookiesToReturn;
        }
    }

     # Gets all the session and persistent cookies.
     #
     # + return - All the session and persistent cookies
     public function getAllCookies() returns Cookie[] {
         lock {
             // TODO:Get persistent cookies.
             return self.allSessionCookies;
         }
     }

     # Removes a specific cookie.
     #
     # + name - Name of the cookie to be removed
     # + domain - Domain of the cookie to be removed
     # + path - Path of the cookie to be removed
     # + return - Return true if the relevant cookie is removed, false otherwise
     public function removeCookie(string name, string domain, string path) returns boolean {
         lock {
             // Removes the session cookie in the cookie store which is matched with  given name, domain and path.
             int k = 0;
             while (k < self.allSessionCookies.length()) {
                 if (name == self.allSessionCookies[k].name && domain == self.allSessionCookies[k].domain && path ==  self.allSessionCookies[k].path) {
                     int j = k;
                     while (j < self.allSessionCookies.length()-1) {
                         self.allSessionCookies[j] = self.allSessionCookies[j + 1];
                         j = j + 1;
                     }
                     Cookie lastCookie = self.allSessionCookies.pop();
                     return true;
                 }
                 k = k + 1;
             }
             // TODO:Remove from the database if it is a persistent cookie.
             return false;
         }
     }

    # Removes all expired cookies.
    public function removeExpiredCookies() {
        lock {
            // TODO:If expired, remove those persistent cookies from the database.
        }
    }

    # Removes all the session and persistent cookies.
    public function clear() {
        lock {
            // TODO:Remove all persistent cookies from the database.
            self.allSessionCookies = [];
        }
    }
};

// Extracts domain name from the request url.
function getDomain(string url) returns string {
    string domain = url;
    string urlType1 = "https://www.";
    string urlType2 = "http://www.";
    string urlType3 = "http://";
    string urlType4 = "https://";
    if (url.startsWith(urlType1)) {
        domain = url.substring(urlType1.length(), url.length());
    }
    else if (url.startsWith(urlType2)) {
        domain = url.substring(urlType2.length(), url.length());
    }
    else if (url.startsWith(urlType3)) {
        domain = url.substring(urlType3.length(), url.length());
    }
    else if (url.startsWith(urlType4)) {
        domain = url.substring(urlType4.length(), url.length());
    }
    return domain;
}

// Returns the identical cookie for a given cookie if exists.
// Identical cookie is the cookie which has the same name,domain & path as in the given cookie.
function getIdenticalCookie(Cookie cookieToCompare, Cookie[] allSessionCookies) returns Cookie? {
    // Searches session cookies.
    int k = 0 ;
    while (k < allSessionCookies.length()) {
        if (cookieToCompare.name == allSessionCookies[k].name && cookieToCompare.domain == allSessionCookies[k].domain  && cookieToCompare.path ==  allSessionCookies[k].path) {
            return allSessionCookies[k];
        }
        k = k + 1;
    }
    // Searches persistent cookies.
    // TODO:get the same name,path,domain cookies from the database.
}

// Returns true , if cookie domain matches with request domain according to rfc-6265. - https://tools.ietf.org/html/rfc6265#section-5.1.3
function isDomainMatched(Cookie cookie, string domain, CookieConfig cookieConfig) returns boolean {
    if (cookie.domain == "") {
        cookie.domain = domain;
        cookie.hostOnly = true;
        return true;
    } else {
        cookie.hostOnly = false;
        if (cookieConfig.blockThirdPartyCookies) {
            if (cookie.domain == domain || domain.endsWith("." + cookie.domain)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}

// Returns true , if cookie path matches with request path according to rfc-6265. - https://tools.ietf.org/html/rfc6265#section-5.1.4
function isPathMatched(Cookie cookie, string path, CookieConfig cookieConfig) returns boolean {
    if (cookie.path == "") {
        cookie.path = path;
        return true;
    } else {
        if (cookieConfig.blockThirdPartyCookies) {
            if (checkPath(path, cookie)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}

function checkPath(string path, Cookie cookie) returns boolean {
    if (cookie.path == path) {
        return true;
    }
    if (path.startsWith(cookie.path) && cookie.path.endsWith("/")) {
        return true;
    }
    if (path.startsWith(cookie.path) && path[cookie.path.length()] == "/" ) {
        return true;
    }
    return false;
}

// Returns true , if cookie expires attribute value is valid according to rfc-6265. - https://tools.ietf.org/html/rfc6265#section-5.1.1
function isExpiresAttributeValid(Cookie cookie) returns boolean {
    if (cookie.expires != "") {
        time:Time|error t1 = time:parse(cookie.expires.substring(0, cookie.expires.length() - 4), "E, dd MMM yyyy HH:mm:ss");
        if (t1 is time:Time) {
            int year = time:getYear(t1);
            if (year <= 69 && year >= 0) {
                time:Time tmAdd = time:addDuration(t1, 2000, 0, 0, 0, 0, 0, 0);
                string|error timeString = time:format(tmAdd, "E, dd MMM yyyy HH:mm:ss");
                if (timeString is string) {
                    cookie.expires = timeString + " GMT";
                    return true;
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    return true;
}

// Adds a persistent cookie to the cookie store according to the rules in rfc-6265. - https://tools.ietf.org/html/rfc6265#section-5.3 , https://tools.ietf.org/html/rfc6265#section-4.1.2
function addPersistentCookie(Cookie? identicalCookie, Cookie cookie, string url, CookieStore cookieStore) {
    if (identicalCookie is Cookie) {
        if (isExpired(cookie)) {
             boolean isRemoved = cookieStore.removeCookie(identicalCookie.name, identicalCookie.domain, identicalCookie.path);
        } else {
            // Removes old cookie and add new persistent cookie.(replace similar cookies)
            if ((identicalCookie.httpOnly == true && url.startsWith("http")) || identicalCookie.httpOnly == false) {
                boolean isRemoved = cookieStore.removeCookie(identicalCookie.name, identicalCookie.domain, identicalCookie.path);
                cookie.creationTime = identicalCookie.creationTime;
                cookie.lastAccessedTime = time:currentTime();
                // TODO:insert into the database.
            }
        }
    } else {
        // Adds if cookie is not expired.
        if (!isExpired(cookie)) {
            cookie.creationTime = time:currentTime();
            cookie.lastAccessedTime = time:currentTime();
            // TODO:insert into the database.
        }
    }
}

// Returns true , if cookie is expired according to the rules in rfc-6265. - https://tools.ietf.org/html/rfc6265#section-4.1.2.2
function isExpired(Cookie cookie) returns boolean {
    if (cookie.maxAge > 0) {
        time:Time exptime = time:addDuration(cookie.creationTime, 0, 0, 0, 0, 0, cookie.maxAge, 0);
        time:Time curTime = time:currentTime();
        if (exptime.time < curTime.time) {
            return true;
        } else {
            return false;
        }
    }
    if (cookie.expires != "") {
        time:Time|error cookieExpires = time:parse(cookie.expires.substring(0, cookie.expires.length() - 4), "E, dd MMM yyyy HH:mm:ss");
        time:Time curTime = time:currentTime();
        if (cookieExpires is time:Time) {
            if (cookieExpires.time < curTime.time) {
                return true;
            }
        } else {
            return false;
        }
    }
    return false;
}

// Adds a session cookie to the cookie store according to the rules in rfc-6265.  - https://tools.ietf.org/html/rfc6265#section-5.3 , https://tools.ietf.org/html/rfc6265#section-4.1.2
function addSessionCookie(Cookie? identicalCookie, Cookie cookie, string url, CookieStore cookieStore) {
    if (identicalCookie is Cookie) {
        // Removes old cookie and add new session cookie.
        if ((identicalCookie.httpOnly == true && url.startsWith("http")) || identicalCookie.httpOnly == false) {
            boolean isRemoved = cookieStore.removeCookie(identicalCookie.name, identicalCookie.domain, identicalCookie.path);
            cookie.creationTime = identicalCookie.creationTime;
            cookie.lastAccessedTime = time:currentTime();
            cookieStore.allSessionCookies[cookieStore.allSessionCookies.length()] = cookie;
       }
    } else {
        // Adds session cookie.
        cookie.creationTime = time:currentTime();
        cookie.lastAccessedTime = time:currentTime();
        cookieStore.allSessionCookies[cookieStore.allSessionCookies.length()] = cookie;
    }
}
