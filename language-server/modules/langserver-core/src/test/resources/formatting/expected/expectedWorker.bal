import ballerina/io;
const annotation v1 on source worker;

public function main(string... args) {
    worker worker1 {
        int a = 0;
    }

    worker worker2 {
        int b = 0;
    }
    @v1
    worker w2 {

    }

    fork {
        @v1
        worker w1 {

        }
    }
}

function test1() {
    worker w1 {
        int x = 2;
        x -> w2;
        error? e = flush w2;
        io:println("Hello, w1");
    }

    worker w2 {
        int s = <- w1;
        io:println("Hello, w2");
    }

    _ = wait {w1, w2};
}

function test2() {
    worker w1 {
    }

    worker w2 returns int {
        return 1;
    }

    _ = wait {w1, w2};
}

function test3() {
    worker
    w1
    {
    }

    worker
    w2
    returns
    int
    {
        return 1;
    }

    _ = wait
    {
    w1
    ,
    w2
    }
    ;
}
