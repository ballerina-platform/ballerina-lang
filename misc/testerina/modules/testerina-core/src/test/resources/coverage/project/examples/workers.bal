import ballerina/io;

public function workerInit() {
    worker w1 {

        int n = 100;
        int sum;
        foreach i in 1...n {
            sum += i;
        }
        io:println("sum of first ", n, " positive numbers = ", sum);
    }
    worker w2 {

        int n = 100;
        int sum;
        foreach i in 1...n {
            sum += i * i;
        }
        io:println("sum of squares of first ", n,
            " positive numbers = ", sum);
    }
}

