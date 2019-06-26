
public function testObjectFieldDefaultable () returns [int, string, int, string] {
    Person p = new Person();
    return [p.age, p.name, p.year, p.month];
}

type Person object {
    public int age = 10;
    public string name = "sample name";

    int year = 50;
    string month = "february";

    function __init (int age = 10, string name = "sample name") {
        self.age = age;
        self.name = name;
    }
};
