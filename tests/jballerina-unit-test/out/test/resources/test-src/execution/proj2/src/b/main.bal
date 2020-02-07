import a;
import ballerina/io;

function __init() returns error? {
	io:println("Initializing module b");
	error sampleErr = error("error returned while initializing module B");
	return sampleErr;
}

public function sample() {

}

listener a:ABC ep = new a:ABC("ModB");
