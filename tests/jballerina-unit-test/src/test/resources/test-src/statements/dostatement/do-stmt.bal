import ballerina/io;

//function testOnFail() returns string {
function testOnFail() returns any|error {
   do {
        error err = error("custom error", message = "error value");
        io:println("Before fail");
        fail err;
        io:println("After fail");
   }
   on fail error e {
      io:println("whoops!!!!");
      return "failed";
   }
   io:println("After do !!!");
   io:println("###############################################");
   return "at end";

}
