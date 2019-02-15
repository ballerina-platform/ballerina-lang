type State "on"|"off";

function finiteAssignmentStateType() returns State {
    State p = "on";
    State comparator = "on";
    if (p == comparator) {
       p = "off";
    }
    return p;
}

type NumberSet 1|2|3|4|5;

function finiteAssignmentNumberSetType() returns NumberSet {
    NumberSet n = 1;
    NumberSet comparator = 1;
    if (n == comparator) {
       n = 5;
    }
    return n;
}

type StringOrInt int|string;

function finiteAssignmentStringOrIntSetType() returns StringOrInt {
    StringOrInt si = 1;
    StringOrInt comparator = 1;
    if (si == comparator) {
       si = "This is a string";
    }
    return si;
}

function finiteAssignmentStringOrIntSetTypeCaseTwo() returns StringOrInt {
    StringOrInt si = "This is a string";
    StringOrInt comparator = "This is a string";
    if (si == comparator) {
       si = 111;
    }
    return si;
}

type Int int;

function finiteAssignmentIntSetType() returns Int {
    Int si = 1;
    Int comparator = 1;
    if (si == comparator) {
       si = 222;
    }
    return si;
}

function finiteAssignmentIntArrayType() returns Int {
    Int[] si = [];
    si[0] = 10001;
    si[1] = 2345;
    Int comparator = 2345;
    if (si[1] == comparator){
        si[1] = 9989;
    }
    return si[1];
}

function finiteAssignmentStateSameTypeComparison() returns int {
    State a = "off";
    State b = "on";
    if (a == b){
       return 1;
    }
    return 2;
}

function finiteAssignmentStateSameTypeComparisonCaseTwo() returns State {
    State a = "off";
    State b = "on";
    if (a != b){
       a = b;
       return a;
    }
    return b;
}

type POrInt Person|int;

type Person record {
   string name;
};

function finiteAssignmentRefValueType() returns POrInt {
    Person p = {name:"abc"};
    POrInt pi = p;
    return pi;
}

function finiteAssignmentRefValueTypeCaseTwo() returns POrInt {
    Person p = {name:"abc"};
    POrInt pi = 4;
    return pi;
}

type PreparedResult "ss"|"sss"|"qqq";

function testFiniteTypeWithTypeCheck() returns PreparedResult {
    var result = foo();
    if (result is PreparedResult) {
        return result;
    } else {
        return "qqq";
    }
}

function foo() returns PreparedResult | error | () {
       PreparedResult x = "ss";
       return x;
}


function testFiniteTypesWithDefaultValues() returns State {
   return assignFiniteValueAsDefaultParam();
}

function assignFiniteValueAsDefaultParam(State cd = "on") returns State {
   Channel c = new(b = cd);
   return c.b ?: "off";
}

type Channel object {

    public State? b;

    function __init (State b = "off", boolean a = true){
        self.b = b;
        State o =  "on";
        if(self.b == o) {
           int i = 4;
        }
    }
};

type CombinedState "on"|"off"|int;

function testFiniteTypesWithUnion() returns CombinedState {
   CombinedState abc = 1;
   return abc;
}

function testFiniteTypesWithUnionCaseOne() returns CombinedState {
   CombinedState abc = "off";
   if( abc == "off"){
       return 100;
   }
   return 0;
}

function testFiniteTypesWithUnionCaseTwo() returns CombinedState {
   CombinedState abc = "off";
   if( abc == "off"){
       return "on";
   }
   return "off";
}

function testFiniteTypesWithUnionCaseThree() returns int {
   CombinedState abc = "off";
   if( abc == "off"){
       return 1001;
   }
   return 1002;
}

function testFiniteTypesWithTuple() returns State {
   State onState = "on";
   (State, int ) b = (onState, 20);
   var (i, j) = b;
   return i;
}

type TypeAliasOne Person;

type TypeAliasTwo TypeAliasOne;

type TypeAliasThree TypeAliasTwo;

function testTypeAliasing() returns string {
    TypeAliasThree p = {name:"Anonymous name"};
    return p.name;
}

type MyType int|string;

function testTypeAliasingCaseOne() returns (MyType,MyType) {
     MyType a = 100;
     MyType b = "hundred";
     return (a,b);
}

public type ParamTest string|int;

function testTypeDefinitionWithVarArgs() returns (ParamTest, ParamTest) {
    string s1 = "Anne";
    ParamTest p1 = testVarArgs("John");
    ParamTest p2 = testVarArgs(s1);
    return (p1, p2);
}

function testVarArgs(ParamTest... p1) returns ParamTest {
    return p1[0];
}

type ArrayCustom int[];

function testTypeDefinitionWithArray() returns (int, int) {
    ArrayCustom val = [34, 23];
    return (val.length() , val[1]);
}

type FuncType function (string) returns int;

function testTypeDefWithFunctions() returns int {
    FuncType fn = function (string s) returns int {
        return s.length();
    };
    return fn.call("Hello");
}

type FuncType2 (function (string) returns int)|string;

function testTypeDefWithFunctions2() returns int {
    FuncType2 fn = function (string s) returns int {
        return s.length();
    };

    if (fn is function (string) returns int) {
        return fn.call("Hello");
    }

    return -1;
}

const int ICON = 5;
const string SCON = "s";

type FiniteType ICON|SCON;

function testFiniteTypeWithConstants() returns (FiniteType, FiniteType) {
    FiniteType f = 5;
    FiniteType s = "s";

    return (f,s);
}
