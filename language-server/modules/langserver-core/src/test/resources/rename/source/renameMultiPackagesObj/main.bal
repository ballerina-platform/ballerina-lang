import ballerina/io;
import pkg1;

function main(string... args) {
    pkg1:TestObject obj = new();
    obj.print();
}
