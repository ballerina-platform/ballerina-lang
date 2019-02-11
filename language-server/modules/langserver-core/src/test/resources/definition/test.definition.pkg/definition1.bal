

import ballerina/io;

const string lastName = "";

type Employee record {
    string name;
    int age;
    string address;
};

function func1 () {
    Person person = {
        id:1,
        age: 21,
        name:"mike"
    };

    string name = person.name;
}

public function main (string... args) {
    func1();

    string[] fruits = ["apple", "banana", "cherry"];
    foreach var v in fruits {
        io:println(v);
    }

    map<string> words = {a:"apple",b:"banana", c:"cherry"};
    foreach var (k,v) in words {
        //var value =? (string)v;
        //io:println(string `words {{k}} : {{value}}`);
    }

    Employee employee = {
        name: "Bob",
        age: 40,
        address: "London, UK"
    };
    Employee[] employees = [employee];

    foreach Employee emp in employees {
        emp = {
            name: "Charlie",
            age: 40,
            address: "UK, London"
        };
        io:println(string `name is {{emp.name}} {{lastName}}`);
    }

    funcInSeparateFile();

    int testLocalVariable = 0;
    testLocalVariable = 1;

    if (true) {
       string testVariableInsideIf = "test";
       testVariableInsideIf = "test";
    }
}
