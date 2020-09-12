public function testSimpleObjectAsStruct () returns [int, string, int, string] {
    Person p = new Person();
    return [p.age, p.name, p.year, p.month];
}

public function testSimpleObjectAsStructWithNew () returns [int, string, int, string] {
    Person p = new;
    return [p.age, p.name, p.year, p.month];
}

function test() {
}

class Person {
    public int age = 10;
    public string name = "sample name";

    int year = 50;
    string month = "february";
}
