import ballerina/lang.errors;

struct Person {
    string name;
    int age;
    Person parent;
    json info;
    map address;
    int[] marks;
    any a;
    float score;
    boolean alive;
}

function testVarDeclarationWithAtLeaseOneNonDeclaredSymbol () (int, errors:TypeConversionError) {
    int a;
    float f = 10.0;
    var a, err = <int>f;
    return a, err;
}

function testIntToVarAssignment() (int) {
    var age = 81;
    return age;
}

function testMultipleIntToVarAssignment() (int,int,int,int) {
    var age, age1, age2, age3 = retFourInt();
    return age, age1, age2, age3;
}

function testMultipleIntToVarAssignmentWithUnderscore() (int,int) {
    var age, age1, _, _ = retFourInt();
    return age, age1;
}

function testMultipleIntToVarAssignmentWithUnderscoreOrderCaseOne() (int,int) {
    var age, _, age1, _ = retFourInt();
    return age, age1;
}

function testMultipleIntToVarAssignmentWithUnderscoreOrderCaseTwo() (int,int) {
    var age, _, _, age1 = retFourInt();
    return age, age1;
}

function retFourInt()(int,int,int,int){
    return 100, 200, 300, 400;
}

function testStringToVarAssignment() (string) {
    var name = "name";
    return name;
}

function testMultipleStringToVarAssignment() (string,string,string,string) {
    var name, name1, name2, name3 = retFourString();
    return name, name1, name2, name3;
}

function retFourString()(string,string,string,string){
    return "name_1", "name_2", "name_3", "name_4";
}

function testBooleanToVarAssignment() (boolean) {
    var isHappy = true;
    return isHappy;
}

function testIncompatibleJsonToStructWithErrors() (Person, errors:TypeConversionError) {
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
    var p, err = <Person> j;
    return p, err;
}


struct PersonA {
    string name;
    int age;
}

function testJsonToStructWithErrors() (PersonA, errors:TypeConversionError) {
    json j = {name:"supun"};

    var person, err = <PersonA> j;

    return person, err;
}

struct A {
    string x;
    int y;
}

struct B {
    string x;
}

function testCompatibleStructForceCasting()(A, errors:TypeCastError) {
    A a = {x: "x-valueof-a", y:4};
    B b = {x: "x-valueof-b"};

    b = (B) a;

    var c, err = (A) b;

    a.x = "updated-x-valueof-a";
    return c, err;
}

function testInCompatibleStructForceCasting()(A, errors:TypeCastError) {
    B b = {x: "x-valueof-b"};

    var a, err = (A) b;

    return a, err;
}

function testAnyToStringWithErrors()(string, errors:TypeCastError) {
    any a = 5;

    var s, err = (string) a;

    return s, err;
}

function testAnyNullToStringWithErrors()(string, errors:TypeCastError) {
    any a = null;

    var s, err = (string) a;

    return s, err;
}

function testAnyToBooleanWithErrors()(boolean, errors:TypeCastError) {
    any a = 5;

    var b, err = (boolean) a;

    return b, err;
}

function testAnyNullToBooleanWithErrors()(boolean, errors:TypeCastError) {
    any a = null;

    var b, err = (boolean) a;

    return b, err;
}

function testAnyToIntWithErrors()(int, errors:TypeCastError) {
    any a = "foo";

    var b, err = (int) a;

    return b, err;
}

function testAnyNullToIntWithErrors()(int, errors:TypeCastError) {
    any a = null;

    var b, err = (int) a;

    return b, err;
}

function testAnyToFloatWithErrors()(float, errors:TypeCastError) {
    any a = "foo";

    var b, err = (float) a;

    return b, err;
}

function testAnyNullToFloatWithErrors()(float, errors:TypeCastError) {
    any a = null;

    var b, err = (float) a;

    return b, err;
}

function testAnyToMapWithErrors()(map, errors:TypeCastError) {
    any a = "foo";

    var b, err = (map) a;

    return b, err;
}
