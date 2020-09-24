 import ballerina/runtime;

public function foo() {
      future  <  (  )  >
               f2   =   start   countInfinity()  ;
    f2.  cancel    (  )  ;
}

function countInfinity() {
   while (true) {
       runtime:sleep(1);
   }
}
