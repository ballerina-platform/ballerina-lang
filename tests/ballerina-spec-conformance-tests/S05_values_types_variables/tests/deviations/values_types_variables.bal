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

// However, for other types of value, what is stored in the variable or member is a 
// reference to the value; the value itself has its own separate storage.
@test:Config {
    groups: ["deviation"]
}
function testNonSimpleValuesStoredInStructuresBroken() {
    table<QuuzRecord> t1 = table{};
    QuuzRecord b4 = { quuzFieldOne: 100 };
    error? err = t1.add(b4);
    if err is error {
        test:assertFail(msg = "failed in adding record to table");
    }
    b4.quuzFieldOne = I;
    
    QuuzRecord b5;
    foreach QuuzRecord barRecord in t1 {
        b5 = barRecord;
    }
    // test:assertEquals(b5.quuzFieldOne, I, msg = "expected table member to have been updated");
    test:assertEquals(b5.quuzFieldOne, 100, msg = "expected table member to not have been updated");
}

// References make it possible for distinct members of a structure to refer to values that are
// identical, in the sense that they are stored in the same location.
@test:Config {
    groups: ["deviation"]
}
function testDistinctStructureMembersReferringToSameValueBroken() {
    table<QuuzRecord> t1 = table{};
    QuuzRecord b4 = { quuzFieldOne: 100 };
    QuuzRecord b5 = { quuzFieldOne: 200 };
    error? err1 = t1.add(b4);
    error? err2 = t1.add(b5);
    error? err3 = t1.add(b4);
    if err1 is error {
        test:assertFail(msg = "failed in adding record to table");
    }
    if err2 is error {
        test:assertFail(msg = "failed in adding record to table");
    }
    if err3 is error {
        test:assertFail(msg = "failed in adding record to table");
    }
    QuuzRecord? b6 = ();
    QuuzRecord? b7 = ();
    int count = 0;
    foreach QuuzRecord barRecord in t1 {
        if (count == 0) {
            b6 = barRecord;
        }
        if (count == 1) {
            b7 = barRecord;
        }
        count += 1;
    }
    test:assertTrue(b6 is QuuzRecord, msg = "expected values to be at the same location");
    // test:assertTrue(b6 === b7, msg = "expected values to be at the same location");
    test:assertFalse(b6 === b7, msg = "expected values to not be at the same location");
}

// Most basic types of structured values (along with one basic type of simple value) are
// iterable, meaning that a value of the type can be accessed as a sequence of simpler values.
// TODO: Support iterable for Tuple type and String type
// https://github.com/ballerina-platform/ballerina-lang/issues/13172
@test:Config {
    groups: ["deviation"]
}
function testIterableTypesBroken() {
    //    string iterableString = "Hello Ballerina";
    //    string result = "";
    //    foreach var char in iterableString {
    //        // TODO
    //    }
}

// TODO: Need to fix https://github.com/ballerina-platform/ballerina-lang/issues/13726 to enable this
@test:Config {
    groups: ["deviation"]
}
function testImplicitInitialValueOfTypedesc() {
    typedesc[] typedescArray = [];
    typedescArray[1] = int;
    //typedesc expectedTypedesc = ();
    //test:assertEquals(typedescArray[0], expectedTypedesc, msg = "expected implicit initial value of typedesc to be ()");
}

public type QuxObject object {
    public string fooFieldOne;

    public function __init() {
        self.fooFieldOne = "init value";
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};

// Implicit initial values for objects not yet supported. Decided to fix this with jBallerina.
// https://github.com/ballerina-platform/ballerina-lang/issues/13728
@test:Config {
    groups: ["deviation"]
}
function testImplicitInitialValueOfObjects() {
    QuxObject[] objArray = [];
    objArray[1] = new QuxObject();
    QuxObject expectedObject = new;
    //test:assertEquals(objArray[0].fooFieldOne, expectedObject.fooFieldOne,
    //    msg = "expected implicit initial value of QuxObject should be '{fooFieldOne:\"init value\"}'");
}
