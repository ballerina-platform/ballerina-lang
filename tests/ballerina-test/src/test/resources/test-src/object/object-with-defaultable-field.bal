
public function testObjectWithSimpleInit () returns (int, string, int, string) {
    Person p = new Person(99);
    return (p.age, p.name, p.year, p.month);
}

type Person object {
    public {
        int age = 10,
        string name;
    }
    private {
        int year;
        string month = "february";
    }

    new (year = 50, int count, name = "sample value1", string val1 = "default value") {
        age = age + count;
        month = val1;
    }
}


