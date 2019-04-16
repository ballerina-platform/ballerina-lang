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

// nil-type-descriptor := ()
// nil-literal := () | null
@test:Config {}
function testNilLiteral() {
    () nil = ();
    () nil2;
    nil2 = ();
    if (nil != nil2) {
        test:assertFail(msg = "expected nil values to be equal");
    }
}

// The nil value can also be written null, for
// compatibility with JSON; the use of null should be restricted to JSON-related contexts.
@test:Config {}
function testNullLiteralWithJson() {
    json nil = null;
    json nil2;
    nil2 = null;
    if (nil != nil2) {
        test:assertFail(msg = "expected json null/nil values to be equal");
    }

    json nil3 = ();
    json nil4;
    nil4 = ();
    if (nil3 != nil4) {
        test:assertFail(msg = "expected json null/nil values to be equal");
    }

    json nil5 = null;
    json nil6 = ();
    if (nil5 != nil6) {
        test:assertFail(msg = "expected json null/nil values to be equal");
    }
}
