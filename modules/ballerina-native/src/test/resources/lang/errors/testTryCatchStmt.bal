import ballerina.lang.error;

struct testError {
    string msg;
    error:error cause;
    string code;
}

struct testDataError {
    string msg;
    error:error cause;
    string data;
}

struct testInputError {
    string msg;
    error:error cause;
    string input;
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
    } catch (error:error e){
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
    } catch (error:error b){
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
    error:error e  = {msg : "test message"};
    throw e;
}

function testUncaughtException(){
    testNestedThrow(1);
}

function testStackTrace()(error:stackTrace){
    error:stackTrace trace;
    try{
        testUncaughtException();
    } catch (error:error e) {
        trace = error:getStackTrace(e);
    }
    return trace;
}