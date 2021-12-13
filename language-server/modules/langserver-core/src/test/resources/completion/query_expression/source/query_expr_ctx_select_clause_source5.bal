type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};

type CustomerTable table<Customer> key(id, name);

function testTableNoDuplicatesAndOnConflictReturnTable() returns boolean {
    boolean testPassed = true;
    error onConflictError = error("Key Conflict", message = "cannot insert.");

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
        on conflict ;

    return testPassed;
}

function getOnConflictError() returns error {
    error onConflictError = error("Key Conflict", message = "cannot insert.");
    
    return onConflictError;
}
