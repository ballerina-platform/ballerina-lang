import ballerina/runtime;

int i = 0;

function testVoidFunction() returns int {
    testVoid();
    return i;
}

function testVoid() {
    worker w1 {
        runtime:sleep(3000);
        testNew();
    }
    worker w2 {
         int x = i + 10;
         i = 5;
    }
}

function testNew(){
    worker w1 {
        runtime:sleep(2000);
    }
    worker w2 {
        i = 10;
    }
}

