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

import ballerina/lang.'int;
import ballerina/log;
import ballerina/time;

// Based on https://tools.ietf.org/html/rfc7234#section-4.2.3
function calculateCurrentResponseAge(Response cachedResponse) returns @tainted int {
    int ageValue = getResponseAge(cachedResponse);
    int dateValue = getDateValue(cachedResponse);
    int now = time:currentTime().time;
    int responseTime = cachedResponse.receivedTime;
    int requestTime = cachedResponse.requestTime;

    int apparentAge = (responseTime - dateValue) >= 0 ? (responseTime - dateValue) : 0;

    int responseDelay = responseTime - requestTime;
    int correctedAgeValue = ageValue + responseDelay;

    int correctedInitialAge = apparentAge > correctedAgeValue ? apparentAge : correctedAgeValue;
    int residentTime = now - responseTime;

    return (correctedInitialAge + residentTime) / 1000;
}

function getResponseAge(Response cachedResponse) returns @tainted int {
    if (!cachedResponse.hasHeader(AGE)) {
        return 0;
    }

    string ageHeaderString = cachedResponse.getHeader(AGE);
    var ageValue = 'int:fromString(ageHeaderString);

    return (ageValue is int) ? ageValue : 0;
}

function getDateValue(Response inboundResponse) returns int {
    if (inboundResponse.hasHeader(DATE)) {
        string dateHeader = inboundResponse.getHeader(DATE); // TODO: May need to handle invalid date headers
        var dateHeaderTime = time:parse(dateHeader, time:TIME_FORMAT_RFC_1123);
        return (dateHeaderTime is time:Time) ? dateHeaderTime.time : 0;
    }

    log:printDebug("Date header not found. Using current time for the Date header.");

    // Based on https://tools.ietf.org/html/rfc7231#section-7.1.1.2
    time:Time currentT = time:currentTime();
    string timeStr = <string>time:format(currentT, time:TIME_FORMAT_RFC_1123);

    inboundResponse.setHeader(DATE, timeStr);
    return currentT.time;
}
