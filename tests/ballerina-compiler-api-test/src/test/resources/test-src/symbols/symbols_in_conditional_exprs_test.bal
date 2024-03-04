boolean moduleFlag1 = false;
boolean moduleFlag2 = true;

() var1 = moduleFlag1 ? true : false;
int var2 = moduleFlag1 ? "True" : false;
boolean var3 = moduleFlag1 ? "True" : false;
string var4 = moduleFlag1 ? "True" : moduleFlag2 ? false : 0;

function testTernaryExprWithSameType(boolean flag) {
    return flag ? true : false;
}

function testTernaryExprWithDifferentType(boolean flag) {
    return flag ? "True" : false;
}

function testNestedTernaryExpr1(boolean flag1, boolean flag2) {
    return flag1 ? flag2 ? "True " : false : 0;
}

function testNestedTernaryExpr2(boolean flag1, boolean flag2) {
    return flag1 ? "True" : flag2 ? false : 0;
}

function testNestedTernaryExpr3(boolean flag1, boolean flag2, boolean flag3) {
    return (flag1 == flag3 ? moduleFlag1 && flag2 : flag1 || flag3) ? "True" : 0 + 32;
}

function testNestedTernaryExpr4(boolean flag1, boolean flag2, boolean flag3) {
    return flag1 == flag3 ? flag1 && flag2 : moduleFlag2 ? "True" + "False" : 0;
}

string? moduleNullableStr = ();
int? moduleNullableInt = 1;

() var5 = moduleNullableStr ?: "False";
() var6 = moduleNullableStr ?: false;
int var7 = moduleNullableStr ?: moduleNullableInt ?: false;

function testElvisExprWithSameType(string? nullableStr) {
    return nullableStr ?: "False";
}

function testElvisExprWithDifferentType(string? nullableStr) {
    return nullableStr ?: false;
}

function testNestedElvisExpr1(string? nullableStr, int? nullableInt) {
    return moduleNullableStr ?: moduleNullableInt ?: false;
}

function testNestedElvisExpr2(string? nullableStr, int? nullableInt, boolean? nullableBool) {
    return moduleNullableStr ?: moduleNullableInt ?: nullableBool ?: 0.0;
}

function testNestedElvisExpr3(string? nullableStr, int? nullableInt, boolean? nullableBool) {
    return moduleNullableStr ?: (nullableBool is boolean ? nullableBool || moduleFlag1 
        : nullableInt ?: "False");
}
