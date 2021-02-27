import ballerina/jballerina.java;
import unit_tests/proj3.a;
import unit_tests/proj3.b;

function init() {
	println("Initializing module c");
}

public function main() {
    b:sample();
    println("Module c main function invoked");
}

listener a:ABC ep = new a:ABC("ModC");

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
