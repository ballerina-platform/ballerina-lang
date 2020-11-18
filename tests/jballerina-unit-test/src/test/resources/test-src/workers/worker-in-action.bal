import ballerina/runtime;

public type ClientEndpointConfiguration record {

};

public client class ABCClient {

    remote function testAction1() returns string {
        @strand{thread:"any"}
        worker sampleWorker {
            string m = "";
            m = <- default;
            string v = "result from sampleWorker";
            v -> default;
        }

        "xxx" -> sampleWorker;
        string result = "";
        result = <- sampleWorker;
        return result;
    }

    remote function testAction2() returns string {
        @strand{thread:"any"}
        worker sampleWorker {
            "request" -> default;
        }

        string result = "";
        result = <- sampleWorker;
        return result;
    }

}

public class Client {
    public ABCClient abcClient = new;

    public function _init_(ClientEndpointConfiguration config) {
        self.abcClient = new;
    }

    public function register(typedesc<any> serviceType) {
    }

    public function 'start() {
    }

    public function getCallerActions() returns ABCClient {
        return self.abcClient;
    }

    public function stop() {
    }
}

function testAction1() returns string {
    ABCClient ep1 = new;
    string x = ep1->testAction1();
    return x;
}

function testAction2() returns string {
    ABCClient ep1 = new;
    string x = ep1->testAction2();
    return x;
}


string testStr = "";
public function testDefaultError () returns string{
    var a = test1(5);
    test2();
    runtime:sleep(200);
    return testStr;
}

function test1(int c) returns error|() {
    @strand{thread:"any"}
    worker w1 returns int {
        int|error a = <- default;
        //need to verify this line is reached
        testStr = "REACHED";
        return 8;
    }
    int b = 9;

    if (true) {
        error e = error("error occurred");
        return e;
    }
    b -> w1;
    return ();
}

function test2() {}
