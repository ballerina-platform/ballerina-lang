import ballerina/jballerina.java;
import unit_tests/proj3.a as a;

function init() {
	println("Initializing module b");
}

public function sample() {

}

listener a:ABC ep = new a:ABC("ModB");

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
