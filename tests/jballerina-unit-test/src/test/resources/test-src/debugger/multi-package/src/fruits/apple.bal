import ballerina/io;

int gInt = 10;
Apple gApple = new();

public type Apple object {
    public function init() {}
    public function print() {
        io:println("apple");
    }
};
