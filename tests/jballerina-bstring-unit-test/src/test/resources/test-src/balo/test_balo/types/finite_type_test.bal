import finiteTypeTest/foo;

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
    var x = foo();
    if (x is foo:PreparedResult) {
        return x;
    } else {
        return "qqq";
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
    [foo:State, int] b = [onState, 20];
    var [i, j] = b;
    return i;
}

function testTypeAliasing() returns string {
    foo:TypeAliasThree p = {name: "Anonymous name"};
    return p.name;
}

function testTypeAliasingCaseOne() returns [foo:MyType, foo:MyType] {
    foo:MyType a = 100;
    foo:MyType b = "hundred";
    return [a, b];
}

function testTypeDefinitionWithVarArgs() returns [foo:ParamTest, foo:ParamTest] {
    string s1 = "Anne";
    foo:ParamTest p1 = testVarArgs("John");
    foo:ParamTest p2 = testVarArgs(s1);
    return [p1, p2];
}

function testVarArgs(foo:ParamTest... p1) returns foo:ParamTest {
    return p1[0];
}

function testTypeDefinitionWithArray() returns [int, int] {
    foo:ArrayCustom val = [34, 23];
    return [val.length() , val[1]];
}

function testTypeDefinitionWithByteArray() returns [int, byte] {
    foo:ByteArrayType val = [34, 23];
    return [val.length() , val[1]];
}

function testFiniteAssignmentByteType() returns foo:ByteType {
    foo:ByteType si = 123;
    foo:ByteType comparator = 123;
    if (si == comparator) {
        si = 222;
    }
    return si;
}

function testByteTypeDefinitionWithVarArgs() returns [foo:BFType, foo:BFType] {
    byte a = 34;
    float f = 4.5;
    foo:BFType p1 = testVarByteArgs(a);
    foo:BFType p2 = testVarByteArgs(f);
    return [p1, p2];
}

function testVarByteArgs(foo:BFType... p1) returns foo:BFType {
    return p1[0];
}

function testTypeDefWithFunctions() returns int {
    foo:BFuncType fn = function (string s) returns int {
        return s.length();
    };
    return fn("Hello");
}

function testTypeDefWithFunctions2() returns int {
    foo:BFuncType2 fn = function (string s) returns int {
        return s.length();
    };

    if (fn is function (string) returns int) {
        return fn("Hello");
    }

    return -1;
}

function testStringOnlyFiniteTypeAssignmentToTypeWithString() returns boolean {
    foo:State a = "on";
    string b = a;
    boolean assignmentSuccessful = a == b;

    string|boolean c = a;
    return assignmentSuccessful && a == c;
}

function testIntOnlyFiniteTypeAssignmentToTypeWithInt() returns boolean {
    foo:NumberSet a = 2;
    int b = a;
    boolean assignmentSuccessful = a == b;

    string|int c = a;
    return assignmentSuccessful && a == c;
}

function testFloatOnlyFiniteTypeAssignmentToTypeWithFloat() returns boolean {
    foo:FloatValue a = 2.0;
    float b = a;
    boolean assignmentSuccessful = a == b;

    float|int c = a;
    return assignmentSuccessful && a == c;
}

function testBooleanOnlyFiniteTypeAssignmentToTypeWithBoolean() returns boolean {
    foo:BooleanValue a = true;
    boolean b = a;
    boolean assignmentSuccessful = a == b;

    anydata c = a;
    return assignmentSuccessful && a == c;
}

function testByteOnlyFiniteTypeAssignmentToTypeWithByte() returns boolean {
    foo:ByteValue a = 12;
    byte b = a;
    boolean assignmentSuccessful = a == b;

    byte|foo:Person c = a;
    return assignmentSuccessful && a == c;
}

function testFiniteTypeAssignmentToBroaderType() returns boolean {
    foo:CombinedState a = "off";
    string|int b = a;
    boolean assignmentSuccessful = a == b;

    anydata c = a;
    assignmentSuccessful = assignmentSuccessful && a == c;

    foo:StringOrInt d = a;
    assignmentSuccessful = assignmentSuccessful && a == d;

    b = d;
    return assignmentSuccessful && a == d;
}

function testFiniteTypeWithConstAssignmentToBroaderType() returns boolean {
    foo:AB ab = foo:A;
    string s = ab;
    boolean assignmentSuccessful = ab == s;

    ab = "b";
    s = ab;
    return assignmentSuccessful && ab == s;
}

function testFiniteTypeWithConstAndTypeAssignmentToBroaderType() returns boolean {
    foo:ABInt ab = foo:A;
    foo:AB|int s = ab;
    boolean assignmentSuccessful = ab == s;

    ab = "b";
    string|int s2 = ab;
    assignmentSuccessful = assignmentSuccessful && ab == s2;

    ab = 12;
    s2 = ab;
    return assignmentSuccessful && ab == s2;
}

function testFiniteTypesAsUnionsAsBroaderTypes_1() returns boolean {
    foo:W a = "foo";
    foo:X b = a;
    boolean assignmentSuccessful = a == b && b == foo:FOO;

    a = true;
    b = a;
    assignmentSuccessful = assignmentSuccessful && a == b && b == true;

    a = 2.0;
    foo:Y c = a;
    assignmentSuccessful = assignmentSuccessful && a == c && c == 2.0;

    a = 1;
    foo:Z d = a;
    return assignmentSuccessful && a == d && a == 1;
}

function testFiniteTypesAsUnionsAsBroaderTypes_2() returns boolean {
    foo:X a = true;
    foo:Y b = a;
    boolean assignmentSuccessful = a == b && a == true;

    b = 2.0;
    foo:Z c = b;
    return assignmentSuccessful && b == c && c == 2.0;
}
