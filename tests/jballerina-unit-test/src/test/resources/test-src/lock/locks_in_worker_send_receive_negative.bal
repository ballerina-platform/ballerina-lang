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

public function test() {
    worker A {
        lock {
        int num = 10;

        num -> B;

        string msg = <- B;
        io:println(string `Received string "${msg}" from worker B`);

        }

    }

    worker B {
        lock {
            int num;

            num = <- A;
            io:println(string `Received integer "${num}" from worker A`);

            string msg = "Hello";
            msg -> A;
        }
    }
}
