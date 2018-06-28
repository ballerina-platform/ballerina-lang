import ballerina/io;

// Workers interact with each other by sending and receiving messages. 
// Ballerina validates every send/receive worker interaction in order to avoid deadlocks.
function main(string... args) {
    worker w1 {
        int i = 100;
        float k = 2.34;
        io:println("[w1 -> w2] i: ", i, " k: ", k);
        // Send messages to worker `w2`. This message contains a tuple value with member types of `int` and `float`.
        (i, k) -> w2;
        // Receive a message from worker `w2`. This message contains a `json` type value.
        json j;
        j <- w2;
        string jStr = j.toString();
        io:println("[w1 <- w2] j: ", jStr);
    }

    worker w2 {
        // Receive a message from the worker `w1`.
        int iw;
        float kw;
        (int, float) vW1;
        vW1 <- w1;
        (iw, kw) = vW1;
        io:println("[w2 <- w1] iw: " + iw + " kw: " + kw);
        // Send a message to worker `w1`.
        json jw = { "name": "Ballerina" };
        io:println("[w2 -> w1] jw: ", jw);
        jw -> w1;
    }
}
