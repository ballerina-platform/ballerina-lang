import ballerina/lang.'string as strings;

final string envVar = "test";
final string varFunc = dummyStringFunction();
final string str = "ballerina is ";
final string varNativeFunc = strings:concat(str, "awesome");
final int varIntExpr = 10 + 10 + 10;
final string varConcat = varFunc + varNativeFunc;

function accessConstant() returns (string) {
    return envVar;
}

function accessConstantViaFunction() returns (string) {
    return varFunc;
}

function dummyStringFunction() returns (string) {
    return "dummy";
}

function accessConstantViaNativeFunction() returns (string) {
    return varNativeFunc;
}


function accessConstantEvalIntegerExpression() returns (int) {
    return varIntExpr;
}

function accessConstantEvalWithMultipleConst() returns (string) {
    return varConcat;
}
