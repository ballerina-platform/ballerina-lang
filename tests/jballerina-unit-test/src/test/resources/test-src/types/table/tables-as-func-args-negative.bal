type Person record {
    readonly string name;
    readonly int age;
};

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

type CustomerTable table<Customer> key(id, name);

type PersonTable table<Person> key(name);

function getStringRepresentation1(table<Person> key(name) tab) returns string {
    return tab.toString();
}

function getStringRepresentation2(table<Person> key<string> tab) returns string {
    return tab.toString();
}

function getStringRepresentation3(table<Person> key<int> tab) returns string {
    return tab.toString();
}

function getStringRepresentation4(table<Person> key<string> | table<Customer> key<[int, string]> tab) returns string {
    return tab.toString();
}

function getStringRepresentation5(table<Person|Customer> key<string> tab) returns string {
    return tab.toString();
}

function getStringRepresentation6(table<Person> key<string|int> tab) returns string {
    return tab.toString();
}

function testDifferentKeySpecifierInParamAndArg() returns boolean {
    table<Person> key(age) tab1 = table [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation1(tab1);
    return str == "name=AAA age=31\nname=CCC age=34";
}

function testKeySpecifierAndKeyConstraint() returns boolean {
    table<Person> key(age) tab1 = table [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation2(tab1);
    return str == "name=AAA age=31\nname=CCC age=34";
}

function testSameKeyConstraintInArgAndParam() returns boolean {
    table<Person> key<int> tab1 = table key(age) [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation2(tab1);
    return str == "name=AAA age=31\nname=CCC age=34";
}

function testNoKeyConstraintParam1() returns boolean {
    table<Person> tab1 = table [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation3(tab1);
    return str == "name=AAA age=31\nname=CCC age=34";
}

function testNoKeyConstraintParam2() returns boolean {
    table<Person> tab1 = table [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation3(tab1);
    return str == "name=AAA age=31\nname=CCC age=34";
}

function getTable1() returns (table<Person> key<string>) {
    table<Person> key(age) tab = table [
         { name: "AAA", age: 31 },
         { name: "CCC", age: 34 }
    ];
    return  tab;
}

function getTable2() returns (table<Person> key(name)) {
    table<Person> key(age) tab = table [
         { name: "AAA", age: 31 },
         { name: "CCC", age: 34 }
    ];
    return  tab;
}
