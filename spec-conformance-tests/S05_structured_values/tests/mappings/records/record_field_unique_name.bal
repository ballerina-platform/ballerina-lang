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

// A mapping value is a container where each member has a key, which is a string, that
// uniquely identifies within the mapping. We use the term field to mean the member together
// its key; the name of the field is the key, and the value of the field is that value of the member;
// no two fields in a mapping value can have the same name.
@test:Config {}
function testRecordFieldUniqueName() {
    float newFloatVal = 2.0;
    BazRecordTwelve b1 = { bazFieldOne: 1.0 };
    b1.bazFieldOne = newFloatVal;
    test:assertEquals(b1.length(), 1, msg = "expected the record to have one field");
    test:assertEquals(b1.bazFieldOne, newFloatVal, msg = "expected the field to have the updated value");

    string stringVal = "test string 1";
    int integerVal = 1;

    b1.bazFieldTwo = stringVal;
    test:assertEquals(b1.length(), 2, msg = "expected the record to have two fields");
    test:assertEquals(b1.bazFieldOne, newFloatVal, msg = "expected the field to have the same value");
    test:assertEquals(b1.bazFieldTwo, stringVal, msg = "expected a field with the newly added value");

    b1.bazFieldTwo = integerVal;
    test:assertEquals(b1.length(), 2, msg = "expected the record to have two fields");
    test:assertEquals(b1.bazFieldOne, newFloatVal, msg = "expected the field to have the same value");
    test:assertEquals(b1.bazFieldTwo, integerVal, msg = "expected the field to have the updated value");
}

public type BazRecordTwelve record {
    float bazFieldOne;
};
