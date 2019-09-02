import ballerina/http;

function testBasicCheckpanic1(int testNumber) returns int|float {
    match testNumber {
        1 => {int i =    checkpanic    getGenericError();}
        2 => {int i =
                        checkpanic
getGenericErrorWithDetail();}
        3 => {int|boolean i =checkpanic     getGenericErrorWithMultiUnion();}
        4 => {return     checkpanic      getFloat();}
        5 => {int i =checkpanic     returnBallerinaPanicError();}
        6 => {int i =      checkpanic
     getCustomError();}
    }
    return 0.0;
}

function testAsyncNonNativeBasic1() returns int {
    future<int> f1 =checkpanic     start    add(5, 2);
    int result = wait f1;
    return result;
}

function testAsyncNonNativeBasic2() returns int {
    future<int> f1 = checkpanic
                          start add(5, 2);
    int result = wait f1;
    return result;
}

function testAsyncNonNativeBasic3() returns int {
    future<int> f1 =
            checkpanic
     start
           add(5, 2)
     ;
    int result = wait f1;
    return result;
}

service serviceName1 on new http:Listener(9090) {
    resource function newResource1(http:Caller caller, http:Request request) returns error? {
        http:Response res = new;
        res.setPayload("sd");checkpanic caller->respond(res);
    }
}

service serviceName2 on new http:Listener(9090) {
    resource function newResource1(http:Caller caller, http:Request request) returns error? {
        http:Response res = new;
        res.setPayload("sd");
        var result =    checkpanic     caller->respond(res);
    }
}

service serviceName3 on new http:Listener(9090) {
    resource function newResource1(http:Caller caller, http:Request request) returns error? {
        http:Response res = new;
        res.setPayload("sd");checkpanic
caller->respond(res);
    }
}

service serviceName4 on new http:Listener(9090) {
    resource function newResource1(http:Caller caller, http:Request request) returns error? {
        http:Response res = new;
        res.setPayload("sd");
        var result =    checkpanic
                            caller->respond(res);
    }
}

service serviceName5 on new http:Listener(9090) {
    resource function newResource1(http:Caller caller, http:Request request) returns error? {
        http:Response res = new;
        res.setPayload("sd");
        var result =
    checkpanic
                            caller->respond(res);
    }
}

function getGenericError() returns int|error {
    error e = error("Generic Error");
    return e;
}

function getGenericErrorWithDetail() returns int|error {
    error e = error("Generic Error" , fatal  =  true ,   message  =   "Something Went Wrong" );
    return e;
}

function getGenericErrorWithMultiUnion() returns int|boolean|error {
    error e = error("Generic Error");
    return e;
}

function getFloat() returns int|float|error {
    float f = 2.2;
    return f;
}

function returnBallerinaPanicError() returns int|error {
    int[2] arr = [1, 2];
    int[] oArr = arr;
    int|error ret = trap oArr[4];
    return ret;
}

public type MyError error<string, record   {| string message?;error cause?  ;   string... ;|}>;

function getCustomError() returns int|MyError {
    MyError e = error("My Error"  ,   message="error" );
    return e;
}

function add(int a, int b) returns int {
    return a + b;
}