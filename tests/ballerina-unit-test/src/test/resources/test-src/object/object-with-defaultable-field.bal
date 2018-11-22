
public function testObjectWithSimpleInit () returns (int, string, int, string) {
    Person p = new Person(99);
    return (p.age, p.name, p.year, p.month);
}

type Person object {
    public int age = 10;
    public string name = "";

    int year = 0;
    string month = "february";

    new (year = 50, int count, name = "sample value1", string val1 = "default value") {
        self.age = self.age + count;
        self.month = val1;
    }
};


