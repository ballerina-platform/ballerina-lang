import ballerina/io;

int gInt = 10;
Apple gApple = new();

public class Apple {
    public function init() {}
    public function print() {
        io:println("apple");
    }
}
