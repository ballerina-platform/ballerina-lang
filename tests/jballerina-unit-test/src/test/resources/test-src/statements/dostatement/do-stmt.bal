import ballerina/io;

//function testOnFailWithReturn () returns boolean {
//   boolean failureHandled = false;
//   do {
//     error err = error("custom error", message = "error value");
//     io:println("Before failure throw");
//     fail err;
//     failureHandled = false;
//   }
//   on fail error e {
//      io:println("whoops error caught !");
//      failureHandled = true;
//   }
//   return failureHandled;
//}

function testOnFail()  {
   do {
     error err = error("custom error", message = "error value");
     io:println("Before failure throw");
     fail err;
   }
   on fail error e {
      io:println("Whoops error caught ! Error: ", e.message());
   }
   io:println("After do block");
}