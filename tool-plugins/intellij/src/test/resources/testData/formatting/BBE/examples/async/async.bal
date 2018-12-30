import ballerina/http;
import ballerina/io;
import ballerina/runtime;

int count = 0;

http:Client clientEp = new ("https://postman-echo.com", config = {});

public function main()   {
// Asynchronously call the function named `sum()`.
future<int> f1 = start sum(40, 50);
// You can pass around the value of the `future` variable
// and call its results later.
int result =  squarePlusCube(f1);
io:println("SQ + CB = ", result);

// Call the `countInfinity()` function, that runs forever in asynchronous
// mode.
future<()> f2 =    start countInfinity();
runtime:  sleep(1000);
// Check whether the function call is done.
io:println  (f2.isDone());
// Check whether someone cancelled the asynchronous execution.
io:println(f2.isCancelled());
// Cancel the asynchronous operation.
boolean cancelled = f2.cancel();
io:println(cancelled);
io:println("Counting done in one second: ", count);
io:println(f2.isDone());
io:println(f2.isCancelled());

// async action call
future<http:Response|error> f3 = start clientEp->get("/get?test=123");
io:println(sum(25, 75));
io:println(f3.isDone());
var response = wait f3;
if (response is http:Response) {
io:println(untaint response.getJsonPayload());
} else if (response is error) {
io:println(response.reason());
}
io:println(f3.isDone());

future<int> f4 = start square(20);
future<string> f5 = start greet("Bert");
// You can wait for either of the asynchronous functions to finish.
// Here `f4` will finish before `f5` since runtim:sleep() is called
// in the `greet()` function to delay its execution. The value returned
// by the asynchronous function that finishes first will be taken as the
// result.
int|string anyResult = wait f4|f5;
io:println(anyResult);

future<int> f6 = start sum(40, 60);
future<int> f7 = start cube(3);
future<string> f8 = start greet("Moose");
// You can wait for all of the above asynchronous functions to finish.
// The result of all these functions can be assigned to a map or a record.
map<int|string> resultMap = wait { first_field: f6, second_field: f7,
third_field: f8 };
io:println(resultMap);

future<string> f9 = start greet("Bert");
record { int first_field; int second_field; string third_field; } rec =
wait { first_field: f6, second_field: f7, third_field: f9 };
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
return wait w2;
}

function countInfinity() {
while (true) {
count += 1;
}
}
