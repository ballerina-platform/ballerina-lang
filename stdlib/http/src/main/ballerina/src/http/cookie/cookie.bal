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

import ballerina/lang.'int as ints;
import ballerina/stringutils;
import ballerina/time;

# Represents a Cookie.
# 
# + name - Name of the cookie
# + value - Value of the cookie
# + path - URI path to which the cookie belongs
# + domain - Host to which the cookie will be sent
# + maxAge - Maximum lifetime of the cookie, represented as number of seconds until the cookie expires
# + expires - Maximum lifetime of the cookie, represented as date and time at which the cookie expires
# + httpOnly - Cookie is sent only to http requests
# + secure - Cookie is sent only to secure channels
# + creationTime - Creation time of the cookie
# + lastAccessedTime - Last accessed time of the cookie
# + hostOnly - Cookie is sent only to the requested host
public type Cookie object {

    public string name = "";
    public string value = "";
    public string domain = "";
    public string path = "";
    public int maxAge = 0;
    public string expires = "";
    public boolean httpOnly = false;
    public boolean secure = false;
    public time:Time creationTime = time:currentTime();
    public time:Time lastAccessedTime = time:currentTime();
    public boolean hostOnly = false;

    // Returns false if the cookie will be discarded at the end of the "session"; true otherwise.
    public function isPersistent() returns boolean {
        if (self.expires == "" && self.maxAge == 0) {
            return false;
        } else {
            return true;
        }
    }

    // Returns true if the attributes of the cookie are in the correct format; false otherwise.
    public function isValid() returns boolean | error {
        error invalidCookieError;
        if (self.name == "" || self.value == "") {
            invalidCookieError = error("Empty name value pair");
            return invalidCookieError;
        }
        if (self.domain != "") {
            self.domain = self.domain.toLowerAscii();
        }
        if (self.domain.startsWith(".")) {
            self.domain = self.domain.substring(1, self.domain.length());
        }
        if (self.domain.endsWith(".")) {
            self.domain = self.domain.substring(0, self.domain.length() - 1);
        }
        if (self.path != "" && (!self.path.startsWith("/") || stringutils:contains(self.path, "?"))) {
            invalidCookieError = error("Path is not in correct format ");
            return invalidCookieError;
        }
        if (self.expires != "" && !toGmtFormat(self)) {
            invalidCookieError = error("Time is not in correct format");
            return invalidCookieError;
        }
        if (self.maxAge < 0) {
            invalidCookieError = error("Max Age is less than zero");
            return invalidCookieError;
        }
        return true;
    }

    // Gets the Cookie object in its string representation to be used in ‘Set-Cookie’ header in the response.
    function toStringValue() returns string {
        string setCookieHeaderValue = "";
        setCookieHeaderValue = appendNameValuePair(setCookieHeaderValue, self.name, self.value);
        if (self.domain != "") {
            setCookieHeaderValue = appendNameValuePair(setCookieHeaderValue, "Domain", self.domain);
        }
        if (self.path != "") {
            setCookieHeaderValue = appendNameValuePair(setCookieHeaderValue, "Path", self.path);
        }
        if (self.expires != "") {
            setCookieHeaderValue = appendNameValuePair(setCookieHeaderValue, "Expires", self.expires);
        }
        if (self.maxAge > 0) {
            setCookieHeaderValue = appendNameIntValuePair(setCookieHeaderValue, "Max-Age", self.maxAge);
        }
        if (self.httpOnly == true) {
            setCookieHeaderValue = appendOnlyName(setCookieHeaderValue, "HttpOnly");
        }
        if (self.secure == true) {
            setCookieHeaderValue = appendOnlyName(setCookieHeaderValue, "Secure");
        }
        setCookieHeaderValue = setCookieHeaderValue.substring(0, setCookieHeaderValue.length() - 2);
        return setCookieHeaderValue;
    }
};

// Converts the cookie's expires time into GMT format.
function toGmtFormat(Cookie cookie) returns boolean {
    time:Time | error t1 = time:parse(cookie.expires, "yyyy-MM-dd HH:mm:ss");
    if (t1 is time:Time) {
        string | error timeString = time:format(<time:Time>t1, "E, dd MMM yyyy HH:mm:ss ");
        if (timeString is string) {
            cookie.expires = timeString + "GMT";
            return true;
        }
        return false;
    } else {
        return false;
    }
}

const string EQUALS = "=";
const string SPACE = " ";
const string SEMICOLON = ";";

function appendNameValuePair(string setCookieHeaderValue, string name, string value) returns string {
    string resultString;
    resultString = setCookieHeaderValue + name + EQUALS + value + SEMICOLON + SPACE;
    return resultString;
}

function appendOnlyName(string setCookieHeaderValue, string name) returns string {
    string resultString;
    resultString = setCookieHeaderValue + name + SEMICOLON + SPACE;
    return resultString;
}

function appendNameIntValuePair(string setCookieHeaderValue, string name, int value) returns string {
    string resultString;
    resultString = setCookieHeaderValue + name + EQUALS + value.toString() + SEMICOLON + SPACE;
    return resultString;
}

// Returns the cookie object from "Set-Cookie" header string value.
function parseSetCookieHeader(string cookieStringValue) returns Cookie {
    Cookie cookie = new;
    string cookieValue = cookieStringValue;
    string[] result = stringutils:split(cookieValue, "; ");
    string[] nameValuePair = stringutils:split(result[0], "=");
    cookie.name = nameValuePair[0];
    cookie.value = nameValuePair[1];
    foreach var item in result {
        nameValuePair = stringutils:split(item, "=");
        match nameValuePair[0] {
            "Domain" => {
                cookie.domain = nameValuePair[1];
            }
            "Path" => {
                cookie.path = nameValuePair[1];
            }
            "Max-Age" => {
                int | error age = ints:fromString(nameValuePair[1]);
                if (age is int) {
                    cookie.maxAge = age;
                }
            }
            "Expires" => {
                cookie.expires = nameValuePair[1];
            }
            "Secure" => {
                cookie.secure = true;
            }
            "HttpOnly" => {
                cookie.httpOnly = true;
            }
        }
    }
    return cookie;
}

// Returns an array of cookie objects from "Cookie" header string value.
function parseCookieHeader(string cookieStringValue) returns Cookie[] {
    Cookie[] cookiesInRequest = [];
    string cookieValue = cookieStringValue;
    int i = 0;
    string[] nameValuePairs = stringutils:split(cookieValue, "; ");
    foreach var item in nameValuePairs {
        string[] nameValue = stringutils:split(item, "=");
        Cookie cookie = new;
        cookie.name = nameValue[0];
        cookie.value = nameValue[1];
        cookiesInRequest[i] = cookie;
        i = i + 1;
    }
    return cookiesInRequest;
}

// Sorts an array of cookies in order to make "Cookie" header in the request.
function sortCookies(Cookie[] cookies) {
    int i = 0;
    int j = 0;
    Cookie temp = new ();
    while (i < cookies.length()) {
        j = i + 1;
        while (j < cookies.length()) {
            if (cookies[i].path.length() < cookies[j].path.length()) {
                temp = cookies[i];
                cookies[i] = cookies[j];
                cookies[j] = temp;
            }
            if (cookies[i].path.length() == cookies[j].path.length()) {
                //sort according to time
                time:Time | error t1 = cookies[i].creationTime;
                time:Time | error t2 = cookies[j].creationTime;
                if (t1 is time:Time && t2 is time:Time) {
                    if (t1.time > t2.time) {
                        temp = cookies[i];
                        cookies[i] = cookies[j];
                        cookies[j] = temp;
                    }
                }
            }
            j = j + 1;
        }
        i = i + 1;
    }
}
