import ballerina/jballerina.java;

int i = 0;

function timerTest() returns int {
    Callback c = new;
    startTimer(100, 3, c);
    sleep(500);
    return i;
}

public class Callback {

    public function exec() {
        i = i + 1;
    }
}

// Interop functions
public function startTimer(int interval, int count, Callback c) = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/Timer"
} external;

public function sleep(int interval) = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/Timer"
} external;
