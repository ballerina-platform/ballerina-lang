
public function testObjectFieldDefaultable () returns (int, string, int, string) {
    Person p = new Person();
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

    new (age = 10, name = "sample name") {

    }
};
