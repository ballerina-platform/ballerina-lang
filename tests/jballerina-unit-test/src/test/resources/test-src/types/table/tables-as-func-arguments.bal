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

function getStringRepresentation3(table<Person> tab) returns string {
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

function testSameKeySpecifierInParamAndArg() returns boolean {
    PersonTable tab1 = table [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation1(tab1);
    return str == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"CCC\",\"age\":34}]";
}

function testKeySpecifierAndKeyConstraint() returns boolean {
    PersonTable tab1 = table [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation2(tab1);
    return str == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"CCC\",\"age\":34}]";
}

function testSameKeyConstraintInArgAndParam() returns boolean {
    table<Person> key<string> tab1 = table key(name) [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation2(tab1);
    return str == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"CCC\",\"age\":34}]";
}

function testNoKeyConstraintParam1() returns boolean {
    table<Person> key<string> tab1 = table key(name) [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation3(tab1);
    return str == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"CCC\",\"age\":34}]";
}

function testNoKeyConstraintParam2() returns boolean {
    table<Person> tab1 = table [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];
    string str = getStringRepresentation3(tab1);
    return str == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"CCC\",\"age\":34}]";
}

function testUnionTypeInParam() returns boolean {
    table<Person> key(name) tab1 = table [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];

    table<Customer> key(id, name) tab2 = table [
        { id: 10 , name: "Foo", lname: "QWER" },
        { id: 11 , name: "Foo" , lname: "UYOR" }
        ];
    string str1 = getStringRepresentation4(tab1);
    string str2 = getStringRepresentation4(tab2);
    return str1 == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"CCC\",\"age\":34}]" && str2 == "[{\"id\":10,\"name\":\"Foo\",\"lname\":\"QWER\"},{\"id\":11,\"name\":\"Foo\",\"lname\":\"UYOR\"}]";
}

function testUnionConstraintParam() returns boolean {
    table<Person> key(name) tab1 = table [
            { name: "AAA", age: 31 },
            { name: "CCC", age: 34 }
            ];

    table<Customer> key(name) tab2 = table [
            { id: 10 , name: "Foo", lname: "QWER" },
            { id: 11 , name: "Bar" , lname: "UYOR" }
            ];
    string str1 = getStringRepresentation5(tab1);
    string str2 = getStringRepresentation5(tab2);
    return str1 == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"CCC\",\"age\":34}]" && str2 == "[{\"id\":10,\"name\":\"Foo\",\"lname\":\"QWER\"},{\"id\":11,\"name\":\"Bar\",\"lname\":\"UYOR\"}]";
}

function testUnionKeyConstraintParam() returns boolean {
    table<Person> key<string> tab1 = table key(name) [
        { name: "AAA", age: 31 },
        { name: "CCC", age: 34 }
        ];

    table<Person> key<int> tab2 = table key(age) [
            { name: "AAA", age: 31 },
            { name: "CCC", age: 34 }
            ];
    string str1 = getStringRepresentation6(tab1);
    string str2 = getStringRepresentation6(tab2);
    return str1 == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"CCC\",\"age\":34}]" && str2 == "[{\"name\":\"AAA\",\"age\":31},{\"name\":\"CCC\",\"age\":34}]";
}
