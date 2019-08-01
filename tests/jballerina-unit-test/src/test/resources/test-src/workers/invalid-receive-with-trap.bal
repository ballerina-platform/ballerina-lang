import ballerina/io;

public function main() {
    worker w1 {
      int i = 2;
      i -> w2;
    }

    worker w2 {
      int j = trap <- w1;
    }
}

