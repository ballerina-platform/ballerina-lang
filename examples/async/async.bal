import ballerina/io;
import ballerina/runtime;
import ballerina/http;
import ballerina/mime;

int count;

endpoint http:Client clientEndpoint { 
    url: "https://postman-echo.com" };

function main(string... args) {
    // Asynchronously call the function named `sum()`.
    future<int> f1 = start sum(40, 50);
    // You can pass around the value of the `future` variable
    // and call its results later.
    int result = square_plus_cube(f1);
    io:println("SQ + CB = ", result);

    // Call the `countInfinity()` function, which runs forever in asynchronous mode.
    future f2 = start countInfinity();
    runtime:sleep(1000);
    // Check whether the function call is done.
    io:println(f2.isDone());
    // Check whether someone cancelled the asynchronous execution.
    io:println(f2.isCancelled());
    // Cancel the asynchronous operation.
    boolean cancelled = f2.cancel();
    io:println(cancelled);
    io:println("Counting done in one second: ", count);
    io:println(f2.isDone());
    io:println(f2.isCancelled());

    // async action call
    future<http:Response|error> f3 = start clientEndpoint->get(
            "/get?test=123");
    io:println(sum(25, 75));
    io:println(f3.isDone());
    var response = await f3;
    match response {
        http:Response resp => {
            io:println("HTTP Response: ",
                       untaint resp.getJsonPayload());
        }
        error err => { io:println(err.message); }
    }
    io:println(f3.isDone());
}

function sum(int a, int b) returns int {
    return a + b;
}

function square(int n) returns int {
    return n * n;
}

function cube(int n) returns int {
    return n * n * n;
}

function square_plus_cube(future<int> f) returns int {
    worker w1 {
        int n = await f;
        int sq = square(n);
        sq -> w2;
    }
    worker w2 {
        int n = await f;
        int cb = cube(n);
        int sq;
        sq <- w1;
        return sq + cb;
    }
}

function countInfinity() {
    while (true) {
        count++;
    }
}
