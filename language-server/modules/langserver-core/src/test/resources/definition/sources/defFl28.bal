function testStartAction() {
    future<int> intFuture = start getIntValue(10, 20);
    TestObject1 testObj = new;
    future<()> nilFuture = start testObj.objectMethod1("Hello");
    
}

function getIntValue(int a, int b) returns int {
    return a + b;
}

type TestObject1 object {
    function objectMethod1(string arg) {
        
    }
};

function testWaitAction() {
    future<int> future1 = start getIntValue(10, 20);
    future<int> future2 = start getIntValue(10, 20);
    
    int futureRes = wait future1;
    var  multiFutureRes1 = wait {future1, future2};
    record {int f1; int f2;} multiFutureRes2 = wait {f1: future1, f2:future2};
    var alternateFutureRes = wait future1 | future2;
}

function sendActionTest() {
    worker w1 {
        int w1Var1 = 12;
        int w1Var2 = 12;
        w1Var1->w2;
        w1Var2->>w2;
    }

    worker w2 {
        int w1Res = <-w1;
        int w1Res2 = <-w1;
    }
}

function testFlush() {
    worker w1 {
        int x = 2;
        x -> w2;
        error? e = flush w2;

    }

    worker w2 {
        int s = <- w1;

    }

    _ = wait {w1, w2};
}

public client class SimpleClient {
    public remote function simpleAction(string actionParam) {

    }
}

function testRemoteAction() {
    SimpleClient sClient = new();
    sClient->simpleAction("Test");
}

