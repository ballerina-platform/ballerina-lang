function invalidWorkReceiveBeforeWorker() {
  int i = 1;
  i -> w1;
  worker w1 {
    int m1 = <- default;
  }
}