import ballerina/io;

public type TestObject object {
    public int age;
    public string name;
    public new(age, name) {}
    public function print(){
        io:println("name:" + name);
    }
};
