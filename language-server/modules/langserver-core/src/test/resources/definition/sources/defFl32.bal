type TestObject1 object {
    function objectMethod1(string arg) {
        
    }
};

public client class SimpleClient {
    public remote function simpleAction(string actionParam) {

    }
}

function testCallStatement() returns error? {
    string arg = "Arg Val";
    SimpleClient sClient = new();
    TestObject1 testObj = new();
    
    testFunction();
    sClient->simpleAction(arg);
    testObj.objectMethod1(arg);
    
    check getError();
    checkpanic getError();
}

function getError() returns error? {
    if (1 == 1) {
        error e = error("Simulated error");
        return e;
    }
}

function testFunction() {
	
}