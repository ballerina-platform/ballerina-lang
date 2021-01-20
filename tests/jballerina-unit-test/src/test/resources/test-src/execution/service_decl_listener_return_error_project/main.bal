import ballerina/jballerina.java;

function init() {
	println("Initializing module a");
}

public function main() {
}

public class ABC {

    private string name = "";

    public function init(string name, boolean errored = false) returns error? {
        self.name = name;
        println("init invoked");
        if (errored) {
            return error(string `${name} errored!!!`);
        }
        return ();
    }

    public function 'start() returns error? {
        println("a:ABC listener start called, service name - " + self.name);
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

service / on ep {

}

service /foo on new ABC("ModA-inline-1") {

}

service /foo on new ABC("ModA-inline-2", true) {

}

public function println(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printlnInternal(stdout1, strValue);
}

function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

function printlnInternal(handle receiver, handle strValue)  = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
