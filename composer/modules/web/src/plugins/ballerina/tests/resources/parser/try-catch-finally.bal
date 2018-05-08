import ballerina/lang.errors;

struct testError {
    string msg;
    errors:Error cause;
    string code;
}

struct testDataError {
    string msg;
    errors:Error cause;
    string data;
}

struct testInputError {
    string msg;
    errors:Error cause;
    string input;
}

function main(string... args) {
    testTryCatch(11);
    testTryCatch(-1);
    testTryCatch(5);
    testFunctionThrow(6);
    testMethodCallInFinally();
}

function testTryCatch(int value)(string){
    string path = "start ";
    try{
        path = path + "insideTry ";
        try {
            path = path + "insideInnerTry ";
            if(value > 10){
                path = path + "onError ";
                testError error = { msg : "error" , code : "test" };
                throw error;
            } else if( value < 0 ) {
                path = path + "onInputError " ;
                testInputError error = {msg : "error", input : "0"};
                throw error;
            }

            path = path + "endInsideInnerTry ";
        } catch (testError ex){
            path = path + "innerTestErrorCatch:" + ex.code + " ";
            throw ex;
        } catch (testDataError e){
            path = path + "innerDataErrorCatch:" + e.msg + " ";
            throw e;
        } finally {
            path = path + "innerFinally ";
        }
        path = path + "endInsideTry ";
    } catch (errors:Error e){
        path = path + "ErrorCatch ";
    } catch (testError ex){
        path = path + "TestErrorCatch ";
    } finally {
        path = path + "Finally ";
    }
    path = path + "End";
    return path;
}

function testFunctionThrow (int arg)(boolean, string){
    string a = "0";
    try {
        a = a + "1";
        int b = testThrow(arg);
        a = a + "2";
    } catch (errors:Error b){
        a = a + "3";
        return true, a;
    }
    a = a + "4";
    return false, a;
}

function testThrow(int a)(int) {
    int c = a + 10;
    return testNestedThrow(c);
}

function testNestedThrow(int a)(int){
    errors:Error e  = {msg : "test message"};
    throw e;
}

function mockFunction ()(string) {
    return "done";
}

function testMethodCallInFinally ()(string) {
    string s = "start";
    try {
        errors:Error e = {msg:"test"};
        throw e;
    }finally {
         s = s + mockFunction();
     }
    return s;
}

function scopeIssueTest () (int) {
    int i = 0;
    while (i < 10) {
        try {
        } catch (errors:Error e) {
        }
        i = i + 1;
    }
    int j6 = 5;
    while (i < 20) {
        int val = j6;
        i = i + 1;
    }
    j6 = i + j6;
    return j6;
}
