
public function testObjectWithInterface () returns (int, string) {
    Person p = {};
    return (p.attachInterface(7), p.month);
}


type Person {
    int age = 10,
}

function Person::attachInterface(int add) returns int {
    int count = age + add;
    return count;
}
