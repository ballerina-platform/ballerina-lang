function testIterableOperation() {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    var customerTable = table key(id, name) from var customer in customerList
        s
}

type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};
