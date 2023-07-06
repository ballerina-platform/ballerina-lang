import fruits;
import ballerina/http;
public function main (string... args) {
    int a = 10;
    fruits:Apple apple = new();
    apple.print();
}
