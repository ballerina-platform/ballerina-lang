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

const OBJECT_INHERENT_TYPE_VIOLATION_REASON = "{ballerina/lang.object}InherentTypeViolation";
const MAPPING_INHERENT_TYPE_VIOLATION_REASON = "{ballerina/lang.map}InherentTypeViolation";

type Details record {|
    string name;
    int yob;
|};

type Department record {
    string name = "IT";
};

readonly class Employee {
    Details details;
    Department dept;
    int id;

    function init(Details & readonly details, Department & readonly dept, int id) {
        self.details = details;
        self.dept = dept;
        self.id = id;
    }

    function getId() returns int {
        return self.id;
    }
}

function testBasicReadOnlyObject() {
    Employee emp = new({name: "Jo", yob: 2000}, {}, 1234);

    any anyVal = emp;
    assertTrue(anyVal is readonly);
    assertTrue(anyVal is Employee);

    object {
        Details details;
        Department dept;
        int id;
    } obj = emp;

    assertTrue(<any> obj.details is readonly);
    assertTrue(obj.details is Details & readonly);
    assertEquality(<Details> {name: "Jo", yob: 2000}, obj.details);

    assertTrue(<any> obj.dept is readonly);
    assertTrue(obj.dept is Department & readonly);
    assertEquality(<Department> {name: "IT"}, obj.dept);

    assertEquality(1234, obj.id);
}

function testInvalidReadOnlyObjectUpdateAtRuntime() {
    Employee emp = new({name: "Jo", yob: 2000}, {}, 1234);

    object {
        Details details;
        Department dept;
        int id;
    } obj = emp;

    var fn = function () {
        obj.details.name = "May";
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(MAPPING_INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("cannot update 'readonly' field 'name' in record of type '(Details & readonly)'",
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

type Controller object {
    function getValue() returns int;
};

type Config record {|
    Controller & readonly c1;
    Controller c2;
    Controller c3;
|};

readonly class DefaultController {
    final string id = "default";
    map<int>? & readonly mp = ();

    function getValue() returns int {
        return 0;
    }
}

readonly class CustomController {
    string id;

    function init(string id) {
        self.id = id;
    }

    function getValue() returns int {
        return 120;
    }
}

class MutableController {
    string id = "mutable";

    function getValue() returns int {
        return 200;
    }
}

function testReadOnlyObjectsForImmutableIntersections1() {
    Controller c2 = new CustomController("immutable");
    Config cn = {c1: new DefaultController(), c2, c3: new MutableController()};

    assertTrue(<any> cn.c1 is Controller);
    assertTrue(<any> cn.c1 is Controller & readonly);
    assertTrue(<any> cn.c2 is Controller);
    assertTrue(cn.c2 is Controller & readonly);
    assertTrue(<any> cn.c3 is Controller);
    assertFalse(cn.c3 is Controller & readonly);

    DefaultController v1 = <DefaultController> cn.c1;
    CustomController v2 = <CustomController> cn.c2;
    MutableController v3 = <MutableController> cn.c3;

    assertEquality("default", v1.id);
    assertTrue(<any> v1.mp is map<int>? & readonly);
    assertEquality((), v1.mp);

    assertEquality("immutable", v2.id);
    assertEquality("mutable", v3.id);
    v3.id = "definitely mutable";
    assertEquality("definitely mutable", v3.id);
}

function testReadOnlyObjectsForImmutableIntersections2() {
    CustomController c2 = new CustomController("immutable");
    Config & readonly cn = {c1: new DefaultController(), c2, c3: new CustomController("other immutable")};

    assertTrue(<any> cn.c1 is Controller);
    assertTrue(<any> cn.c1 is Controller & readonly);
    assertTrue(<any> cn.c2 is Controller);
    assertTrue(<any>cn.c2 is Controller & readonly);
    assertTrue(<any> cn.c3 is Controller);
    assertTrue(<any>cn.c3 is Controller & readonly);

    DefaultController v1 = <DefaultController> cn.c1;
    CustomController v2 = <CustomController> cn.c2;
    CustomController v3 = <CustomController> cn.c3;

    assertEquality("default", v1.id);
    assertTrue(<any> v1.mp is map<int>? & readonly);
    assertEquality((), v1.mp);

    assertEquality("immutable", v2.id);
    assertEquality("other immutable", v3.id);
}

service readonly class ReadOnlyServiceClass {
    int[] y;

    function init(int[] & readonly y) {
        self.y = y;
    }

    resource function get bar() {

    }
}

function testReadOnlyServiceClass() {
    ReadOnlyServiceClass readonlyService = new ReadOnlyServiceClass([1, 2, 3]);
    assertTrue(<any> readonlyService is readonly & service object {int[] y;});

    object {int[] y;} ob = readonlyService;
    var fn = function () {
        ob.y[0] = 1;
    };
    error? res = trap fn();
    assertTrue(res is error);
    error err = <error> res;
    assertEquality("{ballerina/lang.array}InvalidUpdate", err.message());
    assertEquality("modification not allowed on readonly value", err.detail()["message"]);

    fn = function () {
        ob.y = [1, 2];
    };
    res = trap fn();
    assertTrue(res is error);
    err = <error> res;
    assertEquality(OBJECT_INHERENT_TYPE_VIOLATION_REASON, err.message());
    assertEquality("modification not allowed on readonly value", err.detail()["message"]);
}

public class Foo {
    int i = 0;

    public isolated function func() {

    }
}

public readonly class Bar {
    int i = 1;

    function func() {

    }
}

public readonly class Baz {
    int i = 2;

    public function func() {

    }
}

Bar bar = new Bar();

function testReadOnlyClassIntersectionWithMismatchedQualifiersRuntimeNegative() {
    Baz baz = new;
    assertFalse(<any> bar is Foo & readonly);
    assertFalse(<any> baz is Foo & readonly);
}

public readonly class Qux {
    int bits;

    isolated function init(int bits) {
        self.bits = bits;
    }

    public isolated function isEmpty() returns boolean {
        return self.bits == 0;
    }
}

readonly class Quux {
    Qux t;

    function init(Qux t) {
        self.t = t;
    }

    function getQuxBits() returns int {
        return getBits(self.t);
    }
}

public readonly class Corge {
    int bits = 9876;

    public isolated function isEmpty() returns boolean => false;
}

isolated function getBits(Qux t) returns int {
    return t.bits;
}

public function testReadOnlyClassIntersectionWithValidQualifiers() {
    Quux quux = new (new (1234));
    assertEquality(1234, quux.getQuxBits());

    Qux & readonly qux = new Corge();
    assertEquality(9876, getBits(qux));
    assertEquality(9876, getBits(new Corge()));
}

type Obj readonly & object {
    function foo(Obj t) returns Obj;
};

public function testRecursiveObjectArrayReadonlyClone() {
   Obj[] v = [];
   any x = v.cloneReadOnly();
   assertTrue(x is readonly & Obj[]);
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

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
