public type testError record {
    string message;
    error? cause;
    string code;
};

public type testDataError record {
    string message;
    error? cause;
    string data;
};
public type testInputError record {
    string message;
    error? cause;
    string input;
};

public function main(string... args) {
    _ = testTryCatch(11);
    _ = testTryCatch(-1);
    _ = testTryCatch(5);
    var thrownError = testFunctionThrow(6);
    _ = testMethodCallInFinally();
}

function testTryCatch(int value) returns (string){
    string path = "start ";
    try{
        path = path + "insideTry ";
        try {
            path = path + "insideInnerTry ";
            if(value > 10){
                path = path + "onError ";
                testerror tError = error("error" , { code : "test" });
                panic tError;
            } else if( value < 0 ) {
                path = path + "onInputError " ;
                testInputerror tError = error("error", { input : "0" } );
                panic tError;
            }

            path = path + "endInsideInnerTry ";
        } catch (testError ex){
            path = path + "innerTestErrorCatch:" + ex.code + " ";
            panic ex;
        } catch (testDataError e){
            path = path + "innerDataErrorCatch:" + e.message + " ";
            panic e;
        } finally {
            path = path + "innerFinally ";
        }
        path = path + "endInsideTry ";
    } catch (error e){
        path = path + "ErrorCatch ";
    } catch (testError ex){
        path = path + "TestErrorCatch ";
    } finally {
        path = path + "Finally ";
    }
    path = path + "End";
    return path;
}

function testFunctionThrow (int arg) returns (boolean, string){
    string a = "0";
    try {
        a = a + "1";
        int b = testThrow(arg);
        a = a + "2";
    } catch (error b){
        a = a + "3";
        return (true, a);
    }
    a = a + "4";
    return (false, a);
}
function testThrow(int a) returns (int) {
    int c = a + 10;
    return testNestedThrow(c);
}
function testNestedThrow(int a) returns (int){
    error e  = error("test message");
    if (e != null) {
        panic e;
    }
    return 9;
}
function mockFunction () returns (string) {
    return "done";
}

function testMethodCallInFinally () returns (string) {
    string s = "start";
    try {
        error e = error("test");
        panic e;
    }finally {
        return s + mockFunction();
    }
}

function scopeIssueTest () returns (int) {
    int i = 0;
    while (i < 10) {
        try {
        } catch (error e) {
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