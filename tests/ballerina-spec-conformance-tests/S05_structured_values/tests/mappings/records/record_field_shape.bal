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

const CONVERSION_ERROR_REASON = "{ballerina}ConversionError";

// The shape of a mapping value is an unordered collection of field shapes one for each field.
// The field shape for a field f has a name, which is the same as the name of f, and a shape,
// which is the shape of the value of f.
@test:Config {}
function testRecordFieldShape() {
    DefaultOpenRecord r1 = { bazFieldTwo: "test string 1", bazFieldOne: 1.0 };
    var conversionResult = BazRecordEleven.convert(r1);
    test:assertTrue(conversionResult is BazRecordEleven, msg = "expected conversion to succeed");

    // change the value's shape
    r1.bazFieldTwo = 1.0;
    conversionResult = BazRecordEleven.convert(r1);
    if (conversionResult is error) {
        test:assertEquals(conversionResult.reason(), CONVERSION_ERROR_REASON,
            msg = "invalid reason on conversion failure due to shape mismatch");
    } else {
        test:assertFail(msg = "expected conversion to fail");
    }

    // create a record without a required field,
    // but with a new field with a different name and matching value shape
    DefaultOpenRecord r2 = { bazFieldThree: "test string 3", bazFieldOne: 1.0 };
    conversionResult = BazRecordEleven.convert(r2);
    if (conversionResult is error) {
        test:assertEquals(conversionResult.reason(), CONVERSION_ERROR_REASON,
            msg = "invalid reason on conversion failure due to shape mismatch");
    } else {
        test:assertFail(msg = "expected conversion to fail");
    }
}

public type DefaultOpenRecord record {
};

public type BazRecordEleven record {
    float bazFieldOne;
    string bazFieldTwo;
};
