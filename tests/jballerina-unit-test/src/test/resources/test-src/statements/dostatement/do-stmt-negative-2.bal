public function checkOnFailScope() returns int {
    int a = 10;
    int b = 11;
    int c = 0;
    do {
      int d = 100;
      c = a + b;
      error err = error("custom error", message = "error value");
      fail err;
    }
    on fail error e {
      c += d;
    }
    return c;
}
