import ballerina/io;

public function main() {

    @strand{thread:"any"}
    worker w1 {
        123 -> w2;
    }

    @strand{thread:"any"}
    worker w3 {
        456 -> w2;
    }

    @strand{thread:"any"}
    worker w2 {
        record{|
            int a;
            string b;
        |} r = <- { a:w1, b:w3 };
        io:println("r: ", r);
    }
}
