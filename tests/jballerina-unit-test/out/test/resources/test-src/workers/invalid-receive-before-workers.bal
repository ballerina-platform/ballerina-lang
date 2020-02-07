function invalidWorkReceiveBeforeWorker() {
  int m1 = <- w1;
  worker w1 {
    int i = 1;
    i -> default;
  }
}