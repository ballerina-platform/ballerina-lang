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

type USER_DEF_TYPE_ONE "hi"|1;
type USER_DEF_TYPE_TWO "hi"|1|2.0;

// A type S is a subtype of type T if the set of shapes denoted by S is a subset of the set of shapes denoted by T.
@test:Config {}
function testSubtype() {
    USER_DEF_TYPE_ONE u1 = "hi";
    any a = u1;
    test:assertTrue(a is USER_DEF_TYPE_TWO,
                    msg = "expected value's type to be identified as a subtype");
    test:assertTrue(a is string|int,
                    msg = "expected value's type to be identified as a subtype");
}
