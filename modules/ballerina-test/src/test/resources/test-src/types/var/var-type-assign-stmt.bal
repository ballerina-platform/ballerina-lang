function testVarDeclarationWithAtLeaseOneNonDeclaredSymbol () (int, TypeConversionError) {
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

function testAnyToStringWithErrors()(string, TypeCastError) {
    any a = 5;

    var s, err = (string) a;

    return s, err;
}

function testAnyNullToStringWithErrors()(string, TypeCastError) {
    any a = null;

    var s, err = (string) a;

    return s, err;
}

function testAnyToBooleanWithErrors()(boolean, TypeCastError) {
    any a = 5;

    var b, err = (boolean) a;

    return b, err;
}

function testAnyNullToBooleanWithErrors()(boolean, TypeCastError) {
    any a = null;

    var b, err = (boolean) a;

    return b, err;
}

function testAnyToIntWithErrors()(int, TypeCastError) {
    any a = "foo";

    var b, err = (int) a;

    return b, err;
}

function testAnyNullToIntWithErrors()(int, TypeCastError) {
    any a = null;

    var b, err = (int) a;

    return b, err;
}

function testAnyToFloatWithErrors()(float, TypeCastError) {
    any a = "foo";

    var b, err = (float) a;

    return b, err;
}

function testAnyNullToFloatWithErrors()(float, TypeCastError) {
    any a = null;

    var b, err = (float) a;

    return b, err;
}

function testAnyToMapWithErrors()(map, TypeCastError) {
    any a = "foo";

    var b, err = (map) a;

    return b, err;
}
