import ballerina/lang.system;

function testWorkerInVM(int y)(int) {
    int q;
    q = foo(y);
    return q;
}

function foo(int x)(int) {
    int j = 1000;
    float f = 1.234;
    j, f -> sampleWorker1;

    int r = 333;
    float s = 555.444;
    r, s -> sampleWorker2;

    int jj;
    float ff;
    jj, ff <- sampleWorker1;
    system:println("Value received from sampleWorker1 " + jj + " " + ff);

    int rr;
    float ss;
    rr, ss <- sampleWorker2;
    system:println("Value received from sampleWorker2 " + rr + " " + ss);
    return j;

    worker sampleWorker1 {
    int p;
    float g;
    p, g <- default;
    system:println("11111111111 - Value received from default worker is " + p + " " + g);

    int pp = 888;
    float gg = 99.999;
    pp, gg -> default;
    return;
    }

    worker sampleWorker2 {
    int m;
    float n;
    m, n <- default;
    system:println("222222222222 - Value received from default worker is " + n + " " + m);

    int mm = 777;
    float nn = 44.444;
    mm, nn -> default;
    return;
    }

}

