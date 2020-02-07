import ballerina/io;
import ballerina/runtime;

boolean waiting = true;

public function testWaitForAllWorkers() {
    test();
    io:println("Finishing Default Worker");
    while(waiting) {
       runtime:sleep(200);
    }
}

function test() {
    worker w1 {
        return;
    }

    worker w2 {
        runtime:sleep(2000);
        io:println("Finishing Worker w2");
        waiting = false;
    }
}
