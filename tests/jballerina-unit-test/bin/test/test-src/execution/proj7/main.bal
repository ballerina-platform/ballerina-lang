import ballerina/jballerina.java;
import unit_tests/proj7.a as a;
import unit_tests/proj7.b as b;

function init() {
	println("Initializing module c");
}

public function main() returns error? {
    b:sample();
    println("Module c main function invoked");
	error sampleErr = error("error returned while executing main method");
	return sampleErr;
}

listener a:ABC ep = new a:ABC("ModC");

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
