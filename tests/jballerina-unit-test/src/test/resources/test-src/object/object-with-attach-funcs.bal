
public function testObjectWithAttachedFunc1 () returns [int, string, int, string] {
    Person p = new Person(99, 7);
    var [a, b] = p.attachFunc1(344, "added values ");
    return [a, b, p.year, p.month];
}

class Person {
    public int age = 10;
    public string name = "sample name";

    int year = 50;
    string month = "february";

    function init (int year, int count, string name = "sample value1", string val1 = "default value") {
        self.year = year;
        self.name = name;
        self.age = self.age + count;
    }

    function attachFunc1(int add, string value1) returns [int, string] {
        int count = self.age + add;
        string val2 = value1 + self.month;
        return [count, val2];
    }
}
