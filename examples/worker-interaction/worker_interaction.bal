import ballerina/io;
import ballerina/runtime;

// Workers interact with each other by sending and receiving messages. 
// Ballerina validates every send/receive worker interaction in order to avoid deadlocks.
public function main() {
    worker w1 {
        int i = 100;
        float k = 2.34;
        io:println("[w1 -> w2] i: ", i, " k: ", k);
        // Send messages to worker `w2`. This message contains a tuple value with member types of `int` and `float`.
        (i, k) -> w2;
        // Receive a message from worker `w2`. This message contains a `json` type value.
        json j = {};
        j = <- w2;
        string jStr = j.toString();
        io:println("[w1 <- w2] j: ", jStr);
        io:println("[w1 ->> w2] i: ", i);
        // Synchronous send to worker `w2`. Worker `w1` wait until `w2` receives the message.
        error? send = i ->> w2;
        // Synchronous send returns nil for successful send or returns error or panic based on receiving worker's state.
        if (send is error) {
            io:println("w2 failed before receiving sync send");
        } else {
            io:println("[w1 ->> w2] successful!!");
        }

        foreach var n in 1 ... 3 {
            io:println("[w1 -> w3] k: ", k);
            k -> w3;
        }
        io:println("Waiting for worker w3 to fetch messages..");
        // Flush all the message sends to worker `w3`. Worker halts here until all messages are sent or a `w3` failure.
        error? flushResult = flush w3;
        io:println("[w1 -> w3] Flushed!!");
    }

    worker w2 {
        // Receive a message from the worker `w1`.
        int iw;
        float kw;
        (int, float) vW1 = (0, 1.0);
        vW1 = <- w1;
        (iw, kw) = vW1;
        io:println("[w2 <- w1] iw: " + iw + " kw: " + kw);
        // Send a message to worker `w1`.
        json jw = { "name": "Ballerina" };
        io:println("[w2 -> w1] jw: ", jw);
        jw -> w1;
        // Receive the sync message from the worker `w1`
        int lw;
        runtime:sleep(5);
        lw = <- w1;
        io:println("[w2 <- w1] lw: " + lw);
    }

    worker w3 {
        float mw;
        // Slowly receiving messages from `w1`.
        foreach var i in 1 ... 3 {
            runtime:sleep(50);
            mw = <- w1;
            io:println("[w3 <- w1] mw: ", mw);
        }
    }

    wait w1;
}
