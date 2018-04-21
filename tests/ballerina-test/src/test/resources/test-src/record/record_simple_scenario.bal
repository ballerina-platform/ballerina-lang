
public function testSimpleRecord () returns (int, string) {
    Person p = {age: 89, name:"hello world"};
    return (p.age, p.name);
}

type Person {
    int age,
    string name;
};


