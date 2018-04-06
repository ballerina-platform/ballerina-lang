

import ballerina/io;

@readonly string lastName;

type Employee {
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

function main (string[] args) {
    func1();

    string[] fruits = ["apple", "banana", "cherry"];
    foreach v in fruits {
        io:println(v);
    }

    map words = {a:"apple",b:"banana", c:"cherry"};
    foreach k,v in words {
        //var value =? (string)v;
        //io:println(string `words {{k}} : {{value}}`);
    }

    Employee employee = {};
    Employee[] employees = [employee];

    foreach y,emp in employees {
        emp = {name:"kavith"};
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
