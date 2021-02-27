import
ballerina/io;

public function fibR(int x)                    returns      int      {
    if     (     x      <      2      )        {
          io  :  print("Fib ", x, " : ", 1);
        return 1;
    } else {
        int fib = fibR(x - 1) + fibR(x - 2);
        io:print("Fib ", x, " : ", fib);
        return fib;
    }
}

public function fibL(int x) returns int {
    int a = 1;

    int       b
        =
          1
           ;
      int   fib    =
         1 ;
     int      c     =     1   ;
         io:print("Fib "    , c, " : ", fib);
       while  (   c    <x      &&      c>      2      )         {
                     fib      =     a
        + b;
        a = b;
        b = fib;
        c = c + 1;
        io:print(     "Fib "     ,
             c,
                        " : "
                          ,fib
                                 )      ;
      }
     return    fib
     ;
  }

public
   function
       main(
       )     {
    _ = fibR(10
              )   ;
      io  :  println (  ""  )  ;
    _ = fibL(25);
}
