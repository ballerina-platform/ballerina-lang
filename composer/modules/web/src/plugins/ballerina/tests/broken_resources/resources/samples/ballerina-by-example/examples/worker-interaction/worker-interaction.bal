import ballerina/lang.system;
import ballerina/doc;
import ballerina/lang.jsons;

@doc:Description {value:"Workers interact with each other by sending and receiving messages. Ballerina checks the send/receive signatures of every pair of workers and validate in order to avoid deadlocks."}
function main (string... args) {
    int i = 100;
    float k = 2.34;
    system:println("[default -> w1] i: " + i + " k: " + k);
    // Send a messages to worker 'w1'. This message contains two values of type int and float.
    i, k -> w1;
    // Receive a message from worker w1. This message contains a json typed value.
    json j = {};
    j <- w1;
    system:println("[default <- w1] j: " + jsons:toString(j));

    // Define the worker and it's execution logic.
    worker w1 {
        // Receive a message from default worker.
        int iw;
        float kw;
        iw, kw <- default;
        system:println("[w1 <- default] iw: " + iw + " kw: " + kw);
        // Send a message to default worker.
        json jw = {"name":"Ballerina"};
        system:println("[w1 -> default] jw: " + jsons:toString(jw));
        jw -> default;
    }
}
