import unit_tests/proj6.a as a;
import ballerina/io;

function init() {
	io:println("Initializing module b");
}

public function sample() {

}

listener a:ABC ep = new a:ABC("ModB");
