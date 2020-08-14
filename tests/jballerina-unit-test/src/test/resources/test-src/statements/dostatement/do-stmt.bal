import ballerina/io;

function testOnFail() returns boolean {
   boolean failureHandled = false;
   do {
     error err = error("custom error", message = "error value");
     io:println("Before failure throw");
     fail err;
     failureHandled = false;
   }
   on fail error e {
      io:println("whoops error caught !");
      failureHandled = true;
   }
   return failureHandled;
}
