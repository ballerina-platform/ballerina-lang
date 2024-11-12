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

type CustomerWithXmlId record {
    readonly xml id;
    readonly string firstName;
    string lastName;
};

table<CustomerWithXmlId> key(id) _ = table [
        {id: xml `<id>123</id>`, firstName: "Sanjiva", lastName: "Weerawarana"},
        {id: xml `<id>123</id>`, firstName: "James", lastName: "Clark"}
    ];

table<CustomerWithXmlId> key<xml> _ = table key(id) [
        {
            id: xml `<p:id xmlns:p="http://sample.com/wso2/e"><?target data?><!--Contents--><p:empId>5005</p:empId></p:id>`,
            firstName: "Sanjiva",
            lastName: "Weerawarana"
        },
        {
            id: xml `<p:id xmlns:p="http://sample.com/wso2/e"><?target data?><!--Contents--><p:empId>5005</p:empId></p:id>`,
            firstName: "James",
            lastName: "Clark"
        }
    ];

const string name = "A";
const int val = 13;

function createTable() {
    table<Customer> key<string> _ = table key(firstName) [
        {id: 5005, firstName: <string> (val > 10 && !(val < 10) ? (string `Hello ${name}!!!`) : "James"), lastName: "Gordon"},
        {id: 5005, firstName: <string> (val > 10 && !(val < 10) ? (string `Hello ${name}!!!`) : "James"), lastName: "Wayne"}
    ];
}

type CustomerWithListId record {
    readonly int[] id;
    readonly string firstName;
    string lastName;
};

table<CustomerWithListId> key<int[]> _ = table key(id) [
    {id: [5005, 5006], firstName: "James", lastName: "Gordon"},
    {id: [5005, 5006], firstName: "Bruce", lastName: "Wayne"}
];

type CustomerWithTableId record {
    readonly table<Record> id;
    readonly string firstName;
    string lastName;
};

table<CustomerWithTableId> key<table<Record>> _ = table key(id) [
        {
            id: table [
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
                ],
            firstName: "James",
            lastName: "Gordon"
        },
        {
            id: table [
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
                ],
            firstName: "Bruce",
            lastName: "Wayne"
        }
    ];

type CustomerWithByteArrId record {
    readonly byte[] id;
    readonly string firstName;
    string lastName;
};

table<CustomerWithByteArrId> key(id) _ = table [
        {id: <byte[] & readonly>(base16 `5A`), firstName: "Foo", lastName: "QWER"},
        {id: <byte[] & readonly>(base16 `5A`), firstName: "Bar", lastName: "UYOR"}
    ];

const int ID1 = 13;
const int ID2 = 13;

table<Customer> key(id) _ = table [
        {id: ID1, firstName: "Foo", lastName: "QWER"},
        {id: ID2, firstName: "Bar", lastName: "UYOR"}
    ];

//Should throw a duplicate key error. Related to ballerina-lang/issues/35584
table<Customer> key(id) _ = table [
        {id: ID1, firstName: "Foo", lastName: "QWER"},
        {id: 13, firstName: "Bar", lastName: "UYOR"}
    ];
