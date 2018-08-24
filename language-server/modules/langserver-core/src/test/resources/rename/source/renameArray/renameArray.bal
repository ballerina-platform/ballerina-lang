import ballerina/io;
function main(string... args) {
    int[] a = [];
    foreach item in a {
        io:println(a);
    }
    iterate(a);
}

function iterate(int[] m) {
    io:println("");
}
