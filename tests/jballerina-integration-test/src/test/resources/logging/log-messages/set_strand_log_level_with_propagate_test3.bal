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

public function main() {
    log:setStrandLogLevel(log:ERROR, true);
    log:printError("Logging error log from inside main function");
    log:printWarn("Logging warn log from inside main function");
    log:printInfo("Logging info log from inside main function");
    log:printDebug("Logging debug log from inside main function");
    log:printTrace("Logging trace log from inside main function");
    foo();
}

function foo() {
    worker w1 {
        log:setStrandLogLevel(log:WARN, true);
        log:printError("Logging error log from inside foo function - worker w1");
        log:printWarn("Logging warn log from inside foo function - worker w1");
        log:printInfo("Logging info log from inside foo function - worker w1");
        log:printDebug("Logging debug log from inside foo function - worker w1");
        log:printTrace("Logging trace log from inside foo function - worker w1");
        bar();
    }
    log:printError("Logging error log from inside foo function");
    log:printWarn("Logging warn log from inside foo function");
    log:printInfo("Logging info log from inside foo function");
    log:printDebug("Logging debug log from inside foo function");
    log:printTrace("Logging trace log from inside foo function");
}

function bar() {
    worker w2 {
        log:printError("Logging error log from inside bar function - worker w2");
        log:printWarn("Logging warn log from inside bar function - worker w2");
        log:printInfo("Logging info log from inside bar function - worker w2");
        log:printDebug("Logging debug log from inside bar function - worker w2");
        log:printTrace("Logging trace log from inside bar function - worker w2");
    }
    log:printError("Logging error log from inside bar function");
    log:printWarn("Logging warn log from inside bar function");
    log:printInfo("Logging info log from inside bar function");
    log:printDebug("Logging debug log from inside bar function");
    log:printTrace("Logging trace log from inside bar function");
}
