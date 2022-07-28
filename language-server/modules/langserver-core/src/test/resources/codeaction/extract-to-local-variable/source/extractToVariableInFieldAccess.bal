type Person record {
    string name;
    int age;
};

function testFunction() {
    Person p1 = {name: "Anne", age: 10};
    string name = p1.name;
}
