
function testFieldWithExpr() returns [int, string] {
    Person p = new;
    return [p.ep.pp, p.ep.name];
}


class Person {
    public int age = 90;
    public Employee ep = new(88, "sanjiva");
}


class Employee {
    public int pp = 0;
    public string name = "";

    isolated function init (int pp, string name) {
        self.pp = pp;
        self.name = name;
    }
}
