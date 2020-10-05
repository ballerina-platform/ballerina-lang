function testNonErrorTypeWithOnFail () returns string {
   string str = "";
   do {
     error err = error("custom error", message = "error value");
     str += "Before failure throw";
     fail str;
   }
   on fail string e {
      str += "-> Error caught ! ";
   }
   str += "-> Execution continues...";
   return str;
}

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
