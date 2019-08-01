import ballerina/io;

public function main() {
    worker w1 returns boolean|error{
      int i = 2;
      float f = check mayGoWrong();
      i -> w2;
      io:println("w1");
      return false;
    }

    worker w2 {
      int j = 25;
      j = <- w1;
      io:println(j);
    }
}

function mayGoWrong() returns float|error {
    error err = error("err", message = "err msg");
    return err;
}

