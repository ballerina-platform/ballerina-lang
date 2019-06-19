import ballerina/io;

channel<string> strChn = new;
channel<int> intChn = new;
boolean waiting = true;

// Test if worker actions are panicked if the worker is cancelled before sending
function workerCancelledBeforeSend() {
    io:println("**start**");
    // Start the future
    future<()> f1 = start testFunc();
    io:println("**going**");
    string strResult = <- strChn, 66;
    io:println("**going**");
    boolean cancel_w1 = f1.cancel();
    io:println("**going**");
    100 -> intChn, 88;
    io:println("**end**");
    while(waiting){}
}

function testFunc() {
    io:println("**insideFuture**");
    "message" -> strChn, 66;
     io:println("**f-going**");
    worker wy {
        string|error result = trap <- default;
        if (result is error) {
            io:println(result.reason());
        }
        waiting = false;
    }
    int intResult = <- intChn, 88;
    "message" -> wy;
    io:println("**f-going**");
    io:println("**endFuture**");
}
