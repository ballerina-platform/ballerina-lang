import      ballerina/io   ;

public       function          factorialR          (    int     x    )      returns
        int     {
    if
      (  x
        <
           2
             )
                {
                  return       x
                      ;
            }
    else          {
        int
              fact
                      =
                        x
                            *  factorialR   (   x    -    1   )   ;
          return         fact         ;
        }
   }

public       function            factorialL        (    int      x   )
          returns     int {
    if   (   x    ==    0   )    {
            return
                  0
               ;
            }
       int           fact         =    1    ;
    for    var     i      in       1
         ...  x       {
        fact   =    fact     *
           i   ;
      }
    return
           fact    ;
     }

function     printValue  (    int      x
   ,       int       factorialX        )       {
    io  :  println  (         "The factorial of "   ,    x   ,    " is: "   ,   factorialX   )   ;
}

public function main() {
        const
            n1       =    5       ;
     int      a    =    factorialR  (  n1  )    ;
    printValue(n1, a);
            const
        int
             n2
      =
         25
        ;
    int      b     =       factorialL   (   n2   )   ;
         printValue     (   n2   ,     b   )   ;
}
