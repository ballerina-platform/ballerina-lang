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

// Note also that T[n] is a subtype of T[], and that if S is a subtype of T, then S[] is a
// subtype of T[]; this is a consequence of the definition of subtyping in terms of subset
// inclusion of the corresponding sets of shapes.
@test:Config {}
function testArraySubType() {
    int[5] fixedArray = [1, 2, 3, 4, 5];
    any tempArray = fixedArray;
    test:assertTrue(tempArray is int[], msg = "expected fixed length array to be subtype of growing array");

    BazRecordSix bRecordTwo = { bazFieldOne: 12.0, bazFieldTwo: "fieldTwo" };
    any tempRecord = bRecordTwo;
    test:assertTrue(tempRecord is BazRecordFive, msg = "expected BazRecordTwo to be subtype of BazRecord");

    BazRecordSix[] bRecordTwoArray = [bRecordTwo];
    any tempRecordArray = bRecordTwoArray;
    test:assertTrue(tempRecordArray is BazRecordFive[],
        msg = "expected BazRecordTwo[] to be subtype of BazRecord[]");
}

public type BazRecordFive record {
    float bazFieldOne;
};

public type BazRecordSix record {
    float bazFieldOne;
    string bazFieldTwo;
};
