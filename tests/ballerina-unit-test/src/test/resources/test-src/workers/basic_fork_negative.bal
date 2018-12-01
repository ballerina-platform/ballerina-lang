import ballerina/io;

function forkToDefaultWorkerInteraction() returns int {
    int x = 5;
    fork {
	   worker w1 {
	     int a = 5;
	     int b = 0;
	     a -> w2;
	     b = <- w2;
	     a -> default;
	   }
	   worker w2 {
	     int a = 0;
	     int b = 15;
	     a = <- w1;
	     b -> w1;
	     b -> default;
	   }
	}
    map<any> results = wait {w1, w2};
    io:println(results);
	return x;
}
