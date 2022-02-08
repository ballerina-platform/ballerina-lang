function testIterableOperation() {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    string outputNameString =
                from var person in check getPersonList() 

}

function getPersonList() returns Person[]|error {
    Person p1 = {firstName: "Amy", lastName: "Melina", age: 23};
    Person p2 = {firstName: "Frank", lastName: "James", age: 30};

    Person[] personList = [p1, p2];
    return personList; 
}

type Customer record {
    int id;
    string name;
    int noOfItems;
};

type Person record {
    string firstName;
    string lastName;
    int age;
};
