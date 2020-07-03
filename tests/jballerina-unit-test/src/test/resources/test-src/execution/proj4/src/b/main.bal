import a;
import ballerina/io;

function init() {
	io:println("Initializing module b");
	error sampleErr = error("panicked while initializing module B");
	panic sampleErr;
}

public function sample() {

}

listener a:ABC ep = new a:ABC("ModB");
