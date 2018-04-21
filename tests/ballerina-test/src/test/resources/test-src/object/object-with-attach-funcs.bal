
public function testObjectWithAttachedFunc1 () returns (int, string, int, string) {
    Person p = new Person(99, 7);
    var (a, b) = p.attachFunc1(344, "added values ");
    return (a, b, p.year, p.month);
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
    }

    function attachFunc1(int add, string value1) returns (int, string) {
        int count = age + add;
        string val2 = value1 + month;
        return (count, val2);
    }
};


