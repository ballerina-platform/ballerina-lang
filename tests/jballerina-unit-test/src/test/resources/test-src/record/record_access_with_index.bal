import ballerina/io;

type Department record {
    string dptName = "";
    Person[] employees = [];
};

type Person record {
    string name = "default first name";
    string lname = "";
    map<any> adrs = {};
    int age = 999;
    Family family = {};
};

type Family record {
    string spouse = "";
    int noOfChildren?;
    string[] children = [];
};

function testCreateStruct () returns [string?, map<any>?, int?] {
    map<any> address1 = {};
    map<any> address = {"country":"USA", "state":"CA"};
    Person emp = {name:"Jack", adrs:address, age:25};
    return [emp["name"], emp["adrs"], emp["age"]];
}

function testStructOfStruct () returns (string) {

    map<any> address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {employees:emps};

    var result = dpt["employees"][0]["adrs"]["country"];
    string country = <string> result;
    return country;
}

function testReturnStructAttributes () returns string? {
    map<any> address = {"country":"USA", "state":"CA"};
    string[] chldrn = [];
    Family fmly = {children:chldrn};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};
    Person emp2 = {};
    Person[] employees = [emp1, emp2];
    Department dpt = {employees:employees};

    dpt["employees"][0]["family"]["children"][0] = "emily";

    return dpt["employees"][0]["family"]["children"][0];
}

function testExpressionAsIndex () returns string {
    Family family = {spouse:"Kate"};
    int a = 2;
    int b = 5;
    family.children = ["Emma", "Rose", "Jane"];
    return family.children[a * b - 8];
}

function testStructExpressionAsIndex () returns string? {
    string country = "";
    Department dpt = {};
    Family fmly = {};
    fmly.children = [];
    Person emp2 = {};
    map<any> address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};

    emp1["adrs"]["street"] = "20";
    emp1["age"] = 0;

    dpt["employees"] = [emp1, emp2];
    dpt["employees"][0]["family"]["children"][0] = "emily";
    dpt["employees"][0]["family"]["noOfChildren"] = 1;

    return dpt["employees"][0]["family"]["children"][(dpt["employees"][0]["family"]["noOfChildren"] ?: 1) - 1];
}

function testDefaultVal () returns [string?, string?, int?] {
    Person p = {};
    return [p["name"], p["lname"], p["age"]];
}

function testNestedFieldDefaultVal () returns [string?, string?, int?] {
    Department dpt = {};
    dpt["employees"] = [];
    dpt["employees"][0] = {lname:"Smith"};
    return [dpt["employees"][0]["name"], dpt["employees"][0]["lname"], dpt["employees"][0]["age"]];
}

type StructField record {
    string key;
};

function testExpressionAsStructIndex() returns string {
    StructField nameField = { key: "name" };
    Person emp = { name: "Jack", adrs: { "country": "USA", "state": "CA" }, age: 25 };
    var result = emp[nameField.key];
    if result is string {
        return result;
    } else {
        return "fail";
    }
}

type Foo record {|
    string fieldOne;
    int fieldTwo;
    boolean fieldThree;
    () fieldFour;
    float fieldFive;
    decimal fieldSix;
|};

function testDynamicIndexAccessTypes() returns string {
    Foo f = {
        fieldOne: "string",
        fieldTwo: 100,
        fieldThree: true,
        fieldFour: (),
        fieldFive: 25.5,
        fieldSix: 96.9
    };

    string result = "";

    string index = "fieldOne";
    var res1 = f[index];
    if (res1 is string) {
        result += io:sprintf("string:%s", res1);
    }

    index = "fieldTwo";
    string|int|boolean|()|float|decimal res2 = f[index];
    if (res2 is int) {
        result += io:sprintf(":int:%s", res2);
    }

    index = "fieldThree";
    res2 = f[index];
    if (res2 is boolean) {
        result += io:sprintf(":boolean:%s", res2);
    }

    index = "fieldFour";
    string|int|boolean|()|float|decimal res4 = f[index];
    if (res4 is ()) {
        result += io:sprintf(":():%s", res4);
    }

    index = "fieldFive";
    var res5 = f[index];
    if (res5 is float) {
        result += io:sprintf(":float:%s", res5);
    }

    index = "fieldSix";
    res5 = f[index];
    if (res5 is decimal) {
        result += io:sprintf(":decimal:%s", res5);
    }

    return result;
}

type Bar record {|
    int fieldOne;
    string|float fieldTwo;
    boolean...;
|};

function testDynamicIndexAccessTypesWithRestParam(string arg) returns string {
    Bar f = {
        fieldOne: 50,
        fieldTwo: "string",
        "fieldThree": true
    };

    string result = "";

    int|string|float|boolean? res1 = f[arg];
    if (res1 is int) {
        result += io:sprintf(":int:%s", res1);
    }
    if (res1 is string) {
        result += io:sprintf(":string:%s", res1);
    }
    if (res1 is boolean) {
        result += io:sprintf(":boolean:%s", res1);
    }
    if (res1 is ()) {
        result += "()";
    }
    return result;
}

class Obj {
    private int intField;

    function init() {
        self.intField = 10;
    }

    function getIntField() returns int {
        return self.intField;
    }
}

type FooBar record {
    Obj fieldOne?;
    function (int) returns int fieldTwo;
    json fieldThree;
};

function testDynamicIndexAccessTypesWithOpenRecord() returns string {
    Obj obj = new;
    function (int) returns int aFn = x => x * 2;
    json jVal = "json-string";

    string result = "";

    FooBar fb = { fieldOne: obj, fieldTwo: aFn, fieldThree: jVal, "fieldFour": true };
    int[5] indexArr = [1, 2, 3, 4, 5];

    foreach var index in indexArr {
        Obj|(function (int) returns int)|json|anydata|error res = fb[getIndex(index)];
        if (res is Obj) {
            result += io:sprintf(":object:%s", res.getIntField());
            continue;
        }
        if (res is function (int) returns int) {
            result += io:sprintf(":function:%s", res(8));
            continue;
        }
        if (res is ()) {
            result += ":():";
            continue;
        }
        if (res is boolean) {
            result += io:sprintf(":boolean:%s", res);
            continue;
        }
        if (res is json) {
            result += io:sprintf(":json:%s", res);
            continue;
        }
    }
    return result;
}

function getIndex(int index) returns string {
    match index {
        1 => {return "fieldOne";}
        2 => {return "fieldTwo";}
        3 => {return "fieldThree";}
        4 => {return "fieldFour";}
        _ => {return "fieldFive";}
    }
}

type Qux record {|
    int fieldOne?;
    int fieldTwo;
    int...;
|};

function testDynamicIndexAccessWithSingleType() returns int {
    Qux q = { fieldOne: 95, fieldTwo: 96, "fieldThree": 100 };
    string[] index = ["fieldOne", "fieldTwo", "fieldThree"];
    int marks = 0;

    int? 'field = q[index[0]];
    if 'field is int {
        marks += ('field * 2);
    }
    'field = q[index[1]];
    if 'field is int {
        marks += ('field * 2);
    }
    'field = q[index[2]];
    if 'field is int {
        marks += ('field * 2);
    }
    return marks;
}

type Quux record {|
    Qux fieldOne;
|};

function testDynamicIndexAccessWithRecordInsideRecord() returns [int?, int?] {
    Qux q = { fieldOne: 95, fieldTwo: 96, "fieldThree": 100 };
    Quux qu = { fieldOne: q };

    string index = "fieldOne";

    int? r1 = qu["fieldOne"]["fieldOne"];
    int? r2 = qu[index][index];

    return [r1, r2];
}

type Finite "fieldOne"|"fieldTwo"|"fieldThree"|"fieldFive";

type FooQux record {
    string|boolean fieldOne;
    Bar fieldTwo;
    float|Obj fieldThree;
    int fieldFour;
};

function testFiniteTypeAsIndex() returns string {
    Finite index1 = "fieldOne";
    Finite index2 = "fieldTwo";
    Finite index3 = "fieldThree";
    Finite index4 = "fieldFive";

    Bar bar = {
        fieldOne: 50,
        fieldTwo: "barField",
        "fieldThree": true
    };

    Qux q = { fieldOne: 95, fieldTwo: 96, "fieldThree": 100 };
    FooQux foo = { fieldOne: "string", fieldTwo: bar, fieldThree: 98.9, fieldFour: 12, "fieldFive": q };

    string|boolean|Bar|float|Obj|anydata|error r1 = foo[index1];
    var r2 = foo[index2];
    string|boolean|Bar|float|Obj|anydata|error r3 = foo[index3];
    string|boolean|Bar|float|Obj|anydata|error r4 = foo[index4];

    string result = "";
    if r1 is string {
        result += r1;
    }
    if r2 is Bar {
        int|string|float|boolean? r2s = r2[index2];
        if r2s is string {
            result += r2s;
        }
    }
    if r3 is float {
        result += io:sprintf("%s", r3);
    }
    if r4 is Qux {
        int? r5 = r4[index1];
        result += io:sprintf("%s", r5);
    }

    return result;
}

const string FIELD_FOUR = "fieldFour";
type FiniteOne "fieldOne"|"fieldTwo";
type FiniteTwo "fieldTwo"|"fieldThree";
type FiniteThree FiniteOne|FiniteTwo|FIELD_FOUR;

function testUnionInFiniteTypeAsIndex() returns string {
    FiniteThree index1 = "fieldOne";
    FiniteThree index2 = "fieldTwo";
    FiniteThree index3 = "fieldThree";
    FiniteThree index4 = "fieldFour";

    Foo foo = {
        fieldOne: "string",
        fieldTwo: 100,
        fieldThree: true,
        fieldFour: (),
        fieldFive: 25.5,
        fieldSix: 96.9
    };

    string|int|boolean|() r1 = foo[index1];
    var r2 = foo[index2];
    string|int|boolean? r3 = foo[index3];
    string|int|boolean|() r4 = foo[index4];

    string result = "";
    if r1 is string {
        result += r1;
    }
    if r2 is int {
        result += io:sprintf("%s", r2);
    }
    if r3 is boolean {
        result += io:sprintf("%s", r3);
    }
    if r4 is () {
        result += "()";
    }

    return result;
}

function testUnionInFiniteTypeAsIndexNoField() returns string {
    FiniteThree index = "fieldFour";

    Bar f = {
        fieldOne: 50,
        fieldTwo: "string",
        "fieldThree": true
    };

    var r1 = f[index];
    if (r1 is ()) {
        return "Passed";
    } else {
        return "Failed";
    }
}

function testGetNonInitAttribute () returns string? {
    Person emp1 = {};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {dptName:"HR", employees:emps};
    return dpt["employees"][0]["family"]["children"][0];
}

function testGetNonInitArrayAttribute () returns string? {
    Department dpt = {dptName:"HR"};
    return dpt["employees"][0]["family"]["children"][0];
}

function testGetNonInitLastAttribute () returns Person? {
    Department dpt = {};
    return dpt["employees"][0];
}

function testSetFieldOfNonInitChildStruct () {
    Person person = {name:"Jack"};
    person.family.spouse = "Jane";
}

function testSetFieldOfNonInitStruct () {
    Department dpt = {};
    dpt.dptName = "HR";
}
