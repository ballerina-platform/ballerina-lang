import ballerina/io;

// In Ballerina, each function consists of one or more workers, which are independent 
// parallel execution code blocks. If explicit workers are not mentioned with worker blocks,
// the function code will belong to a single implicit default worker.
function main(string... args) {
    worker w1 {
        // Calculate sum(n)
        int n = 10000000;
        int sum;
        foreach i in 1...n {
            sum += i;
        }
        io:println("sum of first ", n, " positive numbers = ", sum);
    }
    worker w2 {
        // Calculate sum(n^2)
        int n = 10000000;
        int sum;
        foreach i in 1...n {
            sum += i * i;
        }
        io:println("sum of squares of first ", n,
            " positive numbers = ", sum);
    }
}
