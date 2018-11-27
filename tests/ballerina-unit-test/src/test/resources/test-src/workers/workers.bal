import ballerina/io;

function workerReturnTest() returns int{
    worker wx returns int {
	    int x = 50;
	    return x + 1;
    }
    return (wait wx) + 1;
}
