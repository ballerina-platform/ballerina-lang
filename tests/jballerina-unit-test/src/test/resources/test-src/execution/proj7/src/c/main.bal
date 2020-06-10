import a;
import b;
import ballerina/io;

function init() {
	io:println("Initializing module c");
}

public function main() returns error? {
    b:sample();
    io:println("Module c main function invoked");
	error sampleErr = error("error returned while executing main method");
	return sampleErr;
}

listener a:ABC ep = new a:ABC("ModC");
