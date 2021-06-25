import ballerina/jballerina.java;
import unit_tests/proj8.a as a;
import unit_tests/proj8.b as b;

function init() {
	println("Initializing module c");
}

public function main() {
    b:sample();
    println("Module c main function invoked");
	error sampleErr = error("panicked while executing main method");
	panic sampleErr;
}

listener a:ABC ep = new a:ABC("ModC");

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

