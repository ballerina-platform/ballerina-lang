type Person record {|
    string firstName;
    string lastName;
    int age;
|};

type Customer record {
    readonly int id;
    readonly string name;
    int noOfItems;
};

type CustomerTable table<Customer> key(id, name);

type CustomerKeyLessTable table<Customer>;

type PersonValue record {|
    Person value;
|};

function getPersonValue((record {| Person value; |}|error?)|(record {| Person value; |}?) returnedVal)
returns PersonValue? {
    var result = returnedVal;
    if (result is PersonValue) {
        return result;
    } else {
        return ();
    }
}

function testIncompatibleTypeWithReturnStream()  {
    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    Person[] outputPersonStream = stream from var person in personList
        where person.firstName == "John"
        let int newAge = 34
        select {
            firstName: person.firstName,
            lastName: person.lastName,
            age: newAge
        };
}

function testIncompatibleTypeWithReturnTable() {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    Customer[] customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        };
}

function testMissingErrorTypeWithReturnTable() {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    CustomerTable customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        };
}

function testOnConflictClauseWithFunction() {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    CustomerTable|error customerTable = table key(id, name) from var customer in customerList
        select {
            id: customer.id,
            name: customer.name,
            noOfItems: customer.noOfItems
        }
        on conflict condition(customer.name);
}

function condition(string name) returns boolean{
    return name == "Anne";
}
