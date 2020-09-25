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

import ballerina/lang.'xml;
import ballerina/lang.'int;
import ballerina/lang.'string;
import ballerina/lang.'object as lang;

listener Listener testListener = new;

service hello on testListener {

    resource function res(int intVal) returns error? {
        return;
    }
}

class Listener {
    *lang:Listener;

    public function init() {
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __detach(service s) returns error? {
    }

    public function __start() returns error? {
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }
}


public function workersTest() {
    @strand {thread: "any"}
    worker w1 {
        // do nothing
    }

    @strand {thread: "any"}
    worker w2 {
        // do nothing
    }
    @strand {thread: "any"}
    worker w3 {
        // do nothing
    }
}

function 'function(int val) returns int {
    return val + 1;
}

public function quotedIdentifiersTest() {
    int 'int = 1;

    int i = 'function('int);

    int '1PlusI = 1 + i;
}

type Person record {
    string name;
};

type Employee record {
    string name;
    boolean intern;
};

Person person1 = { name: "Person 1" };
Employee employee1 = { name: "Employee 1", intern: true };
Employee employee2 = { name: "Employee 2", intern: false };

function tupleDestructureTest1() returns [int, string] {
	[int, string] x = [1, "a"];
    int a;
    string b;
    [a, b] = x;
    return [a, b];
}

function tupleDestructureTest2() returns [int, int[]] {
    [int, int, int, int] x = [1, 2, 3, 4];
    int a;
    int[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest3() returns [int, int[]] {
    [int, int, int...] x = [1, 2, 3, 4];
    int a;
    int[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest4() returns int[] {
    [int...] x = [1, 2, 3, 4];
    int[] a;
    [...a] = x;
    return a;
}

function tupleDestructureTest5() returns [int, int[]] {
    [int, int, int...] x = [1, 2, 3, 4];
    int a;
    int[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest6() returns [int, string[]] {
    [int, string, string] x = [1, "a", "b"];
    int a;
    string[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest7() returns [int, string[]] {
    [int, string...] x = [1, "a", "b"];
    int a;
    string[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest8() returns [int, string, string[]] {
    [int, string, string, string...] x = [1, "a", "b"];
    int a;
    string b;
    string[] c;
    [a, b, ...c] = x;
    return [a, b, c];
}

type FooRecord record {|
    string field1;
    [int, float] field2;
|};

function tupleDestructureTest9() returns [boolean, string, [int, float]] {
    FooRecord foo = {
        field1: "string value",
        field2: [25, 12.5]
    };

    boolean a;
    string b;
    [int, float] c;
    [a, { field1: b, field2: c }] = [true, foo];

    return [a, b, c];
}

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function inherentlyImmutableBasicTypes() {
    any a = ();

    any b = true;

    any c = false;

    any d = 123;

    any e = -124.0f;

    any f = 34.23d;

    any g = "str value";

    error err = error("Reason", message = "error message");
    any|error h = err;

    error myError = AssertionError(ASSERTION_ERROR_REASON, message = "second error message");
    any|error i = myError;

    service ser = service {
        resource function res() {

        }
    };
    any k = ser;

    Employee employee = {name: "Jo", intern: false};
    typedesc<any> typeDesc = typeof Employee;
    any l = typeDesc;

    'xml:Text xmlText = xml `xml text`;
    any n = xmlText;
}

function funcReturnNil() {
}

function testNullableTypeBasics1() returns int? {
    int? i = funcReturnNil();
    i = ();
    i = 5;
    return i;
}

function testNullableTypeBasics2() returns int? {
    int? i = funcReturnNil();
    return i;
}

function testNullableArrayTypes1() returns any {
    float?[] fa = [1.0, 5.0, 3.0, ()];
    float? f = fa[0];
    return f;
}

type SearchResultType RESULT_TYPE_MIXED|RESULT_TYPE_RECENT|RESULT_TYPE_POPULAR;

const RESULT_TYPE_MIXED = "mixed";
const RESULT_TYPE_RECENT = "recent";
const RESULT_TYPE_POPULAR = "popular";

function testNilableTypeInTypeTest() returns string {
    SearchResultType? s = RESULT_TYPE_MIXED;

    if (s is SearchResultType) {
        return <string>s;
    }

    return "()";
}

type InclusiveRecord record {
    int j;
    never p?;
};

type ExclusiveRecord record {|
    int j;
    never p?;
|};

function testInclusiveRecord() {
    InclusiveRecord inclusiveRecord = {j:0, "q":1};
}

function testExclusiveRecord() {
    ExclusiveRecord exclusiveRecord = {j:0};
}

type Student record {
    string firstName;
    string lastName;
    float score;
};


type FullName record {|
    string firstName;
    string lastName;
|};


public function functionWithQueryExpression() {
    Student s1 = {firstName: "Alex", lastName: "George", score: 1.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 0.9};
    Student s3 = {firstName: "John", lastName: "David", score: 1.2};
    Student[] studentList = [s1, s2, s3];
    FullName[] nameList = [];
    var e = from var student in studentList

    do {
        FullName fullName = {firstName: student.firstName, lastName: student.lastName};
        nameList.push(fullName);
    };

    foreach var name in nameList {

    }
}

class OddNumberGenerator {
    int i = 1;

    public function next() returns record {|int value;|}|error? {
        self.i += 2;
        return {value: self.i};
    }
}

type ResultValue record {|
    int value;
|};

type StudentValue record {|
    Student value;
|};

type Subscription record {|
    string firstName;
    string lastName;
    float score;
    string degree;
|};

public function functionWithStreams() {
    OddNumberGenerator oddGen = new;
    var oddNumberStream = new stream<int, error>(oddGen);

    record {|int value;|}|error? oddNumber = oddNumberStream.next();
    if (oddNumber is ResultValue) {
        // do nothing
    }

    Student s1 = {firstName: "Alex", lastName: "George", score: 1.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 0.9};
    Student s3 = {firstName: "John", lastName: "David", score: 1.2};
    Student[] studentList = [s1, s2, s3];
    stream<Student> studentStream = studentList.toStream();

    stream<Subscription> subscriptionStream = studentStream.filter(function (Student student) returns boolean {
        return student.score > 1;
    }).'map(function (Student student) returns Subscription {
        Subscription subscription = {
            firstName: student.firstName,
            lastName: student.lastName,
            score: student.score,
            degree: "Bachelor of Medicine"
        };
        return subscription;
    });


    float? avg = subscriptionStream.reduce(function (float accum, Student student) returns float {
        return accum + <float>student.score / studentList.length();
    }, 0.0);

    if (avg is float) {
        // do nothing
    }
       stream<Student> studentStream2 = studentList.toStream();


    record {|Student value;|}|error? student = studentStream2.next();
    if (student is StudentValue) {
        // do nothing
    }


    error? e = studentStream2.forEach(function (Student student) {
        // do nothing
    });

    if (e is error) {
        // do nothing
    }

    stream<Student> studentStream3 = studentList.toStream();
    var iterator = studentStream3.iterator();
    record {|Student value;|}|error? nextStudent = iterator.next();
    if (nextStudent is StudentValue) {
        // do nothing
    }
}


public function functionWithWorkerInteractions() {
    worker w1 {
        int i = 100;
        float k = 2.34;

        [int, float] t1 = [i, k];
        t1 -> w2;

        json j = {};
        j = <- w2;
        string jStr = j.toString();

        () send = i ->> w2;

        k -> w3;
        k -> w3;
        k -> w3;

        error? flushResult = flush w3;
    }

    worker w2 {
        int iw;
        float kw;
        [int, float] vW1 = [0, 1.0];
        vW1 = <- w1;
        [iw, kw] = vW1;

        json jw = {"name": "Ballerina"};

        jw -> w1;

        int lw;
        lw = <- w1;

    }

    worker w3 {
        float mw;
        mw = <- w1;
        mw = <- w1;
        mw = <- w1;
    }

    wait w1;
}

public function functionWithFork() {

    int i = 100;
    string s = "WSO2";
    map<string> m = {"name": "Bert", "city": "New York", "postcode": "10001"};

    string name = <string>m["name"];
    string city = <string>m["city"];
    string postcode = <string>m["postcode"];

    fork {
        worker W1 {

            i = 23;

            m["name"] = "Moose";

            fork {
                worker W3 {
                    string street = "Wall Street";
                    m["street"] = street;

                    i = i + 100;
                }
            }

            wait W3;
        }

        worker W2 {
            s = "Ballerina";

            m["city"] = "Manhattan";
        }
    }

    _ = wait {W1, W2};

    name = <string>m["name"];
    city = <string>m["city"];

    string street = <string>m["street"];
}

int count = 0;

client class ClientObject {
    public remote function remoteFunc(string arg) returns int|error {
        return 1;
    }
}

ClientObject clientEndpoint = new;


public function functionWithAsynchInvocations() {
    future<int> f1 = start sum(40, 50);

    int result = squarePlusCube(f1);

    _ = wait f1;

    future<()> f2 = start countInfinity();

    f2.cancel();

    future <int|error> f3 = @strand {thread:"any"} start clientEndpoint-> remoteFunc("/get?test=123");

    int|error response = wait f3;

    future<int> f4 = start square(20);
    future<string> f5 = start greet("Bert");

    int|string anyResult = wait f4|f5;

    future<int> f6 = start sum(40, 60);
    future<int> f7 = start cube(3);
    future<string> f8 = start greet("Moose");

    map<int|string> resultMap = wait {first_field: f6, second_field: f7,
                                            third_field: f8};

    record {int first_field; int second_field; string third_field;} rec =
                    wait {first_field: f6, second_field: f7, third_field: f8};
}

function sum(int a, int b) returns int {
    return a + b;
}

function square(int n) returns int {
    return n * n;
}

function cube(int n) returns int {
    return n * n * n;
}

function greet(string name) returns string {
    return "Hello " + name + "!!";
}

function functionWithTableType() {
    table<UndergradStudentRec> underGradTable = table key(id,name) [
            { id: 1, name: "Mary", grade: 12 },
            { id: 2, name: "John", grade: 13 }
        ];
    table<EmployeeRec> varTable = table key(id) [
                { id: 1, age: 30,  salary: 300.5, name: "Mary", married: true },
                { id: 2, age: 20,  salary: 300.5, name: "John", married: true }
            ];
}

type EmployeeRec record {
    readonly int id;
    int age;
    decimal salary;
    string name;
    boolean married;
};

type UndergradStudentRec record {|
    readonly int id;
    readonly string name;
    int grade;
|};

function squarePlusCube(future<int> f) returns int {
    worker w1 {
        int n = wait f;
        int sq = square(n);
        sq -> w2;
    }
    worker w2 returns int {
        int n = wait f;
        int cb = cube(n);
        int sq;
        sq = <- w1;
        return sq + cb;
    }
    return wait w2;
}

function countInfinity() {
    while (true) {
        count += 1;
    }
}

public function functionWithBitwiseOperations() {
    int a = 385;
    'int:Unsigned8 b = 128;
    'int:Unsigned8 res1 = a & b;

    'int:Signed16 c = -32700;
    int d = 249;
    int res2 = c & d;

    'int:Unsigned8 e = 254;
    'int:Unsigned16 f = 511;
    'int:Unsigned8 res3 = e | f;

    'int:Unsigned8 res4 = e ^ f;

    int g = 12345678;
    'int:Signed8 h = -127;
    int res5 = g | h;

    int res6 = g ^ h;

}

public function functionWithShiftOperations() {
    int a = 1;
    int res1 = a << 2;

    'int:Unsigned8 b = 128;
    int res2 = b << 3;

    'int:Signed16 c = -32700;
    int res3 = c >> 2;

    'int:Unsigned8 d = 255;
    int e = 4;
    'int:Unsigned8 res4 = d >> e;

    'int:Signed32 f = 123167;
    int res5 = f >>> 3;

    'int:Unsigned16 g = 32001;
    'int:Unsigned16 res6 = g >> 2;
}

public function functionWithStringOperations() {
    string statement = "Lion in Town. Catch the Lion";
    string s1 = statement.toUpperAscii();

    string s2 = statement.toLowerAscii();

    string s3 = statement.substring(0, 4);


    int? index = statement.indexOf("on");
    if (index is int) {

    }

    int length = statement.length();

    string hello = "Hello";
    string ballerina = "Ballerina!";
    string s4 = hello.concat(" ", ballerina);

    string s5 = ",".'join(hello, ballerina);

    byte[] bArray = hello.toBytes();

    string|error s6 = 'string:fromBytes(bArray);
    if (s6 is string) {
        // do nothing
    }

    string toTrim = "  Ballerina Programming Language  ";
    string s7 = toTrim.trim();

    boolean hasSuffix = statement.endsWith("Lion");

    boolean hasPrefix = statement.startsWith("Lion");

    string name = "Sam";
    int marks = 90;
    string[] subjects = ["English", "Science"];
    float average = 71.5;

    string country = "Sri Lanka";
    string c = country[4];

}

function functionWithIntSubTypes() {
    'int:Signed32 a1 = 2147483647;
    'int:Signed32 a2 = -2147483648;
    'int:Signed16 b1 = 32767;
    'int:Signed16 b2 = -32768;
    'int:Signed8 c1 = 127;
    'int:Signed8 c2 = -128;
    'int:Unsigned32 d1 = 4294967295;
    'int:Unsigned32 d2 = 0;
    'int:Unsigned16 e1 = 65535;
    'int:Unsigned16 e2 = 0;
    'int:Unsigned8 f1 = 255;
    'int:Unsigned8 f2 = 0;
    byte g1 = 255;
    byte g2 = 0;
}

function functionWithCharSubTypes() {
    'string:Char a1 = "a";
    'string t1 = a1;

    'string:Char a2 = "µ";
    'string t2 = a2;

    string b = "ab";
}

type Char 'string:Char;

function testCharTypeAlias() {
    Char value = "a";
    string a = value;
    'string:Char b = value;
}

function testCharConcat() {
    Char a = "a";
    Char b = "b";
    string c = "c";
    xml x = xml `<x/>`;

    string t1 = a + b;
    string t2 = a + c;
    string t3 = c + b;
    xml t4 = a + x;
    xml t5 = x + a;
    string sa = "a";
    xml xa = sa + x;
    xml ax = x + sa;
}

function functionWithUnaryOps() {
    int a;
    int b = 5;
    int c = 2;
    a = -b;
    c = ~b;
}
