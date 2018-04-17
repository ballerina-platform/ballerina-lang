type State "on"|"off";

function testAssignmentStateType() returns State {
    State p = "on";
    if (p == "on") {
       p = "off";
    }
    return p;
}

type NumberSet 1|2|3|4|5;

type TypeAlias State|NumberSet;

type TypeAliasOfAlias TypeAlias;

function testAssignmentCompositeFiniteTypes() returns TypeAliasOfAlias {
    TypeAliasOfAlias v1 = "on";
    TypeAliasOfAlias v2 = 5;
    TypeAliasOfAlias v3;
    if (v1 != v2) {
       v3 = "off";
    }
    return v3;
}

function testTypeSetIntersectionCaseOne() returns State {
    TypeAliasOfAlias v1 = "on";
    State v2 = "off";
    State v3;
    if (v1 != v2) {
       v3 = "off";
    }
    return v3;
}

function testTypeSetIntersectionCaseTwo() returns int {
    int v1 = 2;
    NumberSet v2 = 1;
    int v3;
    if (v1 != v2) {
       v3 = 100;
    }
    return v3;
}

function testTypeSetIntersectionCaseThree() returns string {
    string v1 = "off";
    State v2 = "off";
    string v3 = "good";
    if (v1 == v2) {
       v3 = "very good";
    }
    return v3;
}

function testAssignmentNumberSetType() returns NumberSet {
    NumberSet n = 1;
    if (n == 1) {
       n = 5;
    }
    return n;
}

type StringOrInt int|string;

function testAssignmentStringOrIntSetType() returns StringOrInt {
    StringOrInt si = 1;
    if (si == 1) {
       si = "This is a string";
    }
    return si;
}

function testAssignmentStringOrIntSetTypeCaseTwo() returns StringOrInt {
    StringOrInt si = "This is a string";
    if (si == "This is a string") {
       si = 111;
    }
    return si;
}

type Int int;

function testAssignmentIntSetType() returns Int {
    Int si = 1;
    if (si == 1) {
       si = 222;
    }
    return si;
}

function testAssignmentIntArrayType() returns Int {
    Int[] si = [];
    si[0] = 10001;
    si[1] = 2345;
    if (si[1] == 2345){
        si[1] = 9989;
    }
    return si[1];
}

function testAssignmentStateSameTypeComparison() returns int {
    State a = "off";
    if (a == "on"){
       return 1;
    }
    return 2;
}

function testAssignmentStateSameTypeComparisonCaseTwo() returns State {
    State a = "off";
    if (a != "on"){
       a = "on";
       return a;
    }
    return "on";
}

type POrInt Person|int;

type Person {
   string name;
};

function testAssignmentRefValueType() returns POrInt {
    Person p = {name:"abc"};
    POrInt pi = p;
    return pi;
}

function testAssignmentRefValueTypeCaseTwo() returns POrInt {
    Person p = {name:"abc"};
    POrInt pi = 4;
    return pi;
}

type CONSTANT "constant";

function testRecursiveCallsSingletonParamReturn() returns CONSTANT {
   CONSTANT ac = getAndReturnConstant("constant");
   return ac;
}

function getAndReturnConstant(CONSTANT a) returns CONSTANT {
    return getAndReturnConstantTwo(a);
}

function getAndReturnConstantTwo(CONSTANT a) returns CONSTANT {
    return a;
}

function testSingletonInlineFunction() returns int {
   int r = passSingletons(1, 2, 3);
   return r;
}

type ONE 1;
type TWO 2;
type THREE 3;

function passSingletons(ONE a, TWO b, THREE c) returns TWO {
    return b;
}

THREE uniqueKey = 3;

function testSingletonAsGlobalVar() returns THREE {
    return uniqueKey;
}

function testSingletonArray() returns ONE {
    ONE[] oneArr = [];
    oneArr[0] = 1;
    oneArr[1] = 1;
    oneArr[2] = 1;
    oneArr[3] = 1;
    return oneArr[3];
}

function testSingletonToIntAssignment() returns int {
    ONE a = 1;
    int b = a;
    return b;
}

function testSingletonToFloatAssignment() returns float {
    ONE a = 1;
    float b = a;
    return b;
}

type BINARY_TRUE true;
type BINARY_FALSE false;

type BINARY_STATE BINARY_TRUE | BINARY_FALSE;

function testBooleanSingletons() returns boolean {
    BINARY_TRUE a = true;
    BINARY_STATE master = a;
    if( master == true){
       return a;
    }
    return false;
}

function testBooleanSingletonsCaseTwo() returns boolean {
    BINARY_FALSE a = false;
    BINARY_STATE master = a;
    if( master == false){
       return a;
    }
    return true;
}

type PreparedResult "ss"|"sss"|"qqq";

function testFiniteTypeWithMatch() returns PreparedResult {
     match foo() {
          PreparedResult x => return x;
          () => return "qqq";
          error => return "qqq";
     }
}

function foo() returns PreparedResult | error | () {
        PreparedResult x = "ss";
        return x;
}

function testFiniteTypeWithMatchCaseTwo() returns PreparedResult {
     match fooTwo() {
          PreparedResult x => return x;
          () => return "qqq";
          error => return "qqq";
     }
}

function fooTwo() returns PreparedResult | error | () {
     return ();
}

type UNIQUE_NUM 11211;

function testSingletonTypeWithMatch() returns int {
     match getUnique() {
          UNIQUE_NUM x => return x;
          () => return 0;
          error => return 0;
     }
}

function getUnique() returns UNIQUE_NUM | error | () {
        UNIQUE_NUM x = 11211;
        return x;
}

function testSingletonTypeWithMatchCaseTwo() returns int {
     match getUniqueTwo() {
          UNIQUE_NUM x => return x;
          () => return 0;
          error => return 0;
     }
}

function getUniqueTwo() returns UNIQUE_NUM | error | () {
     return ();
}