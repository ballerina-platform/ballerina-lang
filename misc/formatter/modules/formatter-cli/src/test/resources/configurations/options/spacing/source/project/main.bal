import ballerina/io;

type Student record {|
    string name;
    int age;
|};

public function main(string... args) {
    decimal d = 10.0;
    float|decimal fd = d;
    float b = <float>fd;
    io:println(b);

    Student student = {name: "Harry", age: 12};
    io:println(student.name);
}
