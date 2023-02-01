import ballerina/io;

int a = 10;
int b = 20;

public function main() {
    worker w1 {
        lock {
            a += 1;
            io:println(a);
            3 ->> w2;
            b += 1;
            io:println(b);
        }
    }

    worker w2 {
        lock {
            b += 1;
            io:println(b);
            int x = <- w1;
            io:println(x);
            a += 1;
            io:println(a);
        }
    }
}
