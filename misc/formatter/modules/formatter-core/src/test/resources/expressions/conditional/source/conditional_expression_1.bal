function getValue(int v) returns int|() {
   return v;
}

public function foo() {
   int t = true   ?    7    :    0  ;
   int u = getValue(6)   ?:   8  ;

   string var1 =          true ?
    "hello"    :      "hi";

   string var2 = true ?      "hello" :
                  "hi";

                  string var3 = true ?
         "hello" :
                                    "hi";
}
