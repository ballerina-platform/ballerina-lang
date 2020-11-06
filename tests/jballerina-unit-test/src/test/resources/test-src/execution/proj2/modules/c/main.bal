import unit_tests1/proj2.a;
import unit_tests1/proj2.b;
import ballerina/io;

function init() {
	io:println("Initializing module c");
}

public function main() {
    d:sample();
    io:println("Module c main function invoked");
}

listener c:ABC ep = new c:ABC("ModC");
