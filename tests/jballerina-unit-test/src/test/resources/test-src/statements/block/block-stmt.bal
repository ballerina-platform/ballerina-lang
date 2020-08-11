import ballerina/io;
//function test1() returns error {
function test1()  {

//int o = 1;
//while(true) {
//     io:println("Asda");
//
//     break;
//    //if(o < 5) {
//    //
//    //        o = o + 1;
//    //} else {
//    //    break;
//    //}
//
//}
   do {
        error err = error("custom error", message = "error value");
        io:println("Before fail");
        fail err;

        io:println("After fail");
   }
   on fail error e {
      //error err = error("custom error", message = "error value");
      io:println("whoops!!!!");
      //return err;

   }
   //error err = error("custom error", message = "error value");
     io:println("After do !!!");
      //return err;
   //  error err = error("custom error", message = "error value");
   //        return err;
   //do {
   //   check foo(true);
   //   io:println("After check");
   //}
   //on fail error e {
   //   error err = error("custom error", message = "error value");
   //   io:println("whoops!!!!");
   //   return err;
   //}
   //io:println("After do");
   //
   // do {
   //     var onFailFunc = function () returns error {
   //                           io:println("whoops!!!!");
   //                           error err = error("custom error");
   //                           return err;
   //                      };
   //     match (foo(true)) {
   //             var error(message) => {
   //                 io:println("inside error");
   //                 return onFailFunc();
   //                 //break; no break needed since we return
   //             }
   //             1 => {
   //                 io:println("inside int");
   //                 break;  //had to add this since do desugared to while(true)
   //             }
   //     }
   //     io:println("After check");
   // }
   // io:println("Outside do");
   // io:println("==================================================================================");
   // do {
   //    check foo(true);
   //    error e = error("custom error");
   //    io:println("After check");
   // } on fail error e
   //    io:println("whoops!!!!");
   // }
   // io:println("After do");

        //do {
        //    var onFailFunc = function () {
        //                          io:println("whoops!!!!");
        //                     };
        //    match (foo(true)) {
        //            var error(message) => {
        //                io:println("inside error");
        //                onFailFunc();
        //                break;  //break needed since no return
        //            }
        //            1 => {
        //                io:println("inside int");
        //                break;  //had to add this since do desugared to while(true)
        //            }
        //    }
        //    io:println("After check");
        //}
        //io:println("Outside do");
}
//
//function foo(boolean er) returns int|error {
//    if(er) {
//        error err = error("custom error");
//        return err;
//    }
//    return 1;
//}