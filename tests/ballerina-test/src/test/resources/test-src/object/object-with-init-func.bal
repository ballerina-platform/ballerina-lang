
public function testObjectWithSimpleInit () returns (int, string, int, string) {
    Person p = new Person(99, 7);
    return (p.age, p.name, p.year, p.month);
}

public function testObjectWithSimpleInitWithDiffValues () returns (int, string, int, string) {
    Person p = new Person(675, 27, val1 = "adding value in invocation");
    return (p.age, p.name, p.year, p.month);
}

public function testObjectWithoutRHSType () returns (int, string, int, string) {
    Person p = new (675, 27, val1 = "adding value in invocation");
    return (p.age, p.name, p.year, p.month);
}


type Person object {
    public {
        int age = 10,
        string name = "sample name";
    }
    private {
        int year = 50;
        string month = "february";
    }

    new (year, int count, name = "sample value1", string val1 = "default value") {
        age = age + count;
        month = val1;
    }
};
