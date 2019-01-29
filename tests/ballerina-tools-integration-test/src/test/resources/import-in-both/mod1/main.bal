import ballerina/io;
import manny/mod2;

# Prints `Hello World`.

public function main() {
    io:println("Hello Manny !! I got a message --> " + mod2:add() + " <---->" + mod2:square());
}


