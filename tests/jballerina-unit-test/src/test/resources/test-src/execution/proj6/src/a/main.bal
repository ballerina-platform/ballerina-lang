import ballerina/io;
import ballerina/lang.'object;

function init() {
	io:println("Initializing module a");
}

public function main() {
}

public class ABC {

    *'object:Listener;
    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function __start() returns error? {
        io:println("a:ABC listener __start called, service name - " + self.name);
    }

    public function __gracefulStop() returns error? {
        io:println("a:ABC listener __gracefulStop called, service name - " + self.name);
        return ();
    }

    public function __immediateStop() returns error? {
        io:println("a:ABC listener __immediateStop called, service name - " + self.name);
        if (self.name == "ModB") {
            error sampleErr = error("panicked while stopping module B");
            panic sampleErr;
        }
        return ();
    }

    public function __attach(service s, string? name = ()) returns error? {
        io:println("a:ABC listener __attach called, service name - " + self.name);
    }

    public function __detach(service s) returns error? {
        io:println("a:ABC listener __detach called, service name - " + self.name);
    }
}

listener ABC ep = new ABC("ModA");
