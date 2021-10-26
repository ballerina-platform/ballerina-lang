import ballerina/jballerina.java;

public function main() {
    worker w1 returns boolean|error{
      int i = 2;
      if (0 > 1) {
           return true;
      }
      i -> w2;
      println("w1");
      return false;
    }

    worker w2 {
      int j = 25;
      j = <- w1;
      println(j);
    }
}

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
