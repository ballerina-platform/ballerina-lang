import ballerina/io;
import ballerina/lang.runtime;

public function stopHandlerFunc() returns error? {
    runtime:sleep(1);
    io:println("StopHandler1 of moduleA");
}

public function stopHandlerFunc2() returns error? {
    runtime:sleep(1);
    io:println("StopHandler2 of moduleA");
}

function init() {
    runtime:onGracefulStop(stopHandlerFunc);
    runtime:onGracefulStop(stopHandlerFunc2);
}

public class ListenerA {

    private string name = "";
    public function init(string name) {
        self.name = name;
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        runtime:sleep(1);
        if (self.name == "ModA") {
            panic error("graceful stop of ModuleA");
        }
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

public class ListenerB {

    private string name = "";
    public function init(string name) {
        self.name = name;
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        runtime:sleep(2);
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

listener ListenerA listenerA = new ListenerA("ModA");

final ListenerB listenerB = new ListenerB("ListenerObjectB");

public function main() {
    final ListenerA listenerA = new ListenerA("ModDynA");
    runtime:registerListener(listenerA);
    runtime:registerListener(listenerB);

    checkpanic listenerA.gracefulStop();
    runtime:deregisterListener(listenerA);

    checkpanic listenerB.gracefulStop();
    runtime:deregisterListener(listenerB);
}
