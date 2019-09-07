import ballerina/io;

int y = 9;

public function main(string... args) {
    io:println("test print");
}


function test() returns int{
int c = 100;
   io:println("Test =============");
   return c;
}