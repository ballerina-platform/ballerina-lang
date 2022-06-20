import ballerina/io;
import ballerina/lang.runtime;
import listener_stophandler_async_call_test.moduleA;

public function stopHandlerFunc() returns error? {
    runtime:sleep(1);
    io:println("StopHandler1 of current module");
}

public function stopHandlerFunc2() returns error? {
    runtime:sleep(1);
    io:println("StopHandler2 of current module");
}

function init() {
    runtime:onGracefulStop(stopHandlerFunc);
    runtime:onGracefulStop(stopHandlerFunc2);
}


public class ListenerObj1 {

    private string name = "";

    public function init(string name) {
        self.name = name;
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        runtime:sleep(1);
        if (self.name == "ModC") {
            panic error("graceful stop of current module");
        }
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

listener ListenerObj1 lo1 = new ListenerObj1("ModC");

public function main() {
    ListenerObj1 lo1 = new ListenerObj1("ModDynC");
    runtime:registerListener(lo1);
    checkpanic lo1.gracefulStop();
    runtime:deregisterListener(lo1);
    moduleA:main();
}
