import ballerina/h2;

function testConnecotrInit () returns (int) {
    endpoint h2:Client testDB {
        path: "./target/H2Cleint/",
        name: "TestDBH2",
        username: "SA",
        password: "",
        poolOptions: {maximumPoolSize:1}
    };

    var insertCountRet = testDB -> update("insert into Customers (customerId, name, creditLimit, country)
                                values (1, 'Anne', 1000, 'UK')", ());

    int insertCount = check insertCountRet;
    _ = testDB -> close();
    return insertCount;
}
