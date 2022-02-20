type Customer record {
    readonly int id;
    readonly string firstName;
    string lastName;
};

table<Customer> key(id) _ = table [
        {id: 13, firstName: "Foo", lastName: "QWER"},
        {id: 13, firstName: "Bar", lastName: "UYOR"}
    ];

table<Customer> key(id, firstName) _ = table [
        {id: 13, firstName: "Foo", lastName: "QWER"},
        {id: 13, firstName: "Bar", lastName: "UYOR"},
        {id: 13, firstName: "Foo", lastName: "XYZD"}
    ];

type Record record {
    readonly string x;
    readonly string y;
    int z;
};

table<Record> key(x, y) _ = table [
        {
            x: "x4",
            y: "2022/02/10",
            z: 100
        },
        {
            x: "x1",
            y: "2022/02/40",
            z: 100
        }
    ];
