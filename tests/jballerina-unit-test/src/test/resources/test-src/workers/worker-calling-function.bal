function testWorkerInVM () returns int {
    int q = 0;
    q = testWorker();
    return q;
}

function testWorker () returns int {

    @strand{thread:"any"}
    worker w1 returns int {
        int result = 0;
        int i = 10;
        i -> sampleWorker;
        result = <- sampleWorker;
        return result;
    }

    @strand{thread:"any"}
    worker sampleWorker {
        int r = 120;
        int i = 0;
        i = <- w1;
        r = changeMessage(i);
        r -> w1;
    }
    return wait w1;
}

function changeMessage (int i) returns int {
    return i + 10;
}