import ballerina/io;

int gInt = 10;
Apple gApple = new;

public type Apple object {
    public new(){}
    public function print () {
        io:println("apple");
    }
};
