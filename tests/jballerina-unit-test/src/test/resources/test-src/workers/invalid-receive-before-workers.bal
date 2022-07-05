function invalidWorkReceiveBeforeWorker() {
  int _ = <- w1;
  worker w1 {
    int i = 1;
    i -> function;
  }
}
