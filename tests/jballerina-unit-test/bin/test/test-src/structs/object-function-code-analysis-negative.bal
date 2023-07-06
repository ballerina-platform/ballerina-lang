import ballerina/jballerina.java;

public class Response {

  function f1() returns string|error {
    string|error ret = "";
    if (ret is error) {
      println("ERROR");
    } else if (ret is string) {
      boolean x = true;
      if (x) {
        ret = "X";
      } else {
        ret = "Y";
      }
    }
  }

}

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
