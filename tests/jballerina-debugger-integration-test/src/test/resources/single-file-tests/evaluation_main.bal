// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'int;
import ballerina/lang.'float as langFloat;

type GradStudent record {
    string firstName;
    string lastName;
    int intakeYear;
    int deptId;
};

type Department record {
    int deptId;
    string deptName;
};

type StudentReport record {
    string name;
    string deptName;
    string degree;
    int intakeYear;
};

type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};

type CustomerTable table<Customer> key(id, name);

type CustomerKeyLessTable table<Customer>;

type CustomerValue record {|
    Customer value;
|};

type Student record {
    string firstName;
    string lastName;
    int intakeYear;
    float score;
};

type Report record {
    string name;
    string degree;
    int expectedGradYear;
};

function calGraduationYear(int year) returns int {
    return year + 5;
}

type FullName record {|
    string firstName;
    string lastName;
|};

type '\ \/\:\@\[\`\{\~\u{03C0}_123_ƮέŞŢ_Student record {
    string '1st_name;
    int 'Ȧɢέ_\ \/\:\@\[\`\{\~\u{03C0};
    Grades grades;
};

type Address record {|
    string city;
    string country;
|};

type Grades record {|
    int maths;
    int physics;
    int chemistry;
    int...;
|};

type Employee record {
    readonly int id;
    string name;
    float salary;
};

type EmployeeTable table<Employee> key(id);

class 'Person_\ \/\<\>\:\@\[\`\{\~\u{03C0}_ƮέŞŢ {
    public string '1st_name = "John";
    public int 'Ȧɢέ_\ \/\:\@\[\`\{\~\u{03C0} = 0;
    public 'Person_\ \/\<\>\:\@\[\`\{\~π_ƮέŞŢ? parent = ();
    string email = "default@abc.com";
    string address = "No 20, Palm grove";

    public function getSum(int a, int b) returns int {
        future<int> futureSum = @strand {thread: "any"} start addition(a, b);
        int|error result = wait futureSum;
        if result is int {
            return result;
        } else {
            return -1;
        }
    }
}

public class Location {
    public string city;
    public string country;

    public function init(string city, string country) {
        self.city = city;
        self.country = country;
    }

    public function value() returns string {
        return self.city + ", " + self.country;
    }
}

public type AnonPerson object {
    public string '1st_name;
    public int 'Ȧɢέ_\ \/\:\@\[\`\{\~\u{03C0};
    public 'Person_\ \/\<\>\:\@\[\`\{\~\u{03C0}_ƮέŞŢ? parent;
    string email;
    string address;
};

type EventType "CURRENT"|"EXPIRED"|"ALL"|"RESET"|"TIMER";

class OddNumberGenerator {
    int i = 1;

    public isolated function next() returns record {|int value;|}|error? {
        self.i += 2;
        return {value: self.i};
    }
}

// constants
const nameWithoutType = "Ballerina";
const string nameWithType = "Ballerina";
const map<string> nameMap = {"name": "John"};
const nilWithoutType = ();
const () nilWithType = ();

// enums
enum Color {
    RED,
    BLUE = "Blue"
}

// global variables
var stringValue = "Ballerina";
var decimalValue = 100.0d;
var byteValue = <byte>2;
var floatValue = 2.0;
json jsonValue = {name: "John", age: 20};
var '\ \/\:\@\[\`\{\~\u{03C0}_IL = "IL with global var";

// configurable variables
configurable int port = 9090;

// let expression helper declarations
const globalVar = 2;
int k = let int x = 4 in 2 * x * globalVar;

type SampleErrorData record {
    string message?;
    error cause?;
    string info;
    boolean fatal;
};

type SampleError error<SampleErrorData>;

type Foo record {|
    string message?;
    error cause?;
    string detailMsg;
    boolean isFatal;
|};

type FooError error<Foo>;

public client class Child {

    remote function getName(string firstName, string lastName = "") returns string|error {
        return firstName + lastName;
    }

    remote function getTotalMarks(int maths, int english) returns int {
        future<int> futureSum = @strand {thread: "any"} start sum(maths, english);
        int|error result = wait futureSum;
        if result is int {
            return result;
        } else {
            return -1;
        }
    }
}

type Annot record {
    string foo;
    int bar?;
};

public annotation Annot v1 on type, class;

string strValue = "v1 value";

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function sum(int a, int b) returns int {
    return a + b;
}

function getName(string name) returns string {
    return "Name: " + name;
}

public function getSum(int a, int b) returns int {
    future<int> futureSum = @strand {thread: "any"} start addition(a, b);
    int|error result = wait futureSum;
    if result is int {
        return result;
    } else {
        return -1;
    }
}

public function main() {
    //------------------------ basic, simple type variables ------------------------//

    var varVariable = ();
    boolean booleanVar = true;
    int intVar = 20;
    float floatVar = -10.0;
    decimal decimalVar = 3.5;

    'int:Unsigned8 unsigned8IntVar = 1;
    'int:Unsigned16 unsigned16IntVar = 100;
    'int:Unsigned32 unsigned32IntVar = 1000;
    'int:Signed8 signed8IntVar = -1;
    'int:Signed16 signed16IntVar = -100;
    'int:Signed32 signed32IntVar = -1000;

    //------------------------ basic, sequence type variables ------------------------//

    string stringVar = "foo";
    xml xmlVar = xml `<person gender="male"><firstname>Praveen</firstname><lastname>Nada</lastname></person>`;
    xml xmlVar2 = xml
            `<items>
                <!--Contents-->
                <book>
                    <name>A Study in Scarlet</name>
                    <author><name>Arthur Conan Doyle</name></author>
                </book>
                <planner>Daily Planner<kind>day</kind><pages>365</pages></planner>
                <book>
                    <name>The Sign of Four</name>
                    <author><name>Arthur Conan Doyle</name></author>
                </book>
                <pen><kind>marker</kind><color>blue</color></pen>
            </items>
            <items2>
                <!--Contents-->
                <book>
                    <name>A Study in Scarlet</name>
                    <author><name>Arthur Conan Doyle</name></author>
                </book>
                <planner>Daily Planner<kind>day</kind><pages>365</pages></planner>
                <book>
                    <name>The Sign of Four</name>
                    <author><name>Arthur Conan Doyle</name></author>
                </book>
                <pen><kind>marker</kind><color>blue</color></pen>
            </items2>`;

    //------------------------ basic, structured type variables ------------------------//

    any[] arrayVar = [1, 20, -10.0, "foo"];
    boolean[] booleanArrayVar = [false, true];
    int[] intArrayVar = [1, 2, 3];
    float[] floatArrayVar = [1.5, -2.0, 3.0];
    decimal[] decimalArrayVar = [3, 34.3, -45];
    string[] stringArrayVar = ["foo", "bar"];
    byte[] byteArrayVar = base64 `aa ab cc ad af df 1a d2 f3 a4`;
    [int, string] tupleVar = [20, "foo"];
    map<string> mapVar = {line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka"};

    '\ \/\:\@\[\`\{\~\u{03C0}_123_ƮέŞŢ_Student recordVar = {
        '1st_name: "John Doe",
        'Ȧɢέ_\ \/\:\@\[\`\{\~π: 20,
        grades: {
            maths: 80,
            physics: 75,
            chemistry: 65
        },
        "course": "ballerina"
    };

    record {|string city; string country;|} anonRecord = {city: "London", country: "UK"};

    EmployeeTable tableWithKeyVar = table [
            {id: 1, name: "John", salary: 300.50},
            {id: 2, name: "Bella", salary: 500.50},
            {id: 3, name: "Peter", salary: 750.0}
        ];

    table<Employee> tableWithoutKeyVar = table [
            {id: 1, name: "John", salary: 300.50},
            {id: 2, name: "Bella", salary: 500.50},
            {id: 3, name: "Peter", salary: 750.0}
        ];

    error errorVar = error("SimpleErrorType", message = "Simple error occurred");

    //------------------------ basic, behavioral type variables ------------------------//

    function (string, string) returns string anonFunctionVar =
        function(string x, string y) returns string {
        return x + y;
    };

    future<int> futureVar = start sum(40, 50);
    _ = checkpanic wait futureVar;

    'Person_\ \/\<\>\:\@\[\`\{\~\u{03C0}_ƮέŞŢ objectVar = new;

    AnonPerson anonObjectVar = new 'Person_\ \/\<\>\:\@\[\`\{\~\u{03C0}_ƮέŞŢ();

    Child clientObjectVar = new Child();

    typedesc<int> typedescVar = int;
    stream<int, error?> oddNumberStream = new stream<int, error?>(new OddNumberGenerator());

    //------------------------ Other types ------------------------//

    string|error unionVar = "foo";
    string? optionalVar = "foo";
    any anyVar = 15.0;
    anydata anydataVar = 619;
    byte byteVar = 128;
    json jsonVar = {name: "apple", color: "red", price: 40};
    xml<never> neverVar = <xml<never>>'xml:concat();

    // quoted identifiers
    string '\ \/\:\@\[\`\{\~\u{03C0}_var = "IL with special characters in var";
    string 'üňĩćőđę_var = "IL with unicode characters in var";
    json 'ĠĿŐΒȂɭ_\ \/\:\@\[\`\{\~\u{03C0}_json = {};

    // service object
    service object {} serviceVar = service object {
        final int i = 5;
        resource function get getResource() {
            int k = self.i;
        }
    };

    // Helper declarations for query evaluations
    Student s1 = {firstName: "Martin", lastName: "Sadler", intakeYear: 1990, score: 3.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", intakeYear: 2001, score: 1.9};
    Student s3 = {firstName: "Michelle", lastName: "Guthrie", intakeYear: 2002, score: 3.7};
    Student s4 = {firstName: "George", lastName: "Fernando", intakeYear: 2005, score: 4.0};
    Student[] studentList = [s1, s2, s3];

    error onConflictError = error("Key Conflict", message = "cannot insert.");
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};
    Customer[] customerList = [c1, c2, c3];
    Customer[] conflictedCustomerList = [c1, c2, c1];

    GradStudent gs1 = {firstName: "Michelle", lastName: "Sadler", intakeYear: 1990, deptId: 1};
    GradStudent gs2 = {firstName: "Ranjan", lastName: "Fonseka", intakeYear: 2001, deptId: 3};
    GradStudent gs3 = {firstName: "Martin", lastName: "Guthrie", intakeYear: 2002, deptId: 1};
    GradStudent gs4 = {firstName: "George", lastName: "Fernando", intakeYear: 2005, deptId: 2};
    Department d1 = {deptId: 1, deptName: "Physics"};
    Department d2 = {deptId: 2, deptName: "Mathematics"};
    Department d3 = {deptId: 3, deptName: "Chemistry"};
    GradStudent[] gradStudentList = [gs1, gs2, gs3, gs4];
    Department[] departmentList = [d1, d2, d3];

    // Helper statements for qualified name/function/type evaluation tests
    var pi = langFloat:PI;
}

function printSalaryDetails(int baseSalary, int annualIncrement = 20, float bonusRate = 0.02) returns string {
    return string `[${baseSalary}, ${annualIncrement}, ${bonusRate}]`;
}

function calculate(int a, int b, int c) returns int {
    return a + 2 * b + 3 * c;
}

function printDetails(string name, int age = 18, string... modules) returns string {
    string detailString = "Name: " + name + ", Age: " + age.toString();
    string moduleString = "";
    if (modules.length() == 0) {
        moduleString = "Module(s): ()";
    } else {
        modules.forEach(function(string module) {
            moduleString += 'module + ",";
        });
        moduleString = "Module(s): " + moduleString;
    }
    return string `[${name}, ${age}, ${moduleString}]`;
}

function addition(int a, int b) returns int {
    return a + b;
}

function func(int k) returns int {
    return k * 2;
}

function func2(string y) returns int {
    return y.length();
}

function getSampleError() returns SampleError {
    SampleError e = error SampleError("Sample Error", info = "Detail Msg", fatal = true);
    return e;
}

function getRecordConstrainedError() returns FooError {
    FooError e = error FooError("Some Error", detailMsg = "Failed Message", isFatal = true);
    return e;
}

function processTypeDesc(typedesc t) returns typedesc {
    return t;
}
