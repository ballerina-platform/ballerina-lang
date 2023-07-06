
public function testObjectWithSimpleInit () returns [int, string, int, string] {
    Person p = new Person(99);
    return [p.age, p.name, p.year, p.month];
}

class Person {
    public int age = 10;
    public string name = "";

    int year = 0;
    string month = "february";

    function init (int count, int year = 50, string name = "sample value1", string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age = self.age + count;
        self.month = val1;
    }
}
