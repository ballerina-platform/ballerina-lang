import ballerina/io;

public type Response object {

  function f1() returns string|error {
    string|error ret = "";
    match ret {
      error => { io:println("ERROR"); }
      string => { 
        boolean x = true;
        if (x) {
          ret = "X";
        } else {
          ret = "Y";
        }
      }
    }
  }

};
