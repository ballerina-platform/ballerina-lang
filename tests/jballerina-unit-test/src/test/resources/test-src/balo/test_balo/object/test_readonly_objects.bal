// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import testorg/readonly_objects as ro;

const OBJECT_INHERENT_TYPE_VIOLATION_REASON = "{ballerina/lang.object}InherentTypeViolation";
const MAPPING_INHERENT_TYPE_VIOLATION_REASON = "{ballerina/lang.map}InherentTypeViolation";

function testReadonlyObjects() {
    testBasicReadOnlyObject();
    testInvalidReadOnlyObjectUpdateAtRuntime();
    testReadOnlyObjectsForImmutableIntersections1();
    testReadOnlyObjectsForImmutableIntersections2();
}

function testBasicReadOnlyObject() {
    ro:Employee emp = new({name: "Jo", yob: 2000}, {}, 1234);

    any anyVal = emp;
    assertTrue(anyVal is readonly);
    assertTrue(anyVal is ro:Employee);

    object {
        public ro:Details details;
        public ro:Department dept;
        public int id;
    } obj = emp;

    assertTrue(<any> obj.details is readonly);
    assertTrue(obj.details is ro:Details & readonly);
    assertEquality(<ro:Details> {name: "Jo", yob: 2000}, obj.details);

    assertTrue(<any> obj.dept is readonly);
    assertTrue(obj.dept is ro:Department & readonly);
    assertEquality(<ro:Department> {name: "IT"}, obj.dept);

    assertEquality(1234, obj.id);
}

function testInvalidReadOnlyObjectUpdateAtRuntime() {
    ro:Employee emp = new({name: "Jo", yob: 2000}, {}, 1234);

    object {
        public ro:Details details;
        public ro:Department dept;
        public int id;
    } obj = emp;

    var fn = function () {
        obj.details.name = "May";
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(MAPPING_INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'name' in record of type 'readonly_objects:(testorg/readonly_objects:1.0.0:Details & readonly)'",
                   err.detail()["message"]);

    fn = function () {
        obj.dept = {name: "finance"};
    };
    res = trap fn();
    assertTrue(res is error);

    err = <error> res;
    assertEquality(OBJECT_INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("modification not allowed on readonly value", err.detail()["message"]);

    fn = function () {
        obj.id = 123;
    };
    res = trap fn();
    assertTrue(res is error);

    err = <error> res;
    assertEquality(OBJECT_INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("modification not allowed on readonly value", err.detail()["message"]);
}

function testReadOnlyObjectsForImmutableIntersections1() {
    ro:Controller c2 = new ro:CustomController("immutable");
    ro:Config cn = {c1: new ro:DefaultController(), c2, c3: new ro:MutableController()};

    assertTrue(<any> cn.c1 is ro:Controller);
    assertTrue(<any> cn.c1 is ro:Controller & readonly);
    assertTrue(<any> cn.c2 is ro:Controller);
    assertTrue(cn.c2 is ro:Controller & readonly);
    assertTrue(<any> cn.c3 is ro:Controller);
    assertFalse(cn.c3 is ro:Controller & readonly);

    ro:DefaultController v1 = <ro:DefaultController> cn.c1;
    ro:CustomController v2 = <ro:CustomController> cn.c2;
    ro:MutableController v3 = <ro:MutableController> cn.c3;

    assertEquality("default", v1.id);
    assertTrue(<any> v1.mp is map<int>? & readonly);
    assertEquality((), v1.mp);

    assertEquality("immutable", v2.id);
    assertEquality("mutable", v3.id);
    v3.id = "definitely mutable";
    assertEquality("definitely mutable", v3.id);
}

function testReadOnlyObjectsForImmutableIntersections2() {
    ro:CustomController c2 = new ro:CustomController("immutable");
    ro:Config & readonly cn = {c1: new ro:DefaultController(), c2, c3: new ro:CustomController("other immutable")};

    assertTrue(<any> cn.c1 is ro:Controller);
    assertTrue(<any> cn.c1 is ro:Controller & readonly);
    assertTrue(<any> cn.c2 is ro:Controller);
    assertTrue(<any>cn.c2 is ro:Controller & readonly);
    assertTrue(<any> cn.c3 is ro:Controller);
    assertTrue(<any>cn.c3 is ro:Controller & readonly);

    ro:DefaultController v1 = <ro:DefaultController> cn.c1;
    ro:CustomController v2 = <ro:CustomController> cn.c2;
    ro:CustomController v3 = <ro:CustomController> cn.c3;

    assertEquality("default", v1.id);
    assertTrue(<any> v1.mp is map<int>? & readonly);
    assertEquality((), v1.mp);

    assertEquality("immutable", v2.id);
    assertEquality("other immutable", v3.id);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
