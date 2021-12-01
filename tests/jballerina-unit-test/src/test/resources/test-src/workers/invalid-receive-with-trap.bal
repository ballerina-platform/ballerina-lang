

public function foo() {
    worker w1 {
      int i = 2;
      i -> w2;
    }

    worker w2 {
      int _ = trap <- w1;
    }
}

public function bar() {
    worker w1 {
      "hello" ->> w2;
    }

    worker w2 {
      string _ = trap <- w1;
    }
}
