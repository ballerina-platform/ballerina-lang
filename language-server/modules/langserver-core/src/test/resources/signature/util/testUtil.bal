function helloFunc() {
    int a = 12;
    int c = testFunction(test1(1, test2(1, 2,testFunction(1,2))),);
}

function testFunction (int a, int b) returns (int q){
    return 2;
}

function test1(int a, int b) returns (int c) {
	return 1;
}

function test2(int a, int b, int d) returns (int c) {
	return 1;
}