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
function testMapFieldUniqueName() {
    string key = "intTwo";
    map<int> m1 = { intOne: 1 };
    m1[key] = 2;

    int originalFieldCount = m1.length();
    int newVal = 3;
    m1[key] = newVal;
    test:assertEquals(m1[key], newVal, msg = "expected field value to be the newly updated value");
    test:assertEquals(m1.length(), originalFieldCount, msg = "expected field count to remain unchanged");
}

// The shape of a mapping value is an unordered collection of field shapes one for each field.
// The field shape for a field f has a name, which is the same as the name of f, and a shape,
// which is the shape of the value of f.
@test:Config {}
function testMapFieldShape() {
    map<float|string> m1 = { bazFieldTwo: "test string 1", bazFieldOne: 1.0 };
    var conversionResult = utils:BazRecordTwo.convert(m1);
    test:assertTrue(conversionResult is utils:BazRecordTwo, msg = "expected conversion to succeed");

    // change the value's shape
    m1.bazFieldTwo = 1.0;
    conversionResult = utils:BazRecordTwo.convert(m1);
    test:assertTrue(conversionResult is error, msg = "expected conversion to fail");
    utils:assertErrorReason(conversionResult, "{ballerina}StampError", 
                            "invalid reason on conversion failure due to shape mismatch");

    // remove a required field and add a new field with a different name
    boolean removalStatus = m1.remove("bazFieldTwo");
    test:assertTrue(removalStatus, msg = "expected removal to succeed");
    m1.bazFieldThree = "test string 3";
    conversionResult = utils:BazRecordTwo.convert(m1);
    test:assertTrue(conversionResult is error, msg = "expected conversion to fail");
    utils:assertErrorReason(conversionResult, "{ballerina}StampError", 
                            "invalid reason on conversion failure due to shape mismatch");
}

// A mapping is iterable as sequence of fields, where each field is represented by a 2-tuple (s,
// val) where s is a string for the name of a field, and val is the value of the field. The order
// of the fields in the sequence is implementation-dependent, but implementations are
// encouraged to preserve and use the order in which the fields were added.
@test:Config {}
function testMapIteration() {
    map<string|float|int> m1 = { fieldOne: "valueOne", fieldTwo: 2.0, fieldThree: 3 };
    m1.fieldFour = 4;

    string result = "";
    foreach (string, string|float|int) (key, value) in m1 {
        result += <string> value;
    }
    test:assertEquals(result, "valueOne2.034", msg = "expected iteration over all members in added order");
}

// The inherent type of a mapping value must be a mapping-type-descriptor. The
// inherent type of a mapping value determines a type Tf
// for the value of the field with name f.
// The runtime system will enforce a constraint that a value written to field f will belong to type
// Tf. Note that the constraint is not merely that the value looks like Tf.
@test:Config {}
function testMapInherentTypeViolation() {
    map<string> m1 = { one: "test string 1", two: "test string 2" };
    utils:assertErrorReason(trap utils:insertMemberToMap(m1, "three", 3), "{ballerina}InherentTypeViolation", 
                            "invalid reason on inherent type violating map insertion");

    map<map<string>> m2 = { one: { strOne: "test string 1", strTwo: "test string 2" } };
    // `m3` looks like `map<string>`
    map<string|int> m3 = { one: "test string 1", two: "test string 2" };
    utils:assertErrorReason(trap utils:insertMemberToMap(m2, "two", m3),
                    "{ballerina}InherentTypeViolation", 
                    "invalid reason on inherent type violating map insertion");
}
