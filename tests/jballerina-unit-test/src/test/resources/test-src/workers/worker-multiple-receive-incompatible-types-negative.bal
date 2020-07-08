import ballerina/io;

public function main() {
        @strand{thread:"any"}
        worker w1 {
            "abc" -> w2;
        }

        @strand{thread:"any"}
        worker w2 {
            map<int> m = <- { w1 };
            io:println("m: ", m);
        }
}
