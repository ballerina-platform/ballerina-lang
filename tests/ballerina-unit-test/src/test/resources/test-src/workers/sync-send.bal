import ballerina/runtime;
import ballerina/io;

string append = "";
function simpleSyncSend() returns string {
    string done = process();
    return append;
}

function process() returns string {
   worker w1 {
     int a = 10;
     a -> w2;
     () result = a ->> w2;
     foreach var i in 1 ... 5 {
                           append = append + "w1";
                   }
    }

   worker w2 {
     int b = 15;
     runtime:sleep(10);
      foreach var i in 1 ... 5 {
            append = append + "w2";
             }
     b = <- w1;
     b = <- w1;
   }

   wait w1;
   runtime:sleep(50);
   return "done";
}

string append2 = "";
function multipleSyncSend() returns string{
    worker w1 {
         int a = 10;
         var result = a ->> w2;
         foreach var i in 1 ... 5 {
                               append2 = append2 + "w1";
                       }
         result = a ->> w2;
         foreach var i in 1 ... 5 {
                 append2 = append2 + "w11";
        }
        }

       worker w2 returns error? {
         int b = 15;
         runtime:sleep(10);
          foreach var i in 1 ... 5 {
                append2 = append2 + "w2";
         }
         if(false){
              error err = error("err", { message: "err msg" });
              return err;
         }
         b = <- w1;
         foreach var i in 1 ... 5 {
                         append2 = append2 + "w22";
                          }
         b = <- w1;
         return;
       }
       wait w1;
       return append2;
}

function process2() returns any {
    return returnNil();
}

function returnNil() returns any {
   worker w1 returns any {
     int a = 10;
     a -> w2;
     var result = a ->> w2;
     foreach var i in 1 ... 5 {
                           append = append + "w1";
                   }
      return result;
    }

   worker w2 returns error?{
     if(false){
          error err = error("err", { message: "err msg" });
          return err;
     }
     int b = 15;
     runtime:sleep(10);
      foreach var i in 1 ... 5 {
            append = append + "w2";
             }
     b = <- w1;
     b = <- w1;
     return;
   }

   var result = wait w1;
   return result;
}

string append3 = "";
function multiWorkerSend() returns string{
    worker w1 {
         int a = 10;
         var result = a ->> w2;
         a -> w3;
         foreach var i in 1 ... 5 {
                               append3 = append3 + "w1";
                       }
         result = a ->> w2;
         a -> w3;
         foreach var i in 1 ... 5 {
                 append3 = append3 + "w11";
        }
        }

       worker w2 returns error? {
         if(false){
              error err = error("err", { message: "err msg" });
              return err;
         }

         if(false){
              error err = error("err", { message: "err msg" });
              return err;
         }
         int b = 15;
         runtime:sleep(10);
          foreach var i in 1 ... 5 {
                append3 = append3 + "w2";
                 }
         b -> w3;
         b = <- w1;
         b -> w3;
         foreach var i in 1 ... 5 {
                         append3 = append3 + "w22";
                          }
         b = <- w1;
         return;
       }


       worker w3 returns error? {
                int|error x =  <- w2;
                int b;
                foreach var i in 1 ... 5 {
                       append3 = append3 + "w3";
                        }
                b = <- w1;
                x = <- w2;
                foreach var i in 1 ... 5 {
                                append3 = append3 + "w33";
                                 }
                b = <- w1;
                return;
              }

       wait w1;
       return append3;
}

string append4 = "";
function errorResult() returns error? {
    worker w1 returns error? {
         int a = 10;
         var result = a ->> w2;
         result = a ->> w3;
         foreach var i in 1 ... 5 {
                               append4 = append4 + "w1";
                       }
         result = a ->> w2;
          result = a ->> w3;
         foreach var i in 1 ... 5 {
                 append3 = append4 + "w11";
        }

        return result;
        }

       worker w2 returns error? {
         if(false){
              error err = error("err", { message: "err msg" });
              return err;
         }
         int b = 15;
         runtime:sleep(10);
          foreach var i in 1 ... 5 {
                append4 = append4 + "w2";
                 }
         b -> w3;
         b = <- w1;
         var result = b ->> w3;
         foreach var i in 1 ... 5 {
                         append4 = append4 + "w22";
                          }
         b = <- w1;
         return;
       }

       worker w3 returns error|string {
                if(false){
                     error err = error("err", { message: "err msg" });
                     return err;
                }
                int b;
                int|error be;
                be = <- w2;
                 foreach var i in 1 ... 5 {
                       append4 = append4 + "w3";
                        }
                b = <- w1;
                b = <- w2;
                if (b > 0) {
                    map<string> reason = { k1: "error3" };
                    map<string> details = { message: "msg3" };
                    error er3 = error(reason.k1, details);
                    return er3;
                }
                foreach var i in 1 ... 5 {
                                append4 = append4 + "w33";
                                 }
                b = <- w1;
                return "success";
              }

       error? w1Result = wait w1;
       return w1Result;
}


function panicTest() returns error? {
    worker w1 returns error? {
         int a = 10;
         a -> w2;
         var result = a ->> w3;

         a -> w2;
          result = a ->> w3;


        return result;
        }

       worker w2{
         int b = 15;
         runtime:sleep(10);

         b -> w3;
         b = <- w1;
         var result = b ->> w3;

         b = <- w1;
       }

       worker w3 returns string|error {
                if(false){
                     error err = error("err", { message: "err msg" });
                     return err;
                }
                int b;
                b = <- w2;

                b = <- w1;
                b = <- w2;
                if (b > 0) {
                    map<string> reason = { k1: "error3" };
                    map<string> details = { message: "msg3" };
                    error er3 = error(reason.k1, details);
                    panic er3;
                }

                b = <- w1;
                return "success";
              }

       error? w1Result = wait w1;
       return w1Result;
}
