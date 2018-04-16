
public function testSimpleObjectAsStruct () returns (int, string, int, string) {
    Person p = new Person();
    return (p.age, p.name, p.year, p.month);
}

public function testSimpleObjectAsStructWithNew () returns (int, string, int, string) {
    Person p = new;
    return (p.age, p.name, p.year, p.month);
}

function test() {
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
};


