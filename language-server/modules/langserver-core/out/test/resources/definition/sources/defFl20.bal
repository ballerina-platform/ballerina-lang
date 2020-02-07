import ballerina/io;
import ballerina/runtime;

string append = "";

function process() returns string {
  worker w1 {
    int aa = 10;
    aa -> w2;
    () result = aa ->> w2;

    aa -> w2;
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
      b = <- w1;
  }

  wait w1;
  runtime:sleep(50);
  return "done";
}