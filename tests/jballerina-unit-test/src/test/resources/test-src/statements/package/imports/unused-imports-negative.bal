import ballerina/jballerina.java;
import ballerina/jballerina.java as otherJAVA;
import ballerina/jballerina.java as anotherJAVA;
import ballerina/lang.value;
import ballerina/lang.array as _;

public function foo() {
    handle _ = anotherJAVA:fromString("Ballerina!!!");
}
