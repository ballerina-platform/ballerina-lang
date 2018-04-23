
import ballerina/io;

public function benchmarkSprintfWithFloat() {
    string number = io:sprintf("%10.2f,", 123.331212);
}

public function benchmarkSprintfWithString() {
    string number = io:sprintf("%s is awesome!", "Ballerina");
}

