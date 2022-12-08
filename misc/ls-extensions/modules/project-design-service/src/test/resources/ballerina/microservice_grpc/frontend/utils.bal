// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/log;
import ballerina/regex;

isolated function getUserIdFromCookie(string cookieStr) returns http:Cookie|http:Unauthorized {
    http:Cookie[] cookies = parseCookieHeader(cookieStr);
    http:Cookie[] usernameCookie = cookies.filter(isolated function
                            (http:Cookie cookie) returns boolean {
        return cookie.name == USER_COOKIE_NAME;
    });
    if usernameCookie.length() == 1 {
        return usernameCookie[0];
    }
    return {
        body: USER_COOKIE_NAME + " cookie is not available."
    };
}

isolated function parseCookieHeader(string cookieStringValue) returns http:Cookie[] {
    http:Cookie[] cookiesInRequest = [];
    string cookieValue = cookieStringValue;
    string[] nameValuePairs = regex:split(cookieValue, "; ");
    foreach var item in nameValuePairs {
        if regex:matches(item, "^([^=]+)=.*$") {
            string[] nameValue = regex:split(item, "=");
            http:Cookie cookie;
            if nameValue.length() > 1 {
                cookie = new (nameValue[0], nameValue[1], path = "/");
            } else {
                cookie = new (nameValue[0], "", path = "/");
            }
            cookiesInRequest.push(cookie);
        } else {
            log:printError("Invalid cookie: " + item + ", which must be in the format as [{name}=].");
        }
    }
    return cookiesInRequest;
}

isolated function toProductLocalized(Product product, string price) returns ProductLocalized {
    return {
        id: product.id,
        categories: product.categories,
        description: product.description,
        name: product.name,
        picture: product.picture,
        price_usd: product.price_usd,
        price: price
    };
}
