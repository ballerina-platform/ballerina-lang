import ballerina/jballerina.java;
import unit_tests/proj2.a as a;

function init() returns error? {
	println("Initializing module b");
	error sampleErr = error("error returned while initializing module B");
	return sampleErr;
}

public function sample() {

}

listener a:ABC ep = new a:ABC("ModB");

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
