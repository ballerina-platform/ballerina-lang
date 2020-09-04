type Customer record {|
    string name;
    Address address;
|};

type Address record {|
    int unitNo;
    string street;
|};

function testOrderByClauseWithInvalidOrderField() {
    Customer c1 = {name: "James", address: {unitNo: 1, street: "Main Street"}};
    Customer c2 = {name: "Frank", address: {unitNo: 2, street: "Main Street"}};
    Customer c3 = {name: "Nina", address: {unitNo: 3, street: "Palm Grove"}};

    Customer[] customerList = [c1, c2, c3];

    Customer[] opList = from var customer in customerList
        order by address
        select customer;
}

function testOrderByClauseWithComplexTypeFieldInOrderBy() {
    Customer c1 = {name: "James", address: {unitNo: 1, street: "Main Street"}};
    Customer c2 = {name: "Frank", address: {unitNo: 2, street: "Main Street"}};
    Customer c3 = {name: "Nina", address: {unitNo: 3, street: "Palm Grove"}};

    Customer[] customerList = [c1, c2, c3];

    Customer[] opList = from var customer in customerList
        order by customer.address
        select customer;
}
