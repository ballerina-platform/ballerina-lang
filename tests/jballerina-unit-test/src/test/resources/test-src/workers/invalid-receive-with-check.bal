import ballerina/io;

public function main() {
    worker w1 {
      int i = 2;
      i -> w2;
    }

    worker w2 returns error? {
      int j = check <- w1;
      return;
    }
}

