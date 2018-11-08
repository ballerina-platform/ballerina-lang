import finiteTypeTest/foo version v1;

function finiteAssignmentStateType() returns foo:State {
    foo:State p = "on";
    foo:State comparator = "on";
    if (p == comparator) {
        p = "off";
    }
    return p;
}

function finiteAssignmentNumberSetType() returns foo:NumberSet {
    foo:NumberSet n = 1;
    foo:NumberSet comparator = 1;
    if (n == comparator) {
        n = 5;
    }
    return n;
}

function finiteAssignmentStringOrIntSetType() returns foo:StringOrInt {
    foo:StringOrInt si = 1;
    foo:StringOrInt comparator = 1;
    if (si == comparator) {
        si = "This is a string";
    }
    return si;
}

function finiteAssignmentStringOrIntSetTypeCaseTwo() returns foo:StringOrInt {
    foo:StringOrInt si = "This is a string";
    foo:StringOrInt comparator = "This is a string";
    if (si == comparator) {
        si = 111;
    }
    return si;
}

function finiteAssignmentIntSetType() returns foo:Int {
    foo:Int si = 1;
    foo:Int comparator = 1;
    if (si == comparator) {
        si = 222;
    }
    return si;
}

function finiteAssignmentIntArrayType() returns foo:Int {
    foo:Int[] si = [];
    si[0] = 10001;
    si[1] = 2345;
    foo:Int comparator = 2345;
    if (si[1] == comparator){
        si[1] = 9989;
    }
    return si[1];
}

function finiteAssignmentStateSameTypeComparison() returns int {
    foo:State a = "off";
    foo:State b = "on";
    if (a == b){
        return 1;
    }
    return 2;
}

function finiteAssignmentStateSameTypeComparisonCaseTwo() returns foo:State {
    foo:State a = "off";
    foo:State b = "on";
    if (a != b){
        a = b;
        return a;
    }
    return b;
}

function finiteAssignmentRefValueType() returns foo:POrInt {
    foo:Person p = {name: "abc"};
    foo:POrInt pi = p;
    return pi;
}

function finiteAssignmentRefValueTypeCaseTwo() returns foo:POrInt {
    foo:Person p = {name: "abc"};
    foo:POrInt pi = 4;
    return pi;
}

function testFiniteTypeWithMatch() returns foo:PreparedResult {
    match foo() {
        foo:PreparedResult x => return x;
        () => return "qqq";
        error => return "qqq";
    }
}

function foo() returns foo:PreparedResult|error|() {
    foo:PreparedResult x = "ss";
    return x;
}


function testFiniteTypesWithDefaultValues() returns foo:State {
    return assignFiniteValueAsDefaultParam();
}

function assignFiniteValueAsDefaultParam(foo:State cd = "on") returns foo:State {
    foo:Channel c = new(b = cd);
    return c.b ?: "off";
}

function testFiniteTypesWithUnion() returns foo:CombinedState {
    foo:CombinedState abc = 1;
    return abc;
}

function testFiniteTypesWithUnionCaseOne() returns foo:CombinedState {
    foo:CombinedState abc = "off";
    if (abc == "off"){
        return 100;
    }
    return 0;
}

function testFiniteTypesWithUnionCaseTwo() returns foo:CombinedState {
    foo:CombinedState abc = "off";
    if (abc == "off"){
        return "on";
    }
    return "off";
}

function testFiniteTypesWithUnionCaseThree() returns int {
    foo:CombinedState abc = "off";
    if (abc == "off"){
        return 1001;
    }
    return 1002;
}

function testFiniteTypesWithTuple() returns foo:State {
    foo:State onState = "on";
    (foo:State, int) b = (onState, 20);
    var (i, j) = b;
    return i;
}

function testTypeAliasing() returns string {
    foo:TypeAliasThree p = {name: "Anonymous name"};
    return p.name;
}

function testTypeAliasingCaseOne() returns (foo:MyType, foo:MyType) {
    foo:MyType a = 100;
    foo:MyType b = "hundred";
    return (a, b);
}

function testTypeDefinitionWithVarArgs() returns (foo:ParamTest, foo:ParamTest) {
    string s1 = "Anne";
    foo:ParamTest p1 = testVarArgs("John");
    foo:ParamTest p2 = testVarArgs(s1);
    return (p1, p2);
}

function testVarArgs(foo:ParamTest... p1) returns foo:ParamTest {
    return p1[0];
}

function testTypeDefinitionWithArray() returns (int, int) {
    foo:ArrayCustom val = [34, 23];
    return (val.length() , val[1]);
}

function testTypeDefinitionWithByteArray() returns (int, byte) {
    foo:ByteArrayType val = [34, 23];
    return (val.length() , val[1]);
}

function testFiniteAssignmentByteType() returns foo:ByteType {
    foo:ByteType si = 123;
    foo:ByteType comparator = 123;
    if (si == comparator) {
        si = 222;
    }
    return si;
}

function testByteTypeDefinitionWithVarArgs() returns (foo:BFType, foo:BFType) {
    byte a = 34;
    float f = 4.5;
    foo:BFType p1 = testVarByteArgs(a);
    foo:BFType p2 = testVarByteArgs(f);
    return (p1, p2);
}

function testVarByteArgs(foo:BFType... p1) returns foo:BFType {
    return p1[0];
}
