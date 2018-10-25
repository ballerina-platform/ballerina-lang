import ballerina/io;
import pkg1;

public function main(string... args) {
    pkg1:TestObject obj = new();
    obj.print();
}
