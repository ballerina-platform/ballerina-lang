// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

xmlns "http://sample.com/wso2/a1" as ns;

const ASSERTION_ERROR_REASON = "AssertionError";

final boolean serviceEPAvailable = false;
final string LOCK_STORE = "lockStore";

int name = 10;

type Person record {
    string name = "Person";
    int age = 20;
};

class Employee {
    string name = "Employee";

    function getName() returns string {
        string name = "Name in object";
        return name;
    }
}

function testLocalVarScope() returns string {
    string name = "John Doe";
    return name;
}

function testObjMethodScope() returns string {
    Employee e = new;
    return e.getName();
}

function testRecordScope() returns Person {
    Person p = {};
    return p;
}

function testBlockScope1() returns [string, string] {
    [string, string] result = ["", ""];

    if (true) {
        string name = "Inside first if block";
        result[0] = name;
    }

    if (true) {
        string name = "Inside second if block";
        result[1] = name;
    }

    return result;
}

function testBlockScope2() returns string {
    if (false) {
        string name = "Inside if block";
        return name;
    } else {
        string name = "Inside else block";
        return name;
    }
}

function testLambdaFunctions() returns string {
    var fn = function () returns string {
        string name = "Inside a lambda function";
        return name;
    };

    return fn();
}

function testFunctionParam(string name) returns string {
    return name;
}

function testNestedBlocks() returns string {
    if (true) {
        if (false) {
            string s = "nested if";
        } else {
            string s = "nested else";
        }

        string s = "var after nested if-else";
        return s;
    }

    return "";
}

function testNamespaces1() returns xml {
    xmlns "http://sample.com/wso2/a2" as ns;
    return xml `<ns:greeting>Hello World!</ns:greeting>`;
}

function testNamespaces2(boolean x) returns xml {
    if (x) {
        xmlns "http://sample.com/wso2/a2" as ns;
        return xml `<ns:greeting>Hello World!</ns:greeting>`;
    } else {
        xmlns "http://sample.com/wso2/a3" as ns;
        return xml `<ns:greeting>Hello World!</ns:greeting>`;
    }
}

function testTypeNameAsVariable1() {
    any Employee = "Smith"; // name of the object 'Employee'

    if (Employee is string && Employee == "Smith") {
        return;
    }
    var t = typeof Employee;
    panic error(ASSERTION_ERROR_REASON, message = "expected 'string', found '" + t.toString() + "'");
}

function testTypeNameAsVariable2() {
    any Person = "Smith"; // name of the record 'Person'

    if (Person is string && Person == "Smith") {
        return;
    }
    var t = typeof Person;
    panic error(ASSERTION_ERROR_REASON, message = "expected 'string', found '" + t.toString() + "'");
}

function testTypeNameAsVariable3() {
    any temp = "Temp var";
    if (temp is string) {
        any Person = "Jack"; // name of the record 'Person'

        if (Person is string && Person == "Jack") {
            return;
        }
        var t = typeof Person;
        panic error(ASSERTION_ERROR_REASON, message = "expected 'string', found '" + t.toString() + "'");
    }
}

function testTypeNameAsVariable4() {
    foreach var i in 1...2 {
        any Person = "Jack"; // name of the record 'Person'

        if (Person is string && Person == "Jack") {
            return;
        }
        var t = typeof Person;
        panic error(ASSERTION_ERROR_REASON, message = "expected 'string', found '" + t.toString() + "'");
    }
}

type Person1 record {
    int id = 0;
};

class Student {
    string Student = "Smith";

    public function getName() returns string {
         return self.Student;
    }
}

function testTypeNameAsVariable5() {
    Student s = new;
    any name = s.getName();
    if (name is string && name == "Smith") {
        return;
    }
    var t = typeof name;
    panic error(ASSERTION_ERROR_REASON, message = "expected 'string', found '" + t.toString() + "'");
}

function testGeneratedNames() {
    test:assertFalse(serviceEPAvailable);
    test:assertEquals(LOCK_STORE, "lockStore");
}
