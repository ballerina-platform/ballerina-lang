

public function main() {
    worker w1{
      int i = 20;
      i -> w2;
    }

    worker w2 {
      if(false){
          int j = <- w1;
      }
    }
}

