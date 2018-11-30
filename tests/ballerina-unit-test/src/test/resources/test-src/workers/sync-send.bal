import ballerina/runtime;
import ballerina/io;
string append = "";
 function simpleSyncSend() returns string {
    process();
    return append;
}

function process() {
   worker w1 {
     int a = 10;
     a -> w2;
     error? result;
     result = a ->> w2;
     foreach i in 1 ... 5 {
                           append = append + "w1";
                   }
    }

   worker w2 {
     int b = 15;
     runtime:sleep(10);
      foreach i in 1 ... 5 {
            append = append + "w2";
             }
     b = <- w1;
     b = <- w1;
   }

   wait w1;
}

string append2 = "";
function multipleSyncSend() returns string{
    worker w1 {
         int a = 10;
         var result = a ->> w2;
         foreach i in 1 ... 5 {
                               append2 = append2 + "w1";
                       }
         result = a ->> w2;
         foreach i in 1 ... 5 {
                 append2 = append2 + "w11";
        }
        }

       worker w2 {
         int b = 15;
         runtime:sleep(10);
          foreach i in 1 ... 5 {
                append2 = append2 + "w2";
                 }
         b = <- w1;
         foreach i in 1 ... 5 {
                         append2 = append2 + "w22";
                          }
         b = <- w1;
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
     foreach i in 1 ... 5 {
                           append = append + "w1";
                   }
      return result;
    }

   worker w2 {
     int b = 15;
     runtime:sleep(10);
      foreach i in 1 ... 5 {
            append = append + "w2";
             }
     b = <- w1;
     b = <- w1;
   }

   var result = wait w1;
   return result;
}

string append3 = "";
function multiWorkerSend() returns string{
    worker w1 {
         int a = 10;
         var result = a ->> w2;
         result = a ->> w3;
         foreach i in 1 ... 5 {
                               append3 = append3 + "w1";
                       }
         result = a ->> w2;
          result = a ->> w3;
         foreach i in 1 ... 5 {
                 append3 = append3 + "w11";
        }
        }

       worker w2 {
         int b = 15;
         runtime:sleep(10);
          foreach i in 1 ... 5 {
                append3 = append3 + "w2";
                 }
         b -> w3;
         b = <- w1;
         var result = b ->> w3;
         foreach i in 1 ... 5 {
                         append3 = append3 + "w22";
                          }
         b = <- w1;
       }

       worker w3 {
                int b;
                b = <- w2;
                 foreach i in 1 ... 5 {
                       append3 = append3 + "w3";
                        }
                b = <- w1;
                b = <- w2;
                foreach i in 1 ... 5 {
                                append3 = append3 + "w33";
                                 }
                b = <- w1;
              }

       wait w1;
       return append3;
}


