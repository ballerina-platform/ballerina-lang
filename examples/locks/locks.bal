import ballerina/io;
import ballerina/runtime;
import ballerina/time;

// Shared variable among multiple workers.
string sharedString;

function main (string... args) {
    worker w1 {
        lock {
            // Lock the shared variable for access.
            io:println("worker 1 access - ", time:currentTime().milliSecond());
            sharedString = "sample ";
            runtime:sleep(100);
            io:println("worker 1 release - ", time:currentTime().milliSecond());
        }
    }
    worker w2 {
        runtime:sleep(20);
        lock {
            // Lock the shared variable for access.
            io:println("worker 2 access - ", time:currentTime().milliSecond());
            sharedString = sharedString + "value ";
            runtime:sleep(100);
            io:println("worker 2 release - ", time:currentTime().milliSecond());
        }
    }
    worker w3 {
        runtime:sleep(120);
        lock {
            // Lock the shared variable for access.
            io:println("worker 3 access ", time:currentTime().milliSecond());
            sharedString = sharedString + "added";
            runtime:sleep(100);
            io:println("worker 3 release ", time:currentTime().milliSecond());
            io:println("final result - ", sharedString);
        }
    }
}
