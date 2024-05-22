import ballerina/io;

class Student
{
    final string name;
    int age;

    function init(string name, int age)
    {
        self.name = name;
        self.age = age;
    }
}

public function main()
{
    Student student = new Student("Alice", 52);
    io:println(student);
}
