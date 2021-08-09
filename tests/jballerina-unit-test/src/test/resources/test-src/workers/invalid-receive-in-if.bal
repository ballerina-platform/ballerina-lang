

public function main() {
    worker w1{
      int i = 20;
      i -> w2;
    }

    worker w2 {
      if (0 > 1) {
          int j = <- w1;
      }
    }
}

