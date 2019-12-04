import ballerina/io;

type Person record {
    int id;
    int age;
    float salary;
    string name;
    boolean married;
};

table<Person> dt1 = table {
    {key id, key age, salary, name, married}
};

function name1() returns error? {
    table<Person> dt2 = table {
        {key id, key age, salary, name, married}
    };

    Person p1 = {
        id: 1,
        age: 30,
        salary: 300.50,
        name: "jane",
        married: true
    };
    _ = check dt2.add(p1);
    _ = check dt1.add(p1);

    table<Person> dt4 = table {
        {key id, key age, salary, name, married}
    };
    table<Person> dt9 = table {
        {key id, key age, salary, name, married},
        [
            {1, 26, 3000.50, "marcus", false}
        ]
    };

    table<Person> dt5 = table {
        {key id, key age, salary, name, married},
        [
            {1, 26, 3000.50, "marcus", false}
        ]
    };

    table<Person> dt6 = table {
        {key id, key age, salary, name, married}
    };

    table<Person> dt7 = table {
        {key id, key age, salary, name, married},
        [
            {1, 26, 3000.50, "marcus", false},
            {1, 26, 3000.50, "jui", false},
            {1, 26, 3000.50, "jui", false}
        ]
    };

    Person p2 = {
        id: 1,
        age: 26,
        salary: 3000.50,
        name: "marcus",
        married: false
    };

    Person p3 = {id: 1, age: 26, salary: 3000.50, name: "jui", married: false};

    table<Person> dt8 = table {
        {key id, key age, salary, name, married},
        [
            p2,
            p3
        ]
    };
}

type Person1 record {
    int id;
    int age = -1;
    float salary;
    string name;
    boolean married;
    float amount = 55.0;
};

public function main() {
    table<Person1> personTable = table {
        {key id, age, salary, name, married, amount},
        [
            {1, 30, 300.5, "Mary", true},
            {2, 20, 300.5, "John", true}
        ]
    };

    io:println(personTable);
}
