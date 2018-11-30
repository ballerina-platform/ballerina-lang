import ballerina/io;

public function main() {
    worker w1{
      int i = 20;
      i -> w2;
    }

    worker w2 {
      int j = 25;
      if(false){
          j = <- w1;
      }
    }
}

