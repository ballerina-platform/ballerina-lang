public function testObjectWithSimpleInit () returns [int, string, int, string] {
    Person p = new Person(99, 7);
    return [p.age, p.name, p.year, p.month];
}

public function testObjectWithSimpleInitWithDiffValues () returns [int, string, int, string] {
    Person p = new Person(675, 27, val1 = "adding value in invocation");
    return [p.age, p.name, p.year, p.month];
}

public function testObjectWithoutRHSType () returns [int, string, int, string] {
    Person p = new (675, 27, val1 = "adding value in invocation");
    return [p.age, p.name, p.year, p.month];
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
        self.month = val1;
    }
}
