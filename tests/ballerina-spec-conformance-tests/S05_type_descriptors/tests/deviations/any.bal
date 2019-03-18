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

import ballerina/test;
import utils;

// A value belongs to the any type if and only if its basic type is not error. 
// TODO: disallow adding an error to a map<any> via a function with type-widening params
// https://github.com/ballerina-platform/ballerina-lang/issues/13230
@test:Config {
    groups: ["deviation"]
}
function testErrorExclusionInAnyBroken() {
    map<any> m1 = { one: a, two: b };
    // map<any|error> m2 = m1;
    // utils:assertPanic(function() { m2["three"] = errValOne; },
    //                   "{ballerina}InherentTypeViolation",
    //                   "invalid reason on trying to use error as any");
}
