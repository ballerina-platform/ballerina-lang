import ballerina/io;

type TestObject object {
    public int age;
    public string name;
    new(age, name) {}
};

public function main(string... args) {
    TestObject obj = new(30, "John");
    int age = obj.age;
    io:println(obj.age);
    io:println(age);
}
