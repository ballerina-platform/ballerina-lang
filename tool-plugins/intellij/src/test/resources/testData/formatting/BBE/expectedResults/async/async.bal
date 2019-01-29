import ballerina/http;
import ballerina/io;
import ballerina/runtime;

int count = 0;

http:Client clientEndpoint = new("https://postman-echo.com", config = {});

public function main() {
    // Asynchronously call the function named `sum()`.
    future<int> f1 = start sum(40, 50);
    // You can pass the value of the `future` variable around
    // and call its results later.
    int result = squarePlusCube(f1);
    io:println("SQ + CB = ", result);

    // Call the `countInfinity()` function, that runs forever in asynchronous
    // mode.
    future<()> f2 = start countInfinity();
    runtime:sleep(1000);
    // Check if the function call is done.
    io:println(f2.isDone());
    // Check if someone cancelled the asynchronous execution.
    io:println(f2.isCancelled());
    // Cancel the asynchronous execution.
    boolean cancelled = f2.cancel();
    io:println(cancelled);
    io:println("Counting done in one second: ", count);
    // After cancelling the asynchronous execution of `f2`, check if the
    // function call is done and if someone cancelled the execution.
    io:println(f2.isDone());
    io:println(f2.isCancelled());

    // Asynchronously invoke the action call `get()`.
    future<http:Response|error> f3 = start clientEndpoint->get("/get?test=123");
    io:println(sum(25, 75));
    // Check if the action call is done.
    io:println(f3.isDone());
    // Wait for action call `f3` to finish.
    var response = wait f3;
    // Print the response payload of the action call if successful, or print the
    // reason for failure.
    if (response is http:Response) {
        io:println(untaint response.getJsonPayload());
    } else {
        io:println(response.reason());
    }
    // Check if the action call is done after waiting for it to complete.
    io:println(f3.isDone());

    // Asynchronously invoke the functions named `square()` and `greet()`.
    future<int> f4 = start square(20);
    future<string> f5 = start greet("Bert");

    // You can wait for any one of the given futures to complete.
    // Here `f4` will finish before `f5` since `runtime:sleep()` is called
    // in the `greet()` function to delay its execution. The value returned
    // by the future that finishes first will be taken as the result.
    int|string anyResult = wait f4|f5;
    io:println(anyResult);

    // Asynchronously invoke the functions named `sum()`, `cube()` and `greet()`.
    future<int> f6 = start sum(40, 60);
    future<int> f7 = start cube(3);
    future<string> f8 = start greet("Moose");

    // You can wait for all the given futures to complete.
    // The result of this `wait` action can be assigned to a map or a record.
    // If the result is a map, it will contain the returned values from each
    // future, with the key as the name of the future if a key is not provided.
    map<int|string> resultMap = wait { first_field: f6, second_field: f7,
        third_field: f8 };
    io:println(resultMap);


    // If the result is a record, it will contain the returned values from each
    // future, with the field name as the name of the future if a field name is
    // not provided.
    record { int first_field; int second_field; string third_field; } rec =
    wait { first_field: f6, second_field: f7, third_field: f8 };
    io:println("first field of record --> " + rec.first_field);
    io:println("second field of record --> " + rec.second_field);
    io:println("third field of record --> " + rec.third_field);
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

function greet(string name) returns string {
    // A `runtime:sleep` is added to delay the execution.
    runtime:sleep(2000);
    return "Hello " + name + "!!";
}

function squarePlusCube(future<int> f) returns int {
    worker w1 {
        int n = wait f;
        int sq = square(n);
        sq -> w2;
    }
    worker w2 returns int {
        int n = wait f;
        int cb = cube(n);
        int sq;
        sq = <-w1;
        return sq + cb;
    }
    // Wait for worker `W2` to complete.
    return wait w2;
}

function countInfinity() {
    while (true) {
        count += 1;
    }
}
