import ballerina/io;
public function main(string... args) {
    map<string> a = {};
    foreach var item in a {
        io:println(a);
    }
    iterate(a);
}

function iterate(map<string> m) {
    io:println("");
}
