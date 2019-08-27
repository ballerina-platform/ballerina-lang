

public function main() {
    worker w1{
      int i = 20;
      i -> w2;
    }

    worker w2 {
      int j = 25;
      int sum = 0;
      foreach var i in 1...10 {
          sum = sum + i;
          j = <- w1;
      }
    }
}

