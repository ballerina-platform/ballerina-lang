import ballerina/module1;

boolean moduleFlag1 = false;
boolean moduleFlag2 = true;

function testNestedTernaryExpr2(boolean flag1, boolean flag2, boolean flag3) {
    return flag1 == flag3 ? flag1 && moduleFlag2 : 0;
}

function testNestedTernaryExpr3(boolean flag1, boolean flag2, boolean flag3) {
    module1:TestRecord1 rec1 = {};
    return (flag1 == flag3 ? moduleFlag1 && flag2 : flag1 || flag3) ? rec1 : 0 + 32;
}

string? moduleNullableStr = ();
int? moduleNullableInt = 1;

function testNestedElvisExpr1(string? nullableStr) {
    return nullableStr ?: false;
}

function testNestedElvisExpr2(string? nullableStr, int? nullableInt, boolean? nullableBool) {
    module1:TestRecord1? nullableRec = {};
    return moduleNullableStr ?: moduleNullableInt ?: nullableBool ?: nullableRec ?: 0.0;
}
