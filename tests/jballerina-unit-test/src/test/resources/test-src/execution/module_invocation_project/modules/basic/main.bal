import ballerina/io;
import ballerina/lang.'object;

int initCount = 0;

function init() {
    initCount += 1;
	io:println("Initializing module 'basic'");
}

public function main() {
}

public function getInitCount() returns int {
    return initCount;
}

public class TestListener {

    *'object:Listener;
    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function __start() returns error? {
        io:println("basic:TestListener listener __start called, service name - " + self.name);
    }

    public function __gracefulStop() returns error? {
        io:println("basic:TestListener listener __gracefulStop called, service name - " + self.name);
        return ();
    }

    public function __immediateStop() returns error? {
        io:println("basic:TestListener listener __immediateStop called, service name - " + self.name);
        return ();
    }

    public function __attach(service s, string? name = ()) returns error? {
        io:println("basic:TestListener listener __attach called, service name - " + self.name);
    }

    public function __detach(service s) returns error? {
        io:println("basic:TestListener listener __detach called, service name - " + self.name);
    }
}

listener TestListener ep = new TestListener("basic");
