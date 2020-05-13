function invalidWorkReceiveBeforeWorker() {
  var w1 = function() {
    int i = 1;
    i -> default;
  };
}