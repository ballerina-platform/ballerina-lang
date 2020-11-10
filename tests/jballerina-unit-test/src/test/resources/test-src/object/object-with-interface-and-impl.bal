public function testObjectWithInterface () returns [int, string] {
    Person p = new Person();
    return [p.attachInterface(7), p.month];
}

class Person {
    public int age = 10;

    string month = "february";

    function attachInterface(int add) returns int {
        int count = self.age + add;
        return count;
    }

    public function attachInterface2(int add) returns int {
        int count = self.age + add;
        return count;
    }
}
