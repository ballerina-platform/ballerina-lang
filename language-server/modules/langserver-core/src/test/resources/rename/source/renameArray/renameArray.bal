import ballerina/io;
public function main(string... args) {
    int[] a = [];
    foreach int item in a {
        io:println(a);
    }
    iterate(a);
}

function iterate(int[] m) {
    io:println("");
}
