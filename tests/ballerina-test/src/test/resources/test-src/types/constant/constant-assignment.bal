import ballerina/os;

const string envVar = os:getEnv("env_var");
const string varFunc = dummyStringFunction();
const string str = "ballerina is $$$";
const string varNativeFunc = str.replace("$$$","awesome");
const int varIntExpr = 10+10+10;
const string varConcat = envVar + varFunc + varNativeFunc;

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

