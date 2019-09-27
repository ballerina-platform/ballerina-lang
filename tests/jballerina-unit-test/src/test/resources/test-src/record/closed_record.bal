type Department record {|
    string dptName = "";
    Person[] employees = [];
|};

type Person record {|
    string name = "default first name";
    string lname = "";
    map<any> adrs = {};
    int age = 999;
    Family family = {};
    Person? parent = ();
|};

type Family record {|
    string spouse = "";
    int noOfChildren = 0;
    string[] children = [];
|};

type Employee record {|
    string name = "default first name";
    string lname = "";
    map<any> address = {};
    int age = 999;
    Family family = {};
    Person? parent = ();
    string designation = "";
|};

function testStructOfStruct () returns (string) {

    map<any> address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {employees:emps};

    string country = "";
    var result = dpt.employees[0].adrs["country"];
    country = result is string ? result : "";
    return country;
}

function testReturnStructAttributes () returns (string) {
    map<any> address = {"country":"USA", "state":"CA"};
    string[] chldrn = [];
    Family fmly = {children:chldrn};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};
    Person emp2 = {};
    Person[] employees = [emp1, emp2];
    Department dpt = {employees:employees};

    dpt.employees[0].family.children[0] = "emily";

    return dpt.employees[0].family.children[0];
}

function testExpressionAsIndex () returns (string) {
    Family family = {spouse:"Kate"};
    int a = 2;
    int b = 5;
    family.children = ["Emma", "Rose", "Jane"];
    return family.children[a * b - 8];
}

function testStructExpressionAsIndex () returns string {
    string country;
    Department dpt = {};
    Family fmly = {};
    fmly.children = [];
    Person emp2 = {};
    map<any> address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};

    emp1.adrs["street"] = "20";
    emp1.age = 0;

    dpt.employees = [emp1, emp2];
    dpt.employees[0].family.children[0] = "emily";
    dpt.employees[0].family.noOfChildren = 1;

    return dpt.employees[0].family.children[dpt.employees[0].family.noOfChildren - 1];
}

function testDefaultVal () returns [string, string, int] {
    Person p = {};
    return [p.name, p.lname, p.age];
}

function testNestedFieldDefaultVal () returns [string, string, int] {
    Department dpt = {};
    dpt.employees = [];
    dpt.employees[0] = {lname:"Smith"};
    return [dpt.employees[0].name, dpt.employees[0].lname, dpt.employees[0].age];
}

function testNestedStructInit () returns Person {
    Person p1 = {name:"aaa", age:25, parent:{name:"bbb", age:50}};
    return p1;
}

type NegativeValTest record {|
    int negativeInt = -9;
    int negativeSpaceInt = -8;
    float negativeFloat = -88.234;
    float negativeSpaceFloat = -24.99;
|};

function getStructNegativeValues () returns [int, int, float, float] {
    NegativeValTest tmp = {};
    return [tmp.negativeInt, tmp.negativeSpaceInt, tmp.negativeFloat, tmp.negativeSpaceFloat];
}

function getStruct () returns Person {
    Person p1 = {name:"aaa", age:25, parent:{name:"bbb", lname:"ccc", age:50}};
    return p1;
}

function testGetNonInitAttribute () returns string {
    Person emp1 = {};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {dptName:"HR", employees:emps};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitArrayAttribute () returns string {
    Department dpt = {dptName:"HR"};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitLastAttribute () returns Person {
    Department dpt = {};
    return dpt.employees[0];
}

function testSetFieldOfNonInitChildStruct () {
    Person person = {name:"Jack"};
    person.family.spouse = "Jane";
}

function testSetFieldOfNonInitStruct () {
    Department dpt = {};
    dpt.dptName = "HR";
}

function testStructWithRecordKeyword() returns Employee {
    map<any> address = {"country":"USA", "state":"CA"};
    Employee emp = {name:"John", lname:"Doe", address:address, age:25, designation:"Software Engineer"};
    return emp;
}

type PersonA record {|
    string fname = "";
    string lname = "";
    function() returns string fullName?;
|};

function testFuncPtrAsRecordField() returns string {
    PersonA p = {fname:"John", lname:"Doe"};
    p.fullName = function () returns string {
        return p.lname + ", " + p.fname;
    };

    return p.fullName();
}

public type InMemoryModeConfig record {|
    string name = "";
    string username = "";
    string password = "";
    map<any> dbOptions = {};
|};

public type ServerModeConfig record {|
    string host;
    int port;
    *InMemoryModeConfig;
|};

public type EmbeddedModeConfig record {|
    string path;
    *InMemoryModeConfig;
|};

function testAmbiguityResolution() returns [string, string, string] {
    string s1 = init({});
    string s2 = init({host:"localhost", port:9090});
    string s3 = init({path:"localhost:9090"});
    return [s1, s2, s3];
}

function init(InMemoryModeConfig|ServerModeConfig|EmbeddedModeConfig rec) returns string {
    if (rec is ServerModeConfig) {
        return "Server mode configuration";
    } else if (rec is EmbeddedModeConfig) {
        return "Embedded mode configuration";
    } else {
        return "In-memory mode configuration";
    }
}

public type A record {|
    string a;
    string b;
    string c;
|};

public type B record {|
    string f;
    int g?;
    *A;
|};

public type C record {|
    string i;
    *A;
|};

function testAmbiguityResolution2() returns [string, string, string, string] {
    string s1 = resolve({a:"", b:"", c:""});
    string s2 = resolve({a:"", b:"", c:"", f:""});
    string s3 = resolve({a:"", b:"", c:"", f:"", g:0});
    string s4 = resolve({a:"", b:"", c:"", i:""});
    return [s1, s2, s3, s4];
}

function resolve(A|B|C rec) returns string {
    if (rec is A) {
        return "A";
    } else if (rec is B) {
        return "B";
    } else {
        return "C";
    }
}

public type EmptyRec1 record {||};

public type EmptyRec2 record {| |};

public type EmptyRec3 record {|
|};

public type EmptyRec4 record {|

|};

function testEmptyClosedRecords() returns record {||}[] {
    EmptyRec1 r1 = {};
    EmptyRec2 r2 = {};
    EmptyRec3 r3 = {};
    EmptyRec4 r4 = {};

    record {||}[] recArr= [r1, r2, r3, r4];
    return recArr;
}

type Foo record {|
    string bar;
    int baz;
|};

function testLiteralsAsMappingConstructorKeys() returns boolean {
    Foo f = { "bar": "hello", baz: 1 };
    return f.bar == "hello" && f.baz == 1;
}

type Baz record {|
    string s;
    int i?;
    float? f = ();
|};

string iValue = "i";

function testExpressionAsKeys() returns boolean {
    Baz b = { s: "hello", [iValue]: 1, [getChar("f")]: 2.0 };
    return b.s == "hello" && b?.i == 1 && b?.f == 2.0;
}

string mapValue = "";

function testExpressionAsKeysWithSameKeysDefinedAsLiteralsOrFieldNames() returns boolean {
    Baz b = {
        f: 1.0,
        [getChar("f")]: 4.0,
        [getChar("s")]: addStringToMapValue(" world"),
        [getChar("s")]: addStringToMapValue(" from Ballerina"),
        s: addStringToMapValue("hello")
    };
    return b.s == "hello world from Ballerina" && b?.f == 4.0;
}

function getChar(string st) returns string {
    return st;
}

function addStringToMapValue(string s) returns string {
    mapValue = mapValue + s;
    return mapValue;
}
