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

import ballerina/lang.'boolean;

function testFromString(string s, boolean|error expected) {
    assert(expected, 'boolean:fromString(s));
}

// Util functions

function assert(boolean|error expected, boolean|error actual) {
    if (expected is boolean && actual is boolean) {
        if (expected != actual) {
            string reason = "expected [" + expected.toString() + "] , but found [" + actual.toString() + "]";
            error e = error(reason);
            panic e;
        }

        return;
    }
    if (expected != actual) {
        typedesc<anydata|error> expT = typeof expected;
        typedesc<anydata|error> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}
