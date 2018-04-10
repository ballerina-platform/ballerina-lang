type Person {
    string name;
    int age;
    Person parent;
    json info;
    map address;
    int[] marks;
    any a;
    float score;
    boolean alive;
};

function testIntToVarAssignment() returns (int) {
    var age = 81;
    return age;
}

function testMultipleIntToVarAssignment() returns (int,int,int,int) {
    var (age, age1, age2, age3) = retFourInt();
    return (age, age1, age2, age3);
}

function testMultipleIntToVarAssignmentWithUnderscore() returns (int,int) {
    var (age, age1, a, b) = retFourInt();
    return (age, age1);
}

function testMultipleIntToVarAssignmentWithUnderscoreOrderCaseOne() returns (int,int) {
    var (age, b, age1, c) = retFourInt();
    return (age, age1);
}

function testMultipleIntToVarAssignmentWithUnderscoreOrderCaseTwo() returns (int,int) {
    var (age, a, b, age1) = retFourInt();
    return (age, age1);
}

function retFourInt() returns (int,int,int,int){
    return (100, 200, 300, 400);
}

function testStringToVarAssignment() returns (string) {
    var name = "name";
    return name;
}

function testMultipleStringToVarAssignment() returns (string,string,string,string) {
    var (name, name1, name2, name3) = retFourString();
    return (name, name1, name2, name3);
}

function retFourString() returns (string,string,string,string){
    return ("name_1", "name_2", "name_3", "name_4");
}

function testBooleanToVarAssignment() returns (boolean) {
    var isHappy = true;
    return isHappy;
}

function testAnyToStringWithErrors() returns (string) {
    any a = 5;

    var s = <string> a;

    return s;
}

function testAnyNullToStringWithErrors() returns (string) {
    any a = null;

    var s = <string> a;

    return s;
}

function testAnyToBooleanWithErrors() returns (boolean | error) {
    any a = 5;

    var b = <boolean> a;

    return b;
}

function testAnyNullToBooleanWithErrors() returns (boolean | error) {
    any a = null;

    var b = <boolean> a;

    return b;
}

function testAnyToIntWithErrors() returns (int | error) {
    any a = "foo";

    var b = <int> a;

    return b;
}

function testAnyNullToIntWithErrors() returns (int | error) {
    any a = null;

    var b = <int> a;

    return b;
}

function testAnyToFloatWithErrors() returns (float | error) {
    any a = "foo";

    var b = <float> a;

    return b;
}

function testAnyNullToFloatWithErrors() returns (float | error) {
    any a = null;

    var b = <float> a;

    return b;
}

function testAnyToMapWithErrors() returns (map | error) {
    any a = "foo";

    var b = <map> a;

    return b;
}


function testIncompatibleJsonToStructWithErrors() returns (Person | error) {
    json j = { name:"Child",
                 age:25,
                 parent:{
                            name:"Parent",
                            age:50,
                            parent: "Parent",
                            address:{"city":"Colombo", "country":"SriLanka"},
                            info:null,
                            marks:null
                        },
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87,94,72]
             };
    var p = <Person> j;
    return p;
}

type PersonA {
    string name;
    int age;
};

function testJsonToStructWithErrors() returns (PersonA | error) {
    json j = {name:"supun", age:"25"};

    var p = <PersonA> j;

    return p;
}

type A {
    string x;
    int y;
};

type B {
    string x;
};

function testCompatibleStructForceCasting() returns (A | error) {
    A a = {x: "x-valueof-a", y:4};
    B b = {x: "x-valueof-b"};

    b = <B> a;

    var c = <A> b;

    a.x = "updated-x-valueof-a";
    return c;
}

function testInCompatibleStructForceCasting() returns (A | error) {
    B b = {x: "x-valueof-b"};

    var a = <A> b;

    return a;
}