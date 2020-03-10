import ballerina/http;
import ballerina/io;
import ballerina/runtime;

int count = 0;

http:Client clientEndpoint = new ("http://postman-echo.com");

public function main() {
    // Asynchronously calls the function named `sum()`.
    future<int> f1 = start sum(40, 50);
    // You can pass the value of the `future` variable
    // and call its results later.
    int result = squarePlusCube(f1);
    // Waits for the future `f1` to finish.
    _ = wait f1;
    io:println("SQ + CB = ", result);

    // Calls the `countInfinity()` function, which runs forever
    // in asynchronous mode.
    future<()> f2 = start countInfinity();

    // Cancels the asynchronous execution.
    f2.cancel();
    io:println("Counting done in one second: ", count);

    // Asynchronously invokes the action call `get()`.
    // By default this async call runs on the same physical thread of the caller.
    // `@strand` annotation allows the invocation to run parallel.
    future<http:Response|error> f3 = @strand {thread:"any"} start clientEndpoint-> get("/get?test=123");

    // Waits for action call `f3` to finish.
    http:Response|error response = wait f3;
    // Prints the response payload of the action call if successful or prints the
    // reason for the failure.
    if (response is http:Response) {
        io:println(response.getJsonPayload());
    } else {
        io:println(response.reason());
    }

    // Asynchronously invokes the functions named `square()` and `greet()`.
    future<int> f4 = start square(20);
    future<string> f5 = start greet("Bert");

    // You can wait for any one of the given futures to complete.
    // In this example,`f4` will finish before `f5` since `runtime:sleep()` is called
    // in the `greet()` function to delay its execution. The value returned
    // by the future that finishes first will be taken as the result.
    int|string anyResult = wait f4|f5;
    io:println(anyResult);

    // Asynchronously invokes the functions named `sum()`, `cube()`, and `greet()`.
    future<int> f6 = start sum(40, 60);
    future<int> f7 = start cube(3);
    future<string> f8 = start greet("Moose");
    // A `runtime:sleep` is added to delay the execution.
    runtime:sleep(2000);

    // You can wait for all the given futures to complete.
    // The result of this `wait` action can be assigned to a map or a record.
    // If the result is a map, it will contain the returned values from each
    // future with the key as the name of the future (if a key is not provided).
    map<int|string> resultMap = wait {first_field: f6, second_field: f7,
                                            third_field: f8};
    io:println(resultMap);


    // If the result is a record, it will contain the returned values from each
    // future with the field name as the name of the future (if a field name is
    // not provided).
    record {int first_field; int second_field; string third_field;} rec =
                    wait {first_field: f6, second_field: f7, third_field: f8};
    io:println("first field of record --> ", rec.first_field);
    io:println("second field of record --> ", rec.second_field);
    io:println("third field of record --> ", rec.third_field);
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
        sq = <- w1;
        return sq + cb;
    }
    // Waits for the worker `W2` to complete.
    return wait w2;
}

function countInfinity() {
    while (true) {
        runtime:sleep(1);
        count += 1;
    }
}
