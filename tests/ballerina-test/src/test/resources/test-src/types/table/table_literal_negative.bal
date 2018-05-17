type Person {
    int id,
    int age,
    float salary,
    string name,
    boolean married,
};

function testTableRemoveInvalidFunctionPointer() returns (int, json) {
    Person p1 = { id: 1, age: 35, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 40, salary: 200.50, name: "martin", married: true };
    Person p3 = { id: 3, age: 42, salary: 100.50, name: "john", married: false };


    table<Person> dt = table{};
    _ = dt.add(p1);
    _ = dt.add(p2);
    _ = dt.add(p3);

    int count = check dt.remove(isBellow35Invalid);
    json j = check <json>dt;

    return (count, j);
}

function isBellow35Invalid(Person p) {
    p.age = 10;
}
