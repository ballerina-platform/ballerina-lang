type Customer record {
    int id;
    string firstName;
    readonly Address? address;
};

type Address record {
    int no;
    string street;
    string town;
    Customer? customer;
};

function testCyclesInRecords() returns string {
    Address a1 = {no: 1, street: "E", town: "A", customer: ()};
    Customer c1 = {id: 1, firstName: "G", address: a1};

    a1.customer = c1;

    table<Customer> key(address) t = table [];
    t[a1] = c1;
    return t.toString();
}
