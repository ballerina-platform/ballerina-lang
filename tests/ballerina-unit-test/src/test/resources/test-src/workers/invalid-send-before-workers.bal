function invalidWorkReceiveBeforeWorker() {
  int i = 1;
  i -> w1;
  worker w1 {
    var m1 = <- default;
  }
}