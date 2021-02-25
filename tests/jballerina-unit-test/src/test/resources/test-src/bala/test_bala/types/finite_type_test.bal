import finitetypetest/finite_type_project;

function finiteAssignmentStateType() returns finite_type_project:State {
    finite_type_project:State p = "on";
    finite_type_project:State comparator = "on";
    if (p == comparator) {
        p = "off";
    }
    return p;
}

function finiteAssignmentNumberSetType() returns finite_type_project:NumberSet {
    finite_type_project:NumberSet n = 1;
    finite_type_project:NumberSet comparator = 1;
    if (n == comparator) {
        n = 5;
    }
    return n;
}

function finiteAssignmentStringOrIntSetType() returns finite_type_project:StringOrInt {
    finite_type_project:StringOrInt si = 1;
    finite_type_project:StringOrInt comparator = 1;
    if (si == comparator) {
        si = "This is a string";
    }
    return si;
}

function finiteAssignmentStringOrIntSetTypeCaseTwo() returns finite_type_project:StringOrInt {
    finite_type_project:StringOrInt si = "This is a string";
    finite_type_project:StringOrInt comparator = "This is a string";
    if (si == comparator) {
        si = 111;
    }
    return si;
}

function finiteAssignmentIntSetType() returns finite_type_project:Int {
    finite_type_project:Int si = 1;
    finite_type_project:Int comparator = 1;
    if (si == comparator) {
        si = 222;
    }
    return si;
}

function finiteAssignmentIntArrayType() returns finite_type_project:Int {
    finite_type_project:Int[] si = [];
    si[0] = 10001;
    si[1] = 2345;
    finite_type_project:Int comparator = 2345;
    if (si[1] == comparator){
        si[1] = 9989;
    }
    return si[1];
}

function finiteAssignmentStateSameTypeComparison() returns int {
    finite_type_project:State a = "off";
    finite_type_project:State b = "on";
    if (a == b){
        return 1;
    }
    return 2;
}

function finiteAssignmentStateSameTypeComparisonCaseTwo() returns finite_type_project:State {
    finite_type_project:State a = "off";
    finite_type_project:State b = "on";
    if (a != b){
        a = b;
        return a;
    }
    return b;
}

function finiteAssignmentRefValueType() returns finite_type_project:POrInt {
    finite_type_project:Person p = {name: "abc"};
    finite_type_project:POrInt pi = p;
    return pi;
}

function finiteAssignmentRefValueTypeCaseTwo() returns finite_type_project:POrInt {
    finite_type_project:Person p = {name: "abc"};
    finite_type_project:POrInt pi = 4;
    return pi;
}

function testFiniteTypeWithMatch() returns finite_type_project:PreparedResult {
    var x = foo();
    if (x is finite_type_project:PreparedResult) {
        return x;
    } else {
        return "qqq";
    }
}

function foo() returns finite_type_project:PreparedResult|error|() {
    finite_type_project:PreparedResult x = "ss";
    return x;
}


function testFiniteTypesWithDefaultValues() returns finite_type_project:State {
    return assignFiniteValueAsDefaultParam();
}

function assignFiniteValueAsDefaultParam(finite_type_project:State cd = "on") returns finite_type_project:State {
    finite_type_project:Channel c = new(b = cd);
    return c.b ?: "off";
}

function testFiniteTypesWithUnion() returns finite_type_project:CombinedState {
    finite_type_project:CombinedState abc = 1;
    return abc;
}

function testFiniteTypesWithUnionCaseOne() returns finite_type_project:CombinedState {
    finite_type_project:CombinedState abc = "off";
    if (abc == "off"){
        return 100;
    }
    return 0;
}

function testFiniteTypesWithUnionCaseTwo() returns finite_type_project:CombinedState {
    finite_type_project:CombinedState abc = "off";
    if (abc == "off"){
        return "on";
    }
    return "off";
}

function testFiniteTypesWithUnionCaseThree() returns int {
    finite_type_project:CombinedState abc = "off";
    if (abc == "off"){
        return 1001;
    }
    return 1002;
}

function testFiniteTypesWithTuple() returns finite_type_project:State {
    finite_type_project:State onState = "on";
    [finite_type_project:State, int] b = [onState, 20];
    var [i, j] = b;
    return i;
}

function testTypeAliasing() returns string {
    finite_type_project:TypeAliasThree p = {name: "Anonymous name"};
    return p.name;
}

function testTypeAliasingCaseOne() returns [finite_type_project:MyType, finite_type_project:MyType] {
    finite_type_project:MyType a = 100;
    finite_type_project:MyType b = "hundred";
    return [a, b];
}

function testTypeDefinitionWithVarArgs() returns [finite_type_project:ParamTest, finite_type_project:ParamTest] {
    string s1 = "Anne";
    finite_type_project:ParamTest p1 = testVarArgs("John");
    finite_type_project:ParamTest p2 = testVarArgs(s1);
    return [p1, p2];
}

function testVarArgs(finite_type_project:ParamTest... p1) returns finite_type_project:ParamTest {
    return p1[0];
}

function testTypeDefinitionWithArray() returns [int, int] {
    finite_type_project:ArrayCustom val = [34, 23];
    return [val.length() , val[1]];
}

function testTypeDefinitionWithByteArray() returns [int, byte] {
    finite_type_project:ByteArrayType val = [34, 23];
    return [val.length() , val[1]];
}

function testFiniteAssignmentByteType() returns finite_type_project:ByteType {
    finite_type_project:ByteType si = 123;
    finite_type_project:ByteType comparator = 123;
    if (si == comparator) {
        si = 222;
    }
    return si;
}

function testByteTypeDefinitionWithVarArgs() returns [finite_type_project:BFType, finite_type_project:BFType] {
    byte a = 34;
    float f = 4.5;
    finite_type_project:BFType p1 = testVarByteArgs(a);
    finite_type_project:BFType p2 = testVarByteArgs(f);
    return [p1, p2];
}

function testVarByteArgs(finite_type_project:BFType... p1) returns finite_type_project:BFType {
    return p1[0];
}

function testTypeDefWithFunctions() returns int {
    finite_type_project:BFuncType fn = function (string s) returns int {
        return s.length();
    };
    return fn("Hello");
}

function testTypeDefWithFunctions2() returns int {
    finite_type_project:BFuncType2 fn = function (string s) returns int {
        return s.length();
    };

    if (fn is function (string) returns int) {
        return fn("Hello");
    }

    return -1;
}

function testStringOnlyFiniteTypeAssignmentToTypeWithString() returns boolean {
    finite_type_project:State a = "on";
    string b = a;
    boolean assignmentSuccessful = a == b;

    string|boolean c = a;
    return assignmentSuccessful && a == c;
}

function testIntOnlyFiniteTypeAssignmentToTypeWithInt() returns boolean {
    finite_type_project:NumberSet a = 2;
    int b = a;
    boolean assignmentSuccessful = a == b;

    string|int c = a;
    return assignmentSuccessful && a == c;
}

function testFloatOnlyFiniteTypeAssignmentToTypeWithFloat() returns boolean {
    finite_type_project:FloatValue a = 2.0;
    float b = a;
    boolean assignmentSuccessful = a == b;

    float|int c = a;
    return assignmentSuccessful && a == c;
}

function testBooleanOnlyFiniteTypeAssignmentToTypeWithBoolean() returns boolean {
    finite_type_project:BooleanValue a = true;
    boolean b = a;
    boolean assignmentSuccessful = a == b;

    anydata c = a;
    return assignmentSuccessful && a == c;
}

function testByteOnlyFiniteTypeAssignmentToTypeWithByte() returns boolean {
    finite_type_project:ByteValue a = 12;
    byte b = a;
    boolean assignmentSuccessful = a == b;

    byte|finite_type_project:Person c = a;
    return assignmentSuccessful && a == c;
}

function testFiniteTypeAssignmentToBroaderType() returns boolean {
    finite_type_project:CombinedState a = "off";
    string|int b = a;
    boolean assignmentSuccessful = a == b;

    anydata c = a;
    assignmentSuccessful = assignmentSuccessful && a == c;

    finite_type_project:StringOrInt d = a;
    assignmentSuccessful = assignmentSuccessful && a == d;

    b = d;
    return assignmentSuccessful && a == d;
}

function testFiniteTypeWithConstAssignmentToBroaderType() returns boolean {
    finite_type_project:AB ab = finite_type_project:A;
    string s = ab;
    boolean assignmentSuccessful = ab == s;

    ab = "b";
    s = ab;
    return assignmentSuccessful && ab == s;
}

function testFiniteTypeWithConstAndTypeAssignmentToBroaderType() returns boolean {
    finite_type_project:ABInt ab = finite_type_project:A;
    finite_type_project:AB|int s = ab;
    boolean assignmentSuccessful = ab == s;

    ab = "b";
    string|int s2 = ab;
    assignmentSuccessful = assignmentSuccessful && ab == s2;

    ab = 12;
    s2 = ab;
    return assignmentSuccessful && ab == s2;
}

function testFiniteTypesAsUnionsAsBroaderTypes_1() returns boolean {
    finite_type_project:W a = "foo";
    finite_type_project:X b = a;
    boolean assignmentSuccessful = a == b && b == finite_type_project:FOO;

    a = true;
    b = a;
    assignmentSuccessful = assignmentSuccessful && a == b && b == true;

    a = 2.0;
    finite_type_project:Y c = a;
    assignmentSuccessful = assignmentSuccessful && a == c && c == 2.0;

    a = 1;
    finite_type_project:Z d = a;
    return assignmentSuccessful && a == d && a == 1;
}

function testFiniteTypesAsUnionsAsBroaderTypes_2() returns boolean {
    finite_type_project:X a = true;
    finite_type_project:Y b = a;
    boolean assignmentSuccessful = a == b && a == true;

    b = 2.0;
    finite_type_project:Z c = b;
    return assignmentSuccessful && b == c && c == 2.0;
}
