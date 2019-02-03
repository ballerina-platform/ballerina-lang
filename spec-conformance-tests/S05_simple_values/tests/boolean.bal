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

// boolean-type-descriptor := boolean
// boolean-literal := true | false
@test:Config {}
function testBoolean() {
    boolean b1 = true;
    boolean b2 = true;
    boolean b3 = false;
    boolean b4 = false;

    test:assertTrue(b1, msg = "expected boolean to be true");
    test:assertFalse(b3, msg = "expected boolean to be false");
    test:assertEquals(b1, b2, msg = "expected boolean values to be equal");
    test:assertEquals(b3, b4, msg = "expected boolean values to be equal");
    test:assertNotEquals(b1, b3, msg = "expected boolean values to not be equal");
}
