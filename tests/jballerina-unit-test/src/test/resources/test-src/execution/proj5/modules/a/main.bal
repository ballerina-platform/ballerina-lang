import ballerina/io;

function init() {
	io:println("Initializing module a");
}

public function main() {
}

public class ABC {

    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function 'start() returns error? {
        io:println("a:ABC listener start called, service name - " + self.name);
        if (self.name == "ModB") {
            error sampleErr = error("panicked while starting module B");
            panic sampleErr;
        }
    }

    public function gracefulStop() returns error? {
        io:println("a:ABC listener gracefulStop called, service name - " + self.name);
        return ();
    }

    public function immediateStop() returns error? {
        io:println("a:ABC listener immediateStop called, service name - " + self.name);
        return ();
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
        io:println("a:ABC listener attach called, service name - " + self.name);
    }

    public function detach(service object {} s) returns error? {
        io:println("a:ABC listener detach called, service name - " + self.name);
    }
}

listener ABC ep = new ABC("ModA");
