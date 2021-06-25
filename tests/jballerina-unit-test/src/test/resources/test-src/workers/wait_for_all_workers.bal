import ballerina/jballerina.java;

boolean waiting = true;

public function testWaitForAllWorkers() {
    test();
    println("Finishing Default Worker");
    while(waiting) {
       sleep(200);
    }
}

function test() {
    @strand{thread:"any"}
    worker w1 {
        return;
    }

    @strand{thread:"any"}
    worker w2 {
        sleep(2000);
        println("Finishing Worker w2");
        waiting = false;
    }
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

function println(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printlnInternal(stdout1, strValue);
}
public function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

public function printlnInternal(handle receiver, handle strValue)  = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
