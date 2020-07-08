import ballerina/io;

public function main() {

        @strand{thread:"any"}
        worker w2 returns int {
            map<int> m = <- { w1 };
            io:println("m: ", m);
            return 0;
        }
}
