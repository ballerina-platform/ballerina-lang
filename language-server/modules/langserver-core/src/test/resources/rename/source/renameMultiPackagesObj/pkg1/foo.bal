import ballerina/io;

public type TestObject object {
    public int age;
    public string name = "Bob";

    public function __init(int age, string name) {
        self.name = name;
        self.age = age;
    }

    public function print(){
        io:println("name:" + self.name);
    }
};
