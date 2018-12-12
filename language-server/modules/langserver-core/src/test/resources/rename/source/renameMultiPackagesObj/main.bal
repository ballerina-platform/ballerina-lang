import ballerina/io;
import pkg1;

public function main(string... args) {
    pkg1:TestObject obj = new(55, "Alice");
    obj.print();
}
