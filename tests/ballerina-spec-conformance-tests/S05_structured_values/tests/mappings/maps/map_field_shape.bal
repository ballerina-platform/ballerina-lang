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

// The shape of a mapping value is an unordered collection of field shapes one for each field.
// The field shape for a field f has a name, which is the same as the name of f, and a shape,
// which is the shape of the value of f.
@test:Config {}
function testMapFieldShape() {
    map<float|string> m1 = { bazFieldTwo: "test string 1", bazFieldOne: 1.0 };
    var conversionResult = BazRecordTen.convert(m1);
    test:assertTrue(conversionResult is BazRecordTen, msg = "expected conversion to succeed");

    // change the value's shape
    m1.bazFieldTwo = 1.0;
    conversionResult = BazRecordTen.convert(m1);
    if (conversionResult is error) {
        test:assertEquals(conversionResult.reason(), "{ballerina}ConversionError",
            msg = "invalid reason on conversion failure due to shape mismatch");
    } else {
        test:assertFail(msg = "expected conversion to fail");
    }

    // remove a required field and add a new field with a different name
    boolean removalStatus = m1.remove("bazFieldTwo");
    test:assertTrue(removalStatus, msg = "expected removal to succeed");
    m1.bazFieldThree = "test string 3";
    conversionResult = BazRecordTen.convert(m1);
    if (conversionResult is error) {
        test:assertEquals(conversionResult.reason(), "{ballerina}ConversionError",
            msg = "invalid reason on conversion failure due to shape mismatch");
    } else {
        test:assertFail(msg = "expected conversion to fail");
    }
}

public type BazRecordTen record {
    float bazFieldOne;
    string bazFieldTwo;
};
