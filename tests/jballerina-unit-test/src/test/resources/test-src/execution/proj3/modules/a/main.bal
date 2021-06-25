import ballerina/jballerina.java;

function init() {
	println("Initializing module a");
}

public function main() {
}

public class ABC {

    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function 'start() returns error? {
        println("a:ABC listener start called, service name - " + self.name);
        if (self.name == "ModB") {
            error sampleErr = error("error returned while starting module B");
            return sampleErr;
        }
    }

    public function gracefulStop() returns error? {
        println("a:ABC listener gracefulStop called, service name - " + self.name);
        return ();
    }

    public function immediateStop() returns error? {
        println("a:ABC listener immediateStop called, service name - " + self.name);
        return ();
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
        println("a:ABC listener attach called, service name - " + self.name);
    }

    public function detach(service object {} s) returns error? {
        println("a:ABC listener detach called, service name - " + self.name);
    }
}

listener ABC ep = new ABC("ModA");

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
