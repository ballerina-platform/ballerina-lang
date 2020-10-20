public function foo() {
   worker w1 {
       int     i       =         100;  (  )   send   =   i   ->>   w2  ;
   }







   worker w2 {
       int lw;
          lw   =   <-   w1
          ;
   }
}
