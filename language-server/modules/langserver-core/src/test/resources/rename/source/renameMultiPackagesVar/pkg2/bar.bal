import ballerina/io;
import pkg1;

public function func2(){
    io:println("func2...");
    pkg1:func1();
}
