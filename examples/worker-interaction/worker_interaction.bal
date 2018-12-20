import ballerina/io;
import ballerina/runtime;

// Workers interact with each other by sending and receiving messages. 
// Ballerina validates every worker interactions (send and receive) in order to
// avoid deadlocks.
public function main() {
    worker w1 {
        int i = 100;
        float k = 2.34;

        // Send messages asynchronously to worker `w2`. This message contains a
        // tuple value with member types of `int` and `float`.
        (i, k) -> w2;
        io:println("[w1 -> w2] i: ", i, " k: ", k);

        // Receive a message from worker `w2`. This message contains a `json`
        // typed value.
        json j = {};
        j = <- w2;
        string jStr = j.toString();
        io:println("[w1 <- w2] j: ", jStr);
        io:println("[w1 ->> w2] i: ", i);

        // Send messages synchronously to worker `w2`. Worker `w1` will wait
        // until worker `w2` receives the message.
        () send = i ->> w2;

        // Synchronous send returns nil if the message is sent successfully or
        // returns an error or panics based on the receiving worker's state.
        io:println("[w1 ->> w2] successful!!");

        // Send messages asynchronously to worker `w3`.
        io:println("[w1 -> w3] k: ", k);
        k -> w3;
        k -> w3;
        k -> w3;

        io:println("Waiting for worker w3 to fetch messages..");

        // Flush all messages sent asynchronously to worker `w3`. The worker
        // will halt here until all messages are sent or until worker `w3`
        // fails.
        error? flushResult = flush w3;
        io:println("[w1 -> w3] Flushed!!");
    }

    worker w2 {
        // Receive a message from worker `w1`.
        int iw;
        float kw;
        (int, float) vW1 = (0, 1.0);
        vW1 = <- w1;
        (iw, kw) = vW1;
        io:println("[w2 <- w1] iw: " + iw + " kw: " + kw);

        // Send a message asynchronously to worker `w1`.
        json jw = { "name": "Ballerina" };
        io:println("[w2 -> w1] jw: ", jw);
        jw -> w1;

        // Receive the message sent synchronously from the worker `w1`
        int lw;
        runtime:sleep(5);
        lw = <- w1;
        io:println("[w2 <- w1] lw: " + lw);
    }

    worker w3 {
        float mw;

        // Receive messages from worker `w1` after a certain time.
        runtime:sleep(50);
        mw = <- w1;
        mw = <- w1;
        mw = <- w1;
        io:println("[w3 <- w1] mw: ", mw);
    }

    // Wait for worker `w1`to finish.
    wait w1;
}
