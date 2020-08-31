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

import ballerina/log;

function isNoCacheSet(RequestCacheControl? reqCC, ResponseCacheControl? resCC) returns boolean {
    if (reqCC is RequestCacheControl && reqCC.noCache) {
        return true;
    }

    if (resCC is ResponseCacheControl && resCC.noCache) {
        return true;
    }

    return false;
}

function updateResponseTimestamps(Response response, int requestedTime, int receivedTime) {
    response.requestTime = requestedTime;
    response.receivedTime = receivedTime;
}

function setAgeHeader(Response cachedResponse) {
    cachedResponse.setHeader(AGE, <@untainted>calculateCurrentResponseAge(cachedResponse).toString());
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function updateResponse(Response cachedResponse, Response validationResponse) {
    // 1 - delete warning headers with warn codes 1xx
    // 2 - retain warning headers with warn codes 2xx
    // 3 - use other headers in validation response to replace corresponding headers in cached response
    retain2xxWarnings(cachedResponse);
    replaceHeaders(cachedResponse, validationResponse);
}

function retain2xxWarnings(Response cachedResponse) {
    if (cachedResponse.hasHeader(WARNING)) {
        string[] warningHeaders = <@untainted>cachedResponse.getHeaders(WARNING);
        cachedResponse.removeHeader(WARNING);
        // TODO: Need to handle this in a better way using regex when the required regex APIs are there
        foreach var warningHeader in warningHeaders {
            if (warningHeader.indexOf("214") is int || warningHeader.indexOf("299") is int) {
                log:printDebug(() => "Adding warning header: " + warningHeader);
                cachedResponse.addHeader(WARNING, warningHeader);
                continue;
            }
        }
    }
}

// Based on https://tools.ietf.org/html/rfc7234#section-4.3.4
function replaceHeaders(Response cachedResponse, Response validationResponse) {
    string[] headerNames = <@untainted>validationResponse.getHeaderNames();

    log:printDebug("Updating response headers using validation response.");

    foreach var headerName in headerNames {
        cachedResponse.removeHeader(headerName);
        string[] headerValues = <@untainted>validationResponse.getHeaders(headerName);
        foreach var value in headerValues {
            cachedResponse.addHeader(headerName, value);
        }
    }
}
