import ballerina/io;
import pkg1;

public function func2(){
    io:println("func2...");
    pkg1:TestObject obj = new(50, "Bob");
    obj.print();
}
