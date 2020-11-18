//import ballerina/mysql;
//import ballerina/sql;

type Foo record {|
    string s?;
|};

type Address record {|
    string city;
    string country;
|};

type Person record {|
   string firstName;
   string lastName;
   string deptAccess;
|};

type Department record {|
   string name;
|};

function queryBinaryType(mysql:Client mysqlClient) {
    Foo f = {};
    mysqlClient->query("Select * from Customers", Foo);
    stream<record{}, error> resultStream = mysqlClient->query("Select * from Customers", Foo);
    <stream<Foo, sql:Error>>resultStream;
    <int>1.1;
    Address address = {city: "Colombo", country: "Sri Lanka"};
    {
        name: "Anne",
        age: 18,
        grades: {
            maths: 70,
            physics: 80,
            chemistry: 55
        },
        ...address
    };
}

function testMultipleSelectClausesWithSimpleVariable() {

    Person p1 = {firstName: "Alex", lastName: "George", deptAccess: "XYZ"};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", deptAccess: "XYZ"};
    Person p3 = {firstName: "John", lastName: "David", deptAccess: "XYZ"};

    Department d1 = {name:"HR"};
    Department d2 = {name:"Operations"};

    Person[] personList = [p1, p2, p3];
    Department[] deptList = [d1, d2];

    from var person in personList
            from var dept in deptList
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   deptAccess: dept.name
            };


    var x = from var person in personList
                where (person.age * 2) < 50
                do {
                    FullName fullName = {firstName: person.firstName, lastName: person.lastName};
                    nameList[nameList.length()] = fullName;
                    10;
                };

    1 ..< 10;
    10 ... 20;

    start testAsync();
}

public function testAsync() {
    // do something
}

