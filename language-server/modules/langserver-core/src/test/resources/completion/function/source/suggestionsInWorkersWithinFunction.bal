import ballerina/io;

public function main(string... args) {
    worker w1 {
        int n = 10000000;
        int sum = 0;
        foreach int i in 1 ... n {
            sum += i;
            
        }
        io:println("sum of first ", n, " positive numbers = ", sum);
    }
    worker w2 {
        int n = 10000000;
        int sum = 0;
        foreach int i in 1 ... n {
            sum += i * i;
        }
        io:println("sum of squares of first ", n, 
                   " positive numbers = ", sum);
    }
}
