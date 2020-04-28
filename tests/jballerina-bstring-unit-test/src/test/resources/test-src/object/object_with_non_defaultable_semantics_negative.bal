
public function testObjectWithInterface () returns [int, string] {
    Person p;
    Employee e;
    return [p.age, p.month];
}

public type Person object {
    public int age = 10;
    string month = "february";

    function __init (int age) {
        self.age = age;
    }
};

public type Employee object {
    public int age = 10;
    public Person p = new(30);
    private string month = "february";
};
