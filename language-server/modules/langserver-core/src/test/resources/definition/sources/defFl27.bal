import ballerina/http;

function testFunction1() {
    int testArrValue1 = 1;
    int testArrValue2 = 2;
    int[] testArr = [testArrValue1, testArrValue2];
}

function testFunction2() {
    int value1 = 12;
    string computedFieldName = "field2";

    json testJson = {
        "field1": value1,
        [computedFieldName]: 1
    };
}

service testService = service {
    function serviceFunction1() {
        // Logic goes here
    }
    function serviceFunction2() {
        self.serviceFunction1();
    }
};

function stringTemplateWithText14 () returns (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello ${lastName}, ${firstName}`;
    return s;
}

type TestObject1 object {
    public function init(int arg1, int arg2 = 12, string... restArg) {
        
    }
};

function newConstructorTest() {
    int arg1 = 12;
    int arg2 = 12;
    string restArg = "Hello";
    TestObject1 testObject = new TestObject1(arg1, arg2, restArg);
    TestObject1 testObject2 = new(arg1, arg2, restArg);
}

function testFunction3() {
    string arg1Val = "Arg1";
    string arg2Val = "Arg2";
    int restArg1 = 0;
    int restArg2 = 1;
    int[] restArgArr = [restArg1, restArg2];
    testFunctionReference(arg1Val, arg2Val, restArg1, restArg2);
    testFunctionReference(arg1Val, arg2 = arg2Val);
    testFunctionReference(arg1Val, arg2Val, ...restArgArr);
}

function testFunctionReference(string arg1, string arg2 = "Test", int... restArg) {
    // Logic goes here
}

type TestObject2 object {
    function objectMethod1(string arg) {
        
    }
};

function testMethodCall(string objectArg) {
    TestObject2 testObj2 = new();
    testObj2.objectMethod1(objectArg);
}

service testServiceDef on new http:Listener(8080) {
    resource function testResource(http:Caller caller, http:Request request) {
    }

    function resourceMethod1() {    
    }

    function resourceMethod2() {
        self.resourceMethod1();
    }
}

function testAnonFunctionExpression() {
    var x = function(int arg1, TestObject1 arg2, TestObject1... restArg) returns TestObject2 {
        TestObject2 testObj = new();
        int testVal = 12 + arg1;
        TestObject1[] restArgs = restArg;
        return testObj;
    };

    var y = function(int, TestObject1) returns TestObject1;
    function(int, int) returns int z = (param1, param2) => param1 + param2;
}

function testTypeCastAndTypeOfExpression() {
    any testAny = ();
    var hello = <TestObject1>testAny;
    typedesc<TestObject1> tDesc = typeof hello;
}


function unaryAdditiveMultiplicativeShiftExpr() {
    int intParam1 = 20;
    int intParam2 = 2;
    boolean boolParam = true;
    
    int unaryRes = -intParam1;
    int multRes = intParam1/intParam2;
    int additive = intParam1 + intParam2;
    var shiftRes = intParam1 >>> intParam2;
}

function rangeNumericalTypeTestEqualityExprTest() {
    int startValue = 2;
    int endValue = 15;
    foreach var j in startValue ... endValue {
        // Logic
    }
    boolean res = startValue < endValue;
    boolean equalityRes = startValue == endValue;
    int bitwiseRes = endValue & startValue;
    boolean logicalExpRes = res && equalityRes;
    boolean conditionalRes = logicalExpRes ? res : equalityRes;
    string? stringVal = "TestString";
    string defaultStr = "DefaultString";
    string conditionalRes2 = stringVal ?: defaultStr;
}

function chekingExpr() returns error? {
    int intVal = check getError();
    int intVal2 = checkpanic getError();
    int|error divided = trap divide(1, 0);
}

function getError() returns error|int {
    if (1 == 1) {
        error e = error("Simulated error");
        return e;
    }
    return 1;
}

function divide(int a, int b) returns int {
    return a / b;
}

type TestObject3 object {

};

function testGotoDefObjectWithDefaultInit( ) {
     TestObject3 test3 = new();
}