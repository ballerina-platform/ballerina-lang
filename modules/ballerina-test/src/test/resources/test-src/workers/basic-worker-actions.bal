function workerDeclTest() {
   worker w1 {
     int a = 10;
     int c = a + 1;
   }
   worker w2 {
     int a = 20;
     a = a + 1;
     int c = 1;
   }
}

function simpleMessagePassing() {
   worker w1 {
     int a = 10;
     a -> w2;
     a <- w2;
   }
   worker w2 {
     int a = 0;
     int b = 15;
     a <- w1;
     b -> w1;
   }
}
