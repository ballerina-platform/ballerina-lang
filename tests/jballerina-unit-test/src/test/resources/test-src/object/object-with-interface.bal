
public function testObjectWithInterface () returns [int, string, int, string] {
    Person p = new Employee(100, 10, val1 = "adding value in invocation");
    return [p.age, p.name, p.year, p.month];
}


type Person object {
    public int age;
    public string name;

    int year;
    string month;

    function attachFunc1(int add, string value1) returns [int, string];

    function attachInterface(int add, string value1) returns [int, string];
};

class Employee {
    public int age = 20;
    public string name = "sample name";

    int year = 50;
    string month = "february";

    function init (int year, int count, string name = "sample value1", string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age = self.age + count + 50;
        self.month = val1 + " uuuu";
    }

    function attachFunc1(int add, string value1) returns [int, string] {
        int count = self.age + add;
        string val2 = value1 + self.month;
        return [count, val2];
    }

    function attachInterface(int add, string value1) returns [int, string]{
        int count = self.age + add;
        string val2 = value1 + self.month;
        return [count, val2];
    }
}


