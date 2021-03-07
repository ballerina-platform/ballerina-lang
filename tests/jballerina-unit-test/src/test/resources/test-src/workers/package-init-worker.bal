import ballerina/jballerina.java;

int i = getInt();

function getInt() returns int {
    @strand{thread:"any"}
    worker w1 returns int {
        return 1;
    }
    @strand{thread:"any"}
    worker w2 {
        sleep(10000);
    }
    return wait w1;
}

function test() returns int {
    return i;
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
