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

public type person object {

    public int age = 0;
    public string name = "";
    public string address = "";

    function __init (int a = 10, string n = "Charles") {
        self.name = n;
        self.age = a;
    }

    public function getAge() {
        self.age = 12;
    }
};

function testObjectInitializerInSamePackage1() returns [int, string]{
    person p = new(n = "Peter");
    return [p.age, p.name];
}

function testObjectInitializerInAnotherPackage() returns [int, string]{
    inp:employee e = new("Peter");
    return [e.age, e.name];
}

type employee object {

    public int age = 0;
    public string name = "A";

    function __init (string name, int a = 30) {
        self.name = self.name + name;
        self.age = a;
    }
};

function testObjectInitializerOrder() returns [int, string]{
    employee p = new ("B", a = 40);
    return [p.age, p.name];
}

type Person object {
    string name;
    int age;

    function __init() returns error? {
        self.name = check getError();
        self.age = 25;
    }
};

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

type Person2 object {
    string name;
    int age = 27;
    string profession;
    anydata[] misc;

    function __init(string name, string profession = "", anydata... misc) {
        self.name = name;
        self.profession = profession;
        self.misc = misc;
    }
};

function testInitializerWithRestArgs() returns Person2 {
    json adr = {city: "Colombo", country: "Sri Lanka"};
    Person2 p = new("Pubudu", "Software Engineer", adr);
    return p;
}

type Person3 object {
    string name;
    int age;

    function __init(string name, int age) returns Err? {
        self.name = check getError2(100);
        self.age = 25;
    }
};

type ErrorDetails record {
    int id;
    string message?;
    error cause?;
};

type Err error<string, ErrorDetails>;

function getError2(int errId) returns string|Err {
    Err e = error("Failed to create object", id = errId);
    return e;
}

function testCustomErrorReturn() returns [Person3|Err, Person3|error] {
    Person3|Err p1 = new("Pubudu", 27);
    Person3|error p2 = new("Pubudu", 27);
    return [p1, p2];
}

type Person4 object {
    string name;
    int age;

    function __init(boolean isFoo) returns FooErr|BarErr|() {
        self.name = check getMultipleErrors(isFoo);
        self.age = 25;
    }
};

type FooErrData record {
    string f;
    string message?;
    error cause?;
};

type FooErr error<string, FooErrData>;

type BarErrData record {
    string b;
    string message?;
    error cause?;
};

type BarErr error<string, BarErrData>;

function getMultipleErrors(boolean isFoo) returns string|FooErr|BarErr {
    if (isFoo) {
        FooErr e = error("Foo Error", f = "foo");
        return e;
    } else {
        BarErr e = error("Bar Error", b = "bar");
        return e;
    }
}

function testMultipleErrorReturn() returns [Person4|FooErr|BarErr, Person4|FooErr|BarErr] {
    Person4|FooErr|BarErr p1 = new(true);
    Person4|FooErr|BarErr p2 = new(false);
    return [p1, p2];
}

type Too object {
    public string name;

    public function __init(string name) {
        self.name = name;
    }

    function updateName(string name) {
        self.__init(name);
    }
};

function testInitActionInsideObjectDescriptor() returns string {
    Too t = new("Java");
    t.updateName("Ballerina");
    return t.name;
}

function panicTheStr() returns error|string {
    return error("Panicked");
}

type PanicReceiver object {

    public int age = 0;
    public string name = "A";

    function __init (string name, int a = 30) {
        self.name = self.name + name;
        self.age = a;
        panic error("__init panicked");
    }
};

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

type Student1 object {
     public int marks = 75;

     public function __init() {
     }

     public function getMarks() returns int {
         self.__init();
         return self.marks;
     }
 };

function testInitInvocationInsideObject() returns (boolean) {
    Student1 student = new;
    int marksBeforeChange = student.getMarks();
    student.marks = 80;
    int marksAfterChange = student.getMarks();
    return marksBeforeChange == 75 && marksAfterChange == 80;
}

type Student2 object {
    public string grade;

    public function __init(string grade) {
        self.grade = grade;
    }

    public function resetGrade(string grade) returns string {
        self.__init(grade);
        return self.grade;
    }
};

function testInitInvocationInsideObjectWithArgs() returns (boolean) {
    Student2 student = new("B");
    string marksBeforeChange = student.grade;
    string marksAfterChange = student.resetGrade("B+");
    return marksBeforeChange == "B" && marksAfterChange == "B+";
}

type Student3 object {
    public string name;
    public int marks = 90;

    public function __init(int id) returns error? {
        self.name = check getName(id);
    }

    public function getMarks() returns int {
        var v = self.__init(0);
        return self.marks;
    }
};

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

type Student4 object {
    string[] modules;

    public function __init(string... modules) {
        self.modules = modules;
    }

    public function changeModules(string... modules) {
        self.__init(...modules);
    }
};

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

type Student5 object {
    public string name;
    string[] modules;
    public int marks = 90;

    public function __init(int id, string... modules) returns error? {
        self.name = check getName(id);
        self.modules = modules;
    }

    public function getMarks() returns int {
        var v = self.__init(0);
        return self.marks;
    }
};

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


type Student6 object {
    int id;

    public function __init(int id = 1) {
        self.id = id;
    }

    public function getId() returns int {
        return self.id;
    }
};

function testInitInvocationWithDefaultParams1() returns (boolean) {
    Student6 student = new;
    return student.getId() == 1;
}

type Student7 object {
    int? id;

    public function __init(int? id = 1) {
        self.id = id;
    }

    public function getId() returns int {
        if !(self.id is int) {
            error err = error("ID should be an integer");
            panic err;
        }
        return <int> self.id;
    }
};

function testInitInvocationWithDefaultParams2() returns (boolean) {
    Student7 student = new(4);
    return student.getId() == 4;
}

public type ID int|string;

type Student8 object {
    int id;

    public function __init(ID i=1) {
        self.id = <int> i;
    }

    public function getId() returns int {
        return self.id;
    }
};

function testInitInvocationWithFiniteType() returns (boolean) {
    Student8 student = new(4);
    return student.getId() == 4;
}

type AddError object {
    error er;
    function __init(error simpleError = error("SimpleErrorType", message = "Simple error occurred")) {
        self.er = simpleError;
    }

    public function getError() returns error|() {
        return self.er;
    }
};

function testInitInvocationWithDefaultError() returns (boolean) {
    AddError newError = new;
    var e = newError.getError();
    if !(e is error) {
        error err = error("Returned value should be an error");
        panic err;
    }
    return e is error;
}

type Student9 object {
    int fullMarks;

    public function __init(int firstMark = 80, int secondMark = firstMark) {
        self.fullMarks = firstMark + secondMark;
    }

    public function getMarks() returns int {
        return self.fullMarks;
    }
};

function testInitInvocationWithReferenceToDefaultValue1() returns (boolean) {
    Student9 student = new;
    return student.getMarks() == 160;
}

type Calculate1 object {
    int sum;

    public function __init(int a, int b, int c, int d = a + b + c*c) {
        self.sum = d;
    }

    public function getSum() returns int {
        return self.sum;
    }
};

function testInitInvocationWithReferenceToDefaultValue2() returns (boolean) {
    Calculate1 cal = new(2, 3, 4);
    return cal.getSum() == 21;
}

type Calculate2 object {
    int sum;
    string op;

    public function __init(string operation, int a, int b, int c, int d = a + b + c*c) returns error? {
        self.op = check checkOperation(operation);
        self.sum = d;
    }
};

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

type Student10 object {
    string name = "";
    int age = 11;
    TYPE t1;
    TYPE t2;

    function __init(string name = NAME, int age = AGE, TYPE t1 = NAME, TYPE t2 = AGE) {
        self.name = name;
        self.age = age;
        self.t1 = t1;
        self.t2 = t2;
    }

    function getDetails() returns [string, int, TYPE, TYPE] {
        return [self.name, self.age, self.t1, self.t2];
    }
};

function testConstRefsAsDefaultValue() returns (boolean) {
    Student10 s = new("Andrew");
    [string , int, TYPE, TYPE ] details = s.getDetails();
    return details[0] == "Andrew" && details[1] == 11 && details[2] == "John" && details[3] == 11;
}
