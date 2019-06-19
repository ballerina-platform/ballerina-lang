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

// Both kinds of type descriptor are covariant in the types of their members.

public type ClosedRecordTwo record {|
    string|int fieldOne;
    boolean|float fieldTwo;
|};

public type ClosedRecordThree record {|
    float fieldTwo;
    string fieldOne;
|};

public type ClosedRecordFour record {|
    boolean fieldOne;
    string fieldTwo;
|};

@test:Config {}
function testRecordCovariance() {
    any r = <ClosedRecordThree>{ fieldOne: s1, fieldTwo: 100.0 };
    test:assertTrue(r is ClosedRecordTwo, msg = "expected record to be identified as a subtype");

    r = <ClosedRecordFour>{ fieldOne: false, fieldTwo: s1 };
    test:assertTrue(!(r is ClosedRecordTwo), msg = "expected record to not be identified as a subtype");
}
