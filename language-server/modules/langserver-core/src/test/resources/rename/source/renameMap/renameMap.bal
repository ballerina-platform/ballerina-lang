import ballerina/io;
function main(string... args) {
    map a;
    foreach item in a {
        io:println(a);
    }
    iterate(a);
}

function iterate(map m) {
    io:println("");
}