import ballerina/lang.system;
import ballerina/lang.strings;

const string envVar = system:getEnv("env_var");
const string varFunc = dummyStringFunction();
const string varNativeFunc = strings:replace("ballerina is $$$","$$$","awesome");
const int varIntExpr = 10+10+10;
const string varConcat = envVar + varFunc + varNativeFunc;

function accessConstant() (string) {
    return envVar;
}

function accessConstantViaFunction() (string) {
    return varFunc;
}

function dummyStringFunction() (string) {
    return "dummy";
}

function accessConstantViaNativeFunction() (string) {
    return varNativeFunc;
}


function accessConstantEvalIntegerExpression() (int) {
    return varIntExpr;
}

function accessConstantEvalWithMultipleConst() (string) {
    return varConcat;
}

