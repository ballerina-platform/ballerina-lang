import unit_tests1/proj2.a;
import ballerina/io;

function init() returns error? {
	io:println("Initializing module b");
	error sampleErr = error("error returned while initializing module B");
	return sampleErr;
}

public function sample() {

}

listener c:ABC ep = new c:ABC("ModB");
