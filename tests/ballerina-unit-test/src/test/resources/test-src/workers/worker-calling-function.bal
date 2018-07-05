function testWorkerInVM () returns int {
    int q;
    q = testWorker();
    return q;
}

function testWorker () returns int {

    worker w1 {
        int result;
        int i = 10;
        i -> sampleWorker;
        result <- sampleWorker;
        return result;
    }

    worker sampleWorker {
        int r = 120;
        int i;
        i <- w1;
        r = changeMessage(i);
        r -> w1;
    }

}

function changeMessage (int i) returns int {
    return i + 10;
}