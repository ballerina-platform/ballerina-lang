function testIterableOperation() {
    Customer c1 = {id: 1, name: "Melina", noOfItems: 12};
    Customer c2 = {id: 2, name: "James", noOfItems: 5};
    Customer c3 = {id: 3, name: "Anne", noOfItems: 20};

    Customer[] customerList = [c1, c2, c3];

    Customer[] filtered = from var c in customerList1
                          where c.noOfItems > 10
                          w
                          select c;

}

type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};
