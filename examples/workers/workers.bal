import ballerina/io;

// In Ballerina, each function consists of one or more workers, which are 
// independent execution paths called strands. If explicit workers are
// not mentioned within worker blocks, the function code will belong to a
// single implicit default worker. The default worker in each function wil be
// executed in the same strand as the caller function.
public function main() {
    io:println("Worker execution started");

    // This block belongs to the worker `w1`.
    worker w1 {
        // Calculates the sum(n).
        int n = 10000000;
        int sum = 0;
        foreach var i in 1...n {
            sum += i;
        }
        io:println("sum of first ", n, " positive numbers = ", sum);
    }

    // By default, workers run on the same physical thread of the parent strand.
    // By adding @concurrent annotation, this worker's strand is allowed to run
    // on any available physical thread.
    @concurrent{}
    // This block belongs to the worker `w2`.
    worker w2 {
        // Calculates the sum(n^2).
        int n = 10000000;
        int sum = 0;
        foreach var i in 1...n {
            sum += i * i;
        }
        io:println("sum of squares of first ", n,
            " positive numbers = ", sum);
    }

    // Waits for both workers to finish.
    _ = wait {w1, w2};

    io:println("Worker execution finished");
}
