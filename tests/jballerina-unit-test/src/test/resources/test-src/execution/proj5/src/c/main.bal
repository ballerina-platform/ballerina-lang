import a;
import b;
import ballerina/io;

function init() {
	io:println("Initializing module c");
}

public function main() {
    b:sample();
    io:println("Module c main function invoked");
}

listener a:ABC ep = new a:ABC("ModC");
