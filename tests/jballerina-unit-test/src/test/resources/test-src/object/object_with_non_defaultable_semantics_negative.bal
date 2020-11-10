
public function testObjectWithInterface () returns [int, string] {
    Person p;
    Employee e;
    return [p.age, p.month];
}

public class Person {
    public int age = 10;
    string month = "february";

    isolated function init (int age) {
        self.age = age;
    }
}

public class Employee {
    public int age = 10;
    public Person p = new(30);
    private string month = "february";
}
