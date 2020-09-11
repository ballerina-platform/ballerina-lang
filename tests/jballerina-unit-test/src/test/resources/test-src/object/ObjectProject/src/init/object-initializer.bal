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

import initializers as inp;

public class person {

    public int age = 0;
    public string name = "";
    public string address = "";

    function init (int a = 10, string n = "Charles") {
        self.name = n;
        self.age = a;
    }

    public function getAge() {
        self.age = 12;
    }
}

function testObjectInitializerInSamePackage1() returns [int, string]{
    person p = new(n = "Peter");
    return [p.age, p.name];
}

function testObjectInitializerInAnotherPackage() returns [int, string]{
    inp:employee e = new("Peter");
    return [e.age, e.name];
}

class employee {

    public int age = 0;
    public string name = "A";

    function init (string name, int a = 30) {
        self.name = self.name + name;
        self.age = a;
    }
}

function testObjectInitializerOrder() returns [int, string]{
    employee p = new ("B", a = 40);
    return [p.age, p.name];
}

class Person {
    string name;
    int age;

    function init() returns error? {
        self.name = check getError();
        self.age = 25;
    }
}

function getError() returns string|error {
    error e = error("failed to create Person object", f = "foo");
    return e;
}

function testErrorReturningInitializer() returns Person|error {
    Person|error p = new();
    return p;
}

function testReturnedValWithTypeGuard() returns string {
    Person|error p = new Person();

    if (p is error) {
        return "error";
    } else {
        return "Person";
    }
}

function testAssigningToVar() returns error? {
    var v = new Person();
    if (v is error) {
        return v;
    }
    return ();
}

function testTypeInitInReturn() returns Person|error {
    return new();
}

class Person2 {
    string name;
    int age = 27;
    string profession;
    anydata[] misc;

    function init(string name, string profession = "", anydata... misc) {
        self.name = name;
        self.profession = profession;
        self.misc = misc;
    }
}

function testInitializerWithRestArgs() returns Person2 {
    json adr = {city: "Colombo", country: "Sri Lanka"};
    Person2 p = new("Pubudu", "Software Engineer", adr);
    return p;
}

class Person3 {
    string name;
    int age;

    function init(string name, int age) returns Err? {
        self.name = check getError2(100);
        self.age = 25;
    }
}

type ErrorDetails record {
    int id;
    string message?;
    error cause?;
};

type Err error<ErrorDetails>;

function getError2(int errId) returns string|Err {
    Err e = Err("Failed to create object", id = errId);
    return e;
}

function testCustomErrorReturn() returns [Person3|Err, Person3|error] {
    Person3|Err p1 = new("Pubudu", 27);
    Person3|error p2 = new("Pubudu", 27);
    return [p1, p2];
}

class Person4 {
    string name;
    int age;

    function init(boolean isFoo) returns FooErr|BarErr|() {
        self.name = check getMultipleErrors(isFoo);
        self.age = 25;
    }
}

type FooErrData record {
    string f;
    string message?;
    error cause?;
};

type FooErr error<FooErrData>;

type BarErrData record {
    string b;
    string message?;
    error cause?;
};

type BarErr error<BarErrData>;

function getMultipleErrors(boolean isFoo) returns string|FooErr|BarErr {
    if (isFoo) {
        FooErr e = FooErr("Foo Error", f = "foo");
        return e;
    } else {
        BarErr e = BarErr("Bar Error", b = "bar");
        return e;
    }
}

function testMultipleErrorReturn() returns [Person4|FooErr|BarErr, Person4|FooErr|BarErr] {
    Person4|FooErr|BarErr p1 = new(true);
    Person4|FooErr|BarErr p2 = new(false);
    return [p1, p2];
}

class Too {
    public string name;

    public function init(string name) {
        self.name = name;
    }

    function updateName(string name) {
        self.init(name);
    }
}

function testInitActionInsideObjectDescriptor() returns string {
    Too t = new("Java");
    t.updateName("Ballerina");
    return t.name;
}

function panicTheStr() returns error|string {
    return error("Panicked");
}

class PanicReceiver {

    public int age = 0;
    public string name = "A";

    function init (string name, int a = 30) {
        self.name = self.name + name;
        self.age = a;
        panic error("init panicked");
    }
}

function panicWrapper() {
    PanicReceiver r = new(checkpanic panicTheStr());
}

function testCheckPanicObjectInit(boolean b) returns error|Person4 {
    Person4 r = check new(b);
    return r;
}

function testCheckPanicInObjectInitArg() returns error {
    error|() p = trap panicWrapper();
    return <error>p;
}

function testObjectInitPanic() returns error|PanicReceiver {
    return trap new PanicReceiver("Mr. Panic");
}

class Student1 {
     public int marks = 75;

     public function init() {
     }

     public function getMarks() returns int {
         self.init();
         return self.marks;
     }
}

function testInitInvocationInsideObject() returns (boolean) {
    Student1 student = new;
    int marksBeforeChange = student.getMarks();
    student.marks = 80;
    int marksAfterChange = student.getMarks();
    return marksBeforeChange == 75 && marksAfterChange == 80;
}

class Student2 {
    public string grade;

    public function init(string grade) {
        self.grade = grade;
    }

    public function resetGrade(string grade) returns string {
        self.init(grade);
        return self.grade;
    }
}

function testInitInvocationInsideObjectWithArgs() returns (boolean) {
    Student2 student = new("B");
    string marksBeforeChange = student.grade;
    string marksAfterChange = student.resetGrade("B+");
    return marksBeforeChange == "B" && marksAfterChange == "B+";
}

class Student3 {
    public string name;
    public int marks = 90;

    public function init(int id) returns error? {
        self.name = check getName(id);
    }

    public function getMarks() returns int {
        var v = self.init(0);
        return self.marks;
    }
}

function getName(int id) returns string|error {
    if (id == -1) {
        return error("failed to return a name");
    } else {
        return "Smith";
    }
}

function testObjInitWithCheck(int id) returns ([Student3|error, int, int]) {
    int marksBeforeChange = 0;
    int marksAfterChange = 0;

    Student3|error student = new(id);
    if (student is error) {
        return [student, marksBeforeChange, marksAfterChange];
    }

    Student3 s = <Student3> student;
    marksBeforeChange = s.getMarks();
    s.marks = 95;
    marksAfterChange = s.getMarks();
    return [s, marksBeforeChange, marksAfterChange];
}

function testObjInitWithCheck1() returns (boolean) {
    var [s, marksBeforeChange, marksAfterChange] = testObjInitWithCheck(-1);
    return s is error && marksBeforeChange == 0 && marksAfterChange == 0;
}

function testObjInitWithCheck2() returns (boolean) {
    var [s, marksBeforeChange, marksAfterChange] = testObjInitWithCheck(10);
    return !(s is error) && marksBeforeChange == 90 && marksAfterChange == 95;
}

class Student4 {
    string[] modules;

    public function init(string... modules) {
        self.modules = modules;
    }

    public function changeModules(string... modules) {
        self.init(...modules);
    }
}

function testInitInvocationWithRestArgs() returns (boolean) {
    string[] modules = ["Science", "History"];
    Student4 student = new(...modules);
    string[] nameBeforeChange = student.modules;

    string[] newModules = ["Maths", "Physics"];
    student.changeModules(...newModules);

    string[] nameAfterChange = student.modules;
    return nameBeforeChange[0] == "Science" && nameBeforeChange[1] == "History" &&
        nameAfterChange[0] == "Maths" && nameAfterChange[1] == "Physics";
}

class Student5 {
    public string name;
    string[] modules;
    public int marks = 90;

    public function init(int id, string... modules) returns error? {
        self.name = check getName(id);
        self.modules = modules;
    }

    public function getMarks() returns int {
        var v = self.init(0);
        return self.marks;
    }
}

function testInitInvocationWithCheckAndRestParams(int id, string... modules) returns ([Student5|error, int, int]) {
    int marksBeforeChange = 0;
    int marksAfterChange = 0;

    Student5|error student = new(id, ...modules);
    if (student is error) {
        return [student, marksBeforeChange, marksAfterChange];
    }

    Student5 s = <Student5> student;
    marksBeforeChange = s.getMarks();
    s.marks = 95;
    marksAfterChange = s.getMarks();
    return [s, marksBeforeChange, marksAfterChange];
}

function testInitInvocationWithCheckAndRestParams1() returns (boolean) {
    string[] modules = ["Math", "Physics"];
    var [s, marksBeforeChange, marksAfterChange] = testInitInvocationWithCheckAndRestParams(-1, ...modules);
    return s is error && marksBeforeChange == 0 && marksAfterChange == 0;
}

function testInitInvocationWithCheckAndRestParams2() returns (boolean) {
    string[] modules = ["Math", "Physics"];
    var [s, marksBeforeChange, marksAfterChange] = testInitInvocationWithCheckAndRestParams(10, ...modules);
    return !(s is error) && marksBeforeChange == 90 && marksAfterChange == 95;
}


class Student6 {
    int id;

    public function init(int id = 1) {
        self.id = id;
    }

    public function getId() returns int {
        return self.id;
    }
}

function testInitInvocationWithDefaultParams1() returns (boolean) {
    Student6 student = new;
    return student.getId() == 1;
}

class Student7 {
    int? id;

    public function init(int? id = 1) {
        self.id = id;
    }

    public function getId() returns int {
        if !(self.id is int) {
            error err = error("ID should be an integer");
            panic err;
        }
        return <int> self.id;
    }
}

function testInitInvocationWithDefaultParams2() returns (boolean) {
    Student7 student = new(4);
    return student.getId() == 4;
}

public type ID int|string;

class Student8 {
    int id;

    public function init(ID i=1) {
        self.id = <int> i;
    }

    public function getId() returns int {
        return self.id;
    }
}

function testInitInvocationWithFiniteType() returns (boolean) {
    Student8 student = new(4);
    return student.getId() == 4;
}

class AddError {
    error er;
    function init(error simpleError = error("SimpleErrorType", message = "Simple error occurred")) {
        self.er = simpleError;
    }

    public function getError() returns error|() {
        return self.er;
    }
}

function testInitInvocationWithDefaultError() returns (boolean) {
    AddError newError = new;
    var e = newError.getError();
    if !(e is error) {
        error err = error("Returned value should be an error");
        panic err;
    }
    return e is error;
}

class Student9 {
    int fullMarks;

    public function init(int firstMark = 80, int secondMark = firstMark) {
        self.fullMarks = firstMark + secondMark;
    }

    public function getMarks() returns int {
        return self.fullMarks;
    }
}

function testInitInvocationWithReferenceToDefaultValue1() returns (boolean) {
    Student9 student = new;
    return student.getMarks() == 160;
}

class Calculate1 {
    int sum;

    public function init(int a, int b, int c, int d = a + b + c*c) {
        self.sum = d;
    }

    public function getSum() returns int {
        return self.sum;
    }
}

function testInitInvocationWithReferenceToDefaultValue2() returns (boolean) {
    Calculate1 cal = new(2, 3, 4);
    return cal.getSum() == 21;
}

class Calculate2 {
    int sum;
    string op;

    public function init(string operation, int a, int b, int c, int d = a + b + c*c) returns error? {
        self.op = check checkOperation(operation);
        self.sum = d;
    }
}

function checkOperation(string operation) returns string|error {
    if (operation == "SUB") {
        error e = error("unsupported operation", op = operation);
        return e;
    }
    return operation;
}

function testErrorReturnWithInitialization() returns (boolean) {
    Calculate2|error cal = new("SUB", 2, 3, 4);
    return cal is error;
}

public const NAME = "John";
public const AGE = 11;
public type TYPE NAME|AGE;

class Student10 {
    string name = "";
    int age = 11;
    TYPE t1;
    TYPE t2;

    function init(string name = NAME, int age = AGE, TYPE t1 = NAME, TYPE t2 = AGE) {
        self.name = name;
        self.age = age;
        self.t1 = t1;
        self.t2 = t2;
    }

    function getDetails() returns [string, int, TYPE, TYPE] {
        return [self.name, self.age, self.t1, self.t2];
    }
}

function testConstRefsAsDefaultValue() returns (boolean) {
    Student10 s = new("Andrew");
    [string , int, TYPE, TYPE ] details = s.getDetails();
    return details[0] == "Andrew" && details[1] == 11 && details[2] == "John" && details[3] == 11;
}

function getData(string n) returns string {
    return n;
}

class Student11 {
    string name;
    function init(string n, function (string) returns string fn = (x) => "John") {
        self.name = fn(n);
    }

    function getName() returns string {
        return self.name;
    }
}

function testFunctionPointerAsDefaultableParam1() {
    Student11 s1 = new("Anne", getData);
    if (s1.getName() != "Anne") {
        panic error("Returned string should equal 'Anne'");
    }

    Student11 s2 = new("Anne");
    if (s2.getName() != "John") {
        panic error("Returned string should equal 'John'");
    }
}

function getTotalMarks(int a, int b) returns int {
    return a + b + 10;
}

class Student12 {
    int marks;
    function init(int x, int y, function (int, int) returns int fn =
                                                        function(int a, int b) returns int {return 100;}) {
        self.marks = fn(x, y);
    }

    function getMarks() returns int {
        return self.marks;
    }
}

function testFunctionPointerAsDefaultableParam2() {
    Student12 s1 = new(10, 20, getTotalMarks);
    if (s1.getMarks() != 40) {
        panic error("Returned string should equal '40'");
    }

    Student12 s2 = new(10, 20);
    if (s2.getMarks() != 100) {
        panic error("Returned string should equal '100'");
    }
}
