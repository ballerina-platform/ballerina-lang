import ballerina/io;

function workerTest1() {
    worker default {
        int i =100;
        io:println("default worker --> " + 100);
    }

    worker w1 {
        io:println("worker w1");
    }
}

function workerTest2() returns int{
    worker default {
        int x = 50;
        x -> default;
    }
    int y = <- default;
    return y + 1;
}

function workerTest3() {
    fork {
        worker default {
            int i =100;
            io:println("default worker --> " + 100);
        }
    }
}
