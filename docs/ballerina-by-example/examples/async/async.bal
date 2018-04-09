import ballerina/io;
import ballerina/runtime;
import ballerina/http;
import ballerina/mime;

int count;

endpoint http:ClientEndpoint clientEndpoint { targets:[{url: "https://postman-echo.com" }] };

function main(string[] args) {
  // call the function "sum" asynchronously
  future<int> f1 = async sum(40, 50);
  // future values can be passed around to get the result later
  int result = square_plus_cube(f1);
  io:print("SQ + CB = ");
  io:println(result);

  // call "countInfinity", which will run forever in async mode
  future f2 = async countInfinity();
  runtime:sleepCurrentWorker(1000);
  // checks whether the function call is done
  io:println(f2.isDone());
  // checks if someone cancelled the async execution
  io:println(f2.isCancelled());
  // cancel the async operation
  boolean cancelled = f2.cancel();
  io:println(cancelled);
  io:print("Counting done in one second: ");
  io:println(count);
  io:println(f2.isDone());
  io:println(f2.isCancelled());

  // async action call
  http:Request req = new;
  future<http:Response|http:HttpConnectorError> f3 = async clientEndpoint -> get("/get?test=123", req);
  io:println(sum(25, 75));
  io:println(f3.isDone());
  var response = await f3;
  match response {
    http:Response resp => {
      io:println("HTTP Response: ");
      var msg = resp.getJsonPayload();
      match msg {
        json jsonPayload1 => {
          io:println(jsonPayload1);
        }
        http:PayloadError payloadError1 => {
          io:println(payloadError1.message);
        }
      }
    }
    http:HttpConnectorError err => { io:println(err.message); }
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


