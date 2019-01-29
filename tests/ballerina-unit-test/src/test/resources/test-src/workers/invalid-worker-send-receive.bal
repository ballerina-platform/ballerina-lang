
function invalidWorkerSendReceive() {
    fork {
	   worker w1 {
	     int a = 5;
	     int b = 0;
	     a -> w2;
	     b = <- w3;
	   }
	   worker w2 {
	     int a = 0;
	     int b = 15;
	     a = <- w1;
	     a -> w3;
	   }
	   worker w3 {
	     int a = 0;
	     int b = 15;
	     a = <- w2;
	   }
    }
}