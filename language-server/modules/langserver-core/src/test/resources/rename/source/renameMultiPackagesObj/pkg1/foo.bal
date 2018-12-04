import ballerina/io;

public type TestObject object {
    public int age = "45";
    public string name = "Bob";
    public new(age, name) {}
    public function print(){
        io:println("name:" + name);
    }
};
