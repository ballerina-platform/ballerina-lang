
import ballerina/jballerina.java;
int a1 = 0;

public function getA1() returns int {
    return a1;
}

public function updateA1(int val) returns int {
    a1 = a1 + val;
    return a1;
}


int b = temp();

function temp() returns int {
    println("PackageA");
    return 9;
}

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
