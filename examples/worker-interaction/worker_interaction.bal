import ballerina/io;
import ballerina/runtime;

// Workers interact with each other by sending and receiving messages.
// Ballerina validates every worker interaction (send and receive)
// to avoid deadlocks.
public function main() {
    worker w1 {
        int i = 100;
        float k = 2.34;

        // Sends messages asynchronously to the worker `w2`. This message contains a
        // tuple value with the member types `int` and `float`.
        [int, float] t1 = [i, k];
        t1 -> w2;
        io:println("[w1 -> w2] i: ", i, " k: ", k);

        // Receives a message from the worker `w2`. This message contains `
        // a `json`-typed value.
        json j = {};
        j = <- w2;
        string jStr = j.toString();
        io:println("[w1 <- w2] j: ", jStr);
        io:println("[w1 ->> w2] i: ", i);

        // Sends messages synchronously to the worker `w2`. The worker `w1` will wait
        // until the worker `w2` receives the message.
        () send = i ->> w2;

        // The synchronous sending returns `nil` if the message was successfully sent or
        // returns an error or panics based on the receiving worker's state.
        io:println("[w1 ->> w2] successful!!");

        // Sends messages asynchronously to the worker `w3`.
        io:println("[w1 -> w3] k: ", k);
        k -> w3;
        k -> w3;
        k -> w3;

        io:println("Waiting for worker w3 to fetch messages..");

        // Flushes all messages sent asynchronously to the worker `w3`. The worker
        // will halt at this point until all messages are sent or until the worker `w3`
        // fails.
        error? flushResult = flush w3;
        io:println("[w1 -> w3] Flushed!!");
    }

    worker w2 {
        // Receives a message from the worker `w1`.
        int iw;
        float kw;
        [int, float] vW1 = [0, 1.0];
        vW1 = <- w1;
        [iw, kw] = vW1;
        io:println("[w2 <- w1] iw: ", iw, " kw: ", kw);

        // Sends a message asynchronously to the worker `w1`.
        json jw = {"name": "Ballerina"};
        io:println("[w2 -> w1] jw: ", jw);
        jw -> w1;

        // Receives the message sent synchronously from the worker `w1`
        int lw;
        runtime:sleep(5);
        lw = <- w1;
        io:println("[w2 <- w1] lw: ", lw);
    }

    worker w3 {
        float mw;

        // Receives messages from the worker `w1` after a certain time.
        runtime:sleep(50);
        mw = <- w1;
        mw = <- w1;
        mw = <- w1;
        io:println("[w3 <- w1] mw: ", mw);
    }

    // Waits for the worker `w1`to finish.
    wait w1;
}
