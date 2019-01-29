import ballerina/io;
public function main(string... args) {
    int a = 10;
    while (a < 100) {
        a += 1;
        a -= 1;
    }
}
